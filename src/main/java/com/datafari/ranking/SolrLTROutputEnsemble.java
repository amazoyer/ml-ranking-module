package com.datafari.ranking;



import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ciir.umass.edu.learning.tree.Ensemble;
import ciir.umass.edu.learning.tree.RegressionTree;
import ciir.umass.edu.learning.tree.Split;

public class SolrLTROutputEnsemble extends Ensemble {

	private Map<Integer, String> reverseFeaturesMap;

	private DocumentBuilder xmlParser;

	public SolrLTROutputEnsemble(Ensemble e, Map<String, Integer> featuresMap) throws ParserConfigurationException {
		super(e);
		reverseFeaturesMap = featuresMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		xmlParser = dbFactory.newDocumentBuilder();
	}

	public String toSolrLtrJsonOuput(String name) throws IOException, SAXException {
		JSONObject object = new JSONObject();
		object.put("class", "org.apache.solr.ltr.model.MultipleAdditiveTreesModel");
		object.put("name", name);
		JSONArray features = new JSONArray();
		for (String featureName : reverseFeaturesMap.values()) {
			JSONObject featureObj = new JSONObject();
			featureObj.put("name", featureName);
			features.add(featureObj);
		}
		object.put("features", features);
		JSONObject params = new JSONObject();
		object.put("params", params);
		JSONArray treeJSONArray = new JSONArray();
		params.put("trees", treeJSONArray);

		for (int i = 0; i < trees.size(); i++) {
			JSONObject tree = new JSONObject();
			tree.put("weight", Float.toString(weights.get(i)));
			treeJSONArray.add(tree);

			// poor code of RegressionTree.... Need to parse toString ouput to
			// create json...
			Element split = xmlParser.parse(new ByteArrayInputStream(trees.get(i).toString().getBytes()))
					.getDocumentElement();
			tree.put("root", splitXmlElementToJSON(split));
		}

		return object.toJSONString();
	}

	private JSONObject splitXmlElementToJSON(Element splitXML) {
		JSONObject splitJSON = new JSONObject();
		NodeList nodes = splitXML.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String name = node.getNodeName();
				if (name.equals("split")) {
					Element element = (Element) node;
					splitJSON.put(element.getAttribute("pos"), splitXmlElementToJSON(element));
				} else {
					String content = node.getTextContent().trim();
					switch (name) {
					case "output":
						splitJSON.put("value", content);
						break;
					case "feature":
						splitJSON.put(name, reverseFeaturesMap.get(Integer.parseInt(content)));
						break;
					case "threshold":
						splitJSON.put(name, content);
						break;
					}
				}
			}

		}
		return splitJSON;
	}

	public String toString() {
		String strRep = "<ensemble>" + "\n";
		for (int i = 0; i < trees.size(); i++) {
			strRep += "\t<tree id=\"" + (i + 1) + "\" weight=\"" + weights.get(i) + "\">" + "\n";
			strRep += trees.get(i).toString("\t\t");
			strRep += "\t</tree>" + "\n";
		}
		strRep += "</ensemble>" + "\n";
		return strRep;
	}

}

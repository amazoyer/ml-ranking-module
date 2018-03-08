package com.datafari.ranking;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import com.datafari.ranking.configuration.ConfigUtils;
import com.datafari.ranking.model.TrainingEntry;
import com.francelabs.ranking.dao.ISolrClientProvider;
import com.francelabs.ranking.dao.SolrHttpClientException;

@Named
public class ModelTrainer {
	
	
	private ISolrClientProvider solrClientProvider;
	Logger logger = Logger.getLogger(ModelTrainer.class.getName());
	
	@Inject
	private ConfigUtils configUtils;
	
	@Inject
	public ModelTrainer(ISolrClientProvider solrClientProvider){
		this.solrClientProvider = solrClientProvider;
	}
	
	
	public final String store = "_DEFAULT_";
	public final String efiUserQuery = "efi.user_query";

	public JSONObject parseConfig(String configFileName) throws IOException, JSONException {
		InputStream is = configUtils.getResource(configFileName).getInputStream();
		String jsonTxt = IOUtils.toString(is);
		return new JSONObject(jsonTxt);
	}

	public SolrQuery generateSolrQuery(String queryStr, String docId) {
		SolrQuery query = new SolrQuery(queryStr);
		query.addFilterQuery("id:(" + docId + ")");
		query.addField("id");
		query.addField("score");
		// query.addField("[features store=" + store + " " + efiUserQuery +
		// "=\'\\\'" + efiQuery + "\\\'\']");
		query.addField("[features]");
		return query;
	}



	public QueryResponse sendQuery(SolrQuery solr) throws SolrServerException, IOException {
		return solrClientProvider.getSolrClient().query(solr);
	}
	
	private static String MODEL_RESOURCE_NAME = "schema/feature-store/_DEFAULT_";

	public void sendFeatures(String obj) throws SolrHttpClientException, IOException  {
		solrClientProvider.getSolrHttpClient().sendDelete(MODEL_RESOURCE_NAME);
		solrClientProvider.getSolrHttpClient().sendPut(MODEL_RESOURCE_NAME, obj);
	}

	public List<TrainingEntry> getTrainingEntries() throws SolrServerException, IOException {
		Iterator<String> queryIterator = configUtils.getLine("user_queries.txt").iterator();
		List<TrainingEntry> trainingEntries = new ArrayList<TrainingEntry>();
		while (queryIterator.hasNext()) {
			String[] userQuerySplitted = queryIterator.next().split("\\|");
			String docid = userQuerySplitted[1];
			String queryStr = userQuerySplitted[0];
			Double score = Double.parseDouble(userQuerySplitted[2]);
			String type = userQuerySplitted[3];

			SolrQuery query = generateSolrQuery(queryStr, docid);
			QueryResponse response = sendQuery(query);
			SolrDocumentList results = response.getResults();
			if (results.size() == 1) {
				String featuresValues = (String) results.get(0).getFieldValue("[features]");
				Map<String, Double> features = Arrays.stream(featuresValues.split(",")).collect(Collectors.toMap(
						entry -> (String) entry.split("=")[0], entry -> Double.parseDouble(entry.split("=")[1])));
				trainingEntries.add(new TrainingEntry(queryStr, docid, features, score, type));
			} else {
				logger.log(Level.WARNING, "Got " + results.size() + " results for query " + query.toQueryString());
			}

		}

		return trainingEntries;
	}

}

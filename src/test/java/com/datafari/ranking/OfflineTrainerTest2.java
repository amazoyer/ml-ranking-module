package com.datafari.ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.xml.sax.SAXException;

import com.datafari.ranking.config.AbstractTest;
import com.datafari.ranking.config.LocalSolrTestConfig;
import com.datafari.ranking.config.OnlineAbstractTest;
import com.datafari.ranking.model.TrainingEntry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.francelabs.ranking.dao.SolrHttpClientException;


@ContextConfiguration(classes = LocalSolrTestConfig.class)
public class OfflineTrainerTest2 extends OnlineAbstractTest {


	@Inject
	protected LtrClient ltrClient;

	@Test
	public void readConfig() throws IOException, JSONException {
		JSONObject config = parseConfig("config.json");
		Assert.assertEquals("localhost", config.get("host"));
	}



	@Ignore
	public void buildQuerySolr() throws IOException {
		InputStream out = resourceLoadingUtils.getResource("expected_queries_output.txt").getInputStream();
		BufferedReader readerOut = new BufferedReader(new InputStreamReader(out));
		URLDecoder decoder = new URLDecoder();
		String userQueryOut = null;

		Iterator<String> queryIterator = resourceLoadingUtils.getLine("user_queries.txt").iterator();
		while (queryIterator.hasNext()) {
			userQueryOut = readerOut.readLine();
			String[] userQuerySplitted = queryIterator.next().split("\\|");
			String expectedQuery = "?" + decoder.decode(decoder.decode(userQueryOut, "UTF-8"));
			String query = decoder.decode(
					ltrClient.generateSolrQuery(userQuerySplitted[0], userQuerySplitted[1]).toQueryString(), "UTF-8");
			Assert.assertEquals(expectedQuery, query);
		}
	}

	@Test
	public void testTrain() throws SolrServerException, IOException, SAXException, ParserConfigurationException {
		ClassLoader classLoader = getClass().getClassLoader();
		List<TrainingEntry> trainingEntries = getTrainingEntriesFromFile();
		InMemoryIOEvaluator evaluator = new InMemoryIOEvaluator("NDCG@10");
		evaluator.setNTrees(1);
		String result = evaluator.evaluate(evaluator.readInput(trainingEntries, false, false), null, null);
	}
	
	@Test 
	public void testTrainWithCassandraData() throws JsonParseException, JsonMappingException, IOException{
		File trainingFile = resourceLoadingUtils.getResource("trainingEntriesFromCassandra.json").getFile();
		List<TrainingEntry> trainingEntries = resourceLoadingUtils.getObjectMapper().readValue(trainingFile, new TypeReference<List<TrainingEntry>>(){});
		trainingEntries.forEach(System.out::println);
	}

	@Test
	public void testConvert() throws SolrServerException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		List<TrainingEntry> trainingEntries = getTrainingEntriesFromFile();
		InMemoryIOEvaluator evaluator = new InMemoryIOEvaluator("NDCG@10");
		Iterator<String> expectedEntries = resourceLoadingUtils.getLine("expected_training_entry.txt").iterator();
		trainingEntries.stream().map(evaluator::convertToLambdaMARTFormat).forEach(entry -> {
			Assert.assertTrue(expectedEntries.hasNext());
			Assert.assertEquals(expectedEntries.next(), entry);
		});
	}
	

	public List<TrainingEntry> getTrainingEntriesFromFile() throws SolrServerException, IOException {
		Iterator<String> queryIterator = resourceLoadingUtils.getLine("user_queries.txt").iterator();
		List<TrainingEntry> trainingEntries = new ArrayList<TrainingEntry>();
		while (queryIterator.hasNext()) {
			String[] userQuerySplitted = queryIterator.next().split("\\|");
			String docId = userQuerySplitted[1];
			String queryStr = userQuerySplitted[0];
			Double score = Double.parseDouble(userQuerySplitted[2]);
			String type = userQuerySplitted[3];

			ltrClient.getFeaturesMap(queryStr, docId).ifPresent(
					features -> trainingEntries.add(new TrainingEntry(queryStr, docId, features, score, type)));

		}

		return trainingEntries;
	}
	

	public JSONObject parseConfig(String configFileName) throws IOException, JSONException {
		InputStream is = resourceLoadingUtils.getResource(configFileName).getInputStream();
		String jsonTxt = IOUtils.toString(is);
		return new JSONObject(jsonTxt);
	}
}

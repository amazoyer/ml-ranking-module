package com.datafari.ranking;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.datafari.ranking.model.TrainingEntry;

import junit.framework.TestCase;

public class TrainerTest extends TestCase {
	@Test
	public void readConfig() throws IOException, JSONException {
		ModelTrainer trainer = new ModelTrainer();
		JSONObject config = trainer.parseConfig("config.json");
		Assert.assertEquals("localhost", config.get("host"));
	}

	@Test
	public void sendSolrQueries() throws IOException, SolrServerException {

	}

	@Test
	public void sendModel() throws IOException, ParseException {
		ModelTrainer trainer = new ModelTrainer();
		JSONParser parser = new JSONParser();
		InputStream in = TrainerTest.class.getResourceAsStream("features.json");
		Object obj = parser.parse(new InputStreamReader(in));
		trainer.sendModel(obj.toString());
	}

	@Test
	public void getFeaturesTest() throws IOException, ParseException, SolrServerException {
		ModelTrainer trainer = new ModelTrainer();

		List<TrainingEntry> trainingEntries = trainer.getTrainingEntries();
		for (TrainingEntry trainingEntry : trainingEntries) {
		}

	}

	@Ignore
	public void buildQuerySolr() throws IOException {
		InputStream out = ModelTrainer.class.getResourceAsStream("expected_queries_output.txt");
		BufferedReader readerOut = new BufferedReader(new InputStreamReader(out));
		URLDecoder decoder = new URLDecoder();
		String userQueryOut = null;
		ModelTrainer trainer = new ModelTrainer();

		Iterator<String> queryIterator = trainer.getLine("user_queries.txt").iterator();
		while (queryIterator.hasNext()) {
			userQueryOut = readerOut.readLine();
			String[] userQuerySplitted = queryIterator.next().split("\\|");
			String expectedQuery = "?" + decoder.decode(decoder.decode(userQueryOut, "UTF-8"));
			String query = decoder.decode(
					trainer.generateSolrQuery(userQuerySplitted[0], userQuerySplitted[1]).toQueryString(), "UTF-8");
			Assert.assertEquals(expectedQuery, query);
		}
	}

	@Test
	public void testTrain() throws SolrServerException, IOException, SAXException, ParserConfigurationException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("solrhome_test").getFile());
		EmbeddedSolrServer server = new EmbeddedSolrServer(file.toPath(), "techproducts");
		ModelTrainer trainer = new ModelTrainer(server);
		List<TrainingEntry> trainingEntries = trainer.getTrainingEntries();
		InMemoryIOEvaluator evaluator = new InMemoryIOEvaluator("NDCG@10");
		evaluator.setNTrees(1);
		String result = evaluator.evaluate(evaluator.readInput(trainingEntries, false, false), null, null);
		System.out.println(result);
		server.close();
	}

	@Test
	public void testConvert() throws SolrServerException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("solrhome_test").getFile());
		EmbeddedSolrServer server = new EmbeddedSolrServer(file.toPath(), "techproducts");
		ModelTrainer trainer = new ModelTrainer(server);
		List<TrainingEntry> trainingEntries = trainer.getTrainingEntries();
		InMemoryIOEvaluator evaluator = new InMemoryIOEvaluator("NDCG@10");
		Iterator<String> expectedEntries = trainer.getLine("expected_training_entry.txt").iterator();
		trainingEntries.stream().map(evaluator::convertToLambdaMARTFormat).forEach(entry -> {
			Assert.assertTrue(expectedEntries.hasNext());
			Assert.assertEquals(expectedEntries.next(), entry);
		});
		server.close();
	}
}

package com.datafari.ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.calcite.adapter.java.Map;
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
import com.datafari.ranking.model.TrainingEntry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;


public class OfflineTrainerTest  extends AbstractTest{

	@Test 
	public void testTrainWithCassandraData() throws JsonParseException, JsonMappingException, IOException, SAXException, ParserConfigurationException, ParseException{
		File trainingFile = resourceLoadingUtils.getResource("trainingEntriesFromCassandra.json").getFile();
		List<TrainingEntry> trainingEntries = resourceLoadingUtils.getObjectMapper().readValue(trainingFile, new TypeReference<List<TrainingEntry>>(){});

		//Collections.shuffle(trainingEntries);
//		int midIndice = (trainingEntries.size()-1)/2;
//	    List<TrainingEntry> training = trainingEntries.subList(0, midIndice);
//	    List<TrainingEntry> validation = trainingEntries.subList(midIndice, trainingEntries.size()-1);
//		
		InMemoryIOEvaluator evaluator = new InMemoryIOEvaluator("NDCG@10");
		
		// with 1000 iteration, should have the same results
		evaluator.setNTrees(1000);
		JSONParser parser = new JSONParser();
		Object generatedModel = parser.parse(evaluator.evaluateTrainingEntries(trainingEntries, null, null));
		Object expectedModel = parser.parse(new InputStreamReader(resourceLoadingUtils.getResource("modelDatafari.json").getInputStream()));
		Assert.assertEquals(expectedModel , generatedModel);
	}

}

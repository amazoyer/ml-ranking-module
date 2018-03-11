package com.datafari.ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
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
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;

import com.datafari.ranking.config.AbstractTest;
import com.datafari.ranking.config.LocalSolrTestConfig;
import com.datafari.ranking.config.OnlineAbstractTest;
import com.datafari.ranking.ltr.LtrClient;
import com.datafari.ranking.model.TrainingEntry;
import com.datafari.ranking.trainer.InMemoryIOEvaluator;
import com.datafari.ranking.training.SolrHttpClientException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = LocalSolrTestConfig.class)
public class ToBeDeleted extends OnlineAbstractTest {

	@Inject
	protected LtrClient ltrClient;


	@Test
	public void testTrainWithCassandraData() throws JsonParseException, JsonMappingException, IOException, SAXException,
			ParserConfigurationException, ParseException {
		
		
		File trainingFile = new File("D:\\mltest\\debugTraining.json");
		File modelFile = new File("D:\\mltest\\model.json");
		List<TrainingEntry> trainingEntries = resourceLoadingUtils.getObjectMapper().readValue(trainingFile,
				new TypeReference<List<TrainingEntry>>() {
				});

		// with 1000 iteration, should have the same results
		trainingEntries = trainingEntries.subList(0, trainingEntries.size()/2);
		List<TrainingEntry> validationEntries = trainingEntries.subList(trainingEntries.size()/2, trainingEntries.size()-1);
		Object generatedModel = mlTrainer.train(trainingEntries, validationEntries, null, "NDCG@10", 1000, "DatafariModel");

		
		resourceLoadingUtils.getObjectMapper().writeValue(modelFile, generatedModel);
		}
}

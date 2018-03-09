package com.datafari.ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

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
	public void testTrainWithCassandraData() throws JsonParseException, JsonMappingException, IOException{
		File trainingFile = resourceLoadingUtils.getResource("trainingEntriesFromCassandra.json").getFile();
		List<TrainingEntry> trainingEntries = resourceLoadingUtils.getObjectMapper().readValue(trainingFile, new TypeReference<List<TrainingEntry>>(){});
		trainingEntries.forEach(System.out::println);
	}

}

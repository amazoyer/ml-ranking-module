package com.datafari.ranking;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.xml.sax.SAXException;

import com.datafari.ranking.configuration.ConfigProperties;
import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.datafari.ranking.ltr.LtrClient;
import com.datafari.ranking.ltr.LtrClient.LTR_OBJECT_TYPE;
import com.datafari.ranking.model.TrainingEntry;
import com.datafari.ranking.trainer.MLTrainer;
import com.datafari.ranking.training.ISparkContextProvider;
import com.datafari.ranking.training.SolrHttpClientException;
import com.datafari.ranking.training.TrainingDataBuilder;

public class Main {

	private static final boolean SPLIT_TRAINING_DATA = true;
	private static String MODEL_NAME = "DatafariModel";
	private static String METRIC = "NDCG@10";
	private static String FEATURES_FILE = "featuresDatafari.json";
	private static int nTrees = 300;
	
	private static Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, SolrHttpClientException, ParseException {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(ConfigProperties.class);
		MLTrainer mLTrainer = ctx.getBean(MLTrainer.class);
		LtrClient ltrClient = ctx.getBean(LtrClient.class);
		TrainingDataBuilder trainingDataBuilder = ctx.getBean(TrainingDataBuilder.class);
		ResourceLoadingUtils resourceLoadingUtils = ctx.getBean(ResourceLoadingUtils.class);

		// read features file
		JSONArray features =  resourceLoadingUtils.readJSON(FEATURES_FILE, JSONArray.class);
		logger.info("Features "+FEATURES_FILE+ " loaded");

		
		// send it to Solr
		ltrClient.sendFeatures(features.toJSONString());
		logger.info("Features sent to Solr");

		
		// build features to train from manual query evaluation
		logger.info("Starting retrieving training entries");
		List<TrainingEntry> validationEntries = null;
		List<TrainingEntry> trainingEntries = trainingDataBuilder.retrieveTrainingEntriesFromQueryEvaluation();
		logger.info("Training entries ready");

		// serialize training entries to JSON in file
		// resourceLoadingUtils.getObjectMapper().writer().writeValue(new File("D:\\mltest\\debugTraining.json"), trainingEntries);

		
		
		// train the model (use all available training entries for training set)
		logger.info("Starting to train the model");
		
		
		//List<TrainingEntry> validationEntries = null;
		if (SPLIT_TRAINING_DATA){
			validationEntries = trainingEntries.subList((trainingEntries.size()+1)/2, trainingEntries.size());
			trainingEntries = trainingEntries.subList(0, (trainingEntries.size()+1)/2);
		}
		
		
		JSONObject model = mLTrainer.train(trainingEntries, validationEntries, null, METRIC, nTrees, MODEL_NAME);
		logger.info("Model successfully trained");

		
		// send model to Datafari
		ltrClient.sendLtrObject(model.toJSONString(), MODEL_NAME, LTR_OBJECT_TYPE.model);
		logger.info("Model sent to Solr");
		
		// close all
		((ConfigurableApplicationContext)ctx).close();

	}
}

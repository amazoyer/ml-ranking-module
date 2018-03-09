package com.datafari.ranking;

import java.io.IOException;
import java.util.List;

import javax.inject.Named;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xml.sax.SAXException;

import com.datafari.ranking.model.TrainingEntry;

@Named
public class MLTrainer {

	public JSONObject train(List<TrainingEntry> train, List<TrainingEntry> validation,
			List<TrainingEntry> test, String metric,  int NTrees) throws IOException, SAXException, ParserConfigurationException{
		InMemoryIOEvaluator evaluator = new InMemoryIOEvaluator(metric);
		evaluator.setNTrees(NTrees);
		evaluator.setModelName("DatafariModel");
		return evaluator.evaluateTrainingEntries(train, validation, test);
	}

}

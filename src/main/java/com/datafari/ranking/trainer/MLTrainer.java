package com.datafari.ranking.trainer;

import java.io.IOException;
import java.util.List;
import javax.inject.Named;
import javax.ws.rs.ext.ParamConverter.Lazy;
import javax.xml.parsers.ParserConfigurationException;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;
import com.datafari.ranking.model.TrainingEntry;

@Lazy
@Named
public class MLTrainer {
		public JSONObject train(List<TrainingEntry> train, List<TrainingEntry> validation,
			List<TrainingEntry> test, String metric,  int NTrees, String modelName) throws IOException, SAXException, ParserConfigurationException{
		InMemoryIOEvaluator evaluator = new InMemoryIOEvaluator(metric);
		evaluator.setNTrees(NTrees);
		evaluator.setModelName(modelName);
		return evaluator.evaluateTrainingEntries(train, validation, test);
	}

}

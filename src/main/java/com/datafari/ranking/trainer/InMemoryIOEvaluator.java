package com.datafari.ranking.trainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import com.datafari.ranking.model.TrainingEntry;

import ciir.umass.edu.eval.Evaluator;
import ciir.umass.edu.features.FeatureManager;
import ciir.umass.edu.learning.DataPoint;
import ciir.umass.edu.learning.DenseDataPoint;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.learning.RankList;
import ciir.umass.edu.learning.Ranker;
import ciir.umass.edu.learning.RankerTrainer;
import ciir.umass.edu.learning.SparseDataPoint;
import ciir.umass.edu.learning.tree.LambdaMART;
import ciir.umass.edu.utilities.FileUtils;
import ciir.umass.edu.utilities.RankLibError;
import ciir.umass.edu.utilities.SimpleMath;

public class InMemoryIOEvaluator extends Evaluator {
	
	private String modelName = "model";

	private Map<String, Integer> queries = new HashMap<String, Integer>();
	private Map<String, Integer> features = new HashMap<String, Integer>();
	
	Logger logger = Logger.getLogger(this.getClass());

	private int getId(String newEntry, Map<String, Integer> container) {
		if (!container.containsKey(newEntry)) {
			int id = container.size() + 1;
			container.put(newEntry, id);
			return id;
		} else {
			return container.get(newEntry);
		}
	}

	public int getQueryID(String query) {
		return getId(query, queries);

	}

	public int getFeatureID(String feature) {
		return getId(feature, features);
	}

	public InMemoryIOEvaluator(String metric) {
		super(RANKER_TYPE.LAMBDAMART, metric, metric);
	}

	public JSONObject evaluate(List<RankList> train, List<RankList> validation, List<RankList> test)
			throws IOException, SAXException, ParserConfigurationException {
		int[] features = FeatureManager.getFeatureFromSampleVector(train);
		if (normalize) {
			normalize(train, features);
			if (validation != null)
				normalize(validation, features);
			if (test != null)
				normalize(test, features);
		}

		RankerTrainer trainer = new RankerTrainer();
		LambdaMART ranker = (LambdaMART) trainer.train(type, train, validation, features, trainScorer);

		if (test != null) {
			double rankScore = evaluate(ranker, test);
			logger.info(testScorer.name() + " on test data: " + SimpleMath.round(rankScore, 4));
		}

		if (test != null) {
			double rankScore = evaluate(ranker, test);
			logger.info(testScorer.name() + " on test data: " + SimpleMath.round(rankScore, 4));
		}

		return new SolrLTROutputEnsemble(ranker.getEnsemble(), this.features).toSolrLtrJsonOuput(modelName);

	}

	/**
	 * Read a set of rankings from TrainingEntry list
	 * 
	 * @param inputFile
	 * @param mustHaveRelDoc
	 * @param useSparseRepresentation
	 * @return
	 */
	public List<RankList> readInput(List<TrainingEntry> trainingEntries, boolean mustHaveRelDoc,
			boolean useSparseRepresentation) {
		
		if (trainingEntries == null){
			return null;
		}
		List<RankList> samples = new ArrayList<>();
		int countRL = 0;
		int countEntries = 0;

		try {
			String content = "";
			String lastID = "";
			boolean hasRel = false;
			List<DataPoint> rl = new ArrayList<>();

			for (TrainingEntry trainingEntry : trainingEntries) {
				content = convertToLambdaMARTFormat(trainingEntry);
				DataPoint qp = null;

				if (useSparseRepresentation)
					qp = new SparseDataPoint(content);
				else
					qp = new DenseDataPoint(content);

				if (lastID.compareTo("") != 0 && lastID.compareTo(qp.getID()) != 0) {
					if (!mustHaveRelDoc || hasRel)
						samples.add(new RankList(rl));
					rl = new ArrayList<>();
					hasRel = false;
				}

				if (qp.getLabel() > 0)
					hasRel = true;
				lastID = qp.getID();
				rl.add(qp);
				countEntries++;
			}

			if (rl.size() > 0 && (!mustHaveRelDoc || hasRel))
				samples.add(new RankList(rl));

			logger.info("(" + samples.size() + " ranked lists, " + countEntries + " entries read)");
		} catch (Exception ex) {
			throw RankLibError.create("Error in FeatureManager::readInput(): ", ex);
		}
		return samples;
	}

	private static String SEPARATOR = "\t";

	public String convertToLambdaMARTFormat(TrainingEntry trainingEntry) {
		String flattenFeatures = trainingEntry.getFeatures().entrySet().stream()
				.map(entry -> getFeatureID(entry.getKey()) + ":" + entry.getValue())
				.collect(Collectors.joining(SEPARATOR));

		return trainingEntry.getScore().toString() + SEPARATOR + "qid:" + getQueryID(trainingEntry.getQuery())
				+ SEPARATOR + flattenFeatures + SEPARATOR + "#" + SEPARATOR + trainingEntry.getDocid() + SEPARATOR
				+ trainingEntry.getQuery();
	}

	public void setNTrees(int i) {
		LambdaMART.nTrees = i;
	}

	public JSONObject evaluateTrainingEntries(List<TrainingEntry> trainingEntries, List<TrainingEntry> validation,
			List<TrainingEntry> test) throws IOException, SAXException, ParserConfigurationException {
		return evaluate(readInput(trainingEntries, false, false), readInput(validation, false, false),
				readInput(test, false, false));
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

}

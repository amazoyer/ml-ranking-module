import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

	private LinkedHashSet<String> queries = new LinkedHashSet<String>();
	private LinkedHashSet<String> features = new LinkedHashSet<String>();

	public int getQueryID(String query) {
		if (!queries.contains(query)) {
			queries.add(query);
		}
		return queries.size();
	}

	public int getFeatureID(String feature) {
		if (!features.contains(feature)) {
			features.add(feature);
		}
		return features.size();
	}

	public InMemoryIOEvaluator(String trainMetric, String testMetric) {
		super(RANKER_TYPE.LAMBDAMART, trainMetric, testMetric);
	}


	public String evaluate(List<RankList> train, List<RankList> validation, List<RankList> test) {
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
			System.out.println(testScorer.name() + " on test data: " + SimpleMath.round(rankScore, 4));
		}

		

		if (test != null) {
			double rankScore = evaluate(ranker, test);
			System.out.println(testScorer.name() + " on test data: " + SimpleMath.round(rankScore, 4));
		}
		if (modelFile.compareTo("") != 0) {
			System.out.println("");
			ranker.save(modelFile);
			System.out.println("Model saved to: " + modelFile);
		}
		
		return null;
		//return new SolrLTROutputEnsemble(ranker.getEnsemble()).toSolrLtrJsonOuput();

	}

	/**
	 * Read a set of rankings from a single file.
	 * 
	 * @param inputFile
	 * @param mustHaveRelDoc
	 * @param useSparseRepresentation
	 * @return
	 */
	public List<RankList> readInput(List<TrainingEntry> trainingEntries, boolean mustHaveRelDoc,
			boolean useSparseRepresentation) {
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

			System.out.println("(" + samples.size() + " ranked lists, " + countEntries + " entries read)");
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

}
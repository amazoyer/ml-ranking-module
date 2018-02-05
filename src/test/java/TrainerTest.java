import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ciir.umass.edu.eval.Evaluator;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.learning.tree.LambdaMART;
import ciir.umass.edu.metric.MetricScorer;
import ciir.umass.edu.metric.MetricScorerFactory;
import ciir.umass.edu.metric.NDCGScorer;

public class TrainerTest {

	@Test
	public void readConfig() throws IOException {
		ModelTrainer trainer = new ModelTrainer();
		JSONObject config = trainer.parseConfig("config.json");
		Assert.assertEquals("localhost", config.get("host"));
	}

	@Test
	public void sendSolrQueries() throws IOException, SolrServerException {
		ModelTrainer trainer = new ModelTrainer();

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
		ModelTrainer trainer = new ModelTrainer();
		BufferedReader readerOut = new BufferedReader(new InputStreamReader(out));
		URLDecoder decoder = new URLDecoder();
		String userQueryOut = null;
		Iterator<String> queryIterator = trainer.getQueries().iterator();
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
	public void testTrain() throws SolrServerException, IOException {
		ModelTrainer trainer = new ModelTrainer();
		List<TrainingEntry> trainingEntries = trainer.getTrainingEntries();
		InMemoryIOEvaluator evaluator = new InMemoryIOEvaluator("NDCG@10", "NDCG@10");
		LambdaMART.nTrees = 10;
		evaluator.modelFile = "d:\\temp2.txt";
		String result = evaluator.evaluate(evaluator.readInput(trainingEntries, false, false), null, null);
		System.out.println(result);
	}

}

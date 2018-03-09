package com.datafari.ranking.dao.spark;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;

import com.datafari.ranking.ModelTrainer;
import com.datafari.ranking.model.TrainingQuery;
import com.datastax.spark.connector.japi.CassandraRow;
import com.datastax.spark.connector.japi.rdd.CassandraJavaRDD;
import com.datastax.spark.connector.japi.rdd.CassandraTableScanJavaRDD;
import com.francelabs.ranking.dao.ISolrClientProvider;
import com.francelabs.ranking.dao.SparkContextProviderImpl;

import scala.Tuple2;
import scala.Tuple3;

@Named
public class SparkJobs {

	@Inject
	private SparkContextProviderImpl sparkContextProvider;

	private static ISolrClientProvider solrClientProvider;

	private static ModelTrainer modelTrainer;

	@Inject
	public void setSolrClientProvider(ISolrClientProvider solrClientProvider) {
		SparkJobs.solrClientProvider = solrClientProvider;
	}

	@Inject
	public void setModelTrainer(ModelTrainer modelTrainer) {
		SparkJobs.modelTrainer = modelTrainer;
	}

	private static String DATAFARI_CASSANDRA_TABLE = "datafari";
	private static String DATAFARI_RANKING_TABLE = "ranking";

	public JavaRDD<Tuple2<String, Tuple2<Double, Double>>> getNumFavoritePerdocument() throws InterruptedException {
		CassandraJavaRDD<CassandraRow> rankingRDD = javaFunctions(sparkContextProvider.getSparkContext())
				.cassandraTable(DATAFARI_CASSANDRA_TABLE, DATAFARI_RANKING_TABLE)
				.select("request", "document_id", "ranking");
		JavaPairRDD<String, Tuple2<List<String>, List<String>>> result = rankingRDD
				.mapToPair(SparkFunctions.mapForEvaluation).reduceByKey(SparkFunctions.doubleListReducer).cache();
		return result.map(calculateScore);
	}
	//
	// public JavaPairRDD<String, Tuple2<List<String>, List<String>>>
	// getNumFavoritePerdocument() throws InterruptedException {
	// CassandraJavaRDD<CassandraRow> rankingRDD =
	// javaFunctions(sparkContextProvider.getSparkContext())
	// .cassandraTable(DATAFARI_CASSANDRA_TABLE, DATAFARI_RANKING_TABLE)
	// .select("request", "document_id", "ranking");
	//
	// System.out.println("Count per query");
	// JavaPairRDD<String, Tuple2<List<String>, List<String>>> result =
	// rankingRDD
	// .mapToPair(SparkFunctions.mapForEvaluation).reduceByKey(SparkFunctions.reducer).cache();
	// return result;
	// }

	public void TestCassandra() throws InterruptedException {
		CassandraJavaRDD<CassandraRow> rankingRDD = javaFunctions(sparkContextProvider.getSparkContext())
				.cassandraTable("datafari", "ranking").select("request", "document_id", "ranking");
		System.out.println("Count per query");
		rankingRDD.mapToPair(SparkFunctions.cassandraRankingMapper).reduceByKey(SparkFunctions.functionSum)
				.foreach(entry -> System.out.println(entry._1 + ":" + entry._2));
	}

	/**
	 * 
	 * 
	 * SolrHistoryDocument => query => list (document, clickPosition)
	 * 
	 */
	private static PairFunction<SolrDocument, String, Collection<String>> solrStatsMapper = new PairFunction<SolrDocument, String, Collection<String>>() {
		@Override
		public Tuple2<String, Collection<String>> call(SolrDocument doc) throws Exception {
			return new Tuple2<String, Collection<String>>((String) doc.getFirstValue("q_search"),
					doc.getFieldValues("history").stream().map(entry -> (String) entry)
							.filter(entry -> entry.contains("file:/")).collect(Collectors.toList()));
		}
	};

	public JavaPairRDD<String, Map<String, Tuple2<Long, Long>>> getQueryClickRDD() throws IOException {
		return solrClientProvider.getSolrJavaRDD().queryShards("*:*").mapToPair(solrStatsMapper).aggregateByKey(
				new HashMap<String, Tuple2<Long, Long>>(), SparkFunctions.documentClickCountLocalAggregator,
				SparkFunctions.documentClickCountGlobalAggregator);

	}

	private static Function<Tuple2<String, Tuple2<List<String>, List<String>>>, Tuple2<String, Tuple2<Double, Double>>> calculateScore = new Function<Tuple2<String, Tuple2<List<String>, List<String>>>, Tuple2<String, Tuple2<Double, Double>>>() {

		@Override
		public Tuple2<String, Tuple2<Double, Double>> call(Tuple2<String, Tuple2<List<String>, List<String>>> query)
				throws SolrServerException, IOException {
			SolrQuery solrQuery = new SolrQuery();
			solrQuery.setQuery(query._1());
			solrQuery.setRows(10);
			solrQuery.setFields("id");
			QueryResponse result = solrClientProvider.getSolrClient().query("FileShare", solrQuery);

			// implement some cache
			List<String> goodDocuments = query._2()._1();
			List<String> badDocuments = query._2()._2();

			long truePositives = 0;
			long allGoodDocuments = goodDocuments.size();
			if (allGoodDocuments != 0) {
				truePositives = result.getResults().stream().map(entry -> (String) entry.getFirstValue("id"))
						.filter(goodDocuments::contains).collect(Collectors.counting());
			}

			long falsePositives = 0;
			long allBadDocuments = badDocuments.size();
			if (badDocuments.size() != 0) {
				falsePositives = result.getResults().stream().map(entry -> (String) entry.getFirstValue("id"))
						.filter(badDocuments::contains).collect(Collectors.counting());
			}

			// should use NDCG instead
			double precisionKind = ((double) truePositives) / ((double) (truePositives + falsePositives));
			double recallKind = ((double) truePositives) / ((double) goodDocuments.size());

			return new Tuple2(query._1(), new Tuple2<Double, Double>(precisionKind, recallKind));

		}

	};

	public CassandraTableScanJavaRDD<CassandraRow> getQueryEvaluationRDD() {
		return javaFunctions(sparkContextProvider.getSparkContext()).cassandraTable("datafari", "ranking")
				.select("request", "document_id", "ranking");
	}

	public JavaRDD<Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>>> getTrainingEntriesFromQueryEvaluation() {
		JavaPairRDD<String, Tuple3<String, String, Long>> listQueries = getQueryEvaluationRDD()
				.mapToPair(SparkFunctions.mapCassandraEntryForTrainingEntries);
		return listQueries.map(createTrainingQueries)
				// keep if we there is a feature map
				.filter(entry -> entry._2()._2().isPresent());
	}

	public static Function<Tuple2<String, Tuple3<String, String, Long>>, Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>>> createTrainingQueries = new Function<Tuple2<String, Tuple3<String, String, Long>>, Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>>>() {
		@Override
		public Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>> call(
				Tuple2<String, Tuple3<String, String, Long>> entry) throws Exception {
			String queryStr = entry._2()._1();
			String docId = entry._2()._2();
			Optional<Map<String, Double>> featuresMap = modelTrainer.getFeaturesMap(queryStr, docId);
		
			return new Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>>(entry._1(),
					new Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>(entry._2(), featuresMap));
		}
	};

	public JavaPairRDD<String, Tuple2<List<String>, List<String>>> getAggregatedQueryEvaluationRDD() {
		return getQueryEvaluationRDD().mapToPair(SparkFunctions.mapForEvaluation)
				.reduceByKey(SparkFunctions.doubleListReducer);
	}

	// public void calculatePrecisionRecall() throws InterruptedException {
	// CassandraJavaRDD<CassandraRow> rankingRDD =
	// javaFunctions(sparkContextProvider.getSparkContext())
	// .cassandraTable("datafari", "ranking").select("request", "document_id",
	// "ranking");
	// System.out.println("Count per query");
	// JavaRDD<Object> result =
	// rankingRDD.mapToPair(SparkFunctions.mapForEvaluation)
	// .reduceByKey(SparkFunctions.reducer)
	// .map(entry -> new QueryEvaluation(entry._1(), entry._2()._1(),
	// entry._2()._2()));
	//
	// result.map(calculateScore).foreach(entry -> System.out.println("query : "
	// + entry._1() + " : " + entry._2()._1() + " : " +
	// entry._2()._2()));
	// result.foreach(entry -> System.out.println(entry._1() + " : "+
	// entry._2()._1()+ " : "+ entry._2()._2()));
	//
	// }

	public void getDocFavorite() throws InterruptedException {
		sumBy("favorite").foreach(entry -> System.out.println(entry._1() + " : " + entry._2()));
	}

	public void getDocLike() throws InterruptedException {
		sumBy("like").foreach(entry -> System.out.println(entry._1() + " : " + entry._2()));
	}

	private JavaPairRDD<String, Long> sumBy(String table) {
		CassandraJavaRDD<CassandraRow> rdd = javaFunctions(sparkContextProvider.getSparkContext())
				.cassandraTable("datafari", table).select("document_id");
		return rdd.mapToPair(SparkFunctions.groupByDocumentID).reduceByKey(SparkFunctions.sum);
	}

}

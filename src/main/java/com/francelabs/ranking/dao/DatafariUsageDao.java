package com.francelabs.ranking.dao;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.junit.Test;

import com.datastax.spark.connector.japi.CassandraRow;
import com.datastax.spark.connector.japi.rdd.CassandraJavaRDD;
import com.lucidworks.spark.rdd.SolrJavaRDD;

import scala.Tuple2;
import scala.Tuple3;

public class DatafariUsageDao {

	private static JavaSparkContext sparkContext;
	private static CloudSolrClient solrClient;
	private static String DATAFARI_CASSANDRA_TABLE = "datafari";
	private static String DATAFARI_RANKING_TABLE = "ranking";

	public DatafariUsageDao(JavaSparkContext sparkContext, CloudSolrClient solrClient) {
		this.sparkContext = sparkContext;
		this.solrClient = solrClient;
	}

	public JavaRDD<Tuple2<String, Tuple2<Double, Double>>> getNumFavoritePerdocument() throws InterruptedException {
		CassandraJavaRDD<CassandraRow> rankingRDD = javaFunctions(sparkContext)
				.cassandraTable(DATAFARI_CASSANDRA_TABLE, DATAFARI_RANKING_TABLE)
				.select("request", "document_id", "ranking");

		System.out.println("Count per query");
		JavaPairRDD<String, Tuple2<List<String>, List<String>>> result = rankingRDD
				.mapToPair(SparkFunctions.mapForEvaluation).reduceByKey(SparkFunctions.reducer).cache();
		return result.map(calculateScore);
	}

	public void TestCassandra() throws InterruptedException {
		CassandraJavaRDD<CassandraRow> rankingRDD = javaFunctions(sparkContext).cassandraTable("datafari", "ranking")
				.select("request", "document_id", "ranking");
		System.out.println("Count per query");
		rankingRDD.mapToPair(SparkFunctions.cassandraRankingMapper).reduceByKey(SparkFunctions.functionSum)
				.foreach(entry -> System.out.println(entry._1 + ":" + entry._2));
	}

	/**
	 * 
	 * "click": 1, "positionClickTot": 1
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
	//
	// private Function2<Collection<String>, Collection<String>,
	// Collection<String>> historyReducer = new
	// Function2<Collection<String>,
	// Collection<String>, Collection<String>> (){
	//
	// @Override
	// public Collection<String> call(Collection<String> history1,
	// Collection<String> history2) throws Exception {
	// history1.addAll(history2);
	// return history1;
	// }
	//
	// };

	private static Function2<Map<String, Tuple2<Long, Long>>, Collection<String>, Map<String, Tuple2<Long, Long>>> localAggregator = new Function2<Map<String, Tuple2<Long, Long>>, Collection<String>, Map<String, Tuple2<Long, Long>>>() {
		@Override
		public Map<String, Tuple2<Long, Long>> call(Map<String, Tuple2<Long, Long>> mapListDocumentAndCound,
				Collection<String> documentIDsList) throws Exception {

			// for a query and a list of documents, create the map
			// mapListDocumentAndCound.putAll(documentIDsList.stream().map(entry
			// ->
			// parseHistoryString(entry)).collect(Collectors.toMap(Tuple3::_1,
			// entry ->
			// new Tuple2<Long, Long>(entry._2(),entry._3()))));
			documentIDsList.stream().map(entry -> parseHistoryString(entry))
					.map(entry -> new Tuple3<String, Long, Long>(entry._1(), 1L, entry._2()))
					.forEach(entry -> addEntryToMap(mapListDocumentAndCound, entry));
			return mapListDocumentAndCound;
		}
	};

	private static void addEntryToMap(Map<String, Tuple2<Long, Long>> mapListDocumentAndCound,
			Tuple3<String, Long, Long> historyEntry) {
		if (!mapListDocumentAndCound.containsKey(historyEntry._1())) {
			mapListDocumentAndCound.put(historyEntry._1(),
					new Tuple2<Long, Long>(historyEntry._2(), historyEntry._3()));
		} else {
			Tuple2<Long, Long> existingEntry = mapListDocumentAndCound.get(historyEntry._1());
			// merge Tuple2
			mapListDocumentAndCound.put(historyEntry._1(), new Tuple2<Long, Long>(
					historyEntry._2() + existingEntry._1(), historyEntry._3() + existingEntry._2()));
		}
	}

	private static Function2<Map<String, Tuple2<Long, Long>>, Map<String, Tuple2<Long, Long>>, Map<String, Tuple2<Long, Long>>> globalAggregator = new Function2<Map<String, Tuple2<Long, Long>>, Map<String, Tuple2<Long, Long>>, Map<String, Tuple2<Long, Long>>>() {
		@Override
		public Map<String, Tuple2<Long, Long>> call(Map<String, Tuple2<Long, Long>> map1,
				Map<String, Tuple2<Long, Long>> map2) throws Exception {
			map1.entrySet().stream().map(mapEntry -> new Tuple3<String, Long, Long>(mapEntry.getKey(),
					mapEntry.getValue()._1, mapEntry.getValue()._2())).forEach(entry -> addEntryToMap(map2, entry));
			return map1;
		}
	};

	/**
	 * 
	 * 
	 * 
	 * @param history
	 * @return Tuple : Filename, ClicPosition
	 */
	private static Tuple2<String, Long> parseHistoryString(String history) {
		Pattern p = Pattern.compile("(file:.*)///([0-9]+)");
		Matcher m = p.matcher(history);
		if (m.find()) {
			return new Tuple2<String, Long>(m.group(1), Long.parseLong(m.group(2)));
		}
		throw new RuntimeException("Cannot correctly parse entry : " + history);
	}

	public void TestSolr() throws SolrServerException {
		SolrJavaRDD solrRDD = SolrJavaRDD.get("localhost:2181", "Statistics", sparkContext.sc());
		JavaPairRDD<String, Map<String, Tuple2<Long, Long>>> stat = solrRDD.queryShards("*:*")
				.mapToPair(solrStatsMapper)
				.aggregateByKey(new HashMap<String, Tuple2<Long, Long>>(), localAggregator, globalAggregator);
		stat.foreach(statEntry -> statEntry._2().entrySet().forEach(subStat -> System.out.println(statEntry._1() + " : "
				+ subStat.getKey() + " : " + subStat.getValue()._1() + " : " + subStat.getValue()._2())));
	}

	private static Function<Tuple2<String, Tuple2<List<String>, List<String>>>, Tuple2<String, Tuple2<Double, Double>>> calculateScore = new Function<Tuple2<String, Tuple2<List<String>, List<String>>>, Tuple2<String, Tuple2<Double, Double>>>() {
		@Override
		public Tuple2<String, Tuple2<Double, Double>> call(Tuple2<String, Tuple2<List<String>, List<String>>> query)
				throws SolrServerException, IOException {
			SolrQuery solrQuery = new SolrQuery();
			solrQuery.setQuery(query._1());
			solrQuery.setRows(10);
			solrQuery.setFields("id");
			QueryResponse result = solrClient.query("FileShare", solrQuery);

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

			double precisionKind = ((double) truePositives) / ((double) (truePositives + falsePositives));
			double recallKind = ((double) truePositives) / ((double) goodDocuments.size());

			return new Tuple2(query._1(), new Tuple2<Double, Double>(precisionKind, recallKind));

		}

	};

	public void calculatePrecisionRecall() throws InterruptedException {
		CassandraJavaRDD<CassandraRow> rankingRDD = javaFunctions(sparkContext).cassandraTable("datafari", "ranking")
				.select("request", "document_id", "ranking");
		System.out.println("Count per query");
		JavaPairRDD<String, Tuple2<List<String>, List<String>>> result = rankingRDD
				.mapToPair(SparkFunctions.mapForEvaluation).reduceByKey(SparkFunctions.reducer).cache();
		result.map(calculateScore).foreach(entry -> System.out
				.println("query : " + entry._1() + " : " + entry._2()._1() + " : " + entry._2()._2()));
		// result.foreach(entry -> System.out.println(entry._1() + " : "+
		// entry._2()._1()+ " : "+ entry._2()._2()));

	}

	public void getDocFavorite() throws InterruptedException {
		sumBy("favorite").foreach(entry -> System.out.println(entry._1() + " : " + entry._2()));
	}

	public void getDocLike() throws InterruptedException {
		sumBy("like").foreach(entry -> System.out.println(entry._1() + " : " + entry._2()));
	}

	private static JavaPairRDD<String, Long> sumBy(String table) {
		CassandraJavaRDD<CassandraRow> rdd = javaFunctions(sparkContext).cassandraTable("datafari", table)
				.select("document_id");
		return rdd.mapToPair(SparkFunctions.groupByDocumentID).reduceByKey(SparkFunctions.sum);
	}


}

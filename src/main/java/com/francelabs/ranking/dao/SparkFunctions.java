package com.francelabs.ranking.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import com.datastax.spark.connector.japi.CassandraRow;

import scala.Tuple2;

public class SparkFunctions {

	public static PairFunction<CassandraRow, String, Long> cassandraRankingMapper = new PairFunction<CassandraRow, String, Long>() {
		@Override
		public Tuple2<String, Long> call(CassandraRow entry) throws Exception {
			return new Tuple2<String, Long>(entry.getString("request"), entry.getLong("ranking"));
		}
	};

	public static PairFunction<CassandraRow, String, Tuple2<List<String>, List<String>>> mapForEvaluation = new PairFunction<CassandraRow, String, Tuple2<List<String>, List<String>>>() {
		@Override
		public Tuple2<String, Tuple2<List<String>, List<String>>> call(CassandraRow entry) throws Exception {
			String request = entry.getString("request");
			ArrayList<String> singletonArray = new ArrayList<>(Arrays.asList(entry.getString("document_id")));
			ArrayList<String> emptyList = new ArrayList<String>();
			if (entry.getLong("ranking") > 5) {
				return new Tuple2<String, Tuple2<List<String>, List<String>>>(request,
						new Tuple2<List<String>, List<String>>(singletonArray, emptyList));
			} else {
				return new Tuple2<String, Tuple2<List<String>, List<String>>>(request,
						new Tuple2<List<String>, List<String>>(emptyList, singletonArray));
			}
		}
	};

	public static Function2<Long, Long, Long> functionSum = new Function2<Long, Long, Long>() {
		@Override
		public Long call(Long value1, Long value2) throws Exception {
			return value1 + value2;
		}
	};

	public static Function2<Long, Long, Long> sum = new Function2<Long, Long, Long>() {
		@Override
		public Long call(Long v1, Long v2) throws Exception {
			return v1 + v2;
		}
	};

	public static PairFunction<CassandraRow, String, Long> groupByDocumentID = new PairFunction<CassandraRow, String, Long>() {
		@Override
		public Tuple2<String, Long> call(CassandraRow row) throws Exception {
			return new Tuple2<String, Long>(row.getString("document_id"), 1L);
		}
	};

	public static Function2<Tuple2<List<String>, List<String>>, Tuple2<List<String>, List<String>>, Tuple2<List<String>, List<String>>> reducer = new Function2<Tuple2<List<String>, List<String>>, Tuple2<List<String>, List<String>>, Tuple2<List<String>, List<String>>>() {
		@Override
		public Tuple2<List<String>, List<String>> call(Tuple2<List<String>, List<String>> v1,
				Tuple2<List<String>, List<String>> v2) throws Exception {
			v1._1().addAll(v2._1());
			v1._2().addAll(v2._2());
			return v1;
		}
	};

	

}

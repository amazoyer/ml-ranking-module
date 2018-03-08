package com.datafari.ranking.dao.spark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import com.datafari.ranking.model.QueryEvaluation;
import com.datastax.spark.connector.japi.CassandraRow;
import scala.Tuple2;
import scala.Tuple3;

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

	public static Function2<Tuple2<List<String>, List<String>>, Tuple2<List<String>, List<String>>, Tuple2<List<String>, List<String>>> doubleListReducer = new Function2<Tuple2<List<String>, List<String>>, Tuple2<List<String>, List<String>>, Tuple2<List<String>, List<String>>>() {
		@Override
		public Tuple2<List<String>, List<String>> call(Tuple2<List<String>, List<String>> v1,
				Tuple2<List<String>, List<String>> v2) throws Exception {
			v1._1().addAll(v2._1());
			v1._2().addAll(v2._2());
			return v1;
		}
	};

	/**
	 * 
	 * Merge 2 maps of documents + count + sum of click positions
	 * 
	 */
	public static Function2<Map<String, Tuple2<Long, Long>>, Map<String, Tuple2<Long, Long>>, Map<String, Tuple2<Long, Long>>> documentClickCountGlobalAggregator = new Function2<Map<String, Tuple2<Long, Long>>, Map<String, Tuple2<Long, Long>>, Map<String, Tuple2<Long, Long>>>() {
		@Override
		public Map<String, Tuple2<Long, Long>> call(Map<String, Tuple2<Long, Long>> map1,
				Map<String, Tuple2<Long, Long>> map2) throws Exception {

			map1.entrySet().stream().map(mapEntry -> new Tuple3<String, Long, Long>(mapEntry.getKey(),
					mapEntry.getValue()._1, mapEntry.getValue()._2())).forEach(entry -> addEntryToMap(map2, entry));
			return map2;
		}
	};

	/**
	 * 
	 * add the current entry to the map the map count the number of document
	 * clicked for the current query and the total num of clicks positions
	 * 
	 * @param mapListDocumentAndCound
	 * @param historyEntry
	 */
	public static void addEntryToMap(Map<String, Tuple2<Long, Long>> mapListDocumentAndCound,
			Tuple3<String, Long, Long> historyEntry) {
		
		// history entry value ( ex : 2, 2)
		Tuple2<Long, Long> currentEntry = new Tuple2<Long, Long>(historyEntry._2(), historyEntry._3());
		
		// if  exist, merge with the existing entry
		if (mapListDocumentAndCound.containsKey(historyEntry._1())) {			
			// if exist, merge with last one
			Tuple2<Long, Long> existingEntry = mapListDocumentAndCound.get(historyEntry._1());

			// new value for document
			currentEntry =  new Tuple2<Long, Long>(
					currentEntry._1 + existingEntry._1,
					currentEntry._2 + existingEntry._2	
					);
		}
		
		// push the new entry
		mapListDocumentAndCound.put(historyEntry._1(), currentEntry);

	}

	/**
	 * 
	 * For a list of history event, add the document click count + the global
	 * position of click
	 * 
	 */
	public static Function2<Map<String, Tuple2<Long, Long>>, Collection<String>, Map<String, Tuple2<Long, Long>>> documentClickCountLocalAggregator = new Function2<Map<String, Tuple2<Long, Long>>, Collection<String>, Map<String, Tuple2<Long, Long>>>() {
		@Override
		public Map<String, Tuple2<Long, Long>> call(Map<String, Tuple2<Long, Long>> mapListDocumentAndCound,
				Collection<String> documentIDsList) throws Exception {

			documentIDsList.stream().map(entry -> parseHistoryString(entry))
					.map(entry -> new Tuple3<String, Long, Long>(entry._1(), 1L, entry._2()))
					.forEach(entry -> addEntryToMap(mapListDocumentAndCound, entry));
			return mapListDocumentAndCound;
		}
	};

	/**
	 *
	 * Parse an history line :
	 * 
	 * ///////////////file:/data/owncloud/France
	 * Labs/Clients/Prospects/2016/2016_Datafari_POC_01/Sizing
	 * Architecture.xlsx///1",
	 * 
	 * to tuple : filename, clickposition
	 * 
	 * @param history
	 */
	public static Tuple2<String, Long> parseHistoryString(String history) {
		Pattern p = Pattern.compile("(file:.*)///([0-9]+)");
		Matcher m = p.matcher(history);
		if (m.find()) {
			return new Tuple2<String, Long>(m.group(1), Long.parseLong(m.group(2)));
		}
		throw new RuntimeException("Cannot correctly parse entry : " + history);
	}

}

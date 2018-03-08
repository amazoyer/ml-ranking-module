package com.datafari.ranking;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.datafari.ranking.dao.spark.SparkFunctions;

import scala.Tuple2;

public class SparkFunctionTests {

	@Test
	public void localClickLogAggregatorTest() throws Exception {
		Map<String, Tuple2<Long, Long>> map1 = new HashMap<String, Tuple2<Long, Long>>();
		map1.put("file:/doc1", new Tuple2<Long, Long>(1L, 1L));	
		List<String> historyEntries = new ArrayList<String>();
		historyEntries.add("///////////////file:/doc1///1");
		historyEntries.add("///////////////file:/doc1///2");
		historyEntries.add("///////////////file:/doc1///2");
		Tuple2<Long, Long> result = SparkFunctions.documentClickCountLocalAggregator.call(map1, historyEntries).get("file:/doc1");
		
		Assert.assertEquals(Long.valueOf(4), result._1);
		Assert.assertEquals(Long.valueOf(6), result._2);
	
	}

	@Test
	public void globalClickLogAggregatorTest() throws Exception {
		Map<String, Tuple2<Long, Long>> map1 = new HashMap<String, Tuple2<Long, Long>>();
		map1.put("file:/doc1", new Tuple2<Long, Long>(1L, 1L));	
		map1.put("file:/doc2", new Tuple2<Long, Long>(2L, 2L));	
		
		Map<String, Tuple2<Long, Long>> map2 = new HashMap<String, Tuple2<Long, Long>>();
		map2.put("file:/doc1", new Tuple2<Long, Long>(1L, 1L));	
	
		Map<String, Tuple2<Long, Long>> resultMap = SparkFunctions.documentClickCountGlobalAggregator.call(map1, map2);
		Tuple2<Long, Long> doc1Merged = resultMap.get("file:/doc1");
		Assert.assertEquals(Long.valueOf(2), doc1Merged._1);
		Assert.assertEquals(Long.valueOf(2), doc1Merged._2);
	
		Tuple2<Long, Long> doc2Merged = resultMap.get("file:/doc2");
		Assert.assertEquals(Long.valueOf(2), doc2Merged._1);
		Assert.assertEquals(Long.valueOf(2), doc2Merged._2);
	
		
	}

	@Test
	public void parseHistoryTest() {
		Tuple2<String, Long> historyLine = SparkFunctions.parseHistoryString(
				"///////////////file:/data/owncloud/France Labs/Clients/Prospects/2016/2016_Datafari_Total_Wat_POC_01/Sizing Architecture.xlsx///1");
		Assert.assertEquals(Long.valueOf(1), historyLine._2);
	}

}

package com.datafari.ranking;


import java.io.IOException;

import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Ignore;
import org.junit.Test;

import com.francelabs.ranking.dao.DatafariUsageDao;

import scala.Tuple2;

public class TestRDD extends AbstractTest {

	@Inject
	private DatafariUsageDao dud;

	@Test
	public void getNumFavoritePerdocument() throws InterruptedException {
		dud.getNumFavoritePerdocument().foreach(TestRDD::printEntry);
	}
	
	@Test
	public void testSolr() throws SolrServerException, IOException{
		dud.TestSolr();
	}

	static void printEntry(Tuple2<String,Tuple2<Double,Double>> entry){
		System.out.println("query : " + entry._1() + " : " + entry._2()._1() + " : " + entry._2()._2());
	}




}

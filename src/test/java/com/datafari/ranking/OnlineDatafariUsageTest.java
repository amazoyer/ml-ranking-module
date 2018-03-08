package com.datafari.ranking;


import java.io.IOException;

import javax.inject.Inject;

import org.junit.Test;

import com.francelabs.ranking.dao.DatafariUsageDao;


public class OnlineDatafariUsageTest extends AbstractTest {

	@Inject
	private DatafariUsageDao dud;

	@Test
	public void listQueryEvaluation() throws InterruptedException {
		dud.listQueryEvaluations().forEach(System.out::println);
	}
	
	@Test
	public void listQueryList() throws IOException {
		dud.listQueryClick().forEach(System.out::println);
	}
//	
//	@Ignore
//	public void getNumFavoritePerdocument() throws InterruptedException {
//		dud.getNumFavoritePerdocument().foreach(OnlineDatafariUsageTest::printEntry);
//	}
//	
//	@Ignore
//	public void testSolr() throws SolrServerException, IOException{
//		dud.TestSolr();
//	}
//
//	static void printEntry(Tuple2<String,Tuple2<Double,Double>> entry){
//		System.out.println("query : " + entry._1() + " : " + entry._2()._1() + " : " + entry._2()._2());
//	}




}

package com.datafari.ranking;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Test;

import com.datafari.ranking.config.AbstractTest;
import com.datafari.ranking.config.OnlineAbstractTest;
import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.datafari.ranking.model.TrainingEntry;
import com.francelabs.ranking.dao.DatafariUsageDao;


public class OnlineDatafariUsageTest extends OnlineAbstractTest {

	@Inject
	private DatafariUsageDao dud;

	@Inject
	private ResourceLoadingUtils resourceLoadingUtils;

	@Test
	public void listQueryEvaluation() throws InterruptedException {
		dud.listQueryEvaluations().forEach(System.out::println);
	}
	
	@Test
	public void listQueryList() throws IOException {
		dud.listQueryClick().forEach(System.out::println);
	}
	
	@Test
	public void listTrainingEntriesFromQueryEvaluation() throws IOException {
		dud.listTrainingQueriesFromQueryEvaluation().forEach(System.out::println);
		
	}
	

	@Test
	public void saveTrainingEntries() throws IOException {
		List <TrainingEntry> trainingEntries = dud.listTrainingQueriesFromQueryEvaluation().stream().collect(Collectors.toList());
		this.resourceLoadingUtils.getObjectMapper().writeValue(new File("D:\\mltest\\trainingEntries.json"), trainingEntries);
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

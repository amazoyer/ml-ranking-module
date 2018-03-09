package com.francelabs.ranking.dao;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import com.datafari.ranking.dao.spark.SparkJobs;
import com.datafari.ranking.dao.spark.SparkModelMapper;
import com.datafari.ranking.model.QueryDocumentClickStat;
import com.datafari.ranking.model.QueryEvaluation;
import com.datafari.ranking.model.TrainingEntry;
import com.datafari.ranking.model.TrainingQuery;

@Named
public class DatafariUsageDao {

	@Inject
	SparkJobs jobs;

	public List<QueryEvaluation> listQueryEvaluations() {
		return jobs.getAggregatedQueryEvaluationRDD().cache().map(SparkModelMapper.createQueryEvaluation).collect();
	}

	public List<QueryDocumentClickStat> listQueryClick() throws IOException {
		return jobs.getQueryClickRDD().cache().map(SparkModelMapper.createQueryDocumentClickStat).collect().stream()
				.flatMap(List::stream).collect(Collectors.toList());
	}

	public List<TrainingEntry> listTrainingQueriesFromQueryEvaluation() throws IOException {
		return jobs.getTrainingEntriesFromQueryEvaluation().cache()
				.map(SparkModelMapper.createTrainingQueriesFromQueryEvaluation).collect();
	}
	//
	// public List<TrainingEntry> listTrainingEntries(){
	// jobs.getQueryEvaluationRDD();
	//
	//
	// }

}

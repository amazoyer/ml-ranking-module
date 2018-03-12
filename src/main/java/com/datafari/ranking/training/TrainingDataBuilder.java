package com.datafari.ranking.training;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ext.ParamConverter.Lazy;

import org.apache.spark.api.java.JavaRDD;

import com.datafari.ranking.model.QueryDocumentClickStat;
import com.datafari.ranking.model.QueryEvaluation;
import com.datafari.ranking.model.TrainingEntry;
import com.datafari.ranking.model.TrainingQuery;
import com.datafari.ranking.training.spark.SparkJobs;
import com.datafari.ranking.training.spark.SparkModelMapper;

import scala.Tuple3;

@Lazy
@Named
public class TrainingDataBuilder {

	@Inject
	SparkJobs jobs;

	public List<QueryEvaluation> listQueryEvaluations() {
		return jobs.getAggregatedQueryEvaluationRDD().cache().map(SparkModelMapper.createQueryEvaluation).collect();
	}

	public List<QueryDocumentClickStat> listQueryClick() throws IOException {
		return jobs.getQueryClickRDD().cache().map(SparkModelMapper.createQueryDocumentClickStat).collect().stream()
				.flatMap(List::stream).collect(Collectors.toList());
	}

	public List<TrainingEntry> retrieveTrainingEntriesFromQueryEvaluation() throws IOException {
		return jobs.getTrainingEntriesFromQueryEvaluation().cache()
				.map(SparkModelMapper.createTrainingQueriesFromQueryEvaluation).collect();
	}

	public List<TrainingEntry> retrieveTrainingEntriesFromQueryEvaluationWithNonEvaluatedDocument() {
		return jobs.getTrainingEntriesFromQueryEvaluationWithNonEvaluatedDocument().cache()
				.map(SparkModelMapper.createTrainingQueriesFromQueryEvaluationList).collect().stream()
				.flatMap(l -> l.stream()).collect(Collectors.toList());
	}

}

package com.datafari.ranking.training;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ext.ParamConverter.Lazy;
import com.datafari.ranking.model.QueryDocumentClickStat;
import com.datafari.ranking.model.QueryEvaluation;
import com.datafari.ranking.model.TrainingEntry;
import com.datafari.ranking.training.spark.SparkJobs;
import com.datafari.ranking.training.spark.SparkModelMapper;

/**
 * 
 * Use spark jobs to build training queries from Datafari
 * 
 *
 */
@Lazy
@Named
public class TrainingDataBuilder {

	@Inject
	SparkJobs jobs;

	/**
	 * 
	 * Build training query from click stat (stored in Statistics core of
	 * Datafari Solr)
	 * 
	 */
	public List<TrainingEntry> buildTrainingEntriesFromQueryClick() throws IOException {
		Stream<QueryDocumentClickStat> stream = listQueryClick();
		return stream.map(SparkModelMapper.buildTrainingEntryFromQueryClickStat).collect(Collectors.toList());
	}

	/**
	 * 
	 * Build training query from manual query evaluation (stored in Datafari
	 * Cassandra)
	 * 
	 */
	public List<TrainingEntry> retrieveTrainingEntriesFromQueryEvaluation() throws IOException {
		return jobs.getTrainingEntriesFromQueryEvaluation()
				.map(SparkModelMapper.createTrainingQueriesFromQueryEvaluation).collect();
	}

	/**
	 * 
	 * Build training query from click stat (stored in Statistics core of
	 * Datafari Solr) - also add 10 first not evaluated documents for each query
	 * with a rank
	 * 
	 */
	public List<TrainingEntry> retrieveTrainingEntriesFromQueryEvaluationWithNonEvaluatedDocument() {
		return jobs.getTrainingEntriesFromQueryEvaluationWithNonEvaluatedDocument()
				.map(SparkModelMapper.createTrainingQueriesFromQueryEvaluationList).collect().stream()
				.flatMap(l -> l.stream()).collect(Collectors.toList());
	}

	private List<QueryEvaluation> listQueryEvaluations() {
		return jobs.getAggregatedQueryEvaluationRDD().map(SparkModelMapper.createQueryEvaluation).collect();
	}

	private Stream<QueryDocumentClickStat> listQueryClick() throws IOException {
		return jobs.getQueryClickRDD().map(SparkModelMapper.createQueryDocumentClickStat).collect().stream()
				.flatMap(List::stream);
	}

	private List<QueryDocumentClickStat> retreiveQueryClick() throws IOException {
		return listQueryClick().collect(Collectors.toList());
	}

}

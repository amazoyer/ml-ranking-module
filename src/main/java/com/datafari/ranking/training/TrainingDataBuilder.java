package com.datafari.ranking.training;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	private Stream<QueryDocumentClickStat> listQueryClick() throws IOException {
		return jobs.getQueryClickRDD().cache().map(SparkModelMapper.createQueryDocumentClickStat).collect().stream()
				.flatMap(List::stream);
	}

	private List<QueryDocumentClickStat> retreiveQueryClick() throws IOException {
		return listQueryClick().collect(Collectors.toList());
	}

	public List<TrainingEntry> buildTrainingEntriesFromQueryClick() throws IOException {
		Stream<QueryDocumentClickStat> stream = listQueryClick();
		return stream.map(SparkModelMapper.buildTrainingEntryFromQueryClickStat).collect(Collectors.toList());
	}

	private void test() {
		ArrayList<String> list = new ArrayList<String>();
		Stream<String> listS = list.stream();
		listS.map(mapper);
	}

	private static Function<String, Long> mapper = new Function<String, Long>() {

		@Override
		public Long apply(String t) {
			return 0L;
		}

	};

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

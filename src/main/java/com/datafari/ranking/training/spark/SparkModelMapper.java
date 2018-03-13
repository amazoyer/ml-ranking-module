package com.datafari.ranking.training.spark;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.spark.api.java.function.Function;

import com.datafari.ranking.model.QueryDocumentClickStat;
import com.datafari.ranking.model.QueryEvaluation;
import com.datafari.ranking.model.TrainingEntry;
import com.datafari.ranking.training.ScoreMapper;

import scala.Tuple2;
import scala.Tuple3;

public class SparkModelMapper {

	public static Function<Tuple2<String, Tuple2<List<String>, List<String>>>, QueryEvaluation> createQueryEvaluation = new Function<Tuple2<String, Tuple2<List<String>, List<String>>>, QueryEvaluation>() {
		@Override
		public QueryEvaluation call(Tuple2<String, Tuple2<List<String>, List<String>>> entry) throws Exception {
			return new QueryEvaluation(entry._1(), entry._2()._1(), entry._2()._2());
		}
	};

	public static Function<Tuple2<String, Map<String, Tuple2<Optional<Map<String, Double>>, Tuple2<Long, Long>>>>, List<QueryDocumentClickStat>> createQueryDocumentClickStat = new Function<Tuple2<String, Map<String, Tuple2<Optional<Map<String, Double>>, Tuple2<Long, Long>>>>, List<QueryDocumentClickStat>>() {
		@Override
		public List<QueryDocumentClickStat> call(
				Tuple2<String, Map<String, Tuple2<Optional<Map<String, Double>>, Tuple2<Long, Long>>>> entry)
				throws Exception {
			Long totalClickForQuery = entry._2().values().stream().mapToLong(tuple -> tuple._2()._1()).sum();
			return entry._2().entrySet().stream().filter(subEntry -> subEntry.getValue()._1().isPresent())
					.map(subEntry -> new QueryDocumentClickStat(entry._1(), subEntry.getKey(),
							subEntry.getValue()._2()._1(), subEntry.getValue()._2()._2(), totalClickForQuery))
					.collect(Collectors.toList());
		}
	};

	public static Function<Tuple2<String, List<Tuple3<String, Long, Map<String, Double>>>>, List<TrainingEntry>> createTrainingQueriesFromQueryEvaluationList = new Function<Tuple2<String, List<Tuple3<String, Long, Map<String, Double>>>>, List<TrainingEntry>>() {
		@Override
		public List<TrainingEntry> call(Tuple2<String, List<Tuple3<String, Long, Map<String, Double>>>> entry)
				throws Exception {
			String query = entry._1();
			List<Tuple3<String, Long, Map<String, Double>>> listDocEval = entry._2();
			return listDocEval.stream()
					.map(trainingEntry -> new TrainingEntry(query, trainingEntry._1(), trainingEntry._3(),
							ScoreMapper.convertScoreFromEvaluationEntry(trainingEntry._2()), "HUMAN_JUDGEMENT"))
					.collect(Collectors.toList());
		}
	};

	public static Function<Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>>, TrainingEntry> createTrainingQueriesFromQueryEvaluation = new Function<Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>>, TrainingEntry>() {
		@Override
		public TrainingEntry call(
				Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>> entry)
				throws Exception {
			Tuple3<String, String, Long> infoQuery = entry._2()._1();
			Map<String, Double> features = entry._2()._2()
					.orElseThrow(() -> new RuntimeException("Non present feature map should be filtered before"));
			return new TrainingEntry(infoQuery._1(), infoQuery._2(), features,
					ScoreMapper.convertScoreFromEvaluationEntry(infoQuery._3()), "HUMAN_JUDGEMENT");
		}
	};

	public static java.util.function.Function<QueryDocumentClickStat, TrainingEntry> buildTrainingEntryFromQueryClickStat = new java.util.function.Function<QueryDocumentClickStat, TrainingEntry>() {
		@Override
		public TrainingEntry apply(QueryDocumentClickStat queryDocumentStat) {
			return new TrainingEntry(queryDocumentStat.getQuery(), queryDocumentStat.getDocumentID(), null,
					buildScoreFromClickStat(queryDocumentStat), "QUERY_LOG");
		}

	};

	private static Double buildScoreFromClickStat(QueryDocumentClickStat queryDocumentStat) {
		// TODO should promote high click count (with getClickCount) or
		// percentage of click : (with getTotalClickCountForQuery)
		// TODO should promote document clicked with an high position (with
		// averageClickPosition)

		// here very simple rule
		if (queryDocumentStat.getClickCount() == 0) {
			return 0.1D;
		}
		// high clicked doc if clicked more than 10%
		if (new Double(queryDocumentStat.getClickCount())
				/ new Double(queryDocumentStat.getTotalClickCountForQuery()) > 0.1D) {
			return 1D;
		}

		return 0.5D;

	}
}

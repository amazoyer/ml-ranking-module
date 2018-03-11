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

	public static Function<Tuple2<String, Map<String, Tuple2<Long, Long>>>, List<QueryDocumentClickStat>> createQueryDocumentClickStat = new Function<Tuple2<String, Map<String, Tuple2<Long, Long>>>, List<QueryDocumentClickStat>>() {

		@Override
		public List<QueryDocumentClickStat> call(Tuple2<String, Map<String, Tuple2<Long, Long>>> entry)
				throws Exception {
			Long totalClickForQuery = entry._2().values().stream().mapToLong(tuple -> tuple._1()).sum();
			return entry._2().entrySet()
					.stream().map(subEntry -> new QueryDocumentClickStat(entry._1(), subEntry.getKey(),
							subEntry.getValue()._1(), subEntry.getValue()._2(), totalClickForQuery))
					.collect(Collectors.toList());
		}
	};

	public static Function<Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>>, TrainingEntry> createTrainingQueriesFromQueryEvaluation = new Function<Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>>, TrainingEntry>() {
		@Override
		public TrainingEntry call(
				Tuple2<String, Tuple2<Tuple3<String, String, Long>, Optional<Map<String, Double>>>> entry)
				throws Exception {
			Tuple3<String, String, Long> infoQuery = entry._2()._1();
			Map<String, Double> features = entry._2()._2().orElseThrow(()-> new RuntimeException("Non present feature map should be filtered before"));
			return new TrainingEntry(infoQuery._1(), infoQuery._2(), features, ScoreMapper.convertScoreFromEvaluationEntry(infoQuery._3()), "HUMAN_JUDGEMENT");
		}
	};
	

}
package com.datafari.ranking.trainer;

import com.datafari.ranking.model.QueryDocumentClickStat;

/**
 * 
 * Utility methods used to calculate score for training entries
 * 
 *
 */
public class TrainingEntryScoreCalculator {

	/*
	 * Calculate ranklib score from query click stat
	 * 
	 */
	public static Double buildScoreFromClickStat(QueryDocumentClickStat queryDocumentStat) {
		// TODO should promote high click count (with getClickCount) or
		// percentage of click : (with getTotalClickCountForQuery)
		// TODO should promote document clicked with an high position (with
		// averageClickPosition)

		// here we have very simple rule
		if (queryDocumentStat.getClickCount() == 0) {
			return 0.1D;
		}
		// if clicked more than 10%, put a very high score
		if (new Double(queryDocumentStat.getClickCount())
				/ new Double(queryDocumentStat.getTotalClickCountForQuery()) > 0.1D) {
			return 1D;
		}

		// if not clicked, put a middle score
		return 0.5D;
	}

	/*
	 * Calculate ranklib score from evaluation rank (0 to 10)
	 * 
	 */
	public static Double convertScoreFromEvaluationEntry(Long evaluationEntryRank) {
		return Double.valueOf(evaluationEntryRank) / 10D;
	}

}

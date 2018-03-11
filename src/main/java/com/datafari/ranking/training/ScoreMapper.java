package com.datafari.ranking.training;


public class ScoreMapper {

	
	/*
	 * Can be used to normalize the score from Evaluation to RankLib score
	 * 
	 */
	public static Double convertScoreFromEvaluationEntry(Long evaluationEntryRank) {
		return Double.valueOf(evaluationEntryRank)/10D;
	}
	
	

}

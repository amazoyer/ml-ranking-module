package com.datafari.ranking.model;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


import scala.Serializable;



/**
 * 
 * Training entry. Can be used by InMemoryIOEvalutor to train the model
 * through Ranklib
 * Contains a couple of doc/query + associated features and a rank
 *
 */
public class TrainingEntry implements Serializable {

	private String query;
	private String docid;
	private Map<String, Double> features = Collections.EMPTY_MAP;
	private Double score;
	private String type;

	public TrainingEntry(String query, String docid, Map<String, Double> features, Double score, String type) {
		this.setQuery(query);
		this.setDocid(docid);
		this.setFeatures(features);
		this.setScore(score);
		this.setType(type);
	}

	public TrainingEntry() {

	}

	public Map<String, Double> getFeatures() {
		return features;
	}

	public void setFeatures(Map<String, Double> features) {
		if (features != null) {
			this.features = features;
		}
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDocid() {
		return docid;
	}

	public void setDocid(String docid) {
		this.docid = docid;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public String toString() {
		return "query : " + query + " docid : " + docid + " score : " + score + " features : "
				+ ((features == null) ? "empty"
						: features.entrySet().stream().map(value -> value.getKey() + ":" + value.getValue())
								.collect(Collectors.joining(",")));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TrainingEntry)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		TrainingEntry other = (TrainingEntry) obj;

		if (!other.query.equals(this.query) || !other.docid.equals(this.docid) || !other.score.equals(this.score)
				|| !other.features.equals(this.features)) {
			return false;
		}

		return true;
	}

}

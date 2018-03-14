package com.datafari.ranking.model;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import scala.Serializable;

/**
 * 
 * Click stat for a couple of query/doc
 *
 */
public class QueryDocumentClickStat implements Serializable {

	private String query;
	private String documentID;
	private Long clickCount;
	private Long totalClickCountForQuery;
	private Long totalClickPosition;
	private Map<String, Double> features;

	public QueryDocumentClickStat(String query, String documentID, Long clickCount, Long totalClickPosition,
			Long totalClickCountForQuery, Map<String, Double> features) {
		this.query = query;
		this.documentID = documentID;
		this.clickCount = clickCount;
		this.totalClickPosition = totalClickPosition;
		this.totalClickCountForQuery = totalClickCountForQuery;
		this.features = features;
	}

	public String getDocumentID() {
		return documentID;
	}

	public Long getClickCount() {
		return clickCount;
	}

	public Long getTotalClickPosition() {
		return totalClickPosition;
	}
	
	public Long getTotalClickCountForQuery(){
		return totalClickCountForQuery;
	}

	public String getQuery() {
		return query;
	}

	public Float averageClickPosition() {
		return Float.valueOf(totalClickPosition) / Float.valueOf(clickCount);
	}
	

	public Map<String, Double> getFeatures() {
		return features;
	}

	@Override
	public String toString() {
		return "query : " + query + " document : " + documentID
				+ ((clickCount > 0) ? " clickOnDocForQuery/totalClickForQuery : " + clickCount + "/"
						+ totalClickCountForQuery + " averageClickPosition : " + averageClickPosition() : "")
				+ " features : "
						+ features.entrySet().stream().map(value -> value.getKey() + ":" + value.getValue())
										.collect(Collectors.joining(","));
	}


}

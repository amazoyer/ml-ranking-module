package com.datafari.ranking.model;

import scala.Serializable;

public class QueryDocumentClickStat implements Serializable {

	private String query;
	private String documentID;
	private Long clickCount;
	private Long totalClickCountForQuery;
	private Long totalClickPosition;

	public QueryDocumentClickStat(String query, String documentID, Long clickCount, Long totalClickPosition,
			Long totalClickCountForQuery) {
		this.query = query;
		this.documentID = documentID;
		this.clickCount = clickCount;
		this.totalClickPosition = totalClickPosition;
		this.totalClickCountForQuery = totalClickCountForQuery;
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

	@Override
	public String toString() {
		return "query : " + query + " document : " + documentID
				+ ((clickCount > 0) ? " clickOnDocForQuery/totalClickForQuery : " + clickCount + "/"
						+ totalClickCountForQuery + " averageClickPosition : " + averageClickPosition() : "");
	}

}

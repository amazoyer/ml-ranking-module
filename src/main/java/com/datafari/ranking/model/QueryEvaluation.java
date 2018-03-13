package com.datafari.ranking.model;

import java.util.List;
import java.util.stream.Collectors;

import scala.Serializable;


/**
 * 
 * Slit documents between bad and good docs for a specific query
 * Not used yet in Main class
 *
 */
public class QueryEvaluation implements Serializable {
	private String query;
	private List<String> goodDocuments;
	private List<String> badDocuments;

	public QueryEvaluation(String query, List<String> goodDocuments, List<String> badDocuments) {
		this.query = query;
		this.goodDocuments = goodDocuments;
		this.badDocuments = badDocuments;
	}

	public String getQuery() {
		return query;
	}

	public List<String> getGoodDocuments() {
		return goodDocuments;
	}

	public List<String> getBadDocuments() {
		return badDocuments;
	}

	@Override
	public String toString() {
		return "Query : " + query
		+ " goodDocuments : [" + goodDocuments.stream().collect(Collectors.joining(","))+"]"
		+ " badDocuments : " + badDocuments.stream().collect(Collectors.joining(","))+"]";

	}

}

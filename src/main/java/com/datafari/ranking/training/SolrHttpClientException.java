package com.datafari.ranking.training;

public class SolrHttpClientException extends Exception{

	public SolrHttpClientException(String string) {
		super(string);
	}
	
	public SolrHttpClientException(String string, Exception e) {
		super(string, e);
	}

}

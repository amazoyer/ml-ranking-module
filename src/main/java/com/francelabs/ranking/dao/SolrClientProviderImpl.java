package com.francelabs.ranking.dao;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;

import com.lucidworks.spark.rdd.SolrJavaRDD;

@Named
public class SolrClientProviderImpl implements ISolrClientProvider{

	
	private SparkContextProviderImpl sparkContextProvider;
	
	private SolrHttpClient solrHttpClient;
	private SolrJavaRDD solrJavaRDD;
	private CloudSolrClient solrClient;

	
	@Inject
	public SolrClientProviderImpl(SparkContextProviderImpl sparkContextProvider) {
		this.sparkContextProvider = sparkContextProvider;
		solrClient = new CloudSolrClient.Builder().withZkHost("localhost" + ":2181").build();
		solrJavaRDD = SolrJavaRDD.get("localhost:2181", "Statistics", sparkContextProvider.getSparkContext().sc());
		solrHttpClient = new SolrHttpClient("localhost:8983", "FileShare");
	}


	public SolrClient getSolrClient() {
		return solrClient;
	}


	public SolrJavaRDD getSolrJavaRDD() {
		return solrJavaRDD;
	}
	
	
	@Override
	public SolrHttpClient getSolrHttpClient() throws IOException {
		return solrHttpClient;
	}
	
	public void close() throws IOException{
		solrHttpClient.close();
	}


}

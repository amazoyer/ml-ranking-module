package com.francelabs.ranking.dao;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;

import com.lucidworks.spark.rdd.SolrJavaRDD;

@Named
public class SolrClientProvider {

	
	private SparkContextProvider sparkContextProvider;
	
	private SolrJavaRDD solrJavaRDD;
	private CloudSolrClient solrClient;

	
	@Inject
	public SolrClientProvider(SparkContextProvider sparkContextProvider) {
		this.sparkContextProvider = sparkContextProvider;
		solrClient = new CloudSolrClient.Builder().withZkHost("localhost" + ":2181").build();
		solrJavaRDD = SolrJavaRDD.get("localhost:2181", "Statistics", sparkContextProvider.getSparkContext().sc());
	}


	public SolrClient getSolrClient() {
		return solrClient;
	}


	public SolrJavaRDD getSolrJavaRDD() {
		return solrJavaRDD;
	}


}

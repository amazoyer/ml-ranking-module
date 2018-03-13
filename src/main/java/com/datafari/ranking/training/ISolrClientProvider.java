package com.datafari.ranking.training;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;

import com.lucidworks.spark.rdd.SolrJavaRDD;

/**
 * 
 * Give all needed solr client
 * - Spark Solr RDD
 * - CloudSolrJ Client
 * - Simple HTTP Client
 *
 */
public interface ISolrClientProvider {

	public SolrClient getSolrClient() throws IOException;

	public SolrJavaRDD getSolrJavaRDD() throws IOException;

	public SolrHttpClient getSolrHttpClient() throws IOException;
	
	public void close();

}

package com.datafari.ranking.training;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;

import com.lucidworks.spark.rdd.SolrJavaRDD;

public interface ISolrClientProvider {

	public SolrClient getSolrClient() throws IOException;

	public SolrJavaRDD getSolrJavaRDD() throws IOException;

	public SolrHttpClient getSolrHttpClient() throws IOException;

}

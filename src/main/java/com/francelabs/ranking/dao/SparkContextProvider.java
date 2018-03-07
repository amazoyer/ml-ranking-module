package com.francelabs.ranking.dao;

import java.util.Properties;

import javax.inject.Named;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;

@Named
public class SparkContextProvider {
//	
//	Properties configurationFile = new Properties();
//	configurationFile.load(TestCassandraConnection.class.getClassLoader().getResourceAsStream("config.properties"));
//	conf = new SparkConf(true).setMaster("local").setAppName("test").set("spark.cassandra.connection.host",
//			configurationFile.getProperty("cassandraHost"));
//	solrClient = new CloudSolrClient.Builder().withZkHost(configurationFile.getProperty("solrHost") + ":2181")
//			.build();
//	sparkContext = new JavaSparkContext(conf);
	
	
	public SparkContextProvider(){
		SparkConf conf = new SparkConf(true).setMaster("local").setAppName("test").set("spark.cassandra.connection.host",
				"localhost");
		sparkContext = new JavaSparkContext(conf);	
	}

	
	private JavaSparkContext sparkContext;

	public JavaSparkContext getSparkContext() {
		return sparkContext;
	}

}

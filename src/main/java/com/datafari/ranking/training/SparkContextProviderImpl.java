package com.datafari.ranking.training;
import javax.inject.Named;
import javax.ws.rs.ext.ParamConverter.Lazy;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class SparkContextProviderImpl implements ISparkContextProvider {


	public SparkContextProviderImpl(String cassandraHost) {
		SparkConf conf = new SparkConf(true).setMaster("local").setAppName("ml-ranking")
				.set("spark.cassandra.connection.host", cassandraHost);
		sparkContext = new JavaSparkContext(conf);
	}

	private JavaSparkContext sparkContext;

	public JavaSparkContext getSparkContext() {
		return sparkContext;
	}

}

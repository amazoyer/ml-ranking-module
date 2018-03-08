package com.francelabs.ranking.dao;

import javax.inject.Named;

import org.apache.spark.api.java.JavaSparkContext;


@Named
public interface ISparkContextProvider {

	public JavaSparkContext getSparkContext();

}

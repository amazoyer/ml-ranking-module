package com.datafari.ranking.training;

import javax.inject.Named;
import javax.ws.rs.ext.ParamConverter.Lazy;

import org.apache.spark.api.java.JavaSparkContext;


@Lazy
@Named
public interface ISparkContextProvider {

	public JavaSparkContext getSparkContext();

}

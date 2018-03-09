package com.datafari.ranking.config;

import org.springframework.test.context.ContextConfiguration;

import com.datafari.ranking.ModelTrainer;
import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.datafari.ranking.dao.spark.SparkJobs;
import com.francelabs.ranking.dao.DatafariUsageDao;
import com.francelabs.ranking.dao.SolrClientProviderImpl;
import com.francelabs.ranking.dao.SparkContextProviderImpl;

@ContextConfiguration(classes = { SparkJobs.class , ModelTrainer.class, ResourceLoadingUtils.class, DatafariUsageDao.class, SparkContextProviderImpl.class, SolrClientProviderImpl.class})
public class OnlineAbstractTest extends AbstractTest{

}

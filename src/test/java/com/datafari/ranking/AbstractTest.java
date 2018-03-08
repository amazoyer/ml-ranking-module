package com.datafari.ranking;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.datafari.ranking.configuration.ConfigUtils;
import com.datafari.ranking.dao.spark.SparkJobs;
import com.francelabs.ranking.dao.DatafariUsageDao;
import com.francelabs.ranking.dao.SolrClientProviderImpl;
import com.francelabs.ranking.dao.SparkContextProviderImpl;

@ContextConfiguration(classes = { SparkJobs.class , ModelTrainer.class, ConfigUtils.class, DatafariUsageDao.class, SparkContextProviderImpl.class, SolrClientProviderImpl.class})
@RunWith(SpringRunner.class)
public class AbstractTest {
	
	@Inject
	protected ConfigUtils configUtils;
	

}

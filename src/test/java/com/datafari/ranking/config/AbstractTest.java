package com.datafari.ranking.config;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.datafari.ranking.ModelTrainer;
import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.datafari.ranking.dao.spark.SparkJobs;
import com.francelabs.ranking.dao.DatafariUsageDao;
import com.francelabs.ranking.dao.SolrClientProviderImpl;
import com.francelabs.ranking.dao.SparkContextProviderImpl;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ResourceLoadingUtils.class})
public class AbstractTest {
	
	@Inject
	protected ResourceLoadingUtils resourceLoadingUtils;
	

}

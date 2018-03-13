package com.datafari.ranking.config;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.datafari.ranking.configuration.ConfigProperties;
import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.datafari.ranking.trainer.MLTrainer;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = LocalSolrTestConfig.class)
public class AbstractTest {
	
	@Inject
	protected ResourceLoadingUtils resourceLoadingUtils;

	@Inject
	protected MLTrainer mlTrainer;
	
}

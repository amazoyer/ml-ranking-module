package com.datafari.ranking.config;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.datafari.ranking.MLTrainer;
import com.datafari.ranking.configuration.ResourceLoadingUtils;

@ContextConfiguration(classes = { ResourceLoadingUtils.class, MLTrainer.class})
public class AbstractTest {
	
	@Inject
	protected ResourceLoadingUtils resourceLoadingUtils;

	@Inject
	protected MLTrainer mlTrainer;
	

}

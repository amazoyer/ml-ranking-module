package com.datafari.ranking.config;

import javax.inject.Inject;

import org.springframework.test.context.ContextConfiguration;

import com.datafari.ranking.configuration.ConfigProperties;
import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.datafari.ranking.trainer.MLTrainer;

@ContextConfiguration(classes = ConfigProperties.class)
public class AbstractTest {
	
	@Inject
	protected ResourceLoadingUtils resourceLoadingUtils;

	@Inject
	protected MLTrainer mlTrainer;
	
}

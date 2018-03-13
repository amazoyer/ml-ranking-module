package com.datafari.ranking.config;

import java.io.IOException;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.datafari.ranking.configuration.ConfigProperties;
import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.datafari.ranking.training.ISolrClientProvider;


public class LocalSolrTestConfig extends ConfigProperties{

	@Inject
	ResourceLoadingUtils configUtils;
	
	@Override
	@Bean
	public ISolrClientProvider solrClientProvider() throws IOException {
		return new TestSolrProvider(configUtils);
	}
}

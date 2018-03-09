package com.datafari.ranking.config;

import java.io.IOException;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;

import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.francelabs.ranking.dao.ISolrClientProvider;


public class LocalSolrTestConfig {

	
	@Inject
	ResourceLoadingUtils configUtils;
	
	@Bean
	public ISolrClientProvider solrClientProvider() throws IOException {
		return new TestSolrProvider(configUtils);
	}
}

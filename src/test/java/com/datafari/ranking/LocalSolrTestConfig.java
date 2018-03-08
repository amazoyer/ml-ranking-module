package com.datafari.ranking;

import java.io.IOException;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;

import com.datafari.ranking.configuration.ConfigUtils;
import com.francelabs.ranking.dao.ISolrClientProvider;


public class LocalSolrTestConfig {

	
	@Inject
	ConfigUtils configUtils;
	
	@Bean
	public ISolrClientProvider solrClientProvider() throws IOException {
		return new TestSolrProvider(configUtils);
	}
}

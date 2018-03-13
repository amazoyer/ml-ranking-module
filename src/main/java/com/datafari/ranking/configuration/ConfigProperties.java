package com.datafari.ranking.configuration;

import java.io.IOException;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.datafari.ranking.training.ISolrClientProvider;
import com.datafari.ranking.training.ISparkContextProvider;
import com.datafari.ranking.training.SolrClientProviderImpl;
import com.datafari.ranking.training.SparkContextProviderImpl;


@Configuration
@ComponentScan("com.datafari.ranking")
@PropertySource("classpath:config.properties")
public class ConfigProperties {

	@Value("${cassandraHost}")
	private String cassandraHost;
	

	@Value("${zkHost}")
	private String zkHost;
	

	@Value("${zkPort}")
	private String zkPort;
	
	@Inject
	private ApplicationContext context;
	
	@Bean 
	public ISparkContextProvider sparkContextProvider(){
		return new SparkContextProviderImpl(cassandraHost);
	}
	

	@Bean 
	public ISolrClientProvider solrClientProvider() throws IOException{
		return new SolrClientProviderImpl(context.getBean(ISparkContextProvider.class), zkHost, zkPort);
	}
	
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
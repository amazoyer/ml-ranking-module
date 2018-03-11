package com.datafari.ranking.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.datafari.ranking.training.ISparkContextProvider;
import com.datafari.ranking.training.SparkContextProviderImpl;


@Configuration
@ComponentScan("com.datafari.ranking")
@PropertySource("classpath:config.properties")
public class ConfigProperties {

	@Value("${cassandraHost}")
	private String cassandraHost;
	
	@Bean 
	ISparkContextProvider sparkContextProvider(){
		return new SparkContextProviderImpl(cassandraHost);
	}
	
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
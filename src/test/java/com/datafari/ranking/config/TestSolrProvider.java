package com.datafari.ranking.config;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;

import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.datafari.ranking.training.ISolrClientProvider;
import com.datafari.ranking.training.SolrHttpClient;
import com.lucidworks.spark.rdd.SolrJavaRDD;

public class TestSolrProvider implements ISolrClientProvider {

	private EmbeddedSolrServer server;

	public TestSolrProvider(ResourceLoadingUtils configUtils) throws IOException {
		File file = configUtils.getResource("solrhome_test").getFile();
		server = new EmbeddedSolrServer(file.toPath(), "techproducts");

	}

	@Override
	public SolrClient getSolrClient() throws IOException {
		return server;
	}
	

	@Override
	public SolrJavaRDD getSolrJavaRDD() {
		return null;
	}

	public void close() {
		try {
			server.close();
		} catch (IOException e) {
		}
	}

	@Override
	public SolrHttpClient getSolrHttpClient() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}


}

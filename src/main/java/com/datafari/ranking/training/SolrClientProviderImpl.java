package com.datafari.ranking.training;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ext.ParamConverter.Lazy;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import com.lucidworks.spark.rdd.SolrJavaRDD;

@Lazy
@Named
public class SolrClientProviderImpl implements ISolrClientProvider {
	private static String FILESHARE = "FileShare";
	private static String STATISTICS = "Statistics";
	private static String ZK_HOST = "localhost";
	private static String ZK_PORT = "2181";
	

	private SolrHttpClient solrHttpClient;
	private SolrJavaRDD solrJavaRDD;
	private CloudSolrClient solrClient;

	@Inject
	public SolrClientProviderImpl(ISparkContextProvider sparkContextProvider) {
		solrClient = new CloudSolrClient.Builder().withZkHost(ZK_HOST + ":" + ZK_PORT).build();
		solrClient.setDefaultCollection(FILESHARE);
		solrJavaRDD = SolrJavaRDD.get(ZK_HOST + ":" + ZK_PORT, STATISTICS, sparkContextProvider.getSparkContext().sc());
		solrHttpClient = new SolrHttpClient(solrClient.getZkStateReader().getLeader(FILESHARE, "shard1").getCoreUrl());
	}

	public SolrClient getSolrClient() {
		return solrClient;
	}

	public SolrJavaRDD getSolrJavaRDD() {
		return solrJavaRDD;
	}

	@Override
	public SolrHttpClient getSolrHttpClient() throws IOException {
		return solrHttpClient;
	}


}

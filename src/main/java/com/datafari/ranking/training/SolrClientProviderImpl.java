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

	private SolrHttpClient solrHttpClient;
	private SolrJavaRDD solrJavaRDD;
	private CloudSolrClient solrClient;

	public SolrClientProviderImpl(ISparkContextProvider sparkContextProvider, String zkHost, String zkPort) {
		solrClient = new CloudSolrClient.Builder().withZkHost(zkHost + ":" + zkPort).build();
		solrClient.setDefaultCollection(FILESHARE);
		solrJavaRDD = SolrJavaRDD.get(zkHost + ":" + zkPort, STATISTICS, sparkContextProvider.getSparkContext().sc());
		solrHttpClient = new SolrHttpClient(solrClient.getZkStateReader().getLeader(FILESHARE, "shard1").getCoreUrl());
	}

	public SolrClientProviderImpl() {
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

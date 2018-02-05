
import java.io.IOException;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestSolrConnection {

	private static CloudSolrClient solrClient;

	@BeforeClass
	public static void Init() throws IOException {
		Properties configurationFile = new Properties();
		configurationFile.load(TestCassandraConnection.class.getClassLoader().getResourceAsStream("config.properties"));
		solrClient = new CloudSolrClient.Builder().withZkHost(configurationFile.getProperty("solrHost")+":2181").build();
	}

	@Test
	public void TestSolr() throws SolrServerException, IOException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("*:*");
		solrQuery.setRows(0);
		solrClient.query("Statistics", solrQuery).getQTime();;
	}

}

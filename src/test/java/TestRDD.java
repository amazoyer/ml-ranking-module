
import java.io.IOException;
import java.util.Properties;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.BeforeClass;
import org.junit.Test;
import com.francelabs.ranking.dao.DatafariUsageDao;

public class TestRDD {

	private static SparkConf conf;
	private static JavaSparkContext sparkContext;
	private static CloudSolrClient solrClient;

	@BeforeClass
	public static void Init() throws IOException {
		Properties configurationFile = new Properties();
		configurationFile.load(TestCassandraConnection.class.getClassLoader().getResourceAsStream("config.properties"));
		conf = new SparkConf(true).setMaster("local").setAppName("test").set("spark.cassandra.connection.host",
				configurationFile.getProperty("cassandraHost"));
		solrClient = new CloudSolrClient.Builder().withZkHost(configurationFile.getProperty("solrHost") + ":2181")
				.build();
		sparkContext = new JavaSparkContext(conf);
	}

	@Test
	public void getNumFavoritePerdocument() throws InterruptedException {
		DatafariUsageDao dud = new DatafariUsageDao(sparkContext, solrClient);
		dud.getNumFavoritePerdocument().foreach(entry -> System.out
				.println("query : " + entry._1() + " : " + entry._2()._1() + " : " + entry._2()._2()));
		
	}

}

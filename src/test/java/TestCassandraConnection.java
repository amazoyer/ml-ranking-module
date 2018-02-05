import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;

import java.io.IOException;
import java.util.Properties;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCassandraConnection {

	private static SparkConf conf;
	private static JavaSparkContext sparkContext;

	@BeforeClass
	public static void Init() throws IOException {
		Properties configurationFile = new Properties();
		configurationFile.load(TestCassandraConnection.class.getClassLoader().getResourceAsStream("config.properties"));

		conf = new SparkConf(true).setMaster("local").setAppName("test").set("spark.cassandra.connection.host",
				configurationFile.getProperty("cassandraHost"));
		sparkContext = new JavaSparkContext(conf);

	}
	
	@Test
	public void TestConnectionToDatafariDatabase() throws InterruptedException {
		// should not throws exception
		javaFunctions(sparkContext).cassandraTable("datafari", "ranking").count();

	}


}

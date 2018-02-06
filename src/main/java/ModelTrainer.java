import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONObject;
import org.junit.Assert;

public class ModelTrainer {
	SolrClient client;
	Logger logger = Logger.getLogger(ModelTrainer.class.getName());

	public ModelTrainer(SolrClient client){
		this.client = client;
	}
	
	public ModelTrainer(){
		
	}
	
	
	public final String store = "_DEFAULT_";
	public final String efiUserQuery = "efi.user_query";

	public JSONObject parseConfig(String configFileName) throws IOException {
		InputStream is = ModelTrainer.class.getResourceAsStream(configFileName);
		String jsonTxt = IOUtils.toString(is);
		return new JSONObject(jsonTxt);
	}

	public SolrQuery generateSolrQuery(String queryStr, String docId) {
		SolrQuery query = new SolrQuery(queryStr);
		query.addFilterQuery("id:(" + docId + ")");
		query.addField("id");
		query.addField("score");
		// query.addField("[features store=" + store + " " + efiUserQuery +
		// "=\'\\\'" + efiQuery + "\\\'\']");
		query.addField("[features]");
		return query;
	}

	public Iterable<String> getLine(String fileName) {
		InputStream in = TrainerTest.class.getResourceAsStream(fileName);
		BufferedReader readerIn = new BufferedReader(new InputStreamReader(in));
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					String userQuery = null;

					@Override
					public boolean hasNext() {
						try {
							userQuery = readerIn.readLine();
						} catch (IOException e) {
							return false;
						}
						return userQuery != null;
					}

					@Override
					public String next() {
						return userQuery;
					}
				};
			}
		};

	}

	public QueryResponse sendQuery(SolrQuery solr) throws SolrServerException, IOException {
		return client.query(solr);
	}

	public void sendModel(String obj) throws ClientProtocolException, IOException {

		CloseableHttpClient client = HttpClients.createDefault();
		String url = "http://localhost:8983/solr/techproducts/schema/feature-store/_DEFAULT_";
		HttpDelete httpDelete = new HttpDelete(url);
		HttpPut httpPut = new HttpPut(url);

		CloseableHttpResponse response = client.execute(httpDelete);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		StringEntity entity = new StringEntity(obj);
		httpPut.setEntity(entity);
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("Content-type", "application/json");

		response = client.execute(httpPut);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		client.close();

	}

	public List<TrainingEntry> getTrainingEntries() throws SolrServerException, IOException {
		Iterator<String> queryIterator = this.getLine("user_queries.txt").iterator();
		List<TrainingEntry> trainingEntries = new ArrayList<TrainingEntry>();
		while (queryIterator.hasNext()) {
			String[] userQuerySplitted = queryIterator.next().split("\\|");
			String docid = userQuerySplitted[1];
			String queryStr = userQuerySplitted[0];
			Double score = Double.parseDouble(userQuerySplitted[2]);
			String type = userQuerySplitted[3];

			SolrQuery query = generateSolrQuery(queryStr, docid);
			QueryResponse response = sendQuery(query);
			SolrDocumentList results = response.getResults();
			if (results.size() == 1) {
				String featuresValues = (String) results.get(0).getFieldValue("[features]");
				Map<String, Double> features = Arrays.stream(featuresValues.split(",")).collect(Collectors.toMap(
						entry -> (String) entry.split("=")[0], entry -> Double.parseDouble(entry.split("=")[1])));
				trainingEntries.add(new TrainingEntry(queryStr, docid, features, score, type));
			} else {
				logger.log(Level.WARNING, "Got " + results.size() + " results for query " + query.toQueryString());
			}

		}

		return trainingEntries;
	}

}

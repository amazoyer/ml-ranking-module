package com.datafari.ranking.ltr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ext.ParamConverter.Lazy;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.datafari.ranking.ltr.LtrClient.LTR_OBJECT_TYPE;
import com.datafari.ranking.model.TrainingEntry;
import com.datafari.ranking.training.ISolrClientProvider;
import com.datafari.ranking.training.SolrHttpClient;
import com.datafari.ranking.training.SolrHttpClientException;

@Lazy
@Named
public class LtrClient {

	private ISolrClientProvider solrClientProvider;
	Logger logger = Logger.getLogger(LtrClient.class.getName());

	@Inject
	public LtrClient(ISolrClientProvider solrClientProvider) {
		this.solrClientProvider = solrClientProvider;
	}

	public final String store = "_DEFAULT_";
	public final String efiUserQuery = "efi.user_query";


	public SolrQuery generateSolrQuery(String queryStr, String docId) {
		SolrQuery query = new SolrQuery(queryStr);
		query.addFilterQuery("id:(\"" + docId + "\")");
		query.addField("id");
		query.addField("score");
		query.addField("[features]");
		return query;
	}

	public QueryResponse sendQuery(SolrQuery solr) throws SolrServerException, IOException {
		return solrClientProvider.getSolrClient().query(solr);
	}

	public enum LTR_OBJECT_TYPE {
		model, feature;
	}
	private static String SCHEMA = "schema";
	private static String DEFAULT = "_DEFAULT_";
	private static String STORE_SUFFIX = "-store";

	public void sendFeatures(String features) throws SolrHttpClientException, IOException{
		sendLtrObject(features, null, LTR_OBJECT_TYPE.feature);
	}
	
	public void sendModel(String model, String modelName) throws SolrHttpClientException, IOException{
		sendLtrObject(model, modelName, LTR_OBJECT_TYPE.model);
	}
	
	public void sendLtrObject(String obj, String name,  LTR_OBJECT_TYPE ltrObjectType) throws SolrHttpClientException, IOException {
		SolrHttpClient httpClient = solrClientProvider.getSolrHttpClient();
		String resourceUrl = SCHEMA + "/" + ltrObjectType + STORE_SUFFIX + "/" + DEFAULT;
		httpClient.sendDelete(ltrObjectType.equals(LTR_OBJECT_TYPE.model) ? resourceUrl.replace(DEFAULT, name) : resourceUrl);
		httpClient.sendPut(resourceUrl, obj);

	}
	
	public Optional<Map<String, Double>> getFeaturesMap(String queryStr, String docId)
			throws SolrServerException, IOException {
		SolrQuery query = generateSolrQuery(queryStr, docId);
		QueryResponse response = sendQuery(query);
		SolrDocumentList results = response.getResults();
		if (results.size() == 1) {
			String featuresValues = (String) results.get(0).getFieldValue("[features]");
			return Optional.of(Arrays.stream(featuresValues.split(",")).collect(Collectors
					.toMap(entry -> (String) entry.split("=")[0], entry -> Double.parseDouble(entry.split("=")[1]))));
		} else {
			logger.log(Level.WARNING, "Got " + results.size() + " results for query " + query.toQueryString());
			return Optional.empty();
		}
	}

}

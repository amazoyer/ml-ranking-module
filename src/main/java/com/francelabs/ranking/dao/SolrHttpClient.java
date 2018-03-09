package com.francelabs.ranking.dao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class SolrHttpClient {

	private String url;
	private CloseableHttpClient client;

	public SolrHttpClient(String url) {
		this.url = url;
		client = HttpClients.createDefault();
	}

	public void sendDelete(String resourceUri) throws SolrHttpClientException {
		sendQuery(new HttpDelete(url + resourceUri));
	}

	public void sendPut(String resourceUri, String value) throws SolrHttpClientException {
		HttpPut httpPut = new HttpPut(url + resourceUri);
		StringEntity entity;
		try {
			entity = new StringEntity(value);
		} catch (UnsupportedEncodingException e) {
			throw new SolrHttpClientException("Encoding problem : ", e);
		}
		httpPut.setEntity(entity);
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("Content-type", "application/json");
		sendQuery(httpPut);
	}

	private void sendQuery(HttpUriRequest request) throws SolrHttpClientException {
		CloseableHttpResponse response;
		try {
			response = client.execute(request);
		} catch (IOException e) {
			throw new SolrHttpClientException("Cannot send query : ", e);

		}
		int errorCode = response.getStatusLine().getStatusCode();
		if (errorCode != 200) {
			throw new SolrHttpClientException("Error code : " + errorCode);
		}
	}

	public void close() throws IOException {
		client.close();
	}

}

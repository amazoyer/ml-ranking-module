package com.datafari.ranking.training;

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

	public SolrHttpClient(String url) {
		this.url = url;
	}

	public void sendDelete(String resourceUri) throws SolrHttpClientException, IOException {
		sendQuery(new HttpDelete(url + resourceUri));
	}

	public void sendPut(String resourceUri, String value) throws SolrHttpClientException, IOException {

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

	private void sendQuery(HttpUriRequest request) throws SolrHttpClientException, IOException {

		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response;
		try {
			response = client.execute(request);

			int errorCode = response.getStatusLine().getStatusCode();
			if (errorCode != 200) {
				throw new SolrHttpClientException("Error code : " + errorCode);
			}
		} catch (IOException e) {
			throw new SolrHttpClientException("Cannot send query : ", e);
		} finally {
			client.close();
		}

	}

	public String getUrl() {
		return url;
	}

}

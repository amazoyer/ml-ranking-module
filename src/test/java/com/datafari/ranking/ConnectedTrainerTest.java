package com.datafari.ranking;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.inject.Inject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.datafari.ranking.config.AbstractTest;
import com.datafari.ranking.config.OnlineAbstractTest;
import com.francelabs.ranking.dao.SolrHttpClientException;

@RunWith(SpringRunner.class)

public class ConnectedTrainerTest extends OnlineAbstractTest {

	@Inject
	protected LtrClient ltrClient;

	@Test
	public void sendFeatures() throws IOException, ParseException, SolrHttpClientException {
		JSONParser parser = new JSONParser();
		InputStream in = resourceLoadingUtils.getResource("featuresDatafari.json").getInputStream();
		Object obj = parser.parse(new InputStreamReader(in));
		ltrClient.sendLtrObject(obj.toString(),null, LtrClient.LTR_OBJECT_TYPE.feature);
	}
	
	
	@Ignore
	public void sendModel() throws IOException, ParseException, SolrHttpClientException {
		JSONParser parser = new JSONParser();
		InputStream in = resourceLoadingUtils.getResource("modelDatafari.json").getInputStream();
		Object obj = parser.parse(new InputStreamReader(in));
		ltrClient.sendLtrObject(obj.toString(),"DatafariModel",LtrClient.LTR_OBJECT_TYPE.model);
	}
	
	
}

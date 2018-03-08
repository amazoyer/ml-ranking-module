package com.datafari.ranking;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.inject.Inject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import com.francelabs.ranking.dao.SolrHttpClientException;

public class ConnectedTrainerTest extends AbstractTest {

	@Inject
	protected ModelTrainer modelTrainer;

	@Test
	public void sendFeatures() throws IOException, ParseException, SolrHttpClientException {
		JSONParser parser = new JSONParser();
		InputStream in = configUtils.getResource("featuresDatafari.json").getInputStream();
		Object obj = parser.parse(new InputStreamReader(in));
		modelTrainer.sendFeatures(obj.toString());
	}
	
	
}

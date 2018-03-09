package com.datafari.ranking;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.datafari.ranking.config.AbstractTest;
import com.datafari.ranking.configuration.ResourceLoadingUtils;
import com.datafari.ranking.model.TrainingEntry;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
@RunWith(SpringRunner.class)

public class ResourceLoadingUtilsTest extends AbstractTest {

	@Inject
	ResourceLoadingUtils resourceLoadingUtils;

	@Test
	public void  testTrainingEntrySerialization() throws JsonGenerationException, JsonMappingException, IOException {
		File file = new File("testTrainingEntrySerialization.json");
		TrainingEntry trainingEntry = new TrainingEntry("query", "document", null, 12D, "type");
		resourceLoadingUtils.getObjectMapper().writeValue(file,trainingEntry);
		TrainingEntry trainingEntryDeserialized = resourceLoadingUtils.getObjectMapper().readValue(file, TrainingEntry.class);
		Assert.assertEquals(trainingEntryDeserialized,trainingEntry);
	}

}

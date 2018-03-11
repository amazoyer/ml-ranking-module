package com.datafari.ranking.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.datafari.ranking.model.TrainingEntry;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Named
public class ResourceLoadingUtils implements ResourceLoaderAware {

	private ObjectMapper mapper;
	private JSONParser parser;

	ResourceLoadingUtils() {
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		parser = new JSONParser();

	}

	private ResourceLoader resourceLoader;

	public ObjectMapper getObjectMapper() {
		return mapper;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Resource getResource(String location) {
		return resourceLoader.getResource(location);
	}

	public <T extends JSONAware> T readJSON(String fileName, Class<T> clazz) throws IOException, ParseException {
		InputStream in = getResource(fileName).getInputStream();
		return (T)parser.parse(new InputStreamReader(in));
	}
	

	public List<String> getLines(String fileName) throws IOException {
		return FileUtils.readLines(getResource(fileName).getFile(), "UTF-8");
	}

}

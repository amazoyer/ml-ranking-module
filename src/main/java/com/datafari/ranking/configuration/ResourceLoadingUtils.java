package com.datafari.ranking.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;

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

	ResourceLoadingUtils() {
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

	}

	private ResourceLoader resourceLoader;

	public ObjectMapper getObjectMapper(){
		return mapper;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Resource getResource(String location) {
		return resourceLoader.getResource(location);
	}

	public Iterable<String> getLine(String fileName) throws IOException {
		InputStream in = getResource(fileName).getInputStream();
		BufferedReader readerIn = new BufferedReader(new InputStreamReader(in));
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					String line = null;

					@Override
					public boolean hasNext() {
						try {
							line = readerIn.readLine();
						} catch (IOException e) {
							return false;
						}
						return line != null;
					}

					@Override
					public String next() {
						return line;
					}
				};
			}
		};

	}
}

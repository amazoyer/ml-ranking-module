package com.datafari.ranking.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import javax.inject.Named;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Named
public class ConfigUtils implements ResourceLoaderAware{

	private ResourceLoader resourceLoader;

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

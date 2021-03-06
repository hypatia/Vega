package com.subgraph.vega.internal.http.requests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.subgraph.vega.api.html.IHTMLParser;
import com.subgraph.vega.api.http.requests.IHttpRequestEngine;
import com.subgraph.vega.api.http.requests.IHttpRequestEngineConfig;
import com.subgraph.vega.api.http.requests.IHttpRequestEngineFactory;

public class HttpRequestEngineFactory implements IHttpRequestEngineFactory {
	private final static int NTHREADS = 12;
	private final ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
	private final VegaHttpClient client = HttpClientFactory.createHttpClient();
	private IHTMLParser htmlParser;
	
	@Override
	public IHttpRequestEngineConfig createConfig() {
		return new HttpRequestEngineConfig();
	}

	@Override
	public IHttpRequestEngine createRequestEngine(
			IHttpRequestEngineConfig config) {
		return new HttpRequestEngine(executor, client, config, htmlParser);
	}
	
	protected void setHTMLParser(IHTMLParser htmlParser) {
		this.htmlParser = htmlParser;
	}
	
	protected void unsetHTMLParser(IHTMLParser htmlParser) {
		this.htmlParser = null;
	}
}

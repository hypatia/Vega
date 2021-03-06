package com.subgraph.vega.impl.scanner.handlers;

import org.apache.http.client.methods.HttpUriRequest;

import com.subgraph.vega.api.http.requests.IHttpResponse;
import com.subgraph.vega.api.http.requests.IPageFingerprint;
import com.subgraph.vega.api.scanner.IModuleContext;
import com.subgraph.vega.api.scanner.IPathState;

public class PageVariabilityCheck extends CrawlerModule {
	private final static int BH_CHECKS = 15;
	
	private final InjectionChecks injectionChecks;
	
	public PageVariabilityCheck(InjectionChecks injectionChecks) {
		this.injectionChecks = injectionChecks;
	}

	public void initialize(IPathState ps) {
		final IModuleContext ctx = ps.createModuleContext();
		for(int i = 0; i < BH_CHECKS; i++) {
			ctx.submitRequest(ps.createRequest(), this, i);
		}
	}

	
	@Override
	public void runModule(HttpUriRequest request, IHttpResponse response, IModuleContext ctx) {
		final IPathState ps = ctx.getPathState();
		
		if(response.isFetchFail()) 
			ctx.error(request, response, "Failed to fetch response during page variability check");
		else 
			testResponse(request, response, ctx);
		
		ctx.incrementResponseCount();
		if(!ctx.allResponsesReceived())
			return;
		
		if(ps.getResponseVaries())
			return;
		
		injectionChecks.launchInjectionModules(ps);
		
	}
	
	private void testResponse(HttpUriRequest request, IHttpResponse response, IModuleContext ctx) {
		final IPageFingerprint fp = response.getPageFingerprint();
		final IPathState ps = ctx.getPathState();
		if(!ps.matchesPathFingerprint(fp)) {
			ps.setResponseVaries();
			ctx.debug("Response varies");
		}
	}
}

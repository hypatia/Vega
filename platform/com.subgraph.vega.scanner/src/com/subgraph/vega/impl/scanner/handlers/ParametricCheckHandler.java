package com.subgraph.vega.impl.scanner.handlers;

import org.apache.http.client.methods.HttpUriRequest;

import com.subgraph.vega.api.crawler.ICrawlerResponseProcessor;
import com.subgraph.vega.api.http.requests.IHttpResponse;
import com.subgraph.vega.api.http.requests.IPageFingerprint;
import com.subgraph.vega.api.scanner.IModuleContext;
import com.subgraph.vega.api.scanner.IPathState;

public class ParametricCheckHandler extends CrawlerModule {
	private final static String BOGUS_PARAM = "asdf1234";
	private final ICrawlerResponseProcessor ognlHandler = new OgnlHandler();
	private final InjectionChecks injectionChecks = new InjectionChecks();

	@Override
	public void initialize(IPathState ps) {
		final IModuleContext ctx = ps.createModuleContext();
		// XXX check URI filter and parameter filter
		if(!ps.isParametric() || false) {
			ctx.debug("not parametric??");
			ps.setDone();
			return;
		}

		for(int i = 0; i < 5; i++) {
			ctx.submitAlteredRequest(this, BOGUS_PARAM, i);
		}

	}

	@Override
	public void runModule(HttpUriRequest request, IHttpResponse response,
			IModuleContext ctx) {

		if(response.isFetchFail()) {
			ctx.error(request, response, "during parameter behavior test");
			maybeScheduleNext(ctx);
			return;
		}


		final IPageFingerprint pathFP = ctx.getPathState().getPathFingerprint();
		if(pathFP != null && pathFP.isSame(response.getPageFingerprint())) {
			ctx.getPathState().setBogusParameter();
			maybeScheduleNext(ctx);
			return;
		}

		if(ctx.getPathState().isBogusParameter()) {
			ctx.debug("We classified parameter as no effect, but now it's changing");
			ctx.getPathState().setResponseVaries();
			// XXX problem varies
			maybeScheduleNext(ctx);
			return;
		}

		if(!ctx.getPathState().has404Fingerprints()) {
			ctx.debug("Adding 404 signature from parameter probe");
			ctx.getPathState().add404Fingerprint(response.getPageFingerprint());
		} else if(!ctx.getPathState().has404FingerprintMatching(response.getPageFingerprint())) {
			ctx.debug("Signature does not match previous responses");
			// XXX problem varies
			ctx.getPathState().setResponseVaries();
		}
		maybeScheduleNext(ctx);
	}

	private void maybeScheduleNext(IModuleContext ctx) {
		if(ctx.incrementResponseCount() < 5)
			return;
		scheduleNext(ctx.getPathState());
	}

	private void scheduleNext(IPathState ps) {
		final IModuleContext ctx = ps.createModuleContext();
		final String pname = ps.getFuzzableParameter().getName();
		if(!ps.isBogusParameter() && !ps.getResponseVaries() && pname != null) {
			ctx.submitAlteredParameterNameRequest(ognlHandler, "[0]['"+pname+"']", 0);
			ctx.submitAlteredParameterNameRequest(ognlHandler, "[0]['vega']", 1);
		}
		injectionChecks.initialize(ps);

	}

}

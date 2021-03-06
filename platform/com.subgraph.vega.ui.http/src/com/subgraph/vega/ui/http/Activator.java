package com.subgraph.vega.ui.http;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.subgraph.vega.api.http.proxy.IHttpProxyService;
import com.subgraph.vega.api.http.requests.IHttpRequestEngineFactory;
import com.subgraph.vega.api.model.IModel;
import com.subgraph.vega.internal.ui.http.ProxyStatusLineContribution;

public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.subgraph.vega.ui.http"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private ServiceTracker modelTracker;
	private ServiceTracker proxyServiceTracker;
	private ServiceTracker httpRequestEngineFactoryServiceTracker;
	
	private ProxyStatusLineContribution statusLineContribution = new ProxyStatusLineContribution();
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		modelTracker = new ServiceTracker(context, IModel.class.getName(), null);
		modelTracker.open();
		
		proxyServiceTracker = new ServiceTracker(context, IHttpProxyService.class.getName(), null);
		proxyServiceTracker.open();
		
		httpRequestEngineFactoryServiceTracker = new ServiceTracker(context, IHttpRequestEngineFactory.class.getName(), null);
		httpRequestEngineFactoryServiceTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public IModel getModel() {
		return (IModel) modelTracker.getService();
	}
	
	public IHttpProxyService getProxyService() {
		return (IHttpProxyService) proxyServiceTracker.getService();
	}
	
	public IHttpRequestEngineFactory getHttpRequestEngineFactoryService() {
		return (IHttpRequestEngineFactory) httpRequestEngineFactoryServiceTracker.getService();
	}
	
	public ContributionItem getStatusLineContribution() {
		return statusLineContribution;
	}
	
	public void setStatusLineProxyRunning(int port) {
		statusLineContribution.setProxyRunning(port);
	}
	
	public void setStatusLineProxyStopped() {
		statusLineContribution.setProxyStopped();
	}
}
package com.subgraph.vega.internal.model.web;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.w3c.dom.html2.HTMLDocument;

import com.db4o.ObjectContainer;
import com.db4o.events.CancellableObjectEventArgs;
import com.db4o.events.Event4;
import com.db4o.events.EventListener4;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.query.Predicate;
import com.subgraph.vega.api.events.EventListenerManager;
import com.subgraph.vega.api.events.IEventHandler;
import com.subgraph.vega.api.model.web.IWebEntity;
import com.subgraph.vega.api.model.web.IWebHost;
import com.subgraph.vega.api.model.web.IWebModel;
import com.subgraph.vega.api.model.web.IWebPath;
import com.subgraph.vega.api.model.web.NewWebEntityEvent;
import com.subgraph.vega.api.model.web.forms.IWebForm;
import com.subgraph.vega.internal.model.web.forms.FormParser;

public class WebModel implements IWebModel {
	private final EventListenerManager eventManager = new EventListenerManager();
	
	private final ObjectContainer database;
	private FormParser formParser;
	
	public WebModel(ObjectContainer database) {
		this.formParser = new FormParser(this);
		this.database = database;
		EventRegistry registry = EventRegistryFactory.forObjectContainer(database);
		registry.activating().addListener(new EventListener4<CancellableObjectEventArgs>() {

			@Override
			public void onEvent(Event4<CancellableObjectEventArgs> e,
					CancellableObjectEventArgs args) {
				final Object ob = args.object();
				if(ob instanceof WebEntity) {
					final WebEntity entity = (WebEntity) ob;
					entity.setEventManager(eventManager);
					entity.setDatabase(WebModel.this.database);
				}				
			}
		});
	}
	
	synchronized public Collection<IWebHost> getAllWebHosts() {
		synchronized(database) {
			List<IWebHost> hosts =  database.query(IWebHost.class);
			return hosts;
		}
	}

	@Override
	public Collection<IWebHost> getUnscannedHosts() {
		synchronized(database) {
			final List<IWebHost> hosts =  database.query(new Predicate<IWebHost>() {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean match(IWebHost host) {
					return host.isScanned() == false;
				}			
			});
			return hosts;
		}
	}

	@Override
	public Collection<IWebPath> getUnscannedPaths() {
		synchronized(database) {
			final List<IWebPath> paths = database.query(new Predicate<IWebPath>() {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean match(IWebPath path) {
					return path.isScanned() == false;
				}			
			});
			return paths;
		}
	}

	@Override
	public Collection<IWebPath> getAllPaths() {
		synchronized(database) {
			return database.query(IWebPath.class);
		}
	}

	@Override
	public synchronized IWebPath getWebPathByUri(URI uri) {
		final HttpHost host = uriToHost(uri);
		IWebHost wh = getWebHostByHttpHost(host);
		if(wh == null)
			wh = createWebHostFromHttpHost(host);
		IWebPath path =  wh.addPath(uriToPath(uri));
		return path;
	}
	
	private HttpHost uriToHost(URI uri) {
		return new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
	}
	
	private String uriToPath(URI uri) {
		return uri.getPath();
	}

	private List<NameValuePair> uriToParameterList(URI uri) {
		return URLEncodedUtils.parse(uri, "UTF-8");
	}

	@Override
	public IWebPath addGetTarget(URI uri) {
		final IWebPath path = getWebPathByUri(uri);
		path.addGetParameterList(uriToParameterList(uri));
		return path;
	}

	@Override
	synchronized public WebHost getWebHostByHttpHost(HttpHost host) {
		synchronized(database) {
			for(WebHost wh: database.query(WebHost.class)) {
				if(wh.getHttpHost().equals(host))
					return wh;
			}		
			return null;
		}
	}
	
	@Override
	synchronized public IWebHost createWebHostFromHttpHost(HttpHost host) {
		final WebHost wh = getWebHostByHttpHost(host);
		if(wh != null)
			return wh;
		final WebHost newHost = WebHost.createWebHost(eventManager, database, host);
		newHost.setDatabase(database);
		newHost.getRootPath().setDatabase(database);
		synchronized(database) {
			database.store(newHost);
			database.store(newHost.getRootMountPoint());
			database.store(newHost.getRootPath());
		}
		//newHost.getRootPath().setDatabase(database);
		notifyNewEntity(newHost);
		return newHost;
	}

	@Override
	public void addChangeListenerAndPopulate(IEventHandler listener) {
		synchronized(this) {
			for(IWebHost wh: getAllWebHosts()) {
				listener.handleEvent(new NewWebEntityEvent(wh));
			}
			eventManager.addListener(listener);
		}		
	}

	@Override
	public void removeChangeListener(IEventHandler listener) {
		eventManager.removeListener(listener);		
	}
	
	private void notifyNewEntity(IWebEntity entity) {
		eventManager.fireEvent(new NewWebEntityEvent(entity));
	}

	@Override
	public Collection<IWebForm> parseForms(IWebPath source,
			HTMLDocument document) {
		return formParser.parseForms(source, document);
	}
}

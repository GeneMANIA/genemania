package org.genemania.plugin.cytoscape3;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.delegates.SessionChangeDelegate;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.selection.AbstractNetworkSelectionManager;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;

public class NetworkSelectionManagerImpl extends AbstractNetworkSelectionManager<CyNetwork, CyNode, CyEdge>
										 implements NetworkAboutToBeDestroyedListener, SetCurrentNetworkListener, SessionLoadedListener {
	
	private TaskDispatcher taskDispatcher;
	private CyProperty<Properties> properties;

	public NetworkSelectionManagerImpl(
			CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils,
			TaskDispatcher taskDispatcher,
			CyProperty<Properties> properties
	) {
		super(cytoscapeUtils);
		this.taskDispatcher = taskDispatcher;
		this.properties = properties;
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent event) {
		synchronized (this) {
			handleNetworkChanged(event.getNetwork());
		}
	}

	void handleNetworkChanged(CyNetwork network) {
		if (network == null) {
			return;
		}
		NetworkProxy<CyNetwork, CyNode, CyEdge> networkProxy = cytoscapeUtils.getNetworkProxy(network);
		String networkId =  networkProxy.getIdentifier();
		handleNetworkChanged(networkId);
	}
	
	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent event) {
		synchronized (this) {
			CyNetwork network = event.getNetwork();
			if (network == null) {
				return;
			}
			NetworkProxy<CyNetwork, CyNode, CyEdge> networkProxy = cytoscapeUtils.getNetworkProxy(network);
			String networkId =  networkProxy.getIdentifier();
			handleNetworkDeleted(networkId);
		}
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent event) {
		String path = (String) properties.getProperties().get(GeneMania.DATA_SOURCE_PATH_PROPERTY);
		final File dataSourcePath;
		if (path == null) {
			String version = findVersion(event.getLoadedSession().getNetworks());
			dataSourcePath = new File("gmdata-" + version);
		} else {
			dataSourcePath = new File(path);
		}
		
		GeneManiaTask task = new GeneManiaTask(Strings.sessionChangeListener_title) {
			@Override
			protected void runTask() throws Throwable {
				SessionChangeDelegate<CyNetwork, CyNode, CyEdge> delegate = new SessionChangeDelegate<CyNetwork, CyNode, CyEdge>(dataSourcePath, plugin, progress, cytoscapeUtils);
				delegate.invoke();
				
				CyNetwork currentNetwork = cytoscapeUtils.getCurrentNetwork();
				handleNetworkChanged(currentNetwork);
			}
		};
		taskDispatcher.executeTask(task, cytoscapeUtils.getFrame(), true, true);
	}

	private String findVersion(Set<CyNetwork> networks) {
		for (CyNetwork network : networks) {
			String version = network.getRow(network).get(CytoscapeUtils.DATA_VERSION_ATTRIBUTE, String.class);
			if (version != null) {
				return version;
			}
		}
		return null;
	}
}

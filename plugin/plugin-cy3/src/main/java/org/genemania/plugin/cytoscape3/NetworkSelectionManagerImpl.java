package org.genemania.plugin.cytoscape3;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.genemania.domain.Attribute;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.delegates.SessionChangeDelegate;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.proxies.EdgeProxy;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;
import org.genemania.plugin.selection.AbstractNetworkSelectionManager;
import org.genemania.plugin.selection.SelectionEvent;
import org.genemania.plugin.selection.SelectionListener;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.NetworkGroupDetailPanel;
import org.genemania.plugin.view.components.BaseInfoPanel;

public class NetworkSelectionManagerImpl extends AbstractNetworkSelectionManager<CyNetwork, CyNode, CyEdge>
										 implements NetworkAboutToBeDestroyedListener, SetCurrentNetworkListener, SessionLoadedListener {
	
	private TaskDispatcher taskDispatcher;
	private CyProperty<Properties> properties;
	
	private ExecutorService sessionLoadExecutor = Executors.newSingleThreadExecutor();

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
		// Don't want this to run on the same thread as the session load progress dialog 
		// because there is risk of a UI deadlock between modal dialogs.
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
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
						SessionChangeDelegate<CyNetwork, CyNode, CyEdge> delegate = 
								new SessionChangeDelegate<CyNetwork, CyNode, CyEdge>(dataSourcePath, plugin, progress, cytoscapeUtils);
						delegate.invoke();
						
						CyNetwork currentNetwork = cytoscapeUtils.getCurrentNetwork();
						handleNetworkChanged(currentNetwork);
					}
				};
				taskDispatcher.executeTask(task, cytoscapeUtils.getFrame(), true, true);
			}
		};
		sessionLoadExecutor.submit(runnable);
	}
	
	@Override
	public SelectionListener<Group<?, ?>> createNetworkListSelectionListener(final BaseInfoPanel<Group<?, ?>,
			NetworkGroupDetailPanel<CyNetwork, CyNode, CyEdge>> panel, final ViewState options) {
		return new SelectionListener<Group<?, ?>>() {
			@Override
			public void selectionChanged(SelectionEvent<Group<?, ?>> event) {
				if (!selectionListenerEnabled)
					return;
				
				CyNetwork cyNetwork = cytoscapeUtils.getCurrentNetwork();
				NetworkProxy<CyNetwork, CyNode, CyEdge> networkProxy = cytoscapeUtils.getNetworkProxy(cyNetwork);
				ViewState options = networkOptions.get(networkProxy.getIdentifier());
				
				if (options == null)
					return;
				
				Set<CyEdge> enabledEdges = new HashSet<CyEdge>();
				Set<CyEdge> disabledEdges = new HashSet<CyEdge>();
				
				Map<String, Boolean> selectionChanges = new HashMap<String, Boolean>();
				
				for (Group<?, ?> group : event.items) {
					selectionChanges.put(group.getName(), event.selected);
					options.setGroupHighlighted(group, event.selected);
				}
				
				for (final CyEdge edge : networkProxy.getEdges()) {
					final EdgeProxy<CyEdge, CyNode> edgeProxy = cytoscapeUtils.getEdgeProxy(edge, cyNetwork);
					final String name = edgeProxy.getAttribute(CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
					final Boolean selectionState = selectionChanges.get(name);
					
					if (selectionState == Boolean.TRUE)
						enabledEdges.add(edge);
					else
						disabledEdges.add(edge);
				}
				
				boolean listenerState = selectionListenerEnabled;
				selectionListenerEnabled = false;
				
				if (enabledEdges.size() > 0)
					networkProxy.setSelectedEdges(enabledEdges, true);
				
				if (disabledEdges.size() > 0)
					networkProxy.setSelectedEdges(disabledEdges, false);
				
				selectionListenerEnabled = listenerState;
			}
		};		
	}

	@Override
	public SelectionListener<Network<?>> createNetworkSelectionListener() {
		return new SelectionListener<Network<?>>() {
			@Override
			public void selectionChanged(SelectionEvent<Network<?>> event) {
				if (!selectionListenerEnabled)
					return;
				
				CyNetwork cyNetwork = cytoscapeUtils.getCurrentNetwork();
				NetworkProxy<CyNetwork, CyNode, CyEdge> networkProxy = cytoscapeUtils.getNetworkProxy(cyNetwork);
				ViewState options = networkOptions.get(networkProxy.getIdentifier());
				
				if (options == null)
					return;
				
				Map<String, Boolean> edgeSelectionChanges = new HashMap<String, Boolean>();
				Map<String, Boolean> nodeSelectionChanges = new HashMap<String, Boolean>();
				
				for (Network<?> network : event.items) {
					Attribute attribute = network.adapt(Attribute.class);
					
					if (attribute != null) {
						nodeSelectionChanges.put(network.getName(), event.selected);
						options.setGeneHighlighted(attribute.getName(), true);
					} else {
						edgeSelectionChanges.put(network.getName(), event.selected);
						options.setNetworkHighlighted(network, event.selected);
					}
				}
				
				final Set<CyEdge> enabledEdges = new HashSet<CyEdge>();
				final Set<CyEdge> disabledEdges = new HashSet<CyEdge>();
				
				for (CyEdge edge : networkProxy.getEdges()) {
					EdgeProxy<CyEdge, CyNode> edgeProxy = cytoscapeUtils.getEdgeProxy(edge, cyNetwork);
					
					@SuppressWarnings("unchecked")
					List<String> names = edgeProxy.getAttribute(CytoscapeUtils.NETWORK_NAMES_ATTRIBUTE, List.class);
					
					if (names == null)
						continue;
					
					boolean selectionState = false;
					
					for (String name : names) {
						Boolean selected = edgeSelectionChanges.get(name);
						selectionState = selectionState || selected != null && selected;
						
						if (selectionState)
							break;
					}
					
					String attributeName = edgeProxy.getAttribute(CytoscapeUtils.ATTRIBUTE_NAME_ATTRIBUTE, String.class);
					
					if (attributeName != null) {
						Boolean selected = nodeSelectionChanges.get(attributeName);
						selectionState = selectionState || selected != null && selected;
					}
					
					if (selectionState)
						enabledEdges.add(edge);
					else
						disabledEdges.add(edge);
				}
				
				Set<CyNode> enabledNodes = new HashSet<CyNode>();
				Set<CyNode> disabledNodes = new HashSet<CyNode>();
				
				for (CyNode node : networkProxy.getNodes()) {
					NodeProxy<CyNode> nodeProxy = cytoscapeUtils.getNodeProxy(node, cyNetwork);
					String name = nodeProxy.getAttribute(CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
					
					if (name == null)
						continue;
					
					Boolean selected = nodeSelectionChanges.get(name);
					
					if (selected != null && selected) {
						enabledNodes.add(node);
					} else {
						disabledNodes.add(node);
					}
				}
				
				boolean listenerState = selectionListenerEnabled;
				selectionListenerEnabled = false;
				
				if (enabledEdges.size() > 0)
					networkProxy.setSelectedEdges(enabledEdges, true);
				
				if (disabledEdges.size() > 0)
					networkProxy.setSelectedEdges(disabledEdges, false);
				
				if (enabledNodes.size() > 0)
					networkProxy.setSelectedNodes(enabledNodes, true);
				
				if (disabledNodes.size() > 0)
					networkProxy.setSelectedNodes(disabledNodes, false);
				
				selectionListenerEnabled = listenerState;
			}
		};		
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

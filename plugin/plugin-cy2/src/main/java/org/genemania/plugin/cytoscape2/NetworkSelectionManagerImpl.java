package org.genemania.plugin.cytoscape2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.selection.AbstractNetworkSelectionManager;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;

public class NetworkSelectionManagerImpl extends AbstractNetworkSelectionManager<CyNetwork, CyNode, CyEdge> {
	private final PropertyChangeListener changeListener;
	private final PropertyChangeListener destroyedListener;
	
	public NetworkSelectionManagerImpl(CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils) {
		super(cytoscapeUtils);
		
		changeListener = new NetworkSelectionChangeListener();
		destroyedListener = new NetworkDestroyedListener();
	}
	
	private class NetworkSelectionChangeListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			synchronized (this) {
				String networkId = (String) event.getNewValue();
				handleNetworkChanged(networkId);
			}
		}
	}
	
	private class NetworkDestroyedListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			synchronized (this) {
				String networkId = (String) event.getNewValue();
				handleNetworkDeleted(networkId);
			}
		}
	}

	public PropertyChangeListener getNetworkChangeListener() {
		return changeListener;
	}

	public PropertyChangeListener getNetworkDestroyedListener() {
		return destroyedListener;
	}
}

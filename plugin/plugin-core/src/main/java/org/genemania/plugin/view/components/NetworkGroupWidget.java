/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.genemania.plugin.view.components;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.plugin.Strings;
import org.genemania.plugin.view.util.UiUtils;

/**
 * A UI element for a group of interaction networks.  The networks can be
 * individually selected, or selected as a whole using the top-most checkbox.
 */
public class NetworkGroupWidget {
	private final JPanel panel;
	private final Map<InteractionNetwork, JCheckBox> networkMap;
	private final JCheckBox groupCheckBox;
	private final String groupName;
	private final List<ActionListener> listeners;
	
	public NetworkGroupWidget(InteractionNetworkGroup group, List<InteractionNetwork> networks, UiUtils uiUtils) {
		networkMap = new HashMap<InteractionNetwork, JCheckBox>();
		listeners = new LinkedList<ActionListener>();
		
		panel = uiUtils.createJPanel();
		panel.setLayout(new GridBagLayout());
		
		final JToggleButton expander = uiUtils.createToggleButton();
        GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
        panel.add(expander, constraints);
        
		groupName = group.getName();
        constraints = new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        groupCheckBox = uiUtils.createCheckBox(groupName);
        groupCheckBox.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		handleGroupCheckBoxAction(e);
        	}
        });
        
		panel.add(groupCheckBox, constraints);
        
		final JPanel groupContents = uiUtils.createJPanel();
		groupContents.setLayout(new GridBagLayout());
		constraints = new GridBagConstraints(0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
		panel.add(groupContents, constraints);
		groupContents.setVisible(false);
		
		expander.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				groupContents.setVisible(expander.isSelected());
			}
		});
		
		int networkIndex = 0;
		for (InteractionNetwork network : networks) {
			
            JCheckBox checkBox = uiUtils.createCheckBox(network.getName());
            checkBox.setSelected(network.isDefaultSelected());
            checkBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchronizeCheckState();
				}
            });
            constraints = new GridBagConstraints(0, networkIndex, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 24, 0, 0), 0, 0);
            groupContents.add(checkBox, constraints);
            
            networkMap.put(network, checkBox);
            networkIndex++;
		}
		
		synchronizeCheckState();
	}
	
	private void handleGroupCheckBoxAction(ActionEvent e) {
		// If all networks are currently selected, they'll be deselected.
		// Otherwise, all networks will be selected.
		
		int totalSelected = countSelectedNetworks();
		boolean newState = totalSelected < networkMap.size();
		groupCheckBox.setSelected(newState);
		
		for (JCheckBox checkBox : networkMap.values()) {
			checkBox.setSelected(newState);
		}
		updateLabel(newState ? networkMap.size() : 0);
		
		notifyListeners(e);
	}
	
	private void notifyListeners(ActionEvent e) {
		for (ActionListener listener : listeners) {
			listener.actionPerformed(e);
		}
	}

	private void synchronizeCheckState() {
		int totalSelected = countSelectedNetworks();
		groupCheckBox.setSelected(totalSelected > 0);
		updateLabel(totalSelected);
	}
	
	private void updateLabel(int totalSelected) {
		groupCheckBox.setText(String.format(Strings.detailedSelection_label, groupName, totalSelected, networkMap.size()));
	}

	private int countSelectedNetworks() {
		int totalTrue = 0;
		for (JCheckBox checkBox : networkMap.values()) {
			if (checkBox.isSelected()) {
				totalTrue += 1;
			}
		}
		return totalTrue;
	}
	
	public Component getComponent() {
		return panel;
	}

	public Map<InteractionNetwork, JCheckBox> getCheckBoxes() {
		return Collections.unmodifiableMap(networkMap);
	}

	public void addActionListener(ActionListener listener) {
		listeners.add(listener);
		for (JCheckBox checkBox : networkMap.values()) {
			checkBox.addActionListener(listener);
		}
	}
	
	public void removeActionListener(ActionListener listener) {
		listeners.remove(listener);
		for (JCheckBox checkBox : networkMap.values()) {
			checkBox.removeActionListener(listener);
		}
	}
}

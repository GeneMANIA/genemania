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

package org.genemania.plugin.view;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;

import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.parsers.Query;
import org.genemania.plugin.selection.SelectionEvent;
import org.genemania.plugin.selection.SelectionListener;
import org.genemania.plugin.view.components.BaseInfoPanel;
import org.genemania.plugin.view.util.ScrollablePanel;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class NetworkSelectionPanel extends JPanel {
	
	private final JSplitPane pane;
	private final JPanel leftPanel;
	private final JPanel middlePanel;
	private final Map<Group<?, ?>, GroupModel> models;
	private final Map<Network<?>, JCheckBox> networkSelections;
	private final List<SelectionListener<Object>> selectionListeners;
	private final NetworkUtils networkUtils;
	private final UiUtils uiUtils;

	public NetworkSelectionPanel(NetworkUtils networkUtils, UiUtils uiUtils) {
		this.networkUtils = networkUtils;
		this.uiUtils = uiUtils;
		
		setOpaque(false);
		models = new HashMap<Group<?, ?>, GroupModel>();
		networkSelections = new HashMap<Network<?>, JCheckBox>();
		selectionListeners = new ArrayList<SelectionListener<Object>>();
		setLayout(new GridBagLayout());
		
		leftPanel = uiUtils.createJPanel();
		leftPanel.setLayout(new GridBagLayout());
		
		middlePanel = new ScrollablePanel();
		middlePanel.setLayout(new GridBagLayout());
		
		final JScrollPane leftScrollPane = createScrollPane(leftPanel);
		final Dimension d1 = uiUtils.computeTextSizeHint(getFontMetrics(getFont()), 24, 10);
		leftScrollPane.setMinimumSize(d1);
		leftScrollPane.setPreferredSize(d1);
		
		final JScrollPane rightScrollPane = createScrollPane(middlePanel);
		rightScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		final Dimension d2 = uiUtils.computeTextSizeHint(getFontMetrics(getFont()), 36, 10);
		rightScrollPane.setPreferredSize(d2);
		
		pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
		pane.setBorder(BorderFactory.createEmptyBorder());
		
		add(pane, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}
	
	private void showNetworks(Group<?, ?> group) {
		for (GroupModel model : models.values()) {
			model.networkPanel.setVisible(false);
			model.groupPanel.setBackground(BaseInfoPanel.defaultBackground);
		}
		if (group == null) {
			return;
		}
		GroupModel model = models.get(group);
		NetworkListPanel networkPanel = model.networkPanel;
		networkPanel.setVisible(true);
		model.groupPanel.setBackground(BaseInfoPanel.selectedBackground);
		repaint();
	}
	
	public void setGroups(Collection<Group<?, ?>> groups) {
		leftPanel.removeAll();
		middlePanel.removeAll();
		models.clear();
		networkSelections.clear();
		
		int index = 0;
		for (final Group<?, ?> group : groups) {
			final JCheckBox checkBox = uiUtils.createCheckBox();
			final JLabel label = new JLabel();
			JPanel groupPanel = uiUtils.createJPanel();
			groupPanel.setOpaque(true);
			groupPanel.setLayout(new GridBagLayout());
			groupPanel.add(checkBox , new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			groupPanel.add(label, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			leftPanel.add(groupPanel, new GridBagConstraints(0, index, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			final List<Network<?>> sortedNetworks = new ArrayList<Network<?>>(group.getNetworks());
			Collections.sort(sortedNetworks, networkUtils.getNetworkComparator());
			
			label.addMouseListener(createMouseListener(group));
			ActionListener listener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					validateSelection(label, group);
					notifyListeners();
				}
			};
			
			checkBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleGroupCheckBoxAction(checkBox, sortedNetworks);
					validateSelection(label, group);
					notifyListeners();
				}
			});
			
			NetworkListPanel panel = new NetworkListPanel(group, sortedNetworks, listener);
			panel.setVisible(false);
			models.put(group, new GroupModel(checkBox, label, groupPanel, panel));
			validateSelection(label, group);
			
			middlePanel.add(panel, new GridBagConstraints(0, index, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			index++;
		}
		leftPanel.add(uiUtils.createFillerPanel(), new GridBagConstraints(0, index, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		middlePanel.add(uiUtils.createFillerPanel(), new GridBagConstraints(0, index, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		if (groups.size() > 0) {
			showNetworks(groups.iterator().next());
		}
		resetDividers();
		invalidate();
	}
	
	private MouseListener createMouseListener(final Group<?, ?> group) {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				showNetworks(group);
			}
		};
	}

	private void handleGroupCheckBoxAction(JCheckBox groupCheckBox, Collection<Network<?>> networks) {
		// If all networks are currently selected, they'll be deselected.
		// Otherwise, all networks will be selected.
		
		int totalSelected = 0;
		for (Network<?> network : networks) {
			JCheckBox checkBox = networkSelections.get(network);
			if (checkBox.isSelected()) {
				totalSelected++;
			}
		}
		
		boolean newState = totalSelected < networks.size();
		groupCheckBox.setSelected(newState);
		
		for (Network<?> network : networks) {
			JCheckBox checkBox = networkSelections.get(network);
			checkBox.setSelected(newState);
		}
	}
	
	private void notifyListeners() {
		// Create a very coarse-grained selection event... no details
		SelectionEvent<Object> event = new SelectionEvent<Object>(null, true);
		for (SelectionListener<Object> listener : selectionListeners) {
			listener.selectionChanged(event);
		}
	}

	public void addListener(SelectionListener<Object> listener) {
		selectionListeners.add(listener);
	}
	
	public void removeListener(SelectionListener<Object> listener) {
		selectionListeners.remove(listener);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void validateSelection(JLabel label, Group group) {
		int selected = 0;
		Collection<Network> networks = (Collection<Network>) group.getNetworks();
		for (Network<?> network : networks) {
			if (networkSelections.get(network).isSelected()) {
				selected++;
			}
		}
		GroupModel model = models.get(group);
		model.checkBox.setSelected(selected > 0);
		label.setText(String.format(Strings.detailedSelection_label, group.getName(), selected, networks.size()));
	}
	
	private JScrollPane createScrollPane(JPanel panel) {
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEtchedBorder());
		return scrollPane;
	}

	public void resetDividers() {
		pane.setDividerLocation(0.5);
	}
	
	public Collection<Network<?>> getSelection() {
		HashSet<Network<?>> selection = new HashSet<Network<?>>();
		for (Entry<Network<?>, JCheckBox> entry : networkSelections.entrySet()) {
			if (entry.getValue().isSelected()) {
				selection.add(entry.getKey());
			}
		}
		return selection;
	}
	
	public int getSelectionCount() {
		int count = 0;
		for (Entry<Network<?>, JCheckBox> entry : networkSelections.entrySet()) {
			if (entry.getValue().isSelected()) {
				count++;
			}
		}
		return count;
	}
	
	public Collection<Group<?, ?>> getSelectedGroups() {
		Set<Group<?, ?>> groups = new HashSet<Group<?, ?>>();
		for (Group<?, ?> group : models.keySet()) {
			boolean shouldAddGroup = false;
			Collection<Network<?>> networks = new ArrayList<Network<?>>();
			for (Network<?> network : group.getNetworks()) {
				if (networkSelections.get(network).isSelected()) {
					networks.add(network);
					shouldAddGroup = true;
				}
			}
			if (shouldAddGroup) {
				groups.add(group.filter(networks));
			}
		}
		return groups;
	}
	
	public void selectAllNetworks(boolean selected) {
		for (Entry<Network<?>, JCheckBox> entry : networkSelections.entrySet()) {
			JCheckBox checkBox = entry.getValue();
			if (checkBox.isSelected() != selected) {
				checkBox.setSelected(selected);
			}
		}
		synchronizeGroupCheckBoxState();
	}

	void synchronizeGroupCheckBoxState() {
		for (Entry<Group<?, ?>, GroupModel> entry : models.entrySet()) {
			Group<?, ?> group = entry.getKey();
			GroupModel model = entry.getValue();
			validateSelection(model.label, group);
		}
	}
	
	public void selectDefaultNetworks() {
		for (Entry<Network<?>, JCheckBox> entry : networkSelections.entrySet()) {
			Network<?> network = entry.getKey();
			JCheckBox checkBox = entry.getValue();
			boolean selected = network.isDefaultSelected();
			if (checkBox.isSelected() != selected) {
				checkBox.setSelected(selected);
			}
		}
		synchronizeGroupCheckBoxState();
	}
	
	class NetworkListPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		Map<Network<?>, JPanel> networkPanels;

		private Network<?> selectedNetwork;
		
		public NetworkListPanel(Group<?, ?> group, Collection<Network<?>> networks, ActionListener listener) {
			setOpaque(false);
			networkPanels = new HashMap<Network<?>, JPanel>();
			setLayout(new GridBagLayout());
			int index = 0;
			for (final Network<?> network : networks) {
				JCheckBox checkBox = uiUtils.createCheckBox();
				checkBox.setSelected(network.isDefaultSelected());
				checkBox.addActionListener(listener);
				
				JLabel label = new JLabel(network.getName());
				label.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						selectedNetwork = network;
					}
				});
				
				NetworkDetailPanel panel = new NetworkDetailPanel(checkBox, label, network, group);
				add(panel, new GridBagConstraints(0, index, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0 , 0));
				
				networkSelections.put(network, checkBox);
				networkPanels.put(network, panel);
				index++;
			}
		}
		
		public Network<?> getSelectedNetwork() {
			return selectedNetwork;
		}
	}
	
	class GroupModel {
		JCheckBox checkBox;
		JLabel label;
		JPanel groupPanel;
		NetworkListPanel networkPanel;
		
		public GroupModel(JCheckBox checkBox, JLabel label, JPanel groupPanel, NetworkListPanel networkPanel) {
			this.checkBox = checkBox;
			this.label = label;
			this.groupPanel = groupPanel;
			this.networkPanel = networkPanel;
		}
	}

	class NetworkDetailPanel extends JPanel {
		private JEditorPane descriptionLabel;

		public NetworkDetailPanel(JCheckBox checkBox, JLabel label, Network<?> network, Group<?, ?> group) {
			setOpaque(true);
			setBackground(SystemColor.text);
			
			setLayout(new GridBagLayout());
			final JToggleButton expander = uiUtils.createToggleButton();
			expander.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleExpand(expander.isSelected());
				}
			});
			
			add(checkBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(expander, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0 ,0));
			add(label, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			
			String description = String.format(Strings.networkDetailPanelDescription_label, networkUtils.buildDescriptionHtml(network, group));
			descriptionLabel = uiUtils.createLinkEnabledEditorPane(this, description);
			descriptionLabel.setVisible(false);
			descriptionLabel.setOpaque(true);
			descriptionLabel.setBackground(new Color(0xe0, 0xe0, 0xff));
			descriptionLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(UiUtils.INFO_FONT_SIZE));
			
			add(descriptionLabel, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}

		protected void handleExpand(boolean expanded) {
			descriptionLabel.setVisible(expanded);
			invalidate();
		}
	}
	
	public void setSelection(Query query) {
		selectAllNetworks(false);
		
		for (Group<?, ?> group : query.getGroups()) {
			for (Network<?> network : group.getNetworks()) {
				for (Entry<Network<?>, JCheckBox> entry : networkSelections.entrySet()) {
					Network<?> otherNetwork = entry.getKey();
					
					if (!network.equals(otherNetwork))
						continue;
					
					JCheckBox checkBox = entry.getValue();
					checkBox.setSelected(true);
				}
			}
		}
		
		synchronizeGroupCheckBoxState();
	}
}

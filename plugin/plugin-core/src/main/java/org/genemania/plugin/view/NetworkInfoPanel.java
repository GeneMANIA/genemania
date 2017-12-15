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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Comparator;

import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.view.components.BaseInfoPanel;
import org.genemania.plugin.view.components.ToggleInfoPanel;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class NetworkInfoPanel extends ToggleInfoPanel<Network<?>, NetworkDetailPanel> {

	private double weight;

	public NetworkInfoPanel(
			DataSet data,
			Group<?, ?> group,
			NetworkGroupInfoPanel groupInfoPanel,
			NetworkUtils networkUtils,
			UiUtils uiUtils, 
			ViewState options
	) {
		super(uiUtils);

		setBackground(BaseInfoPanel.defaultBackground);
		int index = 0;
		for (Network<?> network : group.getNetworks()) {
			double networkWeight = network.getWeight() * 100;
			weight += networkWeight;
			NetworkDetailPanel panel = new NetworkDetailPanel(network, group, networkUtils, uiUtils, data);
			addDetailPanel(panel, index);
			index++;
		}
		add(uiUtils.createFillerPanel(), new GridBagConstraints(0, index, 1, 1, 1, 1,
				GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		invalidate();

		addSelectionListener((evt) -> {
			for (Network<?> network : evt.items)
				options.setNetworkHighlighted(network, evt.selected);
			
			for (Group<?, ?> gr : options.getAllGroups())
				options.setGroupHighlighted(gr, false);

			groupInfoPanel.updateSelection(options);
		});
	}

	@Override
	public void applyOptions(final ViewState options) {
	}

	@Override
	protected Comparator<NetworkDetailPanel> getComparator(final int column, Boolean descending) {
		if (descending == null)
			isDescending = !isDescending;
		else
			isDescending = descending;

		return new Comparator<NetworkDetailPanel>() {
			@Override
			public int compare(NetworkDetailPanel o1, NetworkDetailPanel o2) {
				switch (column) {
					case 0:
						return o1.getSubject().getName().compareTo(o2.getSubject().getName()) * (isDescending ? -1 : 1);
					case 1:
						return Double.compare(o1.getWeight(), o2.getWeight()) * (isDescending ? -1 : 1);
					default:
						throw new RuntimeException(String.format(Strings.tableModelInvalidColumn_error, column));
				}
			}
		};
	}

	@Override
	public void setAllEnabled(boolean enabled) {
	}

	@Override
	public void updateSelection(ViewState options) {
		for (NetworkDetailPanel panel : dataModel) {
			Network<?> network = panel.getSubject();
			panel.setHighlighted(options.isNetworkHighlighted(network));
		}
	}

	public double getWeight() {
		return weight;
	}
}

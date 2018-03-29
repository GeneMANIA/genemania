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
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import org.genemania.plugin.GeneMania;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.SessionManager;
import org.genemania.plugin.view.components.BaseInfoPanel;
import org.genemania.plugin.view.components.ToggleDetailPanel;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class NetworkGroupDetailPanel extends ToggleDetailPanel<Group<?, ?>> {
	
	private final Group<?, ?> group;
	private final NetworkInfoPanel networksPanel;
	private final JToggleButton expander;
	private final JCheckBox toggleBox;
	private final Component fillerPanel;
	private final NetworkUtils networkUtils;
	private final DataSet data;
	
	private int barXOffset = -1;
	private int barYOffset = -1;

	public NetworkGroupDetailPanel(
			final Group<?, ?> group,
			final NetworkGroupInfoPanel groupInfoPanel,
			final GeneMania plugin,
			final NetworkUtils networkUtils,
			final UiUtils uiUtils,
			final boolean enabledByDefault,
			final ViewState options
	) {
		super(uiUtils);
		
		this.group = group;
		this.networkUtils = networkUtils;
		this.data = plugin.getDataSetManager().getDataSet();

		networksPanel = new NetworkInfoPanel(data, group, groupInfoPanel, networkUtils, uiUtils, options);
		SessionManager manager = plugin.getSessionManager();
		networksPanel.addSelectionListener(manager.createNetworkSelectionListener());
		
		Color textColor = group.hasInteractions() ? SystemColor.textText : SystemColor.textInactiveText;
		
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		
		setLayout(new GridBagLayout());
		JLabel nameLabel = new JLabel(group.getName());
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		nameLabel.setForeground(textColor);
		
		JLabel scoreLabel = new JLabel(String.format(Strings.networkScore_label, getWeight()));
		scoreLabel.setForeground(textColor);
		
		toggleBox = uiUtils.createCheckBox();
		toggleBox.setSelected(enabledByDefault);
		toggleBox.addActionListener(manager.createChangeListener(group));
		
		if (!group.hasInteractions()) {
			setEnabled(false);
			toggleBox.setSelected(false);
			toggleBox.setEnabled(false);
		}
		
		expander = createToggleButton();
		
		uiUtils.makeSmall(nameLabel, scoreLabel, toggleBox, expander);

		add(expander, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, EXPANDER_PADDING, 0, EXPANDER_PADDING), 0, 0));
		add(toggleBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(nameLabel, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(scoreLabel, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		fillerPanel = uiUtils.createFillerPanel(BaseInfoPanel.defaultBackground);
		add(fillerPanel, new GridBagConstraints(0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(networksPanel, new GridBagConstraints(2, 1, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		computeBarPosition();
		
		Color color = networkUtils.getNetworkColor(data, group);
		g.setColor(color);
		int width = (int) ((getWidth() - barXOffset) * getWeight() / 100d);
		
		for (int y = 0; y < 3; y++) {
			int yOffset = barYOffset - y - 2;
			g.drawLine(barXOffset, yOffset, width + barXOffset, yOffset);
		}
	}
	
	private void computeBarPosition() {
		if (barXOffset == -1) {
			networksPanel.setVisible(true);
			doLayout();
			barXOffset = networksPanel.getX();
			barYOffset = networksPanel.getY();
			networksPanel.setVisible(false);
			fillerPanel.setVisible(false);
		}
	}

	@Override
	public Group<?, ?> getSubject() {
		return group;
	}

	@Override
	protected void doShowDetails(boolean show, int depth) {
		expander.setSelected(show);
		networksPanel.setVisible(show);
		fillerPanel.setVisible(show);
		
		if (depth == 0) {
			invalidate();
			return;
		}
		
		for (Component component : networksPanel.getComponents()) {
			if (!(component instanceof NetworkDetailPanel))
				continue;
			
			NetworkDetailPanel panel = (NetworkDetailPanel) component;
			panel.showDetails(show, depth - 1);
		}
		
		invalidate();
	}

	public void setItemEnabled(boolean enabled) {
		if (!getEnabled() && enabled)
			return;
		
		toggleBox.setSelected(enabled);
	}

	public double getWeight() {
		return networksPanel.getWeight();
	}

	public NetworkInfoPanel getNetworkInfoPanel() {
		return networksPanel;
	}
}

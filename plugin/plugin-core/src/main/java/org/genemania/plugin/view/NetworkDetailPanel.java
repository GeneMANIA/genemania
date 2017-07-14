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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.view.components.ToggleDetailPanel;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class NetworkDetailPanel extends ToggleDetailPanel<Network<?>> {
	
	private final Network<?> network;
	private final JEditorPane descriptionLabel;
	private final JToggleButton expander;
	private final NetworkUtils networkUtils;
	private final DataSet data;
	private Group<?, ?> group;
	
	private int barXOffset = -1;
	private int barYOffset = -1;
	private JLabel nameLabel;
	
	public NetworkDetailPanel(Network<?> network, Group<?, ?> group, NetworkUtils networkUtils, UiUtils uiUtils, DataSet data) {
		super(uiUtils);
		this.networkUtils = networkUtils;
		this.network = network;
		this.group = group;
		this.data = data;
		
		Color textColor = network.hasInteractions() ? SystemColor.textText : SystemColor.textInactiveText;
		
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		setBackground(SystemColor.text);
		setOpaque(true);
		
		setLayout(new GridBagLayout());
		nameLabel = new JLabel(network.getName());
		nameLabel.setForeground(textColor);
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		
		JLabel scoreLabel = new JLabel(String.format(Strings.networkScore_label, getWeight()));
		scoreLabel.setForeground(textColor);
		expander = createToggleButton();
		
		add(expander, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, EXPANDER_PADDING, 0, EXPANDER_PADDING), 0, 0));
		add(nameLabel, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0));
		add(scoreLabel, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		String description = String.format(Strings.networkDetailPanelDescription_label, networkUtils.buildDescriptionHtml(network, group));
		descriptionLabel = uiUtils.createLinkEnabledEditorPane(this, description);
		descriptionLabel.setFont(scoreLabel.getFont());
		descriptionLabel.setVisible(false);
		descriptionLabel.setForeground(textColor);
		add(descriptionLabel, new GridBagConstraints(1, 1, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
	
		uiUtils.makeSmall(nameLabel, scoreLabel, expander, descriptionLabel);
	}

	@Override
	public void setSelected(boolean selected) {
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		computeBarPosition();
		
		Color color = networkUtils.getNetworkColor(data, group);
		g.setColor(color);
		
		int width = (int) ((getWidth() - barXOffset) * getWeight() / 100d);
		for (int y = 0; y < 2; y++) {
			int yOffset = barYOffset - y - 1;
			g.drawLine(barXOffset, yOffset, width + barXOffset, yOffset);
		}
	}

	private void computeBarPosition() {
		if (barXOffset == -1) {
			barXOffset = nameLabel.getX();
			barYOffset = getHeight();
		}
	}

	@Override
	public Network<?> getSubject() {
		return network;
	}

	@Override
	protected void doShowDetails(boolean show, int depth) {
		if (depth == 0) {
			invalidate();
			return;
		}
		expander.setSelected(show);
		descriptionLabel.setVisible(show);
		invalidate();
	}

	public double getWeight() {
		return network.getWeight() * 100d;
	}

	public void setHighlighted(boolean networkHighlighted) {
		super.setSelected(networkHighlighted);
	}
}

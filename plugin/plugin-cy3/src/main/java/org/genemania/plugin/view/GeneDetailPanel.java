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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import org.genemania.domain.Gene;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.view.components.ToggleDetailPanel;
import org.genemania.plugin.view.util.UiUtils;

public class GeneDetailPanel extends ToggleDetailPanel<Gene> {
	private static final long serialVersionUID = 1L;

	/**
	 * For some weird reason, Swing controls clip text rather than wrap
	 * when its width is less than 350 pixels.
	 */
	private static final int MINIMUM_WIDTH_HINT = 350;
	
	final Gene gene;
	final double score;
	final JEditorPane descriptionLabel;
	final boolean isQueryGene;

	private final JToggleButton expander;

	private final Component fillerPanel;

	public GeneDetailPanel(Gene gene, double score, boolean isQueryGene, NetworkUtils networkUtils, UiUtils uiUtils) {
		super(uiUtils);
		
		this.gene = gene;
		this.score = score;
		this.isQueryGene = isQueryGene;
		
		setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.white));
		
		setLayout(new GridBagLayout());
		String name = networkUtils.getGeneLabel(gene);
		JLabel nameLabel = new JLabel(name);
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		uiUtils.makeSmall(nameLabel);
		
		expander = createToggleButton();
		uiUtils.makeSmall(expander);
		
		add(expander, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, EXPANDER_PADDING, 0, EXPANDER_PADDING), 0, 0));
		add(nameLabel, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		if (isQueryGene) {
			nameLabel.setForeground(new Color(0x20, 0x80, 0x20));
		} else {
			JLabel scoreLabel = new JLabel(String.format("%.2f", score * 100d)); //$NON-NLS-1$
			add(scoreLabel, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			uiUtils.makeSmall(scoreLabel);
		}
		
		String description = networkUtils.buildGeneDescription(gene);
		descriptionLabel = uiUtils.createLinkEnabledEditorPane(this, description);
		descriptionLabel.setFont(getFont());
		descriptionLabel.setVisible(false);
		descriptionLabel.setOpaque(true);
		descriptionLabel.setBackground(Color.white);
		uiUtils.makeSmall(descriptionLabel);
		add(descriptionLabel, new GridBagConstraints(1, 1, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		
		fillerPanel = uiUtils.createFillerPanel(Color.white);
		fillerPanel.setVisible(false);
		add(fillerPanel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}

	@Override
	public Gene getSubject() {
		return gene;
	}

	public double getScore() {
		return score;
	}
	
	@Override
	protected void doShowDetails(boolean show, int depth) {
		expander.setSelected(show);
		fillerPanel.setVisible(show);
		descriptionLabel.setVisible(show);
		invalidate();
	}
	
	public boolean getShowDetails() {
		return descriptionLabel.isVisible();
	}
	
	@Override
	public Dimension getMinimumSize() {
		Dimension size = super.getMinimumSize();
		if (descriptionLabel.isVisible()) {
			return new Dimension(Math.max(MINIMUM_WIDTH_HINT, size.width), size.height);
		} else {
			return size;
		}
	}

	public String getGeneName() {
		return gene.getSymbol();
	}
}

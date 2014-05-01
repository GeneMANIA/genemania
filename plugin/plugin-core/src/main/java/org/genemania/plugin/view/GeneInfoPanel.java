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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.genemania.domain.Gene;
import org.genemania.domain.Node;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.SelectionEvent;
import org.genemania.plugin.view.components.ToggleInfoPanel;
import org.genemania.plugin.view.util.UiUtils;

public class GeneInfoPanel extends ToggleInfoPanel<Gene, GeneDetailPanel> {
	private static final long serialVersionUID = 1L;
	private final NetworkUtils networkUtils;
	
	public GeneInfoPanel(NetworkUtils networkUtils, UiUtils uiUtils) {
		super(uiUtils);
		this.networkUtils = networkUtils;
	}
	
	@Override
	public void applyOptions(ViewState options) {
		removeAll();

		SearchResult result = options.getSearchResult();
		Map<Gene, Double> scores = result.getScores();
		List<Gene> sortedNodes = networkUtils.createSortedList(scores);
		int index = 0;
		for (Gene gene : sortedNodes) {
			double score = scores.get(gene);
			Node node = gene.getNode();
			long nodeId = node.getId();
			boolean isQueryNode = result.isQueryNode(nodeId);
			
			GeneDetailPanel panel = new GeneDetailPanel(gene, score, isQueryNode, networkUtils, uiUtils);
			addDetailPanel(panel, index);
			index++;
		}
		add(uiUtils.createFillerPanel(), new GridBagConstraints(0, index, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		invalidate();
	}

	@Override
	public void updateSelection(ViewState options) {
		GeneDetailPanel mostRecent = null;
		for (GeneDetailPanel panel : dataModel) {
			boolean enabled = options.getGeneHighlighted(panel.getGeneName());
			if (enabled) {
				mostRecent = panel;
				panel.showDetails(true, 1);
			}
			panel.setSelected(enabled);
		}
		if (mostRecent != null) {
			ensureVisible(mostRecent);
		}
	}

	@Override
	protected void setAllEnabled(boolean enabled) {
		Set<Gene> genes = new HashSet<Gene>();
		for (GeneDetailPanel panel : dataModel) {
			if (panel.getSelected() != enabled) {
				genes.add(panel.getSubject());
				panel.setSelected(enabled);
			}
		}
		notifyListeners(new SelectionEvent<Gene>(genes, enabled));
	}

	@Override
	protected Comparator<GeneDetailPanel> getComparator(final int column, Boolean descending) {
		if (descending == null) {
			isDescending = !isDescending;
		} else {
			isDescending = descending;
		}
		
		return new Comparator<GeneDetailPanel>() {
			public int compare(GeneDetailPanel o1, GeneDetailPanel o2) {
				switch (column) {
				case 0:
					return o1.getSubject().getSymbol().compareTo(o2.getSubject().getSymbol()) * (isDescending ? -1 : 1);
				case 1:
					if (o1.isQueryGene && o2.isQueryGene) {
						return o1.getSubject().getSymbol().compareTo(o2.getSubject().getSymbol());
					} else if (o1.isQueryGene) {
						return -1;
					} else if (o2.isQueryGene) {
						return 1;
					} else {
						return Double.compare(o1.getScore(), o2.getScore()) * (isDescending ? -1 : 1);
					}
				default:
					throw new RuntimeException(String.format(Strings.tableModelInvalidColumn_error, column));
				}
			}
		};
	}
}

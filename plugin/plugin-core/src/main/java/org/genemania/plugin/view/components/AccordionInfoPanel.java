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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import org.genemania.plugin.selection.SelectionEvent;
import org.genemania.plugin.view.util.UiUtils;

public abstract class AccordionInfoPanel<U, T extends AccordionDetailPanel<U>> extends BaseInfoPanel<U, T> {
	private static final long serialVersionUID = 1L;

	protected T currentlyExpandedPanel;

	public AccordionInfoPanel(UiUtils uiUtils) {
		super(uiUtils);
	}
	
	@Override
	public void addDetailPanel(T panel, final int panelIndex) {
		panel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
				T panel = dataModel.get(panelIndex);
				boolean select;
				if (panel == currentlyExpandedPanel) {
					select = false;
					panel.showDetails(false, 1);
					panel.setSelected(false);
					currentlyExpandedPanel = null;
				} else {
					select = true;
					if (currentlyExpandedPanel != null) {
						currentlyExpandedPanel.showDetails(false, 1);
					}
					panel.showDetails(true, 1);
					clearSelection();
					panel.setSelected(true);
					currentlyExpandedPanel = panel;
				}
				
				Set<U> subjects = new HashSet<U>();
				subjects.add(panel.getSubject());
				notifyListeners(new SelectionEvent<U>(subjects, select));
			}
		});
		add(panel);
		dataModel.add(panel);
		displayModel.add(panel);
	}

	public void setCurrentlyExpandedPanel(T panel) {
		this.currentlyExpandedPanel = panel;
	}
}

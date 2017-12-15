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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import org.genemania.plugin.selection.SelectionEvent;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public abstract class ToggleInfoPanel<U, T extends ToggleDetailPanel<U>> extends BaseInfoPanel<U, T> {
	
	public ToggleInfoPanel(UiUtils uiUtils) {
		super(uiUtils);
	}
	
	@Override
	protected void addDetailPanel(T panel, final int panelIndex) {
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
				boolean selected = !panel.getSelected();
				
				if (selected) {
					Set<U> deselected = new HashSet<U>();
					for (T otherPanel : dataModel) {
						if (otherPanel == panel) {
							continue;
						}
						deselected.add(otherPanel.getSubject());
						otherPanel.setSelected(false);
					}
					if (deselected.size() > 0) {
						notifyListeners(new SelectionEvent<U>(deselected, false));
					}
				}
				
				Set<U> subjects = new HashSet<U>();
				panel.setSelected(selected);
				subjects.add(panel.getSubject());
				notifyListeners(new SelectionEvent<U>(subjects, selected));
			}
		});
		add(panel, new GridBagConstraints(0, panelIndex, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		dataModel.add(panel);
		displayModel.add(panel);
	}
}

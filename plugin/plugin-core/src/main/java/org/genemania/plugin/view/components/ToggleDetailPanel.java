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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JToggleButton;

import org.genemania.plugin.view.util.UiUtils;

public abstract class ToggleDetailPanel<T> extends BaseDetailPanel<T> {
	private static final long serialVersionUID = 1L;
	protected static final int EXPANDER_PADDING = 5;
	private final UiUtils uiUtils;
	
	public ToggleDetailPanel(UiUtils uiUtils) {
		this.uiUtils = uiUtils;
	}
	
	@Override
	public void showDetails(boolean show, int depth) {
		doShowDetails(show, depth);
	}
	
	public JToggleButton createToggleButton() {
		final JToggleButton expandButton = uiUtils.createToggleButton();
		expandButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doShowDetails(expandButton.isSelected(), 1);
			}
		});
		return expandButton;
	}
}

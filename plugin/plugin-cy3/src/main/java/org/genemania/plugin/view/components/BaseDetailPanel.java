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

import javax.swing.JPanel;

public abstract class BaseDetailPanel<T> extends JPanel {
	private static final long serialVersionUID = 1L;
	boolean selected;
	boolean enabled = true;
	
	public boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean value) {
		enabled = value;
		if (!enabled && selected) {
			setSelected(false);
		}
	}
	
	public boolean getSelected() {
		return selected;
	}
	
	protected String escape(String text) {
		return text.replace("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setSelected(boolean selected) {
		if (!enabled && selected) {
			return;
		}
		setBackground(selected ? BaseInfoPanel.selectedBackground : BaseInfoPanel.defaultBackground);
		this.selected = selected;
	}
	
	public abstract T getSubject();
	protected abstract void doShowDetails(boolean show, int depth);
	public abstract void showDetails(boolean show, int depth);
}

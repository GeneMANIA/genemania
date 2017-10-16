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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.SelectionEvent;
import org.genemania.plugin.selection.SelectionListener;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public abstract class BaseInfoPanel<U, T extends BaseDetailPanel<U>> extends JPanel implements Scrollable {
	
	public static final Color selectedBackground;
	public static final Color defaultBackground;
	
	static {
		selectedBackground = new Color(222, 234, 252);
		defaultBackground = SystemColor.text;
	}
	
	protected List<T> dataModel;
	protected List<T> displayModel;
	protected List<SelectionListener<U>> selectionListeners;
	protected boolean isDescending;
	
	private GridBagLayout layout;

	protected final UiUtils uiUtils;

	public BaseInfoPanel(UiUtils uiUtils) {
		this.uiUtils = uiUtils;
		setOpaque(false);
		selectionListeners = new ArrayList<>();
		dataModel = new ArrayList<>();
		displayModel = new ArrayList<>();
		layout = new GridBagLayout();
		setLayout(layout);
		isDescending = true;
	}
	
	public void clearSelection() {
		Set<U> deselected = new HashSet<>();
		
		for (T panel : dataModel) {
			if (!panel.getSelected())
				continue;
			
			panel.setSelected(false);
			deselected.add(panel.getSubject());
		}
		
		SelectionEvent<U> event = new SelectionEvent<>(deselected, false);
		notifyListeners(event);
	}
	
	protected void notifyListeners(SelectionEvent<U> event) {
		for (SelectionListener<U> listener : selectionListeners) {
			listener.selectionChanged(event);
		}
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL)
			return (int) visibleRect.getWidth();
		
		return (int) visibleRect.getHeight();
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 4;
	}
	
	int getFirstVisiblePanelIndex() {
		for (int i = 0; i < displayModel.size(); i++) {
			T panel = displayModel.get(i);
			Rectangle rectangle = panel.getVisibleRect();
			
			if (rectangle.height > 0)
				return i;
		}
		
		return -1;
	}
	
	public void addSelectionListener(SelectionListener<U> listener) {
		selectionListeners.add(listener);
	}
	
	public void removeSelectionListener(SelectionListener<U> listener) {
		selectionListeners.remove(listener);
	}
	
	protected void ensureVisible(final T panel) {
		SwingUtilities.invokeLater(() -> {
			Rectangle bounds = panel.getBounds();
			bounds.x = 0;
			bounds.y = 0;
			panel.scrollRectToVisible(bounds);
		});
	}

	public void showDetails(boolean show, int depth) {
		for (T panel : dataModel) {
			panel.showDetails(show, depth);
		}
	}
	
	public abstract void updateSelection(ViewState options);
	public abstract void applyOptions(ViewState options);
	protected abstract void addDetailPanel(T panel, final int panelIndex);
	public abstract void setAllEnabled(boolean enabled);

	@Override
	public void removeAll() {
		selectionListeners.clear();
		super.removeAll();
		dataModel.clear();
		displayModel.clear();
	}
	
	public void sort(int column, Boolean descending) {
		Comparator<T> comparator = getComparator(column, descending);
		Collections.sort(displayModel, comparator);
		
		int index = 0;
		for (T panel : displayModel) {
			setDisplayIndex(panel, index);
			index++;
		}
		validate();
	}
	
	protected void setDisplayIndex(T panel, int displayIndex) {
		GridBagConstraints constraints = layout.getConstraints(panel);
		constraints.gridy = displayIndex;
		layout.setConstraints(panel, constraints);
		invalidate();
	}
	
	protected abstract Comparator<T> getComparator(int column, Boolean descending);
}

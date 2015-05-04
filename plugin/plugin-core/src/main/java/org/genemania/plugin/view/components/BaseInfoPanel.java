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
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.SelectionEvent;
import org.genemania.plugin.selection.SelectionListener;
import org.genemania.plugin.view.util.UiUtils;

public abstract class BaseInfoPanel<U, T extends BaseDetailPanel<U>> extends JPanel implements Scrollable {
	private static final long serialVersionUID = 1L;

	public static final Color selectedBackground;
	public static final Color defaultBackground;
	protected static final Pattern sortPattern = Pattern.compile("#sort-(\\d+)([+-])?"); //$NON-NLS-1$
	
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
		selectionListeners = new ArrayList<SelectionListener<U>>();
		dataModel = new ArrayList<T>();
		displayModel = new ArrayList<T>();
		layout = new GridBagLayout();
		setLayout(layout);
		isDescending = true;
	}
	
	public void clearSelection() {
		Set<U> deselected = new HashSet<U>();
		for (T panel : dataModel) {
			if (!panel.getSelected()) {
				continue;
			}
			panel.setSelected(false);
			deselected.add(panel.getSubject());
		}
		SelectionEvent<U> event = new SelectionEvent<U>(deselected, false);
		notifyListeners(event);
	}
	
	protected void notifyListeners(SelectionEvent<U> event) {
		for (SelectionListener<U> listener : selectionListeners) {
			listener.selectionChanged(event);
		}
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return (int) visibleRect.getWidth();
		}
		return (int) visibleRect.getHeight();
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 4;
	}
	
	int getFirstVisiblePanelIndex() {
		for (int i = 0; i < displayModel.size(); i++) {
			T panel = displayModel.get(i);
			Rectangle rectangle = panel.getVisibleRect();
			if (rectangle.height > 0) {
				return i;
			}
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
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Rectangle bounds = panel.getBounds();
				bounds.x = 0;
				bounds.y = 0;
				panel.scrollRectToVisible(bounds);
			}
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
	protected abstract void setAllEnabled(boolean enabled);

	public JPanel createExpanderPanel(String contents) {
		JPanel panel = uiUtils.createJPanel();
		panel.setLayout(new GridBagLayout());
		JEditorPane expanders = uiUtils.createEditorPane(contents);
		expanders.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() != EventType.ACTIVATED) {
					return;
				}
				String reference = e.getDescription();
				if ("#ex-all".equals(reference)) { //$NON-NLS-1$
					showDetails(true, -1);
				} else if ("#ex-top".equals(reference)) { //$NON-NLS-1$
					showDetails(false, -1);
					showDetails(true, 0);
				} else if ("#ex-none".equals(reference)) { //$NON-NLS-1$
					showDetails(false, -1);
				} else if ("#en-all".equals(reference)) { //$NON-NLS-1$
					setAllEnabled(true);
				} else if ("#en-none".equals(reference)) { //$NON-NLS-1$
					setAllEnabled(false);
				} else {
					Matcher matcher = sortPattern.matcher(reference);
					if (!matcher.matches()) {
						return;
					}
					int column = Integer.parseInt(matcher.group(1));
					Boolean descending;
					String direction = matcher.group(2);
					if (direction == null) {
						descending = null;
					} else if (direction.equals("-")) { //$NON-NLS-1$
						descending = true;
					} else {
						descending = false;
					}
					sort(column, descending);
				}
			}
		});

		panel.add(expanders, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0 , 0));
		return panel;
	}
	
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

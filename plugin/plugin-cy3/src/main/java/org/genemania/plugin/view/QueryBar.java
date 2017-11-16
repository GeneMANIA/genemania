/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2017 University of Toronto.
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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.genemania.domain.Organism;
import org.genemania.plugin.cytoscape3.model.OrganismManager;
import org.genemania.plugin.view.util.TextIcon;

import com.sun.glass.events.KeyEvent;

@SuppressWarnings("serial")
public class QueryBar extends JPanel {

	private static final String ORGANISM_TOOLTIP = "Organism";
	private static final String DEF_SEARCH_TEXT = "Enter one gene per line...";
	private static final int ICON_SIZE = 32;
	
	private JButton organismButton;
	private JTextField queryTextField;
	private JTextArea queryTextArea;
	private JScrollPane queryScroll;
	
	private Organism selectedOrganism;
	
	private final OrganismManager organismManager;
	private final CyServiceRegistrar serviceRegistrar;
	
	public QueryBar(OrganismManager organismManager, CyServiceRegistrar serviceRegistrar) {
		this.organismManager = organismManager;
		this.serviceRegistrar = serviceRegistrar;
		
		init();
		
		if (organismManager.isInitialized())
			updateOrganisms();
		
		organismManager.addPropertyChangeListener("organisms", evt -> updateOrganisms());
		organismManager.addPropertyChangeListener("loadRemoteOrganismsException", evt -> updateOrganisms());
//		organismManager.addPropertyChangeListener("offline", evt -> updateOrganisms());
	}

	public Set<String> getQueryGenes() {
		Set<String> query = new HashSet<>();
		String text = getQueryTextArea().getText();
		String[] split = text.split("\n");
		
		for (String s : split) {
			s = s.trim();
			
			if (!s.isEmpty())
				query.add(s);
		}
		
		return query;
	}
	
	public Organism getSelectedOrganism() {
		return selectedOrganism != null ? selectedOrganism : null;
	}
	
	public boolean isReady() {
		return selectedOrganism != null && !getQueryGenes().isEmpty();
	}
	
	private void init() {
		setBackground(UIManager.getColor("Table.background"));
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getOrganismButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getQueryTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(getOrganismButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getQueryTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		String tooltip = "Press " + (LookAndFeelUtil.isMac() ? "Command" : "Ctrl") + "+ENTER to run the search";
		getQueryTextField().setToolTipText(tooltip);
		getQueryTextArea().setToolTipText(tooltip);
	}
	
	private JButton getOrganismButton() {
		if (organismButton == null) {
			organismButton = new JButton();
			organismButton.setToolTipText(ORGANISM_TOOLTIP);
			organismButton.setContentAreaFilled(false);
			organismButton.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1, 1, 1, 0),
						BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground"))
			));
			
			Dimension d = new Dimension(ICON_SIZE, Math.max(ICON_SIZE, getQueryTextField().getPreferredSize().height));
			organismButton.setMinimumSize(d);
			organismButton.setPreferredSize(d);
			organismButton.addActionListener(evt -> {
				if (!organismManager.isInitialized())
					return;
				
				if (organismManager.getLoadRemoteOrganismsErrorMessage() == null)
					showOrganismPopup();
				else // Try to load organisms again...
					organismManager.loadRemoteOrganisms();
			});
		}
		
		return organismButton;
	}
	
	private JTextField getQueryTextField() {
		if (queryTextField == null) {
			final Color msgColor = UIManager.getColor("Label.disabledForeground");
			final int vgap = 1;
			final int hgap = 5;
			
			queryTextField = new JTextField() {
				@Override
				public void paint(Graphics g) {
					super.paint(g);
					
					if (getText() == null || getText().trim().isEmpty()) {
						// Set antialiasing
						Graphics2D g2 = (Graphics2D) g.create();
						g2.setRenderingHints(
								new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
						// Set the font
					    g2.setFont(getFont());
						// Get the FontMetrics
					    FontMetrics metrics = g2.getFontMetrics(getFont());
					    // Determine the X coordinate for the text
					    int x = hgap;
					    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
					    int y = (metrics.getHeight() / 2) + metrics.getAscent() + vgap;
						// Draw
						g2.setColor(msgColor);
						g2.drawString(DEF_SEARCH_TEXT, x, y);
						g2.dispose();
					}
				}
			};
			
			queryTextField.setEditable(false);
			queryTextField.setMinimumSize(queryTextField.getPreferredSize());
			queryTextField.setBorder(BorderFactory.createEmptyBorder(vgap, hgap, vgap, hgap));
			queryTextField.setFont(queryTextField.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			
			queryTextField.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					showQueryPopup();
				}
			});
			
			// Since we provide our own search component, it should let Cytoscape know
			// when it has been updated by the user, so Cytoscape can give a better
			// feedback to the user of whether or not the whole search component is ready
			// (e.g. Cytoscape may enable or disable the search button)
			queryTextField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					fireQueryChanged();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					fireQueryChanged();
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					// Nothing to do here...
				}
			});
		}
		
		return queryTextField;
	}
	
	private JTextArea getQueryTextArea() {
		if (queryTextArea == null) {
			queryTextArea = new JTextArea();
			LookAndFeelUtil.makeSmall(queryTextArea);
			
			// When Ctrl+ENTER (command+ENTER on macOS) is pressed, ask Cytoscape to perform the query
			String ENTER_ACTION_KEY = "ENTER_ACTION_KEY";
			KeyStroke enterKey = KeyStroke.getKeyStroke(
					KeyEvent.VK_ENTER,
					LookAndFeelUtil.isMac() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK,
					false
			);
			InputMap inputMap = queryTextArea.getInputMap(JComponent.WHEN_FOCUSED);
			inputMap.put(enterKey, ENTER_ACTION_KEY);
			
			queryTextArea.getActionMap().put(ENTER_ACTION_KEY, new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {System.out.println("\n\nENTER");
					QueryBar.this.firePropertyChange(
							NetworkSearchTaskFactory.SEARCH_REQUESTED_PROPERTY, null, null);
				}
			});
		}
		
		return queryTextArea;
	}
	
	private JScrollPane getQueryScroll() {
		if (queryScroll == null) {
			queryScroll = new JScrollPane(getQueryTextArea());
			queryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			queryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			LookAndFeelUtil.makeSmall(queryScroll);
		}
		
		return queryScroll;
	}
	
	private void updateOrganisms() {
		Set<Organism> organisms = organismManager.getOrganisms();
		String errorMessage = organismManager.getLoadRemoteOrganismsErrorMessage();
		
		if (organisms.isEmpty() && errorMessage != null)
			showOrganismsException(errorMessage);
		else if (selectedOrganism == null || !organisms.contains(selectedOrganism))
			setSelectedOrganism(organisms.isEmpty() ? null : organisms.iterator().next());
    }
	
	private void setSelectedOrganism(Organism newValue) {
		boolean changed = (newValue == null && selectedOrganism != null)
				|| (newValue != null && !newValue.equals(selectedOrganism));
		selectedOrganism = newValue;
		
		if (changed) {
			getOrganismButton().setToolTipText(
					selectedOrganism != null ? 
					ORGANISM_TOOLTIP + " (" + selectedOrganism.getName() + ")" : ORGANISM_TOOLTIP
			);
			getOrganismButton().setIcon(selectedOrganism != null ? getIcon(selectedOrganism) : null);
			fireQueryChanged();
		}
	}
	
	private void showOrganismsException(String errorMessage) {
		selectedOrganism = null;
		getOrganismButton().setToolTipText("<html>" + errorMessage + "<br><br><b>(Click to try again)</b></html>");
		IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		TextIcon icon = new TextIcon(IconManager.ICON_TIMES_CIRCLE, iconManager.getIconFont(24.0f),
				LookAndFeelUtil.getErrorColor(), ICON_SIZE, ICON_SIZE);
		getOrganismButton().setIcon(icon);
		fireQueryChanged();
	}
	
	private void showOrganismPopup() {
		Set<Organism> organisms = organismManager.getOrganisms();
		
		if (organisms == null || organisms.isEmpty())
			return;
		
		JPopupMenu popup = new JPopupMenu();
		popup.setBackground(getBackground());
		
		for (Organism org : organisms) {
			String description = org.getDescription();
			String name = org.getName() + " (" + description + ")";
			Icon icon = getIcon(org);
			
			JCheckBoxMenuItem mi = new JCheckBoxMenuItem(name , icon, org.equals(selectedOrganism));
			LookAndFeelUtil.makeSmall(mi);
			mi.addActionListener(evt -> setSelectedOrganism(org));
			popup.add(mi);
		}
		
		popup.show(getOrganismButton(), 0, getOrganismButton().getHeight());
		popup.requestFocus();
	}
	
	private void showQueryPopup() {
		JPopupMenu popup = new JPopupMenu();
		popup.setBackground(getBackground());
		popup.setLayout(new BorderLayout());
		popup.add(getQueryScroll(), BorderLayout.CENTER);
		
		popup.addPropertyChangeListener("visible", evt -> {
			if (evt.getNewValue() == Boolean.FALSE)
				updateQueryTextField();
		});
		
		getQueryScroll().setPreferredSize(new Dimension(getQueryTextField().getSize().width, 200));
		popup.setPreferredSize(getQueryScroll().getPreferredSize());
		
		popup.show(getQueryTextField(), 0, 0);
		popup.requestFocus();
		getQueryTextArea().requestFocusInWindow();
	}
	
	private void updateQueryTextField() {
		Set<String> query = getQueryGenes();
		String text = query.stream().collect(Collectors.joining(" "));
		getQueryTextField().setText(text);
	}

	private Icon getIcon(Organism org) {
		String description = org != null ? org.getDescription() : null;
		
		if (description == null)
			return null;
		
		description = description.replace(" ", "_").replaceAll("'", "");
		URL resource = getClass().getClassLoader().getResource("/img/organism/" + description + "-32.png");
		
		if (resource == null)
			resource = getClass().getClassLoader().getResource("/img/organism/missing-32.png");
		
		return resource != null ? new ImageIcon(resource) : null;
	}
	
	private void fireQueryChanged() {
		firePropertyChange(NetworkSearchTaskFactory.QUERY_PROPERTY, null, null);
	}
}

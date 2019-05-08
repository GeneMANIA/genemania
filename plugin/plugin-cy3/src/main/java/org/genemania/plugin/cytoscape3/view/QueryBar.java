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
package org.genemania.plugin.cytoscape3.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.util.swing.LookAndFeelUtil.getErrorColor;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Organism;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape3.model.OrganismManager;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.impl.InteractionNetworkGroupImpl;
import org.genemania.plugin.model.impl.WeightingMethod;
import org.genemania.plugin.view.NetworkSelectionPanel;
import org.genemania.plugin.view.util.IconUtil;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.type.CombiningMethod;

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
	
	private OptionsPanel optionsPanel;
	
	private Organism selectedOrganism;
	
	private final OrganismManager organismManager;
	private final NetworkUtils networkUtils;
	private final UiUtils uiUtils;
	private final CyServiceRegistrar serviceRegistrar;
	
	public QueryBar(OrganismManager organismManager, NetworkUtils networkUtils, UiUtils uiUtils,
			CyServiceRegistrar serviceRegistrar) {
		this.networkUtils = networkUtils;
		this.uiUtils = uiUtils;
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
		return selectedOrganism;
	}
	
	public int getGeneLimit() {
		return getOptionsPanel().getGeneLimitSlider().getValue();
	}

	public int getAttributeLimit() {
		return getOptionsPanel().getAttrLimitSlider().getValue();
	}

	public CombiningMethod getCombiningMethod() {
		return getOptionsPanel().getWeightingCombo().getSelectedItem() != null
				? ((WeightingMethod) getOptionsPanel().getWeightingCombo().getSelectedItem()).getMethod()
				: CombiningMethod.AUTOMATIC_SELECT;
	}
	
	public Collection<Group<?, ?>> getSelectedGroups() {
		return getOptionsPanel().getNetworkSelectionPanel().getSelectedGroups();
	}
	
	public OptionsPanel getOptionsPanel() {
		if (optionsPanel == null) {
			optionsPanel = new OptionsPanel();
		}
		
		return optionsPanel;
	}
	
	public boolean isReady() {
		return selectedOrganism != null && !getQueryGenes().isEmpty();
	}
	
	private void init() {
		setBackground(UIManager.getColor("Table.background"));
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
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
			queryScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			queryScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
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
		Organism oldValue = selectedOrganism;
		selectedOrganism = newValue;
		
		if (changed) {
			getOrganismButton().setToolTipText(
					selectedOrganism != null ? 
					ORGANISM_TOOLTIP + " (" + selectedOrganism.getName() + ")" : ORGANISM_TOOLTIP
			);
			getOrganismButton().setIcon(selectedOrganism != null ? getIcon(selectedOrganism) : null);
			
			getOptionsPanel().update();
			
			firePropertyChange("selectedOrganism", oldValue, newValue);
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
		Long taxonId = org != null ? org.getTaxonomyId() : null;
		
		if (taxonId == null)
			return null;
		
		return IconUtil.getOrganismIcon(taxonId);
	}
	
	private void fireQueryChanged() {
		firePropertyChange(NetworkSearchTaskFactory.QUERY_PROPERTY, null, null);
	}
	
	public class OptionsPanel extends JPanel {
		
		private QuerySlider geneLimitSlider;
		private QuerySlider attrLimitSlider;
		private JComboBox<WeightingMethod> weightingCombo;
		private NetworkSelectionPanel networkSelectionPanel;
		
		private OptionsPanel() {
			setBackground(UIManager.getColor("Table.background"));
			
			JLabel geneLimitLabel = new JLabel("Max Resultant Genes:");
			JLabel attrLimitLabel = new JLabel("Max Resultant Attributes:");
			JLabel weightingLabel = new JLabel("Network Weighting:");
			JLabel networkLabel = new JLabel(Strings.retrieveRelatedGenesNetworkPanel_label);
			
			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(TRAILING, true)
									.addComponent(geneLimitLabel)
									.addComponent(attrLimitLabel)
									.addComponent(weightingLabel)
							)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(LEADING, true)
									.addComponent(getGeneLimitSlider(), DEFAULT_SIZE, DEFAULT_SIZE, 260)
									.addComponent(getAttrLimitSlider(), DEFAULT_SIZE, DEFAULT_SIZE, 260)
									.addComponent(getWeightingCombo(), DEFAULT_SIZE, DEFAULT_SIZE, 260)
							)
					)
					.addComponent(networkLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getNetworkSelectionPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(geneLimitLabel)
							.addComponent(getGeneLimitSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(attrLimitLabel)
							.addComponent(getAttrLimitSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(weightingLabel)
							.addComponent(getWeightingCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(networkLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getNetworkSelectionPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			makeSmall(geneLimitLabel, attrLimitLabel, weightingLabel, networkLabel);
			uiUtils.makeLabelsSmall(getGeneLimitSlider().getSlider());
			uiUtils.makeLabelsSmall(getAttrLimitSlider().getSlider());
			
			update();
		}
		
		private QuerySlider getGeneLimitSlider() {
			if (geneLimitSlider == null) {
				geneLimitSlider = new QuerySlider(0, 100, 20);
			}
			
			return geneLimitSlider;
		}
		
		private QuerySlider getAttrLimitSlider() {
			if (attrLimitSlider == null) {
				attrLimitSlider = new QuerySlider(0, 100, 10);
			}
			
			return attrLimitSlider;
		}
		
		private JComboBox<WeightingMethod> getWeightingCombo() {
			if (weightingCombo == null) {
				WeightingMethod[] items = new WeightingMethod[] {
						new WeightingMethod(CombiningMethod.AUTOMATIC_SELECT, Strings.default_combining_method),
						new WeightingMethod(CombiningMethod.AUTOMATIC, Strings.automatic),
						new WeightingMethod(CombiningMethod.BP, Strings.bp),
						new WeightingMethod(CombiningMethod.MF, Strings.mf),
						new WeightingMethod(CombiningMethod.CC, Strings.cc),
						new WeightingMethod(CombiningMethod.AVERAGE, Strings.average),
						new WeightingMethod(CombiningMethod.AVERAGE_CATEGORY, Strings.average_category)
				};
				weightingCombo = new JComboBox<>(items);
				weightingCombo.setSelectedIndex(0);
				makeSmall(weightingCombo);
			}
			
			return weightingCombo;
		}
		
		private NetworkSelectionPanel getNetworkSelectionPanel() {
			if (networkSelectionPanel == null) {
				networkSelectionPanel = new NetworkSelectionPanel(networkUtils, uiUtils);
			}
			
			return networkSelectionPanel;
		}
		
		void update() {
			List<Group<?, ?>> sortedGroups = new ArrayList<>();
			Organism organism = getSelectedOrganism();

			if (organism != null) {
				organism.getInteractionNetworkGroups().forEach(gr -> {
					Collection<InteractionNetwork> interactionNetworks = gr.getInteractionNetworks();
					
					if (interactionNetworks != null && !gr.getInteractionNetworks().isEmpty())
						sortedGroups.add(new InteractionNetworkGroupImpl(gr));
				});
				Collections.sort(sortedGroups, networkUtils.getNetworkGroupComparator());
			}
			
			getNetworkSelectionPanel().setGroups(sortedGroups);
		}
	}
	
	private class QuerySlider extends JPanel {
		
		private JSlider slider;
		private JFormattedTextField textField;
		
		private final int min, max;
		private int value;
		private final List<Object> listeners;
		private boolean ignore;
		
		QuerySlider(int min, int max, int value) {
			this.min = min;
			this.max = max;
			this.value = value;
			listeners = new ArrayList<>();
			
			initUI();
		}

		@SuppressWarnings("unchecked")
		protected void initUI() {
			final GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(getSlider(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addComponent(getSlider())
					.addComponent(getTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			if (isAquaLAF())
				setOpaque(false);
			
			// Change the slider's label sizes -- only works if it's done after the slider has been added to
			// its parent container and had its UI assigned
			final Font tickFont = getSlider().getFont().deriveFont(getSmallFontSize());
			final Dictionary<Integer, JLabel> labelTable = getSlider().getLabelTable();
			
			for (Enumeration<Integer> enumeration = labelTable.keys(); enumeration.hasMoreElements();) {
				int k = enumeration.nextElement();
				final JLabel label = labelTable.get(k);
				label.setFont(tickFont); // Updates the font size
				label.setSize(label.getPreferredSize()); // Updates the label size and slider layout
			}
		}
		
		public int getValue(){
			return value;
		}

		public void setValue(int value) {
			ignore = true;
			this.value = value;
			setSliderValue();
			setFieldValue();
			ignore = false;
		}
		
		private JFormattedTextField getTextField() {
			if (textField == null) {
				textField = new JFormattedTextField() {
					@Override
					public Dimension getPreferredSize() {
						final Dimension d = super.getPreferredSize();
						
						if (this.getGraphics() != null) {
							// Set the preferred text field size after it gets a Graphics
							int sw = 16 + this.getGraphics().getFontMetrics().stringWidth("" + max);
							d.width = Math.max(sw, 48);
						}
						
						return d;
					}
				};
				
				textField.setHorizontalAlignment(SwingConstants.RIGHT);
				makeSmall(textField);
				
				textField.addActionListener((ActionEvent e) -> {
					textFieldValueChanged();
				});
				textField.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						textFieldValueChanged();
					}
				});
			}
			
			return textField;
		}
		
		private JSlider getSlider() {
			if (slider == null) {
				slider = new JSlider(min, max);
				
				slider.setMajorTickSpacing(10);
				slider.setMinorTickSpacing(1);
				slider.setPaintTicks(false);
				slider.setPaintLabels(false);
				slider.setSnapToTicks(true);
				
				makeSmall(slider);
				
				setSliderValue();
				setFieldValue();
				
				slider.addChangeListener((ChangeEvent e) -> {
					if (ignore)
						return;
					
					ignore = true;
					// update the value
					value = getSlider().getValue();

					// Due to small inaccuracies in the slider position, it's possible
					// to get values less than the min or greater than the max.  If so,
					// just adjust the value and don't issue a warning.
					value = clamp(value);

					// set text field value
					setFieldValue();
					// fire event
					fireChangeEvent();
					ignore = false;
				});
			}
			
			return slider;
		}
		
		private void setSliderValue() {
			getSlider().setValue(value);
		}
	  
		private int getFieldValue() {
			int val = -1;
			final Color errColor = getErrorColor();
			
			try {
				val = Integer.valueOf(getTextField().getText());
			} catch (NumberFormatException nfe) {
				getTextField().setForeground(errColor);
				getTextField().setToolTipText("Please enter a valid number");
				
				return value;
			}
			
			if (val < min) {
				getTextField().setForeground(errColor);
				getTextField().setToolTipText("Value is less than lower limit (" + min + ")");
				
				return min;
			}
			if (val > max) {
				getTextField().setForeground(errColor);
				getTextField().setToolTipText("Value is greater than upper limit (" + max + ")");
				
				return max;
			}
			
			getTextField().setToolTipText(null);
			getTextField().setForeground(UIManager.getColor("TextField.foreground"));
			
			return val;
		}
		
		private void setFieldValue() {
			getTextField().setValue(value);
			getTextField().setToolTipText(null);
			getTextField().setForeground(UIManager.getColor("TextField.foreground"));
		}

		private int clamp(int val) {
			val = Math.min(val, max);
			val = Math.max(val, min);
			
			return val;
		}
		
		public void addChangeListener(ChangeListener cl) {
			if (!listeners.contains(cl))
				listeners.add(cl);
		}

		public void removeChangeListener(ChangeListener cl) {
			listeners.remove(cl);
		}
		
		protected void fireChangeEvent() {
			Iterator<Object> iter = listeners.iterator();
			ChangeEvent evt = new ChangeEvent(this);
			
			while (iter.hasNext()) {
				ChangeListener cl = (ChangeListener) iter.next();
				cl.stateChanged(evt);
			}
		}
		
		private void textFieldValueChanged() {
			if (ignore)
				return;
			
			ignore = true;
			int v = getFieldValue();
			
			if (v != value) {
				// update the value
				value = v;
				// set slider value
				setSliderValue();
			}
			
			// fire event
			fireChangeEvent();
			ignore = false;
		}
	}
}

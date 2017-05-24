package org.genemania.plugin.cytoscape3.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.model.ModelElement;

@SuppressWarnings("serial")
public class QueryBar extends JPanel {

	private static final String DEF_SEARCH_TEXT = "Enter one gene per line...";
	private static final int ICON_SIZE = 32;
	
	private JButton organismButton;
	private JTextField searchTextField;
	
	private Vector<ModelElement<Organism>> organisms = new Vector<>();
	private ModelElement<Organism> selectedOrganism;
	
	private boolean dataInitialized;
	
	private final GeneMania<CyNetwork, CyNode, CyEdge> plugin;
	private final RetrieveRelatedGenesController<CyNetwork, CyNode, CyEdge> controller;
	private final CyServiceRegistrar serviceRegistrar;
	
	public QueryBar(
			GeneMania<CyNetwork, CyNode, CyEdge> plugin,
			RetrieveRelatedGenesController<CyNetwork, CyNode, CyEdge> controller,
			CyServiceRegistrar serviceRegistrar
	) {
		this.plugin = plugin;
		this.controller = controller;
		this.serviceRegistrar = serviceRegistrar;
		
		init();
		
		DataSetManager dataSetManager = plugin.getDataSetManager();
		dataSetManager.addDataSetChangeListener((dataSet, progress) -> setDataSet(dataSet));
	}

	public Set<String> getQuery() {
		Set<String> query = new HashSet<>();
		String text = getSearchTextField().getText();
		String[] split = text.split("\n");
		
		for (String s : split) {
			s = s.trim();
			
			if (!s.isEmpty())
				query.add(s);
		}
		
		return query;
	}
	
	public boolean isReady() {
		return selectedOrganism != null && !getQuery().isEmpty();
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		
		if (!dataInitialized && plugin.getDataSetManager().getDataSet() == null) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					if (plugin.getDataSetManager().getDataSet() == null)
						plugin.initializeData(SwingUtilities.getWindowAncestor(QueryBar.this), true);
					
					return null;
				}
				@Override
				protected void done() {
					setDataSet(plugin.getDataSetManager().getDataSet());
				}
			};
			worker.execute();
		}
		
		dataInitialized = true;
	}
	
	private void init() {
		setBackground(UIManager.getColor("Table.background"));
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getOrganismButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getSearchTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(getOrganismButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getSearchTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(0, 0, Short.MAX_VALUE)
		);
	}
	
	private JButton getOrganismButton() {
		if (organismButton == null) {
			organismButton = new JButton();
			organismButton.setToolTipText("Organism");
			organismButton.setContentAreaFilled(false);
			organismButton.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1, 1, 1, 0),
						BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground"))
			));
			
			Dimension d = new Dimension(ICON_SIZE, Math.max(ICON_SIZE, getSearchTextField().getPreferredSize().height));
			organismButton.setMinimumSize(d);
			organismButton.setPreferredSize(d);
			organismButton.addActionListener(evt -> showOrganismPopup());
		}
		
		return organismButton;
	}
	
	private JTextField getSearchTextField() {
		if (searchTextField == null) {
			final Color msgColor = UIManager.getColor("Label.disabledForeground");
			final int vgap = 1;
			final int hgap = 5;
			
			searchTextField = new JTextField() {
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
			
			searchTextField.setEditable(false);
			searchTextField.setMinimumSize(searchTextField.getPreferredSize());
			searchTextField.setBorder(BorderFactory.createEmptyBorder(vgap, hgap, vgap, hgap));
			searchTextField.setFont(searchTextField.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			
			// Since we provide our own search component, it should let Cytoscape know
			// when it has been updated by the user, so Cytoscape can give a better
			// feedback to the user of whether or not the whole search component is ready
			// (e.g. Cytoscape may enable or disable the search button)
			searchTextField.getDocument().addDocumentListener(new DocumentListener() {
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
		
		return searchTextField;
	}
	
	private void setDataSet(final DataSet data) {
    	try {
    		organisms = data != null ? controller.createModel(data) : new Vector<>();
    		
    		if (selectedOrganism == null || !organisms.contains(selectedOrganism))
    			setSelectedOrganism(organisms.isEmpty() ? null : organisms.get(0));
		} catch (DataStoreException e) {
			LogUtils.log(getClass(), e);
		}
    }
	
	private void setSelectedOrganism(ModelElement<Organism> newValue) {
		boolean changed = (newValue == null && selectedOrganism != null)
				|| (newValue != null && !newValue.equals(selectedOrganism));
		selectedOrganism = newValue;
		
		if (changed) {
			getOrganismButton().setIcon(selectedOrganism != null ? getIcon(selectedOrganism) : null);
			fireQueryChanged();
		}
	}
	
	private void showOrganismPopup() {
		if (organisms == null || organisms.isEmpty())
			return;
		
		JPopupMenu popup = new JPopupMenu();
		popup.setBackground(getBackground());
		
		for (ModelElement<Organism> org : organisms) {
			String description = org.getItem().getDescription();
			String name = org.getItem().getName() + " (" + description + ")";
			Icon icon = getIcon(org);
			
			JCheckBoxMenuItem mi = new JCheckBoxMenuItem(name , icon, org.equals(selectedOrganism));
			LookAndFeelUtil.makeSmall(mi);
			mi.addActionListener(evt -> setSelectedOrganism(org));
			popup.add(mi);
		}
		
		popup.show(getOrganismButton(), 0, getOrganismButton().getHeight());
		popup.requestFocus();
	}

	private Icon getIcon(ModelElement<Organism> org) {
		String description = org != null ? org.getItem().getDescription() : null;
		
		if (description == null)
			return null;
		
		description = description.replace(" ", "_").replaceAll("'", "");
		URL resource = getClass().getClassLoader().getResource("/img/organism/" + description + "-32.png");
		
		return resource != null ? new ImageIcon(resource) : null;
	}
	
	private void fireQueryChanged() {
		firePropertyChange(NetworkSearchTaskFactory.QUERY_PROPERTY, null, null);
	}
}

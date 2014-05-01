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

package org.genemania.plugin.completion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultEditorKit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.genemania.completion.CompletionConsumer;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.Gene;
import org.genemania.domain.Organism;
import org.genemania.mediator.lucene.LuceneGeneMediator;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.util.ProgressReporter;

public class CompletionPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final String GENE_HINT = Strings.completionPanelGeneHint_label;

	private int autoTriggerThreshold;
	private JTextField textField;
	private JTable proposalTable;

	private final DynamicTableModel<Gene> resultModel;
	private final DynamicTableModel<Gene> proposalModel;
	
	private final CompletionConsumer consumer;
	private GeneCompletionProvider2 provider;

	private final JTable resultTable;
	
	private int lastCompletionCount;

	private ProgressReporter progress;

	private int limit;

	private JDialog proposalDialog;

	private String lastQuery;
	private int lastSelectionStart;
	private int lastSelectionEnd;

	private JLabel statusLabel;

	private final NetworkUtils networkUtils;

	private final UiUtils uiUtils;

	private final TaskDispatcher taskDispatcher;
	
	public CompletionPanel(int autoTriggerThreshold, NetworkUtils networkUtils, UiUtils uiUtils, TaskDispatcher taskDispatcher) {
		this.networkUtils = networkUtils;
		this.uiUtils = uiUtils;
		this.taskDispatcher = taskDispatcher;
		
		setOpaque(false);
		this.autoTriggerThreshold = autoTriggerThreshold;
		proposalModel = createModel();
		resultModel = createModel();
		limit = 15;
		
		consumer = new CompletionConsumer() {
			public void consume(String completion) {
				lastCompletionCount++;
				if (lastCompletionCount >= limit) {
					return;
				}
				Gene gene = provider.getGene(completion);
				proposalModel.add(gene);
			}

			public void finish() {
				switch (lastCompletionCount) {
				case -1:
					setProposalStatus(Strings.completionPanelTooManyGenes_status);
					break;
				case 0:
					setProposalStatus(Strings.completionPanelNoGenes_status);
					break;
				case 1:
					setProposalStatus(Strings.completionPanelOneGene_status);
				default:
					if (lastCompletionCount >= limit) {
						setProposalStatus(String.format(Strings.completionPanelTooManyGenes2_status, limit, getQuery()));
					} else {
						setProposalStatus(String.format(Strings.completionPanelManyGenes_status, lastCompletionCount));
					}
				}
			}

			public void tooManyCompletions() {
			}
		};
		
		setLayout(new GridBagLayout());

		textField = new JTextField();
		setShowGeneHint(true);
		textField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (getQuery().equals(GENE_HINT)) {
					setShowGeneHint(false);
				}
				checkTrigger();
			}

			public void focusLost(FocusEvent e) {
				if (getQuery().length() == 0) {
					setShowGeneHint(true);
				}
			}
		});
		
		add(textField, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		textField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				checkTrigger(); 
			}

			public void insertUpdate(DocumentEvent e) {
				checkTrigger(); 
			}

			public void removeUpdate(DocumentEvent e) {
				checkTrigger(); 
			}
		});
		
		resultTable = createTable(resultModel);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		final JScrollPane resultPane = new JScrollPane(resultTable);
		Dimension textSizeHint = uiUtils.computeTextSizeHint(getFontMetrics(getFont()), 40, 8);
		resultPane.setMinimumSize(textSizeHint);
		
		add(resultPane, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_KP_DOWN:
					showProposals();
					ListSelectionModel model = proposalTable.getSelectionModel();
					model.clearSelection();
					model.addSelectionInterval(0, 0);
					proposalTable.requestFocus();
					break;
				case KeyEvent.VK_ESCAPE:
					hideProposals();
					break;
				case KeyEvent.VK_ENTER:
					if (proposalTable.getSelectedRowCount() > 0) {
						acceptProposal();
					} else {
						validateEntry(getQuery());
					}
					break;
				}
			}
		});
		
		proposalDialog = new JDialog(uiUtils.getFrame(this), false);
		proposalDialog.setUndecorated(true);
		proposalDialog.setAlwaysOnTop(true);
		JRootPane rootPane = proposalDialog.getRootPane();
		rootPane.setLayout(new GridBagLayout());
		
		Color proposalBackground = new Color(0xFF, 0xFF, 0xE0);
		rootPane.setBackground(proposalBackground);
		proposalTable = createTable(proposalModel);
		proposalTable.setBackground(proposalBackground );
		proposalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		proposalTable.requestFocusInWindow();

		statusLabel = new JLabel();
		rootPane.add(statusLabel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		rootPane.add(proposalTable, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		proposalDialog.pack();
		
		proposalTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				acceptProposal();
			}
		});
		
		proposalTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					acceptProposal();
					break;
				case KeyEvent.VK_ESCAPE:
					hideProposals();
					break;
				}
			}
		});
		
		FocusListener focusListener = new FocusListener() {
			public void focusGained(FocusEvent event) {
			}

			public void focusLost(FocusEvent event) {
				handleFocusLost(event);
			}
		};
		
		addFocusListener(focusListener);
		proposalTable.addFocusListener(focusListener);
		textField.addFocusListener(focusListener);
		textField.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				saveCaret();
			}
			
			public void focusGained(FocusEvent e) {
				restoreCaret();
			}
		});
		
		resultTable.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				hideProposals();
			}

			public void focusLost(FocusEvent e) {
			}
		});
		
		addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}
			
			public void componentResized(ComponentEvent e) {
			}
			
			public void componentMoved(ComponentEvent e) {
			}
			
			public void componentHidden(ComponentEvent e) {
				proposalDialog.setVisible(false);
			}
		});
		
		proposalDialog.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}
			
			public void componentResized(ComponentEvent e) {
			}
			
			public void componentMoved(ComponentEvent e) {
			}
			
			public void componentHidden(ComponentEvent e) {
				textField.requestFocus();
			}
		});
		
		createMenu();
	}

	private void createMenu() {
		JPopupMenu contextMenu = new JPopupMenu();
		JMenuItem pasteMenu = new JMenuItem(Strings.paste_menuLabel);
		pasteMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				textField.requestFocus();
				new DefaultEditorKit.PasteAction().actionPerformed(event);
			}
		});
		contextMenu.add(pasteMenu);
		textField.setComponentPopupMenu(contextMenu);
	}

	public void handleParentMoved() {
		if (proposalDialog.isVisible()) {
			popUpBelow(textField);
		}
	}

	void restoreCaret() {
		textField.select(lastSelectionStart, lastSelectionEnd);
	}

	void saveCaret() {
		lastSelectionStart = textField.getSelectionStart();
		lastSelectionEnd = textField.getSelectionEnd();
	}

	private void handleFocusLost(FocusEvent event) {
		Component component = event.getOppositeComponent();
		if (component == null) {
			return;
		}
		while (component != null) {
			if (component.equals(proposalTable)) {
				return;
			}
			if (component.equals(textField)) {
				return;
			}
			if (component.equals(this)) {
				return;
			}
			component = component.getParent();
		}
		hideProposals();
	}

	void popUpBelow(Component control) {
		Rectangle controlBounds = control.getBounds();
		Point point = new Point();
		point.x = controlBounds.x;
		point.y = controlBounds.y;
		SwingUtilities.convertPointToScreen(point, control.getParent());
		
		Rectangle bounds = proposalDialog.getBounds();
		bounds.x = point.x;
		bounds.y = point.y + controlBounds.height;
		bounds.width = controlBounds.width;
		proposalDialog.setBounds(bounds);
		
		hackForTicket1439();
		hackForTicket1449();
		proposalDialog.setVisible(true);
	}

	private void hackForTicket1449() {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		if (!osName.toLowerCase().startsWith("win")) { //$NON-NLS-1$
			return;
		}
		
		proposalDialog.setFocusableWindowState(false);
	}

	private void hackForTicket1439() {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		if (!"linux".equalsIgnoreCase(osName)) { //$NON-NLS-1$
			return;
		}
		
		proposalDialog.setFocusableWindowState(false);
	}

	void setShowGeneHint(boolean visible) {
		if (visible) {
			textField.setText(GENE_HINT);
			textField.setForeground(Color.gray);
		} else {
			textField.setText(""); //$NON-NLS-1$
			textField.setForeground(Color.black);
		}
	}
	
	@SuppressWarnings("serial")
	private JTable createTable(TableModel model) {
		JTable table = new JTable(model) {
			@Override
			public void addNotify() {
				super.addNotify();
				uiUtils.packColumns(this);
			}
			
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				setForeground(column == 0 ? Color.black : Color.darkGray);
				return super.prepareRenderer(renderer, row, column);
			}
		};
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		return table;
	}

	private DynamicTableModel<Gene> createModel() {
		return new DynamicTableModel<Gene>() {
			public Class<?> getColumnClass(int columnIndex) {
				return String.class;
			}

			public int getColumnCount() {
				return 2;
			}

			public String getColumnName(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return Strings.completionPanelNameColumn_name;
				case 1:
					return Strings.completionPanelDescriptionColumn_name;
				default:
					return ""; //$NON-NLS-1$
				}
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				Gene gene = get(rowIndex);
				if (gene == null) {
					return null;
				}
				switch (columnIndex) {
				case 0:
					return networkUtils.getGeneLabel(gene);
				case 1:
					return gene.getNode().getGeneData().getDescription();
				}
				return null;
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}

			public void setValueAt(Object value, int rowIndex, int columnIndex) {
			}
		};
	}

	public void setProgressReporter(ProgressReporter progress) {
		this.progress = progress;
	}
	
	void setStatus(String message) {
		if (progress != null) {
			progress.setStatus(message);
		}
	}
	
	void setProposalStatus(String message) {
		statusLabel.setText(String.format("<html><b>%s</b></html>", message)); //$NON-NLS-1$
	}

	public void setProvider(GeneCompletionProvider2 provider) {
		if (this.provider != null) {
			this.provider.close();
		}
		this.provider = provider;
		if (provider == null) {
			provider = createEmptyProvider();
		}
		textField.setTransferHandler(new CompletionTransferHandler(provider, new CompletionConsumer() {
			List<String> completions = new ArrayList<String>();
			
			public void consume(String completion) {
				completions.add(completion);
			}

			public void finish() {
				validateGene(completions);
				completions.clear();
			}

			public void tooManyCompletions() {
			}
		}, networkUtils, uiUtils, taskDispatcher));
	}
	
	private GeneCompletionProvider2 createEmptyProvider() {
		try {
			Directory directory = new RAMDirectory();
			Analyzer analyzer = LuceneGeneMediator.createDefaultAnalyzer();
			IndexWriter writer = new IndexWriter(directory, analyzer, MaxFieldLength.UNLIMITED);
			writer.commit();
			writer.close();
			IndexSearcher searcher = new IndexSearcher(directory, true);
			return new GeneCompletionProvider2(searcher, analyzer, new Organism());
		} catch (IOException e) {
			return null;
		}
	}

	private void checkTrigger() {
		String text = getQuery();
		if (text.equals(GENE_HINT)) {
			return;
		}
		if (text.length() >= autoTriggerThreshold) {
			showProposals();
		} else {
			lastQuery = null;
			hideProposals();
		}
	}

	private boolean validateGene(String symbol) {
		Gene gene = provider.getGene(symbol);
		if (gene == null) {
			setStatus(String.format(Strings.completionPanelUnknownSymbol_status, symbol));
			return false;
		}
		for (Gene item : resultModel.getItems()) {
			if (item.getNode().getId() == gene.getNode().getId()) {
				setStatus(String.format(Strings.completionPanelDuplicateGene_status, networkUtils.getGeneLabel(gene)));
				return false;
			}
		}
		resultModel.add(gene);
		return true;
	}
	
	private void validateGene(List<String> genes) {
		for (String symbol : genes) {
			validateGene(symbol);
		}
		repackTable();
	}
	
	private void repackTable() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					repackTable();
				}
			});
			return;
		}
		uiUtils.packColumns(resultTable);
	}

	private void acceptProposal() {
		synchronized (this) {
			Gene gene = proposalModel.get(proposalTable.getSelectedRow());
			String proposal = gene.getSymbol(); 
			if (validateGene(proposal)) {
				setStatus(""); //$NON-NLS-1$
				repackTable();
			}
			ListSelectionModel model = proposalTable.getSelectionModel();
			model.clearSelection();
			model.addSelectionInterval(0, 0);
			hideProposals();
			proposalModel.clear();
			textField.setText(""); //$NON-NLS-1$
		}
	}
	
	private void validateEntry(String symbol) {
		if (symbol == null || symbol.length() == 0) {
			return;
		}
		validateGene(symbol);
		repackTable();
		hideProposals();
		proposalModel.clear();
		textField.setText(""); //$NON-NLS-1$
	}

	public void hideProposals() {
		proposalDialog.setVisible(false);
	}

	private void showProposals() {
		String query = getQuery();
		if (lastQuery != null && lastQuery.equals(query)) {
			return;
		}
		
		// Compute proposals
		computeProposals(query);

		if (lastCompletionCount == 0) {
			hideProposals();
			return;
		}
		
		int selectedIndex = -1;
		int i = 0;
		for (Gene gene : proposalModel.getItems()) {
			if (query.equalsIgnoreCase(gene.getSymbol())) {
				selectedIndex = i;
				break;
			}
			i++;
		}
		
		uiUtils.packColumns(proposalTable);
		proposalDialog.pack();

		popUpBelow(textField);
		if (selectedIndex != -1) {
			ListSelectionModel model = proposalTable.getSelectionModel();
			model.clearSelection();
			model.addSelectionInterval(selectedIndex, selectedIndex);
		}
		textField.requestFocus();
	}

	private void computeProposals(String query) {
		lastCompletionCount = 0;
		proposalModel.clear();
		if (provider != null) {
			provider.computeProposals(consumer, query);
			proposalModel.sort(new Comparator<Gene>() {
				public int compare(Gene gene1, Gene gene2) {
					return gene1.getSymbol().compareTo(gene2.getSymbol());
				}
			});
		}
		lastQuery = query;
	}
	
	private String getQuery() {
		return textField.getText().trim();
	}

	public List<String> getItems() {
		List<String> selection = new ArrayList<String>();
		for (Gene gene : resultModel.getItems()) {
			selection.add(gene.getSymbol());
		}
		return selection;
	}
	
	public void setItems(List<String> items) {
		clear();
		for (String symbol : items) {
			validateGene(symbol);
		}
		repackTable();
		setStatus(""); //$NON-NLS-1$
	}
	
	public int getItemCount() {
		return resultModel.getRowCount();
	}
	
	public int getSelectionCount() {
		return resultTable.getSelectedRowCount();
	}
	
	public void removeSelection() {
		int[] selection = resultTable.getSelectedRows();
		resultModel.removeRows(selection);
		uiUtils.packColumns(resultTable);
	}
	
	public void clear() {
		setStatus(""); //$NON-NLS-1$
		resultModel.clear();
		uiUtils.packColumns(resultTable);
	}

	public void addListSelectionListener(ListSelectionListener listener) {
		resultTable.getSelectionModel().addListSelectionListener(listener);
	}
	
	public void removeListSelectionListener(ListSelectionListener listener) {
		resultTable.getSelectionModel().removeListSelectionListener(listener);
	}
	
	public void addTableModelEventListener(TableModelListener listener) {
		resultModel.addTableModelListener(listener);
	}
	
	public void removeTableModelEventListener(TableModelListener listener) {
		resultModel.removeTableModelListener(listener);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
		resultTable.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	
	public JDialog getProposalDialog() {
		return proposalDialog;
	}
	
	@Override
	public void requestFocus() {
		textField.requestFocus();
	}
}

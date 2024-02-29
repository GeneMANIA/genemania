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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
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

@SuppressWarnings("serial")
public class CompletionPanel extends JPanel {
	
	private static final String GENE_HINT = Strings.completionPanelGeneHint_label;
	private static final Color PROPOSAL_BG_COLOR = Color.WHITE;

	private int autoTriggerThreshold;
	private JTextField textField;
	private JTable proposalTable;

	private final DynamicTableModel<Gene> resultModel;
	private final DynamicTableModel<Gene> proposalModel;
	
	private final CompletionConsumer consumer;
	private GeneCompletionProvider2 provider;

	private JTable resultTable;
	
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
	
	public CompletionPanel(
			int autoTriggerThreshold,
			NetworkUtils networkUtils,
			UiUtils uiUtils,
			TaskDispatcher taskDispatcher
	) {
		this.networkUtils = networkUtils;
		this.uiUtils = uiUtils;
		this.taskDispatcher = taskDispatcher;
		this.autoTriggerThreshold = autoTriggerThreshold;
		
		setOpaque(!uiUtils.isAquaLAF());
		
		proposalModel = createModel();
		resultModel = createModel();
		limit = 15;
		
		consumer = new CompletionConsumer() {
			@Override
			public void consume(String completion) {
				lastCompletionCount++;
				if (lastCompletionCount >= limit) {
					return;
				}
				Gene gene = provider.getGene(completion);
				proposalModel.add(gene);
			}
			@Override
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
			@Override
			public void tooManyCompletions() {
			}
		};
		
		addComponents();
	}
	
	private void addComponents() {
		setShowGeneHint(true);
		
		final JScrollPane resultPane = new JScrollPane(getResultTable());
		final Dimension textSizeHint = uiUtils.computeTextSizeHint(getFontMetrics(getFont()), 40, 8);
		resultPane.setMinimumSize(textSizeHint);
		resultPane.setPreferredSize(textSizeHint);
		
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent event) {
			}
			@Override
			public void focusLost(FocusEvent event) {
				handleFocusLost(event);
			}
		});
		
		addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
			}
			@Override
			public void componentResized(ComponentEvent e) {
			}
			@Override
			public void componentMoved(ComponentEvent e) {
			}
			@Override
			public void componentHidden(ComponentEvent e) {
				getProposalDialog().setVisible(false);
			}
		});
		
		final GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
		layout.setAutoCreateGaps(uiUtils.isWinLAF());
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(resultPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(resultPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		createMenu();
	}

	public JDialog getProposalDialog() {
		if (proposalDialog == null) {
			proposalDialog = new JDialog(uiUtils.getWindow(this));
			proposalDialog.setUndecorated(true);
			proposalDialog.setAlwaysOnTop(true);
			
			final JRootPane rootPane = proposalDialog.getRootPane();
			rootPane.setBackground(PROPOSAL_BG_COLOR);
			
			final GroupLayout layout = new GroupLayout(rootPane);
			rootPane.setLayout(layout);
			layout.setAutoCreateGaps(uiUtils.isWinLAF());
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(getStatusLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getProposalTable(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getStatusLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getProposalTable(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			
			proposalDialog.pack();
			
			proposalDialog.addComponentListener(new ComponentListener() {
				@Override
				public void componentShown(ComponentEvent e) {
				}
				@Override
				public void componentResized(ComponentEvent e) {
				}
				@Override
				public void componentMoved(ComponentEvent e) {
				}
				@Override
				public void componentHidden(ComponentEvent e) {
					getTextField().requestFocus();
				}
			});
		}
		
		return proposalDialog;
	}
	
	private JLabel getStatusLabel() {
		if (statusLabel == null) {
			statusLabel = new JLabel();
			makeSmall(statusLabel);
		}
		
		return statusLabel;
	}
	
	private JTextField getTextField() {
		if (textField == null) {
			textField = new JTextField();
			textField.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					if (getQuery().equals(GENE_HINT)) {
						setShowGeneHint(false);
					}
					checkTrigger();
				}
				@Override
				public void focusLost(FocusEvent e) {
					if (getQuery().length() == 0) {
						setShowGeneHint(true);
					}
				}
			});
			
			textField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkTrigger(); 
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkTrigger(); 
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkTrigger(); 
				}
			});
			
			textField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_KP_DOWN:
						showProposals();
						ListSelectionModel model = getProposalTable().getSelectionModel();
						model.clearSelection();
						model.addSelectionInterval(0, 0);
						getProposalTable().requestFocus();
						break;
					case KeyEvent.VK_ESCAPE:
						hideProposals();
						break;
					case KeyEvent.VK_ENTER:
						if (getProposalTable().getSelectedRowCount() > 0)
							acceptProposal();
						else
							validateEntry(getQuery());
						break;
					}
				}
			});
			
			textField.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					handleFocusLost(e);
					saveCaret();
				}
				@Override
				public void focusGained(FocusEvent e) {
					restoreCaret();
				}
			});
			
			makeSmall(textField);
		}
		
		return textField;
	}
	
	private JTable getResultTable() {
		if (resultTable == null) {
			resultTable = createTable(resultModel);
			resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			
			final TableCellRenderer defHeaderRenderer = resultTable.getTableHeader().getDefaultRenderer();
			
			resultTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column) {
					Component c = defHeaderRenderer.getTableCellRendererComponent(table, value, isSelected,
							hasFocus, row, column);
					c.setForeground(table.isEnabled() ? 
							this.getForeground() : UIManager.getColor("Label.disabledForeground"));
					
					return c;
				}
			});
			
			resultTable.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					hideProposals();
				}
				@Override
				public void focusLost(FocusEvent e) {
				}
			});
			
			makeSmall(resultTable);
		}
		
		return resultTable;
	}
	
	private JTable getProposalTable() {
		if (proposalTable == null) {
			proposalTable = createTable(proposalModel);
			proposalTable.setBackground(PROPOSAL_BG_COLOR);
			proposalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			proposalTable.requestFocusInWindow();
			
			proposalTable.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					proposalTable.clearSelection();
					int row = proposalTable.rowAtPoint(e.getPoint());

					if (row > -1)
						proposalTable.setRowSelectionInterval(row, row);
				}
			});
			
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
			
			proposalTable.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent event) {
				}
				@Override
				public void focusLost(FocusEvent event) {
					handleFocusLost(event);
				}
			});
			
			makeSmall(proposalTable);
		}
		
		return proposalTable;
	}
	
	private void createMenu() {
		JPopupMenu contextMenu = new JPopupMenu();
		JMenuItem pasteMenu = new JMenuItem(Strings.paste_menuLabel);
		pasteMenu.addActionListener(evt -> {
			getTextField().requestFocus();
			new DefaultEditorKit.PasteAction().actionPerformed(evt);
		});
		contextMenu.add(pasteMenu);
		getTextField().setComponentPopupMenu(contextMenu);
	}

	public void handleParentMoved() {
		if (getProposalDialog().isVisible())
			popUpBelow(getTextField());
	}

	void restoreCaret() {
		getTextField().select(lastSelectionStart, lastSelectionEnd);
	}

	void saveCaret() {
		lastSelectionStart = getTextField().getSelectionStart();
		lastSelectionEnd = getTextField().getSelectionEnd();
	}

	private void handleFocusLost(FocusEvent event) {
		Component component = event.getOppositeComponent();
		
		if (component == null)
			return;
		
		while (component != null) {
			if (component.equals(proposalTable))
				return;
			if (component.equals(getTextField()))
				return;
			if (component.equals(this))
				return;
			
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
		
		Rectangle bounds = getProposalDialog().getBounds();
		bounds.x = point.x;
		bounds.y = point.y + controlBounds.height;
		bounds.width = controlBounds.width;
		getProposalDialog().setBounds(bounds);
		
		hackForTicket1439();
		hackForTicket1449();
		getProposalDialog().setVisible(true);
	}

	private void hackForTicket1449() {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		if (!osName.toLowerCase().startsWith("win")) { //$NON-NLS-1$
			return;
		}
		
		getProposalDialog().setFocusableWindowState(false);
	}

	private void hackForTicket1439() {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		if (!"linux".equalsIgnoreCase(osName)) { //$NON-NLS-1$
			return;
		}
		
		getProposalDialog().setFocusableWindowState(false);
	}

	void setShowGeneHint(boolean visible) {
		if (visible) {
			getTextField().setText(GENE_HINT);
			getTextField().setForeground(SystemColor.textInactiveText);
		} else {
			getTextField().setText(""); //$NON-NLS-1$
			getTextField().setForeground(SystemColor.textText);
		}
	}
	
	private JTable createTable(TableModel model) {
		JTable table = new JTable(model) {
			@Override
			public void addNotify() {
				super.addNotify();
				uiUtils.packColumns(this);
			}
		};
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		
		return table;
	}

	private DynamicTableModel<Gene> createModel() {
		return new DynamicTableModel<Gene>() {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return String.class;
			}
			@Override
			public int getColumnCount() {
				return 2;
			}
			@Override
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
			@Override
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
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
			@Override
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
		getStatusLabel().setText(String.format("<html><b>%s</b></html>", message)); //$NON-NLS-1$
	}

	public void setProvider(GeneCompletionProvider2 provider) {
		if (this.provider != null) {
			this.provider.close();
		}
		this.provider = provider;
		if (provider == null) {
			provider = createEmptyProvider();
		}
		getTextField().setTransferHandler(new CompletionTransferHandler(provider, new CompletionConsumer() {
			List<String> completions = new ArrayList<String>();
			
			@Override
			public void consume(String completion) {
				completions.add(completion);
			}
			@Override
			public void finish() {
				validateGene(completions);
				completions.clear();
			}
			@Override
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
		uiUtils.packColumns(getResultTable());
	}

	private void acceptProposal() {
		synchronized (this) {
			Gene gene = proposalModel.get(getProposalTable().getSelectedRow());
			String proposal = gene.getSymbol(); 
			if (validateGene(proposal)) {
				setStatus(""); //$NON-NLS-1$
				repackTable();
			}
			ListSelectionModel model = getProposalTable().getSelectionModel();
			model.clearSelection();
			model.addSelectionInterval(0, 0);
			hideProposals();
			proposalModel.clear();
			getTextField().setText(""); //$NON-NLS-1$
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
		getTextField().setText(""); //$NON-NLS-1$
	}

	public void hideProposals() {
		getProposalDialog().setVisible(false);
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
		
		uiUtils.packColumns(getProposalTable());
		getProposalDialog().pack();

		popUpBelow(getTextField());
		if (selectedIndex != -1) {
			ListSelectionModel model = getProposalTable().getSelectionModel();
			model.clearSelection();
			model.addSelectionInterval(selectedIndex, selectedIndex);
		}
		getTextField().requestFocus();
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
		return getTextField().getText().trim();
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
		return getResultTable().getSelectedRowCount();
	}
	
	public void removeSelection() {
		int[] selection = getResultTable().getSelectedRows();
		resultModel.removeRows(selection);
		uiUtils.packColumns(getResultTable());
	}
	
	public void clear() {
		setStatus(""); //$NON-NLS-1$
		resultModel.clear();
		uiUtils.packColumns(getResultTable());
	}

	public void addListSelectionListener(ListSelectionListener listener) {
		getResultTable().getSelectionModel().addListSelectionListener(listener);
	}
	
	public void removeListSelectionListener(ListSelectionListener listener) {
		getResultTable().getSelectionModel().removeListSelectionListener(listener);
	}
	
	public void addTableModelEventListener(TableModelListener listener) {
		resultModel.addTableModelListener(listener);
	}
	
	public void removeTableModelEventListener(TableModelListener listener) {
		resultModel.removeTableModelListener(listener);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		getTextField().setEnabled(enabled);
		getResultTable().setEnabled(enabled);
		super.setEnabled(enabled);
	}
	
	@Override
	public void requestFocus() {
		getTextField().requestFocus();
	}
}

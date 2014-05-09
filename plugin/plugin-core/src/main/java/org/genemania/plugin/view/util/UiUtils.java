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

package org.genemania.plugin.view.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Image;
import java.awt.SystemColor;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.html.HTMLEditorKit;

import org.genemania.exception.ApplicationException;
import org.genemania.plugin.Strings;
import org.genemania.plugin.SystemUtils;

public class UiUtils {
	private final ImageCache images;
	
	public UiUtils() {
		images = new ImageCache();
	}
	
	public JEditorPane createLinkEnabledEditorPane(final Component parent, String text) {
		return createLinkEnabledEditorPane(parent, text, null);
	}
	
	public JEditorPane createEditorPane(String text) {
		JEditorPane pane = new JEditorPane();
		pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		pane.setEditorKit(new HTMLEditorKit());
		pane.setOpaque(false);
		pane.setEditable(false);
		pane.setText(text);
		return pane;
	}
	
	public JEditorPane createLinkEnabledEditorPane(final Component parent, String text, URL baseUrl) {
		if (baseUrl != null) {
			text = filter(text, baseUrl);
		}
		
		JEditorPane pane = createEditorPane(text);
		HyperlinkListener linkListener = new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (!SystemUtils.openBrowser(e.getURL())) {
						JOptionPane.showMessageDialog(parent, Strings.openHyperlink_error, Strings.default_title, JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		pane.addHyperlinkListener(linkListener);
		return pane;
	}

	private String filter(String text, URL baseUrl) {
		Pattern pattern = Pattern.compile("\\@\\{(.+?)\\}"); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(text);
		
		Set<String> keys = new HashSet<String>();
		int offset = 0;
		while (matcher.find(offset)) {
			String key = matcher.group(1);
			keys.add(key);
			offset = matcher.end();
		}
		
		String base = baseUrl.toString();
		for (String key : keys) {
			String url = String.format("%s%s", base, key);  //$NON-NLS-1$
			text = text.replace(String.format("@{%s}", key), url); //$NON-NLS-1$
		}
		return text;
	}

	public void setPreferredWidth(Component component, int width) {
		component.setSize(width, Short.MAX_VALUE);
		Dimension size = component.getPreferredSize();
		component.setPreferredSize(new Dimension(width, size.height));
	}

	public JToggleButton createToggleButton() {
		JToggleButton expandButton = new JToggleButton();
		expandButton.setIcon(getIcon(ImageCache.ARROW_COLLAPSED_IMAGE));
		expandButton.setSelectedIcon(getIcon(ImageCache.ARROW_EXPANDED_IMAGE));
		expandButton.setBorderPainted(false);
		expandButton.setContentAreaFilled(false);
		expandButton.setFocusable(false);
		expandButton.setBorder(BorderFactory.createEmptyBorder());
		return expandButton;
	}
	
	private Icon getIcon(String id) {
		return getImageCache().getIcon(id);
	}

	public String filterImages(String text) {
		return text.replaceAll("</?[Ii][Mm][Gg].*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void packColumns(JTable table) {
		int padding = 5;
		TableModel model = table.getModel();
		int[] widths = new int[model.getColumnCount()];
		for (int j = 0; j < model.getRowCount(); j++) {
			for (int i = 0; i < model.getColumnCount(); i++) {
				TableCellRenderer renderer = table.getCellRenderer(j, i);
				Component component = renderer.getTableCellRendererComponent(table, model.getValueAt(j, i), true, true, j, i);
				widths[i] = (int) Math.max(widths[i], component.getPreferredSize().getWidth() + table.getIntercellSpacing().getWidth() + padding);
			}
		}
		TableColumnModel columnModel = table.getColumnModel();
		JTableHeader header = table.getTableHeader();
		for (int i = 0; i < widths.length; i++) {
			TableColumn column = columnModel.getColumn(i);
			TableCellRenderer renderer = column.getHeaderRenderer();
			if (renderer == null) {
				renderer = header.getDefaultRenderer();
			}
			Component component = renderer.getTableCellRendererComponent(table, column.getHeaderValue(), true, true, 0, i);
			widths[i] = (int) Math.max(widths[i], component.getPreferredSize().getWidth());
			header.setResizingColumn(column);
			column.setWidth(widths[i]);
		}
	}
	
	public Frame getFrame(Component parent) {
		while (parent != null) {
			if (parent instanceof Frame) {
				return (Frame) parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

	public Component createFillerPanel(Color background) {
		JPanel panel = new JPanel();
		panel.setBackground(background);
		return panel;
	}
	
	public Component createFillerPanel() {
		return createFillerPanel(SystemColor.text);
	}
	
	public JPanel createJPanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		return panel;
	}
	
	public JCheckBox createCheckBox(String label) {
		if (label == null) {
			label = ""; //$NON-NLS-1$
		}
		JCheckBox checkBox = new JCheckBox(label);
		checkBox.setOpaque(false);
		return checkBox;
	}
	
	public JCheckBox createCheckBox() {
		return createCheckBox(null);
	}

	public JRadioButton createRadioButton(String label) {
		JRadioButton button = new JRadioButton(label);
		button.setOpaque(false);
		return button;
	}
	
	private File getFileAWT(Component parent, String title, File initialFile, final String typeDescription, final Set<String> extensions, FileSelectionMode mode) throws ApplicationException {
		// Use AWT dialog for Mac since it lets us use Finder's file chooser
		final String fileDialogForDirectories = System.getProperty("apple.awt.fileDialogForDirectories");
		System.setProperty("apple.awt.fileDialogForDirectories", mode == FileSelectionMode.OPEN_DIRECTORY ? "true" : "false");
		try {
			FileDialog dialog;
			switch (mode) {
			case OPEN_FILE:
			case OPEN_DIRECTORY:
				dialog = new FileDialog(getFrame(parent), title, FileDialog.LOAD);
				break;
			case SAVE_FILE:
				dialog = new FileDialog(getFrame(parent), title, FileDialog.SAVE);
				break;
			default:
				throw new ApplicationException(String.valueOf(mode));
			}
			dialog.setFilenameFilter(new FilenameFilter() {
				@Override
				public boolean accept(File directory, String name) {
					String[] parts = name.split("[.]"); //$NON-NLS-1$
					String lastPart = parts[parts.length - 1];
					for (String extension : extensions) {
						if (extension.equalsIgnoreCase(lastPart)) {
							return true;
						}
					}
					return false;
				}
			});
			File directory = null;
			if (initialFile.isDirectory()) {
				directory = initialFile;
			}
			if (initialFile.isFile() || !initialFile.exists()) {
				dialog.setFile(initialFile.getName());
				directory = initialFile.getParentFile();
			}
			if (directory != null) {
				dialog.setDirectory(directory.getAbsolutePath());
			}
			dialog.setTitle(title);
			dialog.setVisible(true);
			String file = dialog.getFile();
			if (file == null) {
				return null;
			}
			String targetDirectory = dialog.getDirectory();
			if (targetDirectory == null) {
				return null;
			}
			return new File(String.format("%s%s%s", targetDirectory, File.separator, file)); //$NON-NLS-1$
		} finally {
			System.setProperty("apple.awt.fileDialogForDirectories", fileDialogForDirectories);
		}
	}
	
	private File getFileSwing(Component parent, String title, File initialFile, final String typeDescription, final Set<String> extensions, FileSelectionMode mode) throws ApplicationException {
		JFileChooser chooser = new JFileChooser(initialFile);
		chooser.setDialogTitle(title);
		chooser.setSelectedFile(initialFile);
		if (typeDescription != null && extensions != null && extensions.size() > 0) {
			chooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File file) {
					String[] parts = file.getName().split("[.]"); //$NON-NLS-1$
					String lastPart = parts[parts.length - 1];
					for (String extension : extensions) {
						if (extension.equalsIgnoreCase(lastPart)) {
							return true;
						}
					}
					return false;
				}

				@Override
				public String getDescription() {
					return typeDescription;
				}
			});
		}
		int option;
		switch (mode) {
		case OPEN_FILE:
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			option = chooser.showOpenDialog(parent);
			break;
		case OPEN_DIRECTORY:
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			option = chooser.showOpenDialog(parent);
			break;
		case SAVE_FILE:
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			option = chooser.showSaveDialog(parent);
			break;
		default:
			throw new ApplicationException(String.valueOf(mode));
		}
		if (option != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return chooser.getSelectedFile();
	}
	
	public File getFile(Component parent, String title, File initialFile, final String typeDescription, final Set<String> extensions, FileSelectionMode mode) throws ApplicationException {
		if (isMacOSX()) {
			// Use Finder instead of Swing for Mac.
			return getFileAWT(parent, title, initialFile, typeDescription, extensions, mode);
		} else {
			return getFileSwing(parent, title, initialFile, typeDescription, extensions, mode);
		}
	}
	
	public boolean isMacOSX() {
	    String osName = System.getProperty("os.name"); //$NON-NLS-1$
	    return osName.startsWith("Mac OS X"); //$NON-NLS-1$
	}
	
	public Dimension computeTextSizeHint(FontMetrics metrics, int columns, int rows) {
		int emSize = SwingUtilities.computeStringWidth(metrics, "M"); //$NON-NLS-1$
		return new Dimension(columns * emSize, metrics.getHeight() * rows);
	}
	
	public ImageCache getImageCache() {
		return images;
	}
	
	public Image getFrameIcon() {
		return images.getImage(ImageCache.ICON);
	}
}

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

package org.genemania.plugin.view;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.genemania.plugin.LogUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
	
	public AboutDialog(final Frame parent, final UiUtils uiUtils, final URL resourceBaseUrl) {
		super(parent, Strings.aboutDialog_title, true);
		
		parent.setIconImage(uiUtils.getFrameIcon());

		final InputStream aboutText = getClass().getResourceAsStream("about.html"); //$NON-NLS-1$
		final StringBuilder buffer = new StringBuilder();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(aboutText));
		
		try {
			String line = reader.readLine();
			
			while (line != null) {
				buffer.append(line);
				line = reader.readLine();
			}
		} catch (IOException e) {
			LogUtils.log(getClass(), e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				LogUtils.log(getClass(), e);
			}
		}
		
		final JPanel panel = uiUtils.createJPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
		panel.setOpaque(true);
		panel.setBackground(Color.WHITE);
		
		final JEditorPane pane = uiUtils.createLinkEnabledEditorPane(this, buffer.toString(), resourceBaseUrl);
		pane.setFont(getFont());
		
		pane.setAlignmentX(CENTER_ALIGNMENT);
		panel.add(pane);
		
		final JPanel buttonPanel = createButtonPanel(uiUtils);
		buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
		buttonPanel.setOpaque(false);
		panel.add(buttonPanel);
		
		uiUtils.setPreferredWidth(pane, 500);

		add(panel);
		
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
	}
	
	private JPanel createButtonPanel(final UiUtils uiUtils) {
		final JButton closeButton = new JButton(new AbstractAction(Strings.aboutDialogCloseButton_label) {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		} );
		
		final JPanel buttonPanel = uiUtils.createOkCancelPanel(null, closeButton);
		uiUtils.setDefaultOkCancelKeyStrokes(getRootPane(), closeButton.getAction(), closeButton.getAction());
		getRootPane().setDefaultButton(closeButton);
		
		return buttonPanel;
	}
}

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
package org.genemania.plugin.data.lucene.view;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.genemania.plugin.Strings;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public abstract class AbstractEditDialog extends JDialog {
	
	private boolean canceled;
	private JButton saveButton;

	public AbstractEditDialog(Frame owner, String title, boolean modality) {
		super(owner, title, modality);
		canceled = true;
	}

	public boolean isCanceled() {
		return canceled;
	}
	
	protected JPanel createButtonPanel(final UiUtils uiUtils) {
		final Component parent = this;
		saveButton = new JButton(new AbstractAction(Strings.save_label) {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = false;
				parent.setVisible(false);
			}
		});
		
		final JButton cancelButton = new JButton(new AbstractAction(Strings.cancel_label) {
			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				parent.setVisible(false);
			}
		});
		
		final JPanel buttonPanel = uiUtils.createOkCancelPanel(saveButton, cancelButton);
		uiUtils.setDefaultOkCancelKeyStrokes(getRootPane(), saveButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(saveButton);
		
		return buttonPanel;
	}
	
	public void validateSettings() {
		saveButton.setEnabled(isValidForm());
	}

	protected abstract boolean isValidForm();
}

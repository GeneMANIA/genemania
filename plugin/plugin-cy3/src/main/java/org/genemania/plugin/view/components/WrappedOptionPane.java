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

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class WrappedOptionPane extends JOptionPane {
	public static final int DEFAULT_WIDTH = 40;
	
	private static final long serialVersionUID = 1L;
	private int charactersPerLine;

	public WrappedOptionPane(String message, int messageType, int optionType, int charactersPerLine) {
		super(message, messageType, optionType, null, null, null);
		this.charactersPerLine = charactersPerLine;
	}
	
	@Override
	public int getMaxCharactersPerLineCount() {
		return charactersPerLine;
	}
	
	public static int showConfirmDialog(Component parentComponent, String message, String title, int optionType, int messageType, int charactersPerLine) {
		WrappedOptionPane pane = new WrappedOptionPane(message, messageType, optionType, charactersPerLine);
        pane.setInitialValue(null);
        JDialog dialog = pane.createDialog(parentComponent, title);
        pane.selectInitialValue();
        dialog.setVisible(true);
        dialog.dispose();

        Object selectedValue = pane.getValue();

        if (selectedValue == null) {
            return CLOSED_OPTION;
        }
        if (selectedValue instanceof Integer) {
            return (Integer) selectedValue;
        }
        return CLOSED_OPTION;
	}
}

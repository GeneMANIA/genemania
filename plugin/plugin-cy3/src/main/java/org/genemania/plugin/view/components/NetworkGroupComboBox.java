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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.plugin.Strings;

@SuppressWarnings("serial")
public class NetworkGroupComboBox extends JComboBox {
	private final List<InteractionNetworkGroup> groups;
	private final DefaultComboBoxModel groupModel;

	public NetworkGroupComboBox() {
		super(new DefaultComboBoxModel());
		groupModel = (DefaultComboBoxModel) getModel();
		this.groups = new ArrayList<InteractionNetworkGroup>();
	}
	
	public void updateNetworkGroups(Collection<InteractionNetworkGroup> groups) {
		groupModel.removeAllElements();
		this.groups.clear();
		
		if (groups == null) {
			return;
		}
		
		for (InteractionNetworkGroup group : groups) {
			this.groups.add(group);
		}
		Collections.sort(this.groups, new Comparator<InteractionNetworkGroup>() {
			public int compare(InteractionNetworkGroup o1, InteractionNetworkGroup o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		groupModel.addElement(Strings.networkGroupComboBoxCreateGroup_label);
		for (InteractionNetworkGroup group : this.groups) {
			groupModel.addElement(group.getName());
		}
	}
	
	public InteractionNetworkGroup getGroup() {
		int index = getSelectedIndex();
		switch (index) {
		case -1:
			return null;
		case 0:
			InteractionNetworkGroup group = new InteractionNetworkGroup();
			group.setId(-1);
			return group;
		}
		return groups.get(index - 1);
	}

	public void setGroup(InteractionNetworkGroup target) {
		for (int i = 0; i < groups.size(); i++) {
			InteractionNetworkGroup group = groups.get(i);
			if (target.getId() == group.getId()) {
				groupModel.setSelectedItem(group.getName());
				return;
			}
		}
	}

	public boolean containsGroup(String groupName) {
		for (InteractionNetworkGroup group : groups) {
			if (group.getName().equalsIgnoreCase(groupName)) {
				return true;
			}
		}
		return false;
	}
}

/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
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

package org.genemania.dto;

import java.io.Serializable;

public class InteractionDto implements Serializable {

	private static final long serialVersionUID = 8337560860451949723L;
	
	private NodeDto node1 = new NodeDto();
	private NodeDto node2 = new NodeDto();
	private double weight;

	public InteractionDto() {
	}

	public InteractionDto(NodeDto node1, NodeDto node2, double weight) {
		super();
		this.node1 = node1;
		this.node2 = node2;
		this.weight = weight;
	}

	public NodeDto getNodeVO1() {
		return node1;
	}

	public void setNodeVO1(NodeDto node1) {
		this.node1 = node1;
	}

	public NodeDto getNodeVO2() {
		return node2;
	}

	public void setNodeVO2(NodeDto node2) {
		this.node2 = node2;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append(node1.getId());
		ret.append("\t");
		ret.append(node2.getId());
		ret.append("\t");
		ret.append(weight);
		return ret.toString();
	}
	
}

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

package org.genemania.domain;

import java.util.Collection;
import java.util.LinkedList;

public class ResultInteractionNetworkGroup implements Comparable {
	private Collection<ResultInteractionNetwork> resultNetworks = new LinkedList<ResultInteractionNetwork>();
	private InteractionNetworkGroup networkGroup = null;
	private Double weight = null;

	public ResultInteractionNetworkGroup() {
	}

	public ResultInteractionNetworkGroup(
			Collection<ResultInteractionNetwork> resultNetworks,
			InteractionNetworkGroup networkGroup, Double weight) {
		super();
		this.resultNetworks = resultNetworks;
		this.networkGroup = networkGroup;
		this.weight = weight;
	}

	public Collection<ResultInteractionNetwork> getResultNetworks() {
		return resultNetworks;
	}

	public void setResultNetworks(
			Collection<ResultInteractionNetwork> resultNetworks) {
		this.resultNetworks = resultNetworks;
	}

	public InteractionNetworkGroup getNetworkGroup() {
		return networkGroup;
	}

	public void setNetworkGroup(InteractionNetworkGroup networkGroup) {
		this.networkGroup = networkGroup;
	}

	public double getWeight() {
		if (this.weight != null) {
			return this.weight;
		}

		this.weight = new Double(0);

		for (ResultInteractionNetwork n : this.resultNetworks) {
			this.weight += n.getWeight();
		}

		return this.weight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((networkGroup == null) ? 0 : networkGroup.hashCode());
		result = prime * result
				+ ((resultNetworks == null) ? 0 : resultNetworks.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultInteractionNetworkGroup other = (ResultInteractionNetworkGroup) obj;
		if (networkGroup == null) {
			if (other.networkGroup != null)
				return false;
		} else if (!networkGroup.equals(other.networkGroup))
			return false;
		if (resultNetworks == null) {
			if (other.resultNetworks != null)
				return false;
		} else if (!resultNetworks.equals(other.resultNetworks))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResultInteractionNetworkGroup [networkGroup=" + networkGroup
				+ ", resultNetworks=" + resultNetworks + ", weight=" + weight
				+ "]";
	}

	@Override
	public int compareTo(Object obj) {

		if (obj instanceof ResultInteractionNetworkGroup) {
			ResultInteractionNetworkGroup other = (ResultInteractionNetworkGroup) obj;

			return this.getNetworkGroup().getName().compareToIgnoreCase(
					other.getNetworkGroup().getName());
		}

		return 0;
	}

}

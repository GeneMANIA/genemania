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

public class ResultInteractionNetwork implements Comparable {
	private Collection<ResultInteraction> resultInteractions = new LinkedList<ResultInteraction>();
	private InteractionNetwork network;
	private double weight;

	public ResultInteractionNetwork() {

	}

	public ResultInteractionNetwork(
			Collection<ResultInteraction> resultInteractions,
			InteractionNetwork network, double weight) {
		super();
		this.resultInteractions = resultInteractions;
		this.network = network;
		this.weight = weight;
	}

	public InteractionNetwork getNetwork() {
		return network;
	}

	public void setNetwork(InteractionNetwork network) {
		this.network = network;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public Collection<ResultInteraction> getResultInteractions() {
		return resultInteractions;
	}

	public void setResultInteractions(
			Collection<ResultInteraction> resultInteractions) {
		this.resultInteractions = resultInteractions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((network == null) ? 0 : network.hashCode());
		result = prime
				* result
				+ ((resultInteractions == null) ? 0 : resultInteractions
						.hashCode());
		long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		ResultInteractionNetwork other = (ResultInteractionNetwork) obj;
		if (network == null) {
			if (other.network != null)
				return false;
		} else if (!network.equals(other.network))
			return false;
		if (resultInteractions == null) {
			if (other.resultInteractions != null)
				return false;
		} else if (!resultInteractions.equals(other.resultInteractions))
			return false;
		if (Double.doubleToLongBits(weight) != Double
				.doubleToLongBits(other.weight))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResultInteractionNetwork [network=" + network
				+ ", resultInteractions=" + resultInteractions + ", weight="
				+ weight + "]";
	}

	@Override
	public int compareTo(Object obj) {

		if (obj instanceof ResultInteractionNetwork) {
			ResultInteractionNetwork other = (ResultInteractionNetwork) obj;

			if( this.getWeight() <other.getWeight() ){
				return 1;
			} else if( this.getWeight() > other.getWeight() ) {
				return -1;
			} else {
				return 0;
			}
		}

		return 0;
	}

}

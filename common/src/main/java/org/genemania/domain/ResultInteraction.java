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

public class ResultInteraction implements Comparable {

	private Interaction interaction;
	private ResultGene fromGene;
	private ResultGene toGene;

	public ResultInteraction(Interaction interaction, ResultGene fromGene,
			ResultGene toGene) {
		super();
		this.interaction = interaction;
		this.fromGene = fromGene;
		this.toGene = toGene;
	}

	public Interaction getInteraction() {
		return interaction;
	}

	public void setInteraction(Interaction interaction) {
		this.interaction = interaction;
	}

	public ResultGene getFromGene() {
		return fromGene;
	}

	public void setFromGene(ResultGene fromGene) {
		this.fromGene = fromGene;
	}

	public ResultGene getToGene() {
		return toGene;
	}

	public void setToGene(ResultGene toGene) {
		this.toGene = toGene;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fromGene == null) ? 0 : fromGene.hashCode());
		result = prime * result
				+ ((interaction == null) ? 0 : interaction.hashCode());
		result = prime * result + ((toGene == null) ? 0 : toGene.hashCode());
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
		ResultInteraction other = (ResultInteraction) obj;
		if (fromGene == null) {
			if (other.fromGene != null)
				return false;
		} else if (!fromGene.equals(other.fromGene))
			return false;
		if (interaction == null) {
			if (other.interaction != null)
				return false;
		} else if (!interaction.equals(other.interaction))
			return false;
		if (toGene == null) {
			if (other.toGene != null)
				return false;
		} else if (!toGene.equals(other.toGene))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResultInteraction [fromGene=" + fromGene + ", interaction="
				+ interaction + ", toGene=" + toGene + "]";
	}

	@Override
	public int compareTo(Object o) {

		if (o instanceof ResultInteraction) {
			ResultInteraction other = (ResultInteraction) o;

			if (this.getInteraction().getWeight() < other.getInteraction()
					.getWeight()) {
				return -1;
			} else if (this.getInteraction().getWeight() > other
					.getInteraction().getWeight()) {
				return 1;
			} else {
				return 0;
			}
		}

		return 0;
	}

}

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

/**
 * NetworkVO: Light Network Value Object 
 * Created Oct 08, 2008
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.genemania.Constants;

public class NetworkDto implements Serializable {

	// __[static]_______________________________________________________________
	private static final long serialVersionUID = -1359860909742038328L;
	private static NumberFormat DBL = new DecimalFormat("###.00"); 
	
	// __[attributes]___________________________________________________________
	private long id;
	private double weight;
	private String type;
	private Collection<InteractionDto> interactions = new ArrayList<InteractionDto>();

	// __[constructors]_________________________________________________________
	public NetworkDto() {
	}

	public NetworkDto(long id, double weight) {
		super();
		this.id = id;
		this.weight = weight;
	}
	
	// __[accessors]____________________________________________________________
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Collection<InteractionDto> getInteractions() {
		return interactions;
	}

	public void setInteractions(Collection<InteractionDto> interactions) {
		this.interactions = interactions;
	}
	
	// __[public helpers]____________________________________________________________
	public void addInteraction(InteractionDto interaction) {
		this.interactions.add(interaction);
	}
	
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		for(InteractionDto interaction: interactions) {
			ret.append(interaction.getNodeVO1().getId());
			ret.append(Constants.DEFAULT_FIELD_SEPARATOR_TXT);
			ret.append(interaction.getNodeVO2().getId());
			ret.append(Constants.DEFAULT_FIELD_SEPARATOR_TXT);
			ret.append(DBL.format(interaction.getWeight()));
			ret.append(Constants.DEFAULT_INTERACTION_SEPARATOR_TXT);
		}
		return ret.toString().trim();
	}
	
}

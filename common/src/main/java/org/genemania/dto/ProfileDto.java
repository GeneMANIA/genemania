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
 * ProfileVO: Light Profile Value Object 
 * Created Oct 29, 2009
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.genemania.Constants;

public class ProfileDto implements Serializable {

	// __[static]_______________________________________________________________
	private static final long serialVersionUID = 4428189877554945333L;
	
	// __[attributes]___________________________________________________________
	private long id;
	private Collection<ProfileRowDto> individualGeneProfiles = new ArrayList<ProfileRowDto>();

	// __[constructors]_________________________________________________________
	public ProfileDto() {
	}

	// __[accessors]____________________________________________________________
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Collection<ProfileRowDto> getIndividualGeneProfiles() {
		return individualGeneProfiles;
	}

	public void setIndividualGeneProfiles(Collection<ProfileRowDto> individualGeneProfiles) {
		this.individualGeneProfiles = individualGeneProfiles;
	}

	// __[public helpers]____________________________________________________________
	public void addProfileRow(ProfileRowDto profileRow) {
		this.individualGeneProfiles.add(profileRow);
	}
	
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		for(ProfileRowDto row: individualGeneProfiles) {
			ret.append(row.getNodeId());
			ret.append(Constants.DEFAULT_FIELD_SEPARATOR_TXT);
			Iterator<String> it = row.getFeatures().iterator();
			while(it.hasNext()) {
				String feature = it.next();
				ret.append(feature);
				if(it.hasNext()) {
					ret.append(Constants.DEFAULT_FIELD_SEPARATOR_TXT);
				}
			}
			ret.append(Constants.DEFAULT_INTERACTION_SEPARATOR_TXT);
		}
		return ret.toString().trim();
	}

}

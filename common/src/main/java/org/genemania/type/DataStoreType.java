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
 * DataStoreType: TODO add description
 * Created Jun 24, 2010
 * @author Ovi Comes
 */
package org.genemania.type;

import java.io.Serializable;

public enum DataStoreType implements Serializable {

	// __[static]______________________________________________________________
	UNKNOWN("unknown"), LUCENE("lucene");

	// __[attributes]__________________________________________________________
	private String code = "";

	// __[constructors]________________________________________________________
	DataStoreType(String code) {
		this.code = code;
	}

	// __[accessors]___________________________________________________________
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public static DataStoreType fromCode(String aCode) {
		DataStoreType ret = DataStoreType.UNKNOWN;
		DataStoreType[] values = DataStoreType.values();
		for (int i = 0; i < values.length; i++) {
			DataStoreType next = (DataStoreType) values[i];
			if (next.getCode().equalsIgnoreCase(aCode)) {
				ret = next;
				break;
			}
		}
		return ret;
	}

}

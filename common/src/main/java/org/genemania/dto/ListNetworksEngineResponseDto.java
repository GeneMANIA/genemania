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
 * UploadNetworkWebResponseDto: Website-specific Upload Network response data transfer object   
 * Created Oct 19, 2009
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;
import java.util.Collection;

public class ListNetworksEngineResponseDto implements Serializable {

    // __[static]______________________________________________________________
    private static final long serialVersionUID = -7833700961159453816L;
    Collection<Long> networkIds;

    // __[constructors]________________________________________________________
    public ListNetworksEngineResponseDto() {
    }

    public Collection<Long> getNetworkIds() {
        return networkIds;
    }

    public void setNetworkIds(Collection<Long> networkIds) {
        this.networkIds = networkIds;
    }
}

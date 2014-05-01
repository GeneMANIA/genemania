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

// Generated Aug 12, 2010 3:07:22 PM by Hibernate Tools 3.2.0.CR1

/**
 * GeneData
 */
public class GeneData implements java.io.Serializable {

    private static final long serialVersionUID = -2633519861894626072L;
    
    private long              id;
    private String            description;
    private String            externalId;
    private GeneNamingSource  linkoutSource;

    public GeneData() {
    }

    public GeneData(String description) {
        this.description = description;
    }

    public GeneData(String description, String externalId, GeneNamingSource linkoutSource) {
        this.description = description;
        this.externalId = externalId;
        this.linkoutSource = linkoutSource;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public GeneNamingSource getLinkoutSource() {
        return this.linkoutSource;
    }

    public void setLinkoutSource(GeneNamingSource linkoutSource) {
        this.linkoutSource = linkoutSource;
    }

}

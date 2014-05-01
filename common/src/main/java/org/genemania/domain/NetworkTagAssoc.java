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


/**
 * NetworkTagAssoc
 */
public class NetworkTagAssoc implements java.io.Serializable {

    private static final long  serialVersionUID = -5331062359703204522L;

    private long               id;
    private InteractionNetwork network_id;
    private Tag                tag_id;

    public NetworkTagAssoc() {
    }

    public NetworkTagAssoc(InteractionNetwork network_id, Tag tag_id) {
        this.network_id = network_id;
        this.tag_id = tag_id;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public InteractionNetwork getNetwork_id() {
        return this.network_id;
    }

    public void setNetwork_id(InteractionNetwork network_id) {
        this.network_id = network_id;
    }

    public Tag getTag_id() {
        return this.tag_id;
    }

    public void setTag_id(Tag tag_id) {
        this.tag_id = tag_id;
    }

}

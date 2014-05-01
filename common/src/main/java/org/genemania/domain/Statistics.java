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

import java.util.Date;

/**
 * Statistics
 */
public class Statistics implements java.io.Serializable {

    private static final long serialVersionUID = -5513373729152923216L;

    private long              id;
    private long              organisms;
    private long              networks;
    private long              interactions;
    private long              genes;
    private long              predictions;
    private Date              date;

    public Statistics() {
    }

    public Statistics(long organisms, long networks, long interactions, long genes, long predictions, Date date) {
        this.organisms = organisms;
        this.networks = networks;
        this.interactions = interactions;
        this.genes = genes;
        this.predictions = predictions;
        this.date = date;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrganisms() {
        return this.organisms;
    }

    public void setOrganisms(long organisms) {
        this.organisms = organisms;
    }

    public long getNetworks() {
        return this.networks;
    }

    public void setNetworks(long networks) {
        this.networks = networks;
    }

    public long getInteractions() {
        return this.interactions;
    }

    public void setInteractions(long interactions) {
        this.interactions = interactions;
    }

    public long getGenes() {
        return this.genes;
    }

    public void setGenes(long genes) {
        this.genes = genes;
    }

    public long getPredictions() {
        return this.predictions;
    }

    public void setPredictions(long predictions) {
        this.predictions = predictions;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}

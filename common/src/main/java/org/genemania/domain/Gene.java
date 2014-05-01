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
 * Gene
 */
public class Gene implements java.io.Serializable {

    private static final long serialVersionUID = -5362571815869848425L;
    
    private long              id;
    private String            symbol;
    private String            symbolType;
    private GeneNamingSource  namingSource;
    private Node              node;
    private Organism          organism;
    private boolean           defaultSelected;

    public Gene() {
    }

    public Gene(String symbol, String symbolType, GeneNamingSource namingSource, Node node, Organism organism) {
        this.symbol = symbol;
        this.symbolType = symbolType;
        this.namingSource = namingSource;
        this.node = node;
        this.organism = organism;
    }

    public Gene(String symbol, String symbolType, GeneNamingSource namingSource, Node node, Organism organism,
            boolean defaultSelected) {
        this.symbol = symbol;
        this.symbolType = symbolType;
        this.namingSource = namingSource;
        this.node = node;
        this.organism = organism;
        this.defaultSelected = defaultSelected;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbolType() {
        return this.symbolType;
    }

    public void setSymbolType(String symbolType) {
        this.symbolType = symbolType;
    }

    public GeneNamingSource getNamingSource() {
        return this.namingSource;
    }

    public void setNamingSource(GeneNamingSource namingSource) {
        this.namingSource = namingSource;
    }

    public Node getNode() {
        return this.node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Organism getOrganism() {
        return this.organism;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public boolean isDefaultSelected() {
        return this.defaultSelected;
    }

    public void setDefaultSelected(boolean defaultSelected) {
        this.defaultSelected = defaultSelected;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		Gene other = (Gene) obj;
		if (defaultSelected != other.defaultSelected)
			return false;
		if (id != other.id)
			return false;
		if (namingSource == null) {
			if (other.namingSource != null)
				return false;
		} else if (!namingSource.equals(other.namingSource))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (organism == null) {
			if (other.organism != null)
				return false;
		} else if (!organism.equals(other.organism))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (symbolType == null) {
			if (other.symbolType != null)
				return false;
		} else if (!symbolType.equals(other.symbolType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Gene [defaultSelected=" + defaultSelected + ", id=" + id
				+ ", namingSource=" + namingSource + ", node=" + node
				+ ", organism=" + organism + ", symbol=" + symbol
				+ ", symbolType=" + symbolType + "]";
	}
    
    

}

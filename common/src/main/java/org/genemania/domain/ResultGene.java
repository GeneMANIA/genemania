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

public class ResultGene implements Comparable {
	public static class Link {
		private String name;
		private String url;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Link(String name, String url) {
			super();
			this.name = name;
			this.url = url;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((url == null) ? 0 : url.hashCode());
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
			Link other = (Link) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (url == null) {
				if (other.url != null)
					return false;
			} else if (!url.equals(other.url))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Link [name=" + name + ", url=" + url + "]";
		}

	}

	private Gene gene;
	private double score;
	boolean queryGene;
	private Collection<ResultOntologyCategory> resultOntologyCategories = new LinkedList<ResultOntologyCategory>();
	private Collection<ResultAttribute> resultAttributes = new LinkedList<ResultAttribute>();
	private Collection<Link> links = new LinkedList<Link>();
	private String typedName;

	public ResultGene() {

	}

	public ResultGene(Gene gene, double score, boolean queryGene,
			Collection<Link> links, String typedName) {
		super();
		this.gene = gene;
		this.score = score;
		this.queryGene = queryGene;
		this.links = links;
		this.typedName = typedName;
	}

	public Collection<Link> getLinks() {
		return links;
	}

	public void setLinks(Collection<Link> links) {
		this.links = links;
	}

	public Gene getGene() {
		return gene;
	}

	public void setGene(Gene gene) {
		this.gene = gene;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public boolean isQueryGene() {
		return queryGene;
	}

	public void setQueryGene(boolean queryGene) {
		this.queryGene = queryGene;
	}

	public Collection<ResultOntologyCategory> getResultOntologyCategories() {
		return resultOntologyCategories;
	}

	public void setResultOntologyCategories(
			Collection<ResultOntologyCategory> resultOntologyCategories) {
		this.resultOntologyCategories = resultOntologyCategories;
	}

	public String getTypedName() {
		return typedName;
	}

	public void setTypedName(String typedName) {
		this.typedName = typedName;
	}

	public Collection<ResultAttribute> getResultAttributes() {
		return resultAttributes;
	}

	public void setResultAttributes(Collection<ResultAttribute> attrbiutes) {
		this.resultAttributes = attrbiutes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((resultAttributes == null) ? 0 : resultAttributes.hashCode());
		result = prime * result + ((gene == null) ? 0 : gene.hashCode());
		result = prime * result + ((links == null) ? 0 : links.hashCode());
		result = prime * result + (queryGene ? 1231 : 1237);
		result = prime
				* result
				+ ((resultOntologyCategories == null) ? 0
						: resultOntologyCategories.hashCode());
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((typedName == null) ? 0 : typedName.hashCode());
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
		ResultGene other = (ResultGene) obj;
		if (resultAttributes == null) {
			if (other.resultAttributes != null)
				return false;
		} else if (!resultAttributes.equals(other.resultAttributes))
			return false;
		if (gene == null) {
			if (other.gene != null)
				return false;
		} else if (!gene.equals(other.gene))
			return false;
		if (links == null) {
			if (other.links != null)
				return false;
		} else if (!links.equals(other.links))
			return false;
		if (queryGene != other.queryGene)
			return false;
		if (resultOntologyCategories == null) {
			if (other.resultOntologyCategories != null)
				return false;
		} else if (!resultOntologyCategories
				.equals(other.resultOntologyCategories))
			return false;
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score))
			return false;
		if (typedName == null) {
			if (other.typedName != null)
				return false;
		} else if (!typedName.equals(other.typedName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResultGene [gene=" + gene + ", score=" + score + ", queryGene="
				+ queryGene + ", resultOntologyCategories="
				+ resultOntologyCategories + ", attrbiutes=" + resultAttributes
				+ ", links=" + links + ", typedName=" + typedName + "]";
	}

	@Override
	public int compareTo(Object o) {

		if (o instanceof ResultGene) {
			ResultGene other = (ResultGene) o;
			if (this.getScore() < other.getScore()) {
				return 1;
			} else if (this.getScore() > other.getScore()) {
				return -1;
			} else {
				return 0;
			}
		}

		return 0;
	}

}

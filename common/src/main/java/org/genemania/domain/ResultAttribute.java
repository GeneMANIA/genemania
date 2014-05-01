package org.genemania.domain;

import java.util.Collection;
import java.util.LinkedList;

import org.genemania.domain.ResultGene.Link;

public class ResultAttribute {

	public static class Link {
		private String url, name;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Link(String name, String url) {
			super();
			this.url = url;
			this.name = name;
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

	}

	// TODO add some sort of score field, probably derived from the attribute's
	// weight in the engine where it is considered a network
	private double weight;
	private Attribute attribute;
	private ResultAttributeGroup resultAttributeGroup;
	private int numAnnotatedInSample, numAnnotatedInTotal;
	private Collection<Link> links = new LinkedList<Link>();

	public ResultAttribute(Attribute attribute,
			ResultAttributeGroup resultAttributeGroup, double weight,
			int numAnnotatedInSample, int numAnnotatedInTotal) {
		this.attribute = attribute;
		this.resultAttributeGroup = resultAttributeGroup;
		this.weight = weight;
		this.numAnnotatedInSample = numAnnotatedInSample;
		this.numAnnotatedInTotal = numAnnotatedInTotal;
	}

	public ResultAttribute() {

	}

	public int getNumAnnotatedInSample() {
		return numAnnotatedInSample;
	}

	public void setNumAnnotatedInSample(int numAnnotatedInSample) {
		this.numAnnotatedInSample = numAnnotatedInSample;
	}

	public int getNumAnnotatedInTotal() {
		return numAnnotatedInTotal;
	}

	public void setNumAnnotatedInTotal(int numAnnotatedInTotal) {
		this.numAnnotatedInTotal = numAnnotatedInTotal;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public ResultAttributeGroup getResultAttributeGroup() {
		return resultAttributeGroup;
	}

	public void setResultAttributeGroup(
			ResultAttributeGroup resultAttributeGroup) {
		this.resultAttributeGroup = resultAttributeGroup;
	}

	public Collection<Link> getLinks() {
		return links;
	}

	public void setLinks(Collection<Link> links) {
		this.links = links;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((links == null) ? 0 : links.hashCode());
		result = prime * result + numAnnotatedInSample;
		result = prime * result + numAnnotatedInTotal;
		result = prime
				* result
				+ ((resultAttributeGroup == null) ? 0 : resultAttributeGroup
						.hashCode());
		long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		ResultAttribute other = (ResultAttribute) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		if (links == null) {
			if (other.links != null)
				return false;
		} else if (!links.equals(other.links))
			return false;
		if (numAnnotatedInSample != other.numAnnotatedInSample)
			return false;
		if (numAnnotatedInTotal != other.numAnnotatedInTotal)
			return false;
		if (resultAttributeGroup == null) {
			if (other.resultAttributeGroup != null)
				return false;
		} else if (!resultAttributeGroup.equals(other.resultAttributeGroup))
			return false;
		if (Double.doubleToLongBits(weight) != Double
				.doubleToLongBits(other.weight))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResultAttribute [weight=" + weight + ", attribute=" + attribute
				+ ", resultAttributeGroup=" + resultAttributeGroup
				+ ", numAnnotatedInSample=" + numAnnotatedInSample
				+ ", numAnnotatedInTotal=" + numAnnotatedInTotal + ", links="
				+ links + "]";
	}

}

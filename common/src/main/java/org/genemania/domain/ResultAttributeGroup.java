package org.genemania.domain;

import java.util.Collection;
import java.util.LinkedList;

public class ResultAttributeGroup {

	private AttributeGroup attributeGroup;
	private Collection<ResultAttribute> resultAttributes = new LinkedList<ResultAttribute>();
	private Double weight = null;

	public ResultAttributeGroup() {

	}

	public ResultAttributeGroup(AttributeGroup attributeGroup) {
		this.attributeGroup = attributeGroup;
	}

	public AttributeGroup getAttributeGroup() {
		return attributeGroup;
	}

	public void setAttributeGroup(AttributeGroup attributeGroup) {
		this.attributeGroup = attributeGroup;
	}

	public Collection<ResultAttribute> getResultAttributes() {
		return resultAttributes;
	}

	public void setResultAttributes(Collection<ResultAttribute> rAttributes) {
		this.resultAttributes = rAttributes;
	}

	public double getWeight() {
		if (this.weight != null) {
			return this.weight;
		}

		this.weight = new Double(0);

		for (ResultAttribute a : this.resultAttributes) {
			this.weight += a.getWeight();
		}

		return this.weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeGroup == null) ? 0 : attributeGroup.hashCode());
		result = prime
				* result
				+ ((resultAttributes == null) ? 0 : resultAttributes.hashCode());
		result = prime * result + ((weight == null) ? 0 : weight.hashCode());
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
		ResultAttributeGroup other = (ResultAttributeGroup) obj;
		if (attributeGroup == null) {
			if (other.attributeGroup != null)
				return false;
		} else if (!attributeGroup.equals(other.attributeGroup))
			return false;
		if (resultAttributes == null) {
			if (other.resultAttributes != null)
				return false;
		} else if (!resultAttributes.equals(other.resultAttributes))
			return false;
		if (weight == null) {
			if (other.weight != null)
				return false;
		} else if (!weight.equals(other.weight))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResultAttributeGroup [attributeGroup=" + attributeGroup
				+ ", resultAttributes=" + resultAttributes + ", weight="
				+ weight + "]";
	}

}

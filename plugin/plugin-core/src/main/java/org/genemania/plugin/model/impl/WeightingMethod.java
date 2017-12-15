package org.genemania.plugin.model.impl;

import org.genemania.type.CombiningMethod;

public class WeightingMethod {

	private CombiningMethod method;
	private String description;

	public WeightingMethod(CombiningMethod method, String description) {
		this.method = method;
		this.description = description;
	}

	public CombiningMethod getMethod() {
		return method;
	}
	
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public int hashCode() {
		final int prime = 13;
		int result = 17;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof WeightingMethod))
			return false;
		WeightingMethod other = (WeightingMethod) obj;
		if (method != other.method)
			return false;
		return true;
	}
}
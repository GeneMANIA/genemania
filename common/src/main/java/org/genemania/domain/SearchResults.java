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

import org.genemania.type.CombiningMethod;
import org.genemania.type.SearchResultsErrorCode;

public class SearchResults {
	private Collection<ResultInteractionNetworkGroup> resultNetworkGroups = new LinkedList<ResultInteractionNetworkGroup>();
	private Collection<ResultGene> resultGenes = new LinkedList<ResultGene>();
	private Collection<ResultOntologyCategory> resultOntologyCategories = new LinkedList<ResultOntologyCategory>();
	private CombiningMethod weighting;
	private Collection<ResultAttributeGroup> resultAttributeGroups = new LinkedList<ResultAttributeGroup>();
	private String error;
	private SearchResultsErrorCode errorCode;
	private SearchParameters parameters;

	public SearchResults() {
	}

	public SearchResults(
			Collection<ResultInteractionNetworkGroup> resultNetworkGroups,
			Collection<ResultGene> resultGenes,
			Collection<ResultOntologyCategory> resultOntologyCategories,
			Collection<ResultAttributeGroup> resultAttributeGroups,
			CombiningMethod weighting) {
		super();
		this.resultNetworkGroups = resultNetworkGroups;
		this.resultGenes = resultGenes;
		this.resultOntologyCategories = resultOntologyCategories;
		this.weighting = weighting;
		this.resultAttributeGroups = resultAttributeGroups;
	}

	public SearchResults(String error) {
		super();
		this.error = error;
		this.errorCode = SearchResultsErrorCode.UNKNOWN;
	}

	public SearchResults(String error, SearchResultsErrorCode errorCode) {
		super();
		this.error = error;
		this.errorCode = errorCode;
	}

	public Collection<ResultAttributeGroup> getResultAttributeGroups() {
		return resultAttributeGroups;
	}

	public void setResultAttributeGroups(
			Collection<ResultAttributeGroup> resultAttributeGroups) {
		this.resultAttributeGroups = resultAttributeGroups;
	}

	public Collection<ResultOntologyCategory> getResultOntologyCategories() {
		return resultOntologyCategories;
	}

	public void setResultOntologyCategories(
			Collection<ResultOntologyCategory> resultOntologyCategories) {
		this.resultOntologyCategories = resultOntologyCategories;
	}

	public Collection<ResultInteractionNetworkGroup> getResultNetworkGroups() {
		return resultNetworkGroups;
	}

	public void setResultNetworkGroups(
			Collection<ResultInteractionNetworkGroup> resultNetworkGroups) {
		this.resultNetworkGroups = resultNetworkGroups;
	}

	public Collection<ResultGene> getResultGenes() {
		return resultGenes;
	}

	public void setResultGenes(Collection<ResultGene> resultGenes) {
		this.resultGenes = resultGenes;
	}

	public CombiningMethod getWeighting() {
		return weighting;
	}

	public void setWeighting(CombiningMethod weighting) {
		this.weighting = weighting;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public SearchResultsErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(SearchResultsErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public SearchParameters getParameters() {
		return parameters;
	}

	public void setParameters(SearchParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		result = prime * result
				+ ((errorCode == null) ? 0 : errorCode.hashCode());
		result = prime
				* result
				+ ((resultAttributeGroups == null) ? 0 : resultAttributeGroups
						.hashCode());
		result = prime * result
				+ ((resultGenes == null) ? 0 : resultGenes.hashCode());
		result = prime
				* result
				+ ((resultNetworkGroups == null) ? 0 : resultNetworkGroups
						.hashCode());
		result = prime
				* result
				+ ((resultOntologyCategories == null) ? 0
						: resultOntologyCategories.hashCode());
		result = prime * result
				+ ((weighting == null) ? 0 : weighting.hashCode());
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
		SearchResults other = (SearchResults) obj;
		if (error == null) {
			if (other.error != null)
				return false;
		} else if (!error.equals(other.error))
			return false;
		if (errorCode != other.errorCode)
			return false;
		if (resultAttributeGroups == null) {
			if (other.resultAttributeGroups != null)
				return false;
		} else if (!resultAttributeGroups.equals(other.resultAttributeGroups))
			return false;
		if (resultGenes == null) {
			if (other.resultGenes != null)
				return false;
		} else if (!resultGenes.equals(other.resultGenes))
			return false;
		if (resultNetworkGroups == null) {
			if (other.resultNetworkGroups != null)
				return false;
		} else if (!resultNetworkGroups.equals(other.resultNetworkGroups))
			return false;
		if (resultOntologyCategories == null) {
			if (other.resultOntologyCategories != null)
				return false;
		} else if (!resultOntologyCategories
				.equals(other.resultOntologyCategories))
			return false;
		if (weighting != other.weighting)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SearchResults [resultNetworkGroups=" + resultNetworkGroups
				+ ", resultGenes=" + resultGenes
				+ ", resultOntologyCategories=" + resultOntologyCategories
				+ ", weighting=" + weighting + ", resultAttributeGroups="
				+ resultAttributeGroups + ", error=" + error + ", errorCode="
				+ errorCode + "]";
	}

}

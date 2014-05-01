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
import java.util.List;
import java.util.Set;

/**
 * NetworkMetadata
 */
public class NetworkMetadata implements java.io.Serializable {

	private static final long serialVersionUID = -7985449821375250646L;

	private long id;
	private String source;
	private String reference;
	private String pubmedId;
	private String authors;
	private String publicationName;
	private String yearPublished;
	private String processingDescription;
	private String networkType;
	private String alias;
	private long interactionCount;
	private String dynamicRange;
	private String edgeWeightDistribution;
	private long accessStats;
	private String comment;
	private String other;
	private String title;
	private String url;
	private String sourceUrl;
	private Collection<String> invalidInteractions;

	public NetworkMetadata() {
	}

	public NetworkMetadata(String source, String reference, String pubmedId,
			String authors, String publicationName, String yearPublished,
			String processingDescription, String networkType, String alias,
			long interactionCount, String dynamicRange,
			String edgeWeightDistribution, long accessStats, String comment,
			String other, String title, String url, String sourceUrl) {
		this.source = source;
		this.reference = reference;
		this.pubmedId = pubmedId;
		this.authors = authors;
		this.publicationName = publicationName;
		this.yearPublished = yearPublished;
		this.processingDescription = processingDescription;
		this.networkType = networkType;
		this.alias = alias;
		this.interactionCount = interactionCount;
		this.dynamicRange = dynamicRange;
		this.edgeWeightDistribution = edgeWeightDistribution;
		this.accessStats = accessStats;
		this.comment = comment;
		this.other = other;
		this.title = title;
		this.url = url;
		this.sourceUrl = sourceUrl;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getReference() {
		return this.reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getPubmedId() {
		return this.pubmedId;
	}

	public void setPubmedId(String pubmedId) {
		this.pubmedId = pubmedId;
	}

	public String getAuthors() {
		return this.authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getPublicationName() {
		return this.publicationName;
	}

	public void setPublicationName(String publicationName) {
		this.publicationName = publicationName;
	}

	public String getYearPublished() {
		return this.yearPublished;
	}

	public void setYearPublished(String yearPublished) {
		this.yearPublished = yearPublished;
	}

	public String getProcessingDescription() {
		return this.processingDescription;
	}

	public void setProcessingDescription(String processingDescription) {
		this.processingDescription = processingDescription;
	}

	public String getNetworkType() {
		return this.networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public long getInteractionCount() {
		return this.interactionCount;
	}

	public void setInteractionCount(long interactionCount) {
		this.interactionCount = interactionCount;
	}

	public Collection<String> getInvalidInteractions() {
		return invalidInteractions;
	}

	public void setInvalidInteractions(Collection<String> invalidInteractions) {
		this.invalidInteractions = invalidInteractions;
	}

	public String getDynamicRange() {
		return this.dynamicRange;
	}

	public void setDynamicRange(String dynamicRange) {
		this.dynamicRange = dynamicRange;
	}

	public String getEdgeWeightDistribution() {
		return this.edgeWeightDistribution;
	}

	public void setEdgeWeightDistribution(String edgeWeightDistribution) {
		this.edgeWeightDistribution = edgeWeightDistribution;
	}

	public long getAccessStats() {
		return this.accessStats;
	}

	public void setAccessStats(long accessStats) {
		this.accessStats = accessStats;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getOther() {
		return this.other;
	}

	public void setOther(String other) {
		this.other = other;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSourceUrl() {
		return this.sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

}

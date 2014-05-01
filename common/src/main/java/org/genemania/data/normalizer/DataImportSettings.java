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

package org.genemania.data.normalizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.type.DataLayout;
import org.genemania.type.ImportedDataFormat;
import org.genemania.type.NetworkProcessingMethod;

public class DataImportSettings {
	Organism organism;
	InteractionNetworkGroup networkGroup;
	InteractionNetwork network;
	double organismConfidence;
	DataLayout dataLayout;
	ImportedDataFormat dataFormat;
	NetworkProcessingMethod processingMethod;
	private List<Integer> idColumns;
	private String delimiter;
	private String color;
	private String source;

	public DataImportSettings() {
		dataLayout = DataLayout.UNKNOWN;
		dataFormat = ImportedDataFormat.UNKNOWN;
		processingMethod = NetworkProcessingMethod.UNKNOWN;
	}
	
	public Organism getOrganism() {
		return organism;
	}

	public void setOrganism(Organism organism) {
		this.organism = organism;
	}

	public InteractionNetworkGroup getNetworkGroup() {
		return networkGroup;
	}

	public void setNetworkGroup(InteractionNetworkGroup networkGroup) {
		this.networkGroup = networkGroup;
	}

	public InteractionNetwork getNetwork() {
		return network;
	}

	public void setNetwork(InteractionNetwork network) {
		this.network = network;
	}

	public double getOrganismConfidence() {
		return organismConfidence;
	}

	public void setOrganismConfidence(double organismConfidence) {
		this.organismConfidence = organismConfidence;
	}

	public DataLayout getDataLayout() {
		return dataLayout;
	}

	public void setDataLayout(DataLayout dataLayout) {
		this.dataLayout = dataLayout;
	}

	public ImportedDataFormat getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(ImportedDataFormat dataFormat) {
		this.dataFormat = dataFormat;
	}

	public NetworkProcessingMethod getProcessingMethod() {
		return processingMethod;
	}

	public void setProcessingMethod(NetworkProcessingMethod processingMethod) {
		this.processingMethod = processingMethod;
	}

	public void setIdColumns(List<Integer> idColumns) {
		this.idColumns = new ArrayList<Integer>(idColumns);
	}
	
	public List<Integer> getIdColumns() {
		return Collections.unmodifiableList(idColumns);
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	public String getDelimiter() {
		return delimiter;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	public String getColor() {
		return color;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public String getSource() {
		return source;
	}
}

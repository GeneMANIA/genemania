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

package org.genemania.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genemania.domain.Gene;
import org.genemania.exception.DataStoreException;

/**
 * Provides a mechanism for validating the contents of the gene textarea on the
 * server side
 */
public interface GeneService {

	/**
	 * The result returned by the service to the client webapp for validation
	 */
	public class ValidationResult {
		private Map<Integer, PossibleGene> genes = new HashMap<Integer, PossibleGene>();
		private Integer size = null;

		public int getSize() {
			return this.size;
		}

		public Map<Integer, PossibleGene> getGenes() {
			return this.genes;
		}

		public void addGene(int lineNumber, PossibleGene gene) {
			genes.put(lineNumber, gene);

			int possibleNewSize = lineNumber + 1; // lineNumber starts at 0
			if (size == null || possibleNewSize > size) {
				size = possibleNewSize;
			}
		}
	}

	/**
	 * Represents a gene
	 */
	public class PossibleGene {
		public enum Type {
			VALID, INVALID, DUPLICATE, SYNONYM, EMPTY
		}

		private Type type;
		private String name;
		private String other;

		public PossibleGene(String name, Type type) {
			this.type = type;
			this.name = name;
			this.other = "";
		}

		public PossibleGene(String name, Type type, String other) {
			this.type = type;
			this.name = name;
			this.other = other;
		}

		public String getName() {
			return this.name;
		}

		public Type getType() {
			return this.type;
		}

		public String getOther() {
			return this.other;
		}
	}

	public class GeneNames {
		List<String> validGenes;
		List<String> invalidGenes;

		public List<String> getValidGenes() {
			return validGenes;
		}

		public void setValidGenes(List<String> validGenes) {
			this.validGenes = validGenes;
		}

		public List<String> getInvalidGenes() {
			return invalidGenes;
		}

		public void setInvalidGenes(List<String> invalidGenes) {
			this.invalidGenes = invalidGenes;
		}

		public GeneNames(List<String> validGenes, List<String> invalidGenes) {
			super();
			this.validGenes = validGenes;
			this.invalidGenes = invalidGenes;
		}

	}

	/**
	 * Gets a list of valid and invalid gene names for the genes specified
	 * 
	 * @param organismId
	 *            The ID of the organism
	 * @param geneLines
	 *            The list of newline separated gene names
	 * @return The gene names
	 */
	public GeneNames getGeneNames(Integer organismId, String geneLines);

	/**
	 * Validates a list of genes
	 * 
	 * @param organismId
	 *            The ID of the organism
	 * @param geneList
	 *            a list of genes in a string, one gene per line
	 * @return the result, which contains the map (line number) => (gene name,
	 *         whether valid)
	 */
	public ValidationResult validateGeneLines(int organismId, String geneLines);

	/**
	 * Gets the set of default genes for an organism
	 * 
	 * @param organismId
	 *            The ID of the organism
	 * @return The set of default genes
	 * @throws DataStoreException
	 *             on database error
	 */
	public Collection<Gene> getDefaultGenesForOrganism(int organismId)
			throws DataStoreException;

	/**
	 * Gets the genes with the specified names
	 * 
	 * @param organismId
	 *            The organism the gene belongs to
	 * @param geneNames
	 *            The names of the genes
	 * @return The gene
	 * @throws DataStoreException
	 *             on database error
	 */
	public Collection<Gene> findGenesForOrganism(int organismId,
			List<String> geneNames) throws DataStoreException;

	/**
	 * Gets the gene with the specified name
	 * 
	 * @param organismId
	 *            The organism the gene belongs to
	 * @param geneName
	 *            The name of the gene
	 * @return The gene
	 * @throws DataStoreException
	 *             on database error
	 */
	public Gene findGeneForOrganism(int organismId, String geneName)
			throws DataStoreException;

	/**
	 * Finds a gene with the specified node ID
	 * 
	 * @param id
	 *            The gene node ID
	 * @return The gene
	 */
	public Gene findGeneForId(int organismId, long id);

}

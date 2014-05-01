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

package org.genemania.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.genemania.dao.GeneDao;
import org.genemania.dao.OrganismDao;
import org.genemania.domain.Gene;
import org.genemania.exception.DataStoreException;
import org.genemania.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;

public class GeneServiceImpl implements GeneService {

	@Autowired
	private GeneDao geneDao;

	@Autowired
	private OrganismDao organismDao;

	private class ValidationOracle {
		// line number => gene name
		List<String> lineToGene;

		// gene node id => line numbers of genes
		Map<Long, List<Integer>> synonyms = new HashMap<Long, List<Integer>>();

		// gene name => gene node id
		List<Long> lineToId;

		// gene name => line numbers of genes
		Map<String, List<Integer>> duplicates = new HashMap<String, List<Integer>>();

		public ValidationOracle(int organismId, List<String> genes) {
			lineToGene = new ArrayList<String>(genes.size());
			lineToId = new ArrayList<Long>(genes.size());

			// create the initial lookup maps
			int lineNumber = 0;
			for (String line : genes) {
				String gene = line.toLowerCase();

				// add to lines map
				lineToGene.add(gene);

				// add to synonyms map
				Long id = geneDao.getNodeId(organismId, gene);
				if (!synonyms.containsKey(id)) {
					synonyms.put(id, new LinkedList<Integer>());
				}
				synonyms.get(id).add(lineNumber);
				lineToId.add(id);

				// add to duplicates map
				if (!duplicates.containsKey(gene)) {
					duplicates.put(gene, new LinkedList<Integer>());
				}
				duplicates.get(gene).add(lineNumber);

				lineNumber++; // end of loop; increment counter
			}
		}

		public boolean isLineNumberValid(int line) {
			return 0 <= line && line <= lineToGene.size() - 1;
		}

		private void assertValidLine(int line) {
			if (!isLineNumberValid(line)) {
				throw new RuntimeException(
						"Line number not valid : 0 <= line <= "
								+ (lineToGene.size() - 1));
			}
		}

		public boolean isGeneValid(int line) {
			assertValidLine(line);
			return lineToId.get(line) != null;
		}

		public boolean isGeneDuplicate(int line) {
			assertValidLine(line);
			String gene = lineToGene.get(line);

			if (!duplicates.containsKey(gene)) {
				return false;
			} else {
				List<Integer> dupeLines = duplicates.get(gene);

				if (dupeLines.size() == 0) {
					return false;
				} else {
					int lineOfFirstIntance = dupeLines.get(0);
					return lineOfFirstIntance != line;
				}
			}
		}

		public boolean isGeneSynonym(int line) {
			assertValidLine(line);
			Long id = lineToId.get(line);

			if (!synonyms.containsKey(id)) {
				return false;
			} else {
				List<Integer> synonymLines = synonyms.get(id);

				if (synonymLines.size() == 0) {
					return false;
				} else {
					int lineOfFirstInstance = synonymLines.get(0);
					return lineOfFirstInstance != line;
				}
			}
		}

		public String getSynonym(int line) {
			assertValidLine(line);

			if (!isGeneSynonym(line)) {
				throw new RuntimeException("Gene on line " + line
						+ " with name ``" + lineToGene.get(line)
						+ "'' is not valid");
			}

			long id = lineToId.get(line);
			int lineOfSynonym = synonyms.get(id).get(0);
			String nameOfSynonym = lineToGene.get(lineOfSynonym);

			return nameOfSynonym;
		}

		public boolean isGeneEmpty(int line) {
			assertValidLine(line);
			String gene = lineToGene.get(line);

			return gene.equals("");
		}
	}

	public GeneDao getGeneDao() {
		return geneDao;
	}

	public void setGeneDao(GeneDao geneDao) {
		this.geneDao = geneDao;
	}

	public OrganismDao getOrganismDao() {
		return organismDao;
	}

	public void setOrganismDao(OrganismDao organismDao) {
		this.organismDao = organismDao;
	}

	@Override
	public ValidationResult validateGeneLines(int organismId, String geneLines) {
		String[] lines = (geneLines + "\nEND OF TEXT").split("\n");
		List<String> genes = new ArrayList<String>(lines.length);

		for (String line : lines) {
			String gene = line.trim();
			genes.add(gene);
		}
		genes.remove(genes.size() - 1); // remove END OF TEXT

		ValidationOracle oracle = new ValidationOracle(organismId, genes);
		ValidationResult result = new ValidationResult();

		for (int line = 0; line < genes.size(); line++) {
			String gene = genes.get(line);
			PossibleGene possibleGene;

			if (oracle.isGeneEmpty(line)) {
				possibleGene = new PossibleGene(gene, PossibleGene.Type.EMPTY);
			} else if (oracle.isGeneValid(line)) {

				if (oracle.isGeneDuplicate(line)) {
					possibleGene = new PossibleGene(gene,
							PossibleGene.Type.DUPLICATE);
				} else if (oracle.isGeneSynonym(line)) {
					possibleGene = new PossibleGene(gene,
							PossibleGene.Type.SYNONYM, oracle.getSynonym(line));
				} else {
					possibleGene = new PossibleGene(gene,
							PossibleGene.Type.VALID);
				}

			} else {
				possibleGene = new PossibleGene(gene, PossibleGene.Type.INVALID);
			}

			result.addGene(line, possibleGene);
		}

		return result;
	}

	@Override
	public Collection<Gene> getDefaultGenesForOrganism(int organismId)
			throws DataStoreException {
		return organismDao.getDefaultGenes(organismId);
	}

	@Override
	public Gene findGeneForOrganism(int organismId, String geneName)
			throws DataStoreException {
		List<String> names = new LinkedList<String>();
		names.add(geneName);
		return geneDao.getGenesForSymbols(organismId, names).get(0);
	}

	@Override
	public Collection<Gene> findGenesForOrganism(int organismId,
			List<String> geneNames) throws DataStoreException {
		return geneDao.getGenesForSymbols(organismId, geneNames);
	}

	@Override
	public Gene findGeneForId(int organismId, long id) {
		return geneDao.findGeneForId(organismId, id);
	}

	@Override
	public GeneNames getGeneNames(Integer organismId, String geneLines) {
		ValidationResult validationResult = validateGeneLines(
				organismId, geneLines);
		List<String> invalidGeneNames = new LinkedList<String>();
		List<String> validGeneNames = new LinkedList<String>();

		for (int line : validationResult.getGenes().keySet()) {
			PossibleGene pg = validationResult.getGenes().get(line);

			if (pg.getType() == PossibleGene.Type.VALID) {
				validGeneNames.add(pg.getName());
			} else if (pg.getType() == PossibleGene.Type.INVALID) {
				invalidGeneNames.add(pg.getName());
			}
		}

		return new GeneNames(validGeneNames, invalidGeneNames);
	}

}

/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
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

package org.genemania.plugin.apps;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.genemania.engine.apps.VectorCrossValidator;
import org.genemania.exception.ApplicationException;
import org.genemania.plugin.model.Group;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

@SuppressWarnings("nls")
public class NetworkAssessor extends AbstractValidationApp {
    @Option(name = "--networks", usage = "comma delimited list of network names or group codes to assess; e.g. 'BIOGRID,coexp', or 'all', or 'default'; or 'preferred' for our selection heuristic.", required = true)
    private String fNetworkList;

    @Option(name = "--exclude-networks", usage = "comma delimited list of network names or group codes to exclude; e.g. 'BIOGRID,coexp', or 'all', or 'default'; or 'preferred' for our selection heuristic.")
    private String fNetworkExcludeList;

    @Option(name = "--baseline", usage = "comma delimited list of baseline network names or group codes to use; e.g. 'BIOGRID,coexp', or 'all', or 'default'; or 'preferred' for our selection heuristic.", required = true)
    private String fBaselineList;

    @Option(name = "--exclude-baseline", usage = "comma delimited list of baseline network names or group codes to exclude; e.g. 'BIOGRID,coexp', or 'all', or 'default'; or 'preferred' for our selection heuristic.")
    private String fBaselineExcludeList;

    public static void main(String[] args) throws Exception {
        Logger.getLogger("org.genemania").setLevel(Level.WARN);

		NetworkAssessor assessor = new NetworkAssessor();
        CmdLineParser parser = new CmdLineParser(assessor);
        try {
        	parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println(String.format("\nUsage: %s options\n", assessor.getClass().getSimpleName())); //$NON-NLS-1$
            parser.printUsage(System.err);
            return;
        }
        assessor.initialize();
        assessor.doAssessment();
	}

	private void doAssessment() throws ApplicationException {
		checkFile(fQueryFile);
		checkWritable(fOutputFile);
		
		VectorCrossValidator validator = createValidator(null, null);
		validator.setOutFilename(null);
		Collection<Group<?, ?>> baselineNetworks = parseNetworks(fBaselineList, fBaselineExcludeList, fOrganism);
		Collection<Group<?, ?>> subjectNetworks = parseNetworks(fNetworkList, fNetworkExcludeList, fOrganism);
		
		if (baselineNetworks.size() == 0) {
			throw new ApplicationException("None of the baseline networks you specified were recognized.");
		}

		if (subjectNetworks.size() == 0) {
			throw new ApplicationException("None of the subject networks you specified were recognized.");
		}
		
		try {
			PrintWriter writer = new PrintWriter(new File(fOutputFile));
			try {
				validator.setNetworkIds(collapseNetworks(baselineNetworks));
				validator.setAttrIds(collapseAttributeGroups(baselineNetworks));
				validator.initValidation();
				Map<String, double[]> baselineMeasures = validator.crossValidate();
	
				validator.setNetworkIds(collapseNetworks(subjectNetworks));
				validator.setAttrIds(collapseAttributeGroups(subjectNetworks));
				validator.initValidation();
				Map<String, double[]> measures = validator.crossValidate();
				
				List<String> queryIds = new ArrayList<String>(baselineMeasures.keySet());
				sortResults(queryIds, baselineMeasures, measures);
				
				writer.print("QUERY");
				String[] names = validator.getMeasureNames();
				for (String name : names) {
					writer.print("\tBASELINE-");
					writer.print(name);
					writer.print("\tSUBJECT-");
					writer.print(name);
					writer.print("\t%ERR-");
					writer.print(name);
				}
				writer.println();
				
				for (String queryId : queryIds) {
					writer.print(queryId);
					if (baselineMeasures.get(queryId) == VectorCrossValidator.SKIPPED && measures.get(queryId) == VectorCrossValidator.SKIPPED) {
						writer.print("\tskipped");
					} else {
						for (int i = 0; i < names.length; i++) {
							Double baseline = getMeasure(baselineMeasures, queryId, i);
							Double subject = getMeasure(measures, queryId, i);
							writer.print("\t");
							writer.print(baseline);
							
							writer.print("\t");
							writer.print(subject);
							
							writer.print("\t");
							if (baseline != null && baseline == 0) {
								writer.print("-");
							} else if (baseline == null || subject == null) {
								writer.print("failed");
							} else {
								double score = (measures.get(queryId)[i] - baseline) / baseline;
								writer.print(String.format("%f", score));
							}
						}
					}
					writer.println();
				}
			} finally {
				writer.close();
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private Double getMeasure(Map<String, double[]> measures, String queryId, int i) {
		double[] query = measures.get(queryId);
		if (query == null) {
			return null;
		}
		return query[i];
	}

	void sortResults(List<String> queryIds, final Map<String, double[]> baselineMeasures, final Map<String, double[]> measures) {
		final int primaryMeasure = 0;
		Collections.sort(queryIds, new Comparator<String>() {
			public int compare(String s1, String s2) {
				double[] baseline1 = baselineMeasures.get(s1);
				double[] baseline2 = baselineMeasures.get(s2);
				double[] measure1 = measures.get(s1);
				double[] measure2 = measures.get(s2);
				
				// Skipped queries are at the very end.
				if (baseline1 == VectorCrossValidator.SKIPPED && baseline2 == VectorCrossValidator.SKIPPED) {
					return s1.compareTo(s2);
				}
				if (baseline1 == VectorCrossValidator.SKIPPED) {
					return 1;
				}
				if (baseline2 == VectorCrossValidator.SKIPPED) {
					return -1;
				}
				
				// Queries with failures in both measurements are next.
				if (baseline1 == null && measure1 == null && baseline2 == null && measure2 == null) {
					return s1.compareTo(s2);
				}
				if (baseline1 == null && measure1 == null) {
					return 1;
				}
				if (baseline2 == null && measure2 == null) {
					return -1;
				}
				
				// ...followed by queries with at least one failure
				if ((baseline1 == null || measure1 == null) && baseline2 != null && measure2 != null) {
					return 1;
				}
				if ((baseline2 == null || measure2 == null) && baseline1 != null && measure1 != null) {
					return -1;
				}
				if (baseline2 == null && baseline1 != null) {
					return 1;
				}
				if (baseline1 == null && baseline2 != null) {
					return -1;
				}
				
				// ...then queries with failures on both sides
				if (baseline1 == null || measure1 == null && baseline2 == null || measure2 == null) {
					return s1.compareTo(s2);
				}
				
				// Undefined values come next
				if (baseline1[primaryMeasure] == 0 && baseline2[primaryMeasure] == 0) {
					return s1.compareTo(s2);
				}
				if (baseline1[primaryMeasure] == 0) {
					return 1;
				}
				if (baseline2[primaryMeasure] == 0) {
					return -1;
				}
				
				double score1 = (measure1[primaryMeasure] - baseline1[primaryMeasure]) / baseline1[primaryMeasure];
				double score2 = (measure2[primaryMeasure] - baseline2[primaryMeasure]) / baseline2[primaryMeasure];
				int result = -Double.compare(score1, score2);
				if (result == 0) {
					return s1.compareTo(s2);
				}
				return result;
 			}
		});
	}
}

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


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.genemania.engine.apps.VectorCrossValidator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

@SuppressWarnings("nls")
public class CrossValidator extends AbstractValidationApp {
    @Option(name = "--networks", usage = "comma delimited list of network ids or group codes to use eg '3,4,19,coexp', or 'all', or 'default', or 'preferred' for our selection heuristic.", required = true)
    private String fNetworkList;
    
    @Option(name = "--exclude-networks", usage = "comma delimited list of network ids or group codes to exclude eg '3,4,19,coexp', or 'all', or 'default', or 'preferred' for our selection heuristic.")
    private String fNetworkExcludeList;

    @Option(name = "--labels", usage = "optional, write validation set labels for each fold to file, defaults to 'false'")
    private boolean fWriteLabels = false;
    
    private void doValidation() throws Exception {
		checkFile(fQueryFile);
    	checkWritable(fOutputFile);
    	
        VectorCrossValidator vcv = createValidator(fNetworkList, fNetworkExcludeList);
        vcv.setWriteLabels(fWriteLabels);
        vcv.setOutFilename(fOutputFile);
        
        // run the x-val 
        long start = System.currentTimeMillis();
        vcv.initValidation();
        vcv.crossValidate();

        long duration = System.currentTimeMillis() - start;
        int total = vcv.getQueryCounter();
		System.err.println(String.format("Performed %d predictions in %.2fs", total, duration / 1000.0)); //$NON-NLS-1$
	}
	
	public static void main(String[] args) throws Exception {
        Logger.getLogger("org.genemania").setLevel(Level.WARN);

		CrossValidator validator = new CrossValidator();
        CmdLineParser parser = new CmdLineParser(validator);
        try {
        	parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println(String.format("\nUsage: %s options\n", validator.getClass().getSimpleName())); //$NON-NLS-1$
            parser.printUsage(System.err);
            return;
        }
        validator.initialize();
        validator.doValidation();
	}
}

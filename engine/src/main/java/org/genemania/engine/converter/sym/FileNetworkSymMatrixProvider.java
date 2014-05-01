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


package org.genemania.engine.converter.sym;

import java.io.File;
import java.io.IOException;

import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.impl.FileInteractionCursor;
import org.genemania.util.ProgressReporter;

/**
 * load networks from text files located in a directory
 * with the convention of the text files being named
 *
 *   organismId.networkId.txt
 *
 */
public class FileNetworkSymMatrixProvider extends CursorNetworkSymMatrixProvider implements INetworkSymMatrixProvider {

    private long organismId;
    private String networkDir;
    private boolean isBinary;
    
    public FileNetworkSymMatrixProvider(long organismId, String networkDir, NodeIds nodeIds, boolean isBinary) {
        this.organismId = organismId;
        this.networkDir = networkDir;
        this.nodeIds = nodeIds;
        this.isBinary = isBinary;
    }
    
    public SymMatrix getNetworkMatrix(long networkId, ProgressReporter progress) throws ApplicationException {    	
        File file;
        try {
        	file = getFile(networkId);
        } catch (IOException e) {
        	throw new ApplicationException(e);
        }
        if (isBinary) {
            cursor = new FileInteractionCursor(networkId, file, "UTF8", 0, 1,-1, '\t');
        }
        else {
            cursor = new FileInteractionCursor(networkId, file, "UTF8", 0, 1, 2, '\t');            
        }
        try {
            return convertNetworkToMatrix(progress);
        } finally {
            cursor.close();
        }
    }

	private File getFile(long networkId) throws IOException {
        File file = new File(networkDir + File.separator + organismId + "." + networkId + ".txt.gz");
        if (file.exists()) {
        	return file;
        }
        
        return new File(networkDir + File.separator + organismId + "." + networkId + ".txt");
	}
}
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

/**
 * UploadNetworkEngineRequestDto: Engine-specific Upload Network request data transfer object   
 * Created Oct 19, 2009
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Reader;
import java.io.Serializable;

import org.genemania.type.DataLayout;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.util.ProgressReporter;

public class UploadNetworkEngineRequestDto implements Serializable {

    // __[static]______________________________________________________________
    private static final long serialVersionUID = -1315493487050286877L;

    // __[attributes]__________________________________________________________
    long organismId;
    String namespace;
    long networkId;
    DataLayout layout = DataLayout.UNKNOWN;
    NetworkProcessingMethod method = NetworkProcessingMethod.UNKNOWN;
    int sparsification;
    Reader data;
    ProgressReporter progressReporter;

    // __[constructors]________________________________________________________
    public UploadNetworkEngineRequestDto() {
    }

    /**
     * @return the organismId
     */
    public long getOrganismId() {
        return organismId;
    }

    /**
     * @param organismId the organismId to set
     */
    public void setOrganismId(long organismId) {
        this.organismId = organismId;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @return the networkId
     */
    public long getNetworkId() {
        return networkId;
    }

    /**
     * @param networkId the networkId to set
     */
    public void setNetworkId(long networkId) {
        this.networkId = networkId;
    }

    /**
     * @return the layout
     */
    public DataLayout getLayout() {
        return layout;
    }

    /**
     * @param layout the layout to set
     */
    public void setLayout(DataLayout layout) {
        this.layout = layout;
    }

    /**
     * @return the method
     */
    public NetworkProcessingMethod getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(NetworkProcessingMethod method) {
        this.method = method;
    }

    /**
     * @return the sparsification
     */
    public int getSparsification() {
        return sparsification;
    }

    /**
     * @param sparsification the sparsification to set
     */
    public void setSparsification(int sparsification) {
        this.sparsification = sparsification;
    }

    /**
     * @return the data
     */
    public Reader getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Reader data) {
        this.data = data;
    }

    /**
     * @return the progressReporter
     */
    public ProgressReporter getProgressReporter() {
        return progressReporter;
    }

    /**
     * @param progressReporter the progressReporter to set
     */
    public void setProgressReporter(ProgressReporter progressReporter) {
        this.progressReporter = progressReporter;
    }
    // __[accessors]___________________________________________________________
}

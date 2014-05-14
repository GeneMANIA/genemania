package org.genemania.adminweb.service;

import java.io.File;
import java.io.InputStream;

import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.exception.DatamartException;

public interface NetworkService {
    Network addNetwork(int organismId, int groupId, String originalFilename, InputStream inputStream);
    Network replaceNetwork(int organismId, int networkId, String originalFilename, InputStream inputStream);
    String updateNetwork(int organismId, int networkId, String field, String value) throws DatamartException;
    void deleteNetwork(int organismId, int networkId) throws DatamartException;
    Network updateNetworkMetadata(int organismId, int networkId);
    Network getNetwork(int organismId, int networkId) throws DatamartException;
    void refreshPubmed(Network network, long pubmedId) throws DatamartException;
    Network replaceAttributeMetadata(int organismId, int networkId, String originalFilename, InputStream inputStream);
    File getNetworkFile(Network network) throws DatamartException;
    File getMetadataFile(Network network) throws DatamartException;
    void deleteAttributeMetadata(long organismId, long networkId)
            throws DatamartException;
}

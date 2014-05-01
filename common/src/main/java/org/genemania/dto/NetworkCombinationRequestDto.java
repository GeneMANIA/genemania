package org.genemania.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.genemania.util.ProgressReporter;

public class NetworkCombinationRequestDto implements Serializable {
    private static final long serialVersionUID = 8091749432007358360L;

    String namespace;
    long organismId;
    List<NetworkDto> networks;
    Collection<AttributeDto> attributes;
    InteractionVisitor interactionVistor;
    ProgressReporter progressReporter;
    
    public String getNamespace() {
        return namespace;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    public long getOrganismId() {
        return organismId;
    }
    public void setOrganismId(long organismId) {
        this.organismId = organismId;
    }
    public List<NetworkDto> getNetworks() {
        return networks;
    }
    public void setNetworks(List<NetworkDto> networks) {
        this.networks = networks;
    }
    public Collection<AttributeDto> getAttributes() {
        return attributes;
    }
    public void setAttributes(Collection<AttributeDto> attributes) {
        this.attributes = attributes;
    }
    public InteractionVisitor getInteractionVistor() {
        return interactionVistor;
    }
    public void setInteractionVistor(InteractionVisitor interactionVistor) {
        this.interactionVistor = interactionVistor;
    }
    public ProgressReporter getProgressReporter() {
        return progressReporter;
    }
    public void setProgressReporter(ProgressReporter progressReporter) {
        this.progressReporter = progressReporter;
    }
}

package org.genemania.dto;

public interface InteractionVisitor {
    public void visit(long node1, long node2, double weight);    
}

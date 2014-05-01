package org.genemania.dto;

import java.io.Serializable;

/*
 * communicate selected attributes
 */
public class AttributeDto implements Serializable {
    private static final long serialVersionUID = -6843944154522389158L;

    private long id;
    private long groupId;   // perhaps redundant, if we can lookup id -> group with dao/domain
    private double weight;
    
    public void setId(long id) {
        this.id = id;
    }
    public long getId() {
        return id;
    }    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    public double getWeight() {
        return weight;
    }
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
    public long getGroupId() {
        return groupId;
    }
}

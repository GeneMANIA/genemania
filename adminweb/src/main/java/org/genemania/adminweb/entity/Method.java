package org.genemania.adminweb.entity;

import com.j256.ormlite.field.DatabaseField;

public class Method {
    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField(columnName = "NAME", canBeNull = false)
    private String name;
        
    @DatabaseField(columnName = "CODE")
    private String code;
    
    
}

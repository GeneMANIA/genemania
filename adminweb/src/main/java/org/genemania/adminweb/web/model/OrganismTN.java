package org.genemania.adminweb.web.model;

/*
 * tree node for organism level information
 */
public class OrganismTN extends TreeNode {
    public static final String NODETYPE = "organism";

    private int id;
    private String name;
    private String code;

    public OrganismTN(String title) {
        super(title);
        setIsFolder(true); // TODO: rename this class as folder for consistency
        setType(NODETYPE);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getKey() {
        return String.format("o=%d", getId());
    }
}

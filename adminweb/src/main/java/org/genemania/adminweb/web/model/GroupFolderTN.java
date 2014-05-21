package org.genemania.adminweb.web.model;

/*
 * tree node representing a network group
 */
public class GroupFolderTN extends TreeNode {
    public static final String NODETYPE = "group_folder_node";

    private int id;
    private int organismId;

    public GroupFolderTN(String title) {
        super(title);
        setFolder(true);
        setType(NODETYPE);
    }

    public int getOrganismId() {
        return organismId;
    }

    public void setOrganismId(int organismId) {
        this.organismId = organismId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getKey() {
        return String.format("o=%d:g=%d", getOrganismId(), getId());
    }
}

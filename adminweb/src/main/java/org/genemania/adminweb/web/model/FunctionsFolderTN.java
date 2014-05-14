package org.genemania.adminweb.web.model;


public class FunctionsFolderTN extends TreeNode {
    public static final String NODETYPE = "functions_folder_node";

    private int organismId;

    public FunctionsFolderTN(String title) {
        super(title);
        setIsFolder(true);
        setType(NODETYPE);
    }

	public int getOrganismId() {
		return organismId;
	}

	public void setOrganismId(int organismId) {
		this.organismId = organismId;
	}
}

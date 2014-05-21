package org.genemania.adminweb.web.model;

public class NetworksFolderTN extends TreeNode {
    public static final String NODETYPE = "networks";

    private int organismId;

    public NetworksFolderTN(String title) {
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
}

package org.genemania.adminweb.web.model;

public class NetworksFolderTN extends TreeNode {
    public static final String NODETYPE = "networks";

    private int organismId;

    public NetworksFolderTN(String title) {
        super(title);
        setFolder(true);
        setType(NODETYPE);
    }


    @Override
    public String getKey() {
        return String.format("o=%d:networksFolder", getOrganismId());
    }

	public int getOrganismId() {
		return organismId;
	}

	public void setOrganismId(int organismId) {
		this.organismId = organismId;
	}
}

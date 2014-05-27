package org.genemania.adminweb.web.model;

public class IdentifiersFolderTN extends TreeNode {
    public static final String NODETYPE = "identifiers_folder_node";

    private int id;
    private int organismId;
    private int numIdentifiers;
    private int numSymbols;

    public IdentifiersFolderTN(String title) {
        super(title);
        setFolder(true);
        setType(NODETYPE);
    }

    @Override
    public String getKey() {
        return String.format("o=%d:identifiersFolder", getOrganismId());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

	public int getOrganismId() {
		return organismId;
	}

	public void setOrganismId(int organismId) {
		this.organismId = organismId;
	}

	public int getNumIdentifiers() {
		return numIdentifiers;
	}

	public void setNumIdentifiers(int numIdentifiers) {
		this.numIdentifiers = numIdentifiers;
	}

	public int getNumSymbols() {
		return numSymbols;
	}

	public void setNumSymbols(int numSymbols) {
		this.numSymbols = numSymbols;
	}
}

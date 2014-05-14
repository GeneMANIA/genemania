package org.genemania.adminweb.web.model;

public class IdentifiersTN extends TreeNode {
    public static final String NODETYPE = "identifiers";

    private int id;
    private String filename;
    private int fileId;
    private int organismId;
    private int numIdentifiers;
    private int numSymbols;
    private String date;

    public IdentifiersTN(String title) {
        super(title);
        setType(NODETYPE);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String getKey() {
        return String.format("o=%d:i=%d", getOrganismId(), getId());
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }
}

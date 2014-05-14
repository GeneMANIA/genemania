package org.genemania.adminweb.web.service;

import java.util.List;

import org.genemania.adminweb.entity.Functions;
import org.genemania.adminweb.entity.Group;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.web.model.AttributesFolderTN;
import org.genemania.adminweb.web.model.FunctionsFolderTN;
import org.genemania.adminweb.web.model.FunctionsTN;
import org.genemania.adminweb.web.model.GroupFolderTN;
import org.genemania.adminweb.web.model.IdentifiersFolderTN;
import org.genemania.adminweb.web.model.IdentifiersTN;
import org.genemania.adminweb.web.model.NetworkTN;
import org.genemania.adminweb.web.model.TreeNode;

public interface TreeBuilderService {

    public GroupFolderTN getGroupTN(int organismId, Group group);

    public TreeNode getGroupedNetworkTree(int organismId);

    public NetworkTN getNetworkTN(Network network);

    public IdentifiersFolderTN getIdentifiersFolderTN(int organismId);

    public IdentifiersTN getIdentifiersTN(Identifiers identifiers);

    public List<TreeNode> getOrganismDataTree(int organismId);

    public AttributesFolderTN getAttributesFolderTN(int organismId);

    public FunctionsFolderTN getFunctionsFolderTN(int organismId);

    public FunctionsTN getFunctionsTN(Functions functions);
}

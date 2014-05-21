package org.genemania.adminweb.web.model;

import java.util.ArrayList;
import java.util.List;

/*
 * Node of a tree in the UI treeview, fields as required
 * by fancytree plugin. subclasses adding application
 * specific fields.
 *
 * icon field is a bit weird, since it can be a string
 * or boolean
 */
public class TreeNode {
    private String title; // this is name
    private boolean folder = false;
    private Object icon;
    private List<TreeNode> children;
    private String key;

    private String type; // all our nodes add this field

    public TreeNode(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
     public boolean getFolder() {
        return folder;
    }
     public void setFolder(boolean isFolder) {
        this.folder = isFolder;
    }

    public Object getIcon() {
        if (icon == null) {
            return null;
        }

        if (icon instanceof Boolean) {
            return icon;
        }

        return icon.toString();
    }

    // null means default icon
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void noIcon() {
        this.icon = Boolean.FALSE;
    }

    public List<TreeNode> getChildren() {
        return children;
    }
    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public void addChild(TreeNode child) {
        if (children == null) {
            children = new ArrayList<TreeNode>();
        }
        children.add(child);
    }

    public void addChildren(List<TreeNode> moreChildren) {
        if (children == null) {
            children = new ArrayList<TreeNode>();
        }
        children.addAll(moreChildren);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}

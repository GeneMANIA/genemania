package org.genemania.adminweb.dao;

import org.genemania.adminweb.entity.Group;

import com.j256.ormlite.dao.Dao;

public interface GroupDao extends Dao<Group, Integer> {
    public Group getGroupByCode(String code);
}

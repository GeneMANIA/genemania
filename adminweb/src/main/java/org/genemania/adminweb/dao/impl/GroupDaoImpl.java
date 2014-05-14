package org.genemania.adminweb.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.genemania.adminweb.dao.GroupDao;
import org.genemania.adminweb.entity.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class GroupDaoImpl extends BaseDaoImpl <Group, Integer> implements GroupDao {
    final Logger logger = LoggerFactory.getLogger(GroupDaoImpl.class);

    public GroupDaoImpl (ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Group.class);
    }

    @Override
    public Group getGroupByCode(String code) {
        try {
            QueryBuilder<Group, Integer> queryBuilder = queryBuilder();
            queryBuilder.where()
            .eq(Group.CODE_FIELD, code);

            List<Group> result = queryBuilder.query();
            return result.get(0);
        }
        catch (SQLException e) {
            logger.error("failed to get group with code: ", code);
            return null;
        }
    }
}

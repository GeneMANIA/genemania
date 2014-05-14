package org.genemania.adminweb.service.impl;

import java.sql.SQLException;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.Format;
import org.genemania.adminweb.entity.Group;
import org.genemania.adminweb.service.SetupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetupServiceImpl implements SetupService {

	final Logger logger = LoggerFactory.getLogger(SetupServiceImpl.class);

    @Autowired
    DatamartDb dmdb;

    @Override
    public void setup() {
        try {
//            dmdb.createTables();
            populateBase();
        }
        catch (SQLException e) {
            logger.error("failed to create tables", e);
        }
    }

    void createGroup(String name, String code, String type) throws SQLException {
        Group group = new Group();
        group.setName(name);
        group.setCode(code);
        group.setGroupType(type);
        dmdb.getGroupDao().create(group);
    }

    void createFormat(String name, String description) throws SQLException {
        Format format = new Format();
        format.setName(name);
        format.setDescription(description);
        dmdb.getFormatDao().create(format);
    }

    void populateBase() throws SQLException {

        // network groups
        createGroup("Co-expression", "coexp", "NETWORK");
        createGroup("Physical interactions", "pi", "NETWORK");
        createGroup("Genetic interactions", "gi", "NETWORK");
        createGroup("Co-localization", "coloc", "NETWORK");
        createGroup("Shared protein domains", "spd", "NETWORK");
        createGroup("Predicted", "predict", "NETWORK");
        createGroup("Pathway", "path", "NETWORK");
        createGroup("Other", "other", "NETWORK");
        createGroup("Attributes", "attrib", "ATTRIBUTE");
        createGroup("Functions", "func", "FUNCTION");

        // file formats
        createFormat("profile", "real-valued profile, n-columns, Pearson correlation");
        createFormat("network", "2 or 3 columns, third column assumed 1 if not given");
        createFormat("direct to shared neighbour", "2 columns, log-freq dot-prod");
        createFormat("attributes to shared neighbour", "variable length rows, log-req dot-prod");
        createFormat("attributes", "variable length rows, loaded as a group of connected networks per attribute");
        createFormat("direct network - already normalized", "direct network, already normalized");
    }

    public DatamartDb getDatamartDb() {
		return dmdb;
	}

	public void setDatamartDb(DatamartDb dmdb) {
		this.dmdb = dmdb;
	}
}

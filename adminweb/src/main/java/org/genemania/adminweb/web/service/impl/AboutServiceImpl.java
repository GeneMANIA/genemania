package org.genemania.adminweb.web.service.impl;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.web.model.ViewModel;
import org.genemania.adminweb.web.service.AboutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AboutServiceImpl implements AboutService {

    @Autowired
    private DatamartDb dmdb;
    private String version;
    private String genemaniaPort;

    @Override
    public void updateModel(ViewModel model) {
        model.put("APP_VERSION", version);
        model.put("GENEMANIA_PORT", genemaniaPort);
        model.put("DB_INFO", dmdb.info());
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGenemaniaPort() {
        return genemaniaPort;
    }

    public void setGenemaniaPort(String genemaniaPort) {
        this.genemaniaPort = genemaniaPort;
    }


}

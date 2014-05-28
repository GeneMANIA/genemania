 package org.genemania.adminweb.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.DataFile;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * download given file by id,
 * response includes original filename
 */
@Controller
public class DownloadController extends BaseController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DatamartDb dmdb;

    @Override
    public String toString() {
        return super.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/download/file")
    public void downloadFile(@RequestParam("id") int fileId,
        HttpServletResponse response) throws DatamartException, IOException, SQLException {
        response.setContentType("text/plain");

        DataFile dataFile = dmdb.getDataFileDao().queryForId(fileId);
        String filename = dataFile.getOriginalFilename();
        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", filename));

        File file = fileStorageService.getFile(dataFile.getFilename());
        IOUtils.copy(new FileInputStream(file), response.getOutputStream());
        response.flushBuffer();
    }
}

package org.genemania.adminweb.web.service.impl;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.DataFile;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.web.service.DataPreviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * implement serverside protocol of DataTables
 *
 *   https://datatables.net/manual/server-side
 *
 */
@Component
public class DataPreviewServiceImpl implements DataPreviewService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DatamartDb dmdb;

    @Override
    public Map<String, Object> getPreview(int fileId, int draw, int start, int length)
            throws DatamartException {

        DataFile dataFile = null;
        try {
            dataFile = dmdb.getDataFileDao().queryForId(fileId);
        }
        catch (SQLException e) {
            throw new DatamartException("failed to find file", e);
        }

        File file = fileStorageService.getFile(dataFile.getFilename());

        Map result = new HashMap<String, List<String[]>>();

        List<String[]> data = null;

        try {
            data = read(file, start, length);
        }
        catch (IOException e) {
            throw new DatamartException("failed to load file", e);
        }

        result.put("draw", draw);
        result.put("recordsTotal", data.size());
        result.put("recordsFiltered", data.size());
        result.put("data", data);

        return result;
    }

    /*
     * simple parse, later decorate with validation metadata (valid lines/fields etc) TODO
     */
    public List<String[]> read(File file, int start, int length) throws IOException {

        CSVReader reader = new CSVReader(new InputStreamReader(
                new BufferedInputStream(new FileInputStream(file)), "UTF8"),
                '\t', CSVParser.NULL_CHARACTER);

        List<String[]> lines = new ArrayList();
        try {
            int i = 0;
            String[] line = reader.readNext();
            while (line != null) {
                i += 1;

                if (i < start) {
                    continue;
                }

                if (i > start + length) {
                    return lines;
                }

                lines.add(line);

                line = reader.readNext();
            }
        }
        finally {
            reader.close();
        }

        return lines;
    }
}

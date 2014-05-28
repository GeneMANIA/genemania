package org.genemania.adminweb.web.service;

import org.genemania.adminweb.exception.DatamartException;

import java.util.Map;

public interface DataPreviewService {
    public Map<String, Object> getPreview(int fileId, int draw, int fromLine, int numLines)
            throws DatamartException;
}

package org.genemania.adminweb.web.controller;

import java.util.Map;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.web.service.DataPreviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * provide parsed subset of data file in json format
 * for display
 */
@Controller
public class DataPreviewController extends BaseController {

    @Autowired
    DataPreviewService dataPreviewService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/preview/file")
    public Map<String, Object> downloadFile(@RequestParam("id") int fileId,
                             @RequestParam("draw") int draw,
                             @RequestParam("start") int start,
                             @RequestParam("length") int length)
        throws DatamartException {

        return dataPreviewService.getPreview(fileId, draw, start, length);
    }
}

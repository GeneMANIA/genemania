package org.genemania.adminweb.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.IdentifiersService;
import org.genemania.adminweb.web.model.IdentifiersTN;
import org.genemania.adminweb.web.service.TreeBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class IdentifiersController extends BaseController {
    final Logger logger = LoggerFactory.getLogger(IdentifiersController.class);

    @Autowired
    private IdentifiersService identifiersService;

    @Autowired
    private TreeBuilderService treeBuilderService;

    @RequestMapping(method = RequestMethod.POST, value = "/addidentifiers")
    @ResponseBody
    public Map<String, String> addIdentifiers(@RequestParam("files") MultipartFile file,
            @RequestParam("organismId") int organismId) throws DatamartException, IOException {
        logger.info("processing identifier file upload: Organism {} file {}", organismId, file.getOriginalFilename());

        IdentifiersTN node = null;

        Identifiers identifiers = identifiersService.addIdentifiers(organismId, file.getOriginalFilename(), file.getInputStream());
        node = treeBuilderService.getIdentifiersTN(identifiers);

        Map<String, String> responseMap = new HashMap<String, String>();

        // node corresponding to the updated network ... just return
        // key for now but maybe we can pass back the node and update
        // tree on client without another reload() round trip?
        if (node != null) {
            responseMap.put("key", node.getKey());
        }
        else {
            responseMap.put("key", "");
        }

        return responseMap;

    }

    @RequestMapping(method = RequestMethod.POST, value = "/replaceidentifiers")
    @ResponseBody
    public Map<String, String> replaceIdentifiers(@RequestParam("files") MultipartFile file,
            @RequestParam("organismId") int organismId,
            @RequestParam("identifiersId") int identifiersId) throws DatamartException, IOException {

        logger.info("processing identifier file upload: Organism {} identifiers {} file {}",
            organismId, identifiersId, file.getOriginalFilename());

        identifiersService.replaceIdentifiers(organismId, identifiersId,
                file.getOriginalFilename(), file.getInputStream());

        // TODO: something useful to stick in here?
        Map<String, String> responseMap = new HashMap<String, String>();
        return responseMap;

    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/deleteidentifiers", params = "deleteButton")
    public Map<String, String> deleteIdentifiers(@RequestParam("organismId") int organismId,
            @RequestParam("identifiersId") int identifiersId) throws DatamartException {

      logger.info("delete identifiers");
      identifiersService.deleteIdentifiers(organismId, identifiersId);

      Map<String, String> responseMap = new HashMap<String, String>();
      return responseMap;
    }

}

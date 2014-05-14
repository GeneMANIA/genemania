package org.genemania.adminweb.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.genemania.adminweb.entity.Functions;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.web.model.FunctionsForm;
import org.genemania.adminweb.web.service.FormProcessorService;
import org.genemania.adminweb.web.service.FunctionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FunctionsController extends BaseController {
    final Logger logger = LoggerFactory.getLogger(FunctionsController.class);

    @Autowired
    FunctionsService functionsService;

    @Autowired
    private FormProcessorService formProcessorService;

    @RequestMapping(method = RequestMethod.POST, value = "/addfunctions")
    @ResponseBody
    public Map<String, String> addFunctions(@RequestParam("files") MultipartFile file,
            @RequestParam("organismId") int organismId) throws DatamartException, IOException {

        Functions functions = functionsService.addFunctions(organismId, file.getOriginalFilename(), file.getInputStream());

        Map<String, String> responseMap = new HashMap<String, String>();

        return responseMap;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/replacefunctions")
    @ResponseBody
    public Map<String, String> addFunctions(
            @RequestParam("files") MultipartFile file,
            @RequestParam("organismId") int organismId,
            @RequestParam("functionsId") int functionsId
            ) throws DatamartException, IOException {

        Functions functions = functionsService.replaceFunctions(organismId, functionsId,
                file.getOriginalFilename(), file.getInputStream());

        Map<String, String> responseMap = new HashMap<String, String>();

        return responseMap;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/updateFunctions", params = "submitButton")
    public String updateFunctions(@ModelAttribute("FunctionsForm")
        FunctionsForm functionsForm, BindingResult result) throws DatamartException {

        logger.info("update controller: updateNetwork");
        formProcessorService.updateFunctions(functionsForm);
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addfunctiondescriptions")
    @ResponseBody
    public Map<String, String> addFunctionDescriptions(
            @RequestParam("files") MultipartFile file,
            @RequestParam("organismId") int organismId,
            @RequestParam("functionsId") int functionsId)
                    throws DatamartException, IOException {

        Functions functions = functionsService.addFunctionDescriptions(organismId, functionsId,
                file.getOriginalFilename(), file.getInputStream());

        Map<String, String> responseMap = new HashMap<String, String>();

        return responseMap;
    }

}

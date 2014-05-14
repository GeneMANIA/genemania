package org.genemania.adminweb.web.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.genemania.adminweb.web.model.ViewModel;
import org.genemania.adminweb.web.service.AboutService;
import org.genemania.adminweb.web.service.OrganismService;
import org.genemania.adminweb.web.service.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BuildController extends BaseController {
    final Logger logger = LoggerFactory.getLogger(BuildController.class);

    @Autowired
    OrganismService organismService;

    @Autowired
    AboutService aboutService;

    // redirecting to the build_info page prevents form resubmits on
    // page refresh: Post/Redirect/Get (PRG) pattern. thanks yet again
    // stackoverflow
    @RequestMapping("/build")
    public String build(// HttpServletRequest request, HttpServletResponse response,
            @RequestParam("organismToBuild") String orgCode) throws IOException {
        logger.info("build controller");
        trigger(orgCode); // TODO: check value & clean data, this is quick & dirty!
        return "redirect:/build_info";
    }


    // quick & dirty, prototyping
    public static String TRIGGER_FILE="/tmp/BUILD";
    public static String DONE_FILE="/tmp/DONE";
    private void trigger(String orgCode) throws IOException {
        File file = new File(TRIGGER_FILE);
        if (!file.exists()) {
            FileUtils.writeStringToFile(file, orgCode, "UTF8");
//            FileUtils.touch(file);
        }
    }

    private void getInfo(ViewModel model) throws IOException {

        File file = new File(TRIGGER_FILE);
        if (file.exists()) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String trigger_date = sdf.format(file.lastModified());
            model.put("TRIGGER_DATE", trigger_date);
        }
        else {
            model.put("TRIGGER_DATE", "no build currently running");
        }

        file = new File(DONE_FILE);
        if (file.exists()) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String done_date = sdf.format(file.lastModified());
            model.put("DONE_DATE", done_date);
        }
        else {
            model.put("DONE_DATE", "no previous build");
        }
    }

    /*
     * for populating build form with an organism specified
     */
    private void getOrganisms(ViewModel model) throws IOException {
        model.put("organisms", organismService.getOrganismsTree());
    }

    @RequestMapping("/build_info")
    public ModelAndView buildInfo(HttpServletRequest request) throws IOException {
        logger.info("build_info controller");
        ViewModel model = ServiceSupport.generateModel(request);
        getInfo(model);
        getOrganisms(model);
        aboutService.updateModel(model);
        return new ModelAndView("buildinf", model);
    }

}

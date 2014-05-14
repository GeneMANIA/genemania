package org.genemania.adminweb.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.genemania.adminweb.web.model.ViewModel;
import org.genemania.adminweb.web.service.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelpController extends BaseController {
    final Logger logger = LoggerFactory.getLogger(HelpController.class);

    @RequestMapping("/help_formats")
    public ModelAndView main(HttpServletRequest request) {
        logger.debug("help controller");
        ViewModel model = ServiceSupport.generateModel(request);
        return new ModelAndView("help_formats", model);
    }
}

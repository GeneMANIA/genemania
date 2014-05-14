package org.genemania.adminweb.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.genemania.adminweb.web.model.ViewModel;
import org.genemania.adminweb.web.service.AboutService;
import org.genemania.adminweb.web.service.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController extends BaseController {
    final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    AboutService aboutService;

    @RequestMapping("/")
    public ModelAndView main(HttpServletRequest request) {
        ViewModel model = ServiceSupport.generateModel(request);
        aboutService.updateModel(model);
        return new ModelAndView("index", model);
    }
}

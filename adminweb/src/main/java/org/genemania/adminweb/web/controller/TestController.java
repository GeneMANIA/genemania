package org.genemania.adminweb.web.controller;

import javax.servlet.http.HttpSession;

import org.genemania.adminweb.exception.DatamartException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestController extends BaseController {

    /*
     * fire off javascript unit tests
     */
    @RequestMapping(method = RequestMethod.GET, value = "/test")
    public ModelAndView test(HttpSession session) {
        return new ModelAndView("test");
    }

    /*
     * generate a checked exception for testing error handling
     */
    @RequestMapping(method = RequestMethod.GET, value = "/testerror")
    public ModelAndView testerror(HttpSession session) throws DatamartException {
        throw new DatamartException("internal test of exception handling");
    }

    /*
     * generate a runtime exception
     */
    @RequestMapping(method = RequestMethod.GET, value = "/testerror2")
    public ModelAndView testerror2(HttpSession session) {
        throw new RuntimeException("internal test of exception handling");
    }
}
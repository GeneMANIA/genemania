package org.genemania.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestController {
	/**
	 * Test the error page
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/tests")
	public ModelAndView error(HttpSession session) {

		ModelAndView mv = ModelAndViewFactory.create("/tests");
		return mv;
	}
}

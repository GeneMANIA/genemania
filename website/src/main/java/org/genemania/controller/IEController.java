package org.genemania.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IEController {
	/**
	 * Give a specific error page for IE
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/ie")
	public ModelAndView error(HttpSession session) {

		ModelAndView mv = ModelAndViewFactory.create("/ie");
		return mv;
	}
}

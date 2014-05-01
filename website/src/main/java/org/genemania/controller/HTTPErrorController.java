package org.genemania.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HTTPErrorController {

	@RequestMapping(method = RequestMethod.GET, value = "/http_error")
	public ModelAndView show(
			@RequestParam(value = "type", required = false) String type,
			HttpSession session) {

		ModelAndView mv = ModelAndViewFactory.create("/http_error", "httpError.jsp");
		mv.addObject("type", type);
		return mv;
	}
}

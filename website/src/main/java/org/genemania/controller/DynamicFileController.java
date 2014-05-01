package org.genemania.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DynamicFileController {

	// usually just for testing (you want to post for long content)
	@RequestMapping(method = RequestMethod.GET, value = "/file")
	public ModelAndView show(
			@RequestParam("content") String content,
			@RequestParam("type") String contentType,
			@RequestParam(required = false, value = "disposition") String disposition,
			HttpSession session) {

		return getModelAndView(content, contentType, disposition);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/file")
	public ModelAndView create(
			@RequestParam("content") String content,
			@RequestParam("type") String contentType,
			@RequestParam(required = false, value = "disposition") String disposition,
			HttpSession session) {

		return getModelAndView(content, contentType, disposition);
	}

	private ModelAndView getModelAndView(String content, String contentType,
			String disposition) {
		ModelAndView mv = ModelAndViewFactory.create("/file", "dynamicFile.jsp");
		mv.addObject("content", content);
		mv.addObject("contentType", contentType);
		mv.addObject("disposition", disposition);

		return mv;
	}
}

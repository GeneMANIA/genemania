package org.genemania.controller;

import org.springframework.web.servlet.ModelAndView;

public class ModelAndViewFactory {

	public static ModelAndView create(String url, String jsp) {

		String prevDirs = "../";

		for (int i = 0; i < url.length(); i++) {
			if (url.charAt(i) == '/' && i != url.length() - 1 && i != 0) {
				prevDirs += "../";
			}
		}

		String path = prevDirs + "WEB-INF/jsp/" + jsp;

		return new ModelAndView(path);
	}

	public static ModelAndView create(String url) {
		String jsp = url;

		// remove trailing / if there
		if (jsp.charAt(jsp.length() - 1) == '/') {
			jsp = jsp.substring(0, jsp.length() - 2);
		}

		// if still have /, then get just the filename part after the last /
		if (jsp.contains("/")) {
			jsp = url.substring(url.lastIndexOf('/') + 1);
		}

		return create(url, jsp + ".jsp");
	}

}

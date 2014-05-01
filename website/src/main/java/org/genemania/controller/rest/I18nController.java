package org.genemania.controller.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

@Controller
public class I18nController {

	@Autowired
	private LocaleResolver localeResolver;

	private Map<String, String> messages;

	@RequestMapping(method = RequestMethod.GET, value = "/i18n")
	@ResponseBody
	public Map<String, String> list(HttpSession session,
			HttpServletRequest request) throws IOException {
		if (this.messages == null || true) {
			messages = new HashMap<String, String>();

			String lang = localeResolver.resolveLocale(request).toString();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					session.getServletContext().getResourceAsStream(
							"/i18n/messages_" + lang + ".properties")));

			while (br.ready()) {
				String line = br.readLine();

				if (!line.matches("\\s*[\\w\\.]+\\s*=\\s*.+\\s*")) {
					continue;
				}

				String[] parts = line.split("=");

				String key = parts[0].trim();
				String value = parts[1].trim();

				messages.put(key, value);
			}

			br.close();

		}

		return messages;
	}

	public LocaleResolver getLocaleResolver() {
		return localeResolver;
	}

	public void setLocaleResolver(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}

}

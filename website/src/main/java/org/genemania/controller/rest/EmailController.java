package org.genemania.controller.rest;

import javax.servlet.http.HttpSession;

import org.genemania.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EmailController {

	@Autowired
	EmailService emailService;

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService logEmailService) {
		this.emailService = logEmailService;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/mail")
	@ResponseBody
	public void create(@RequestParam("message") String message,
			@RequestParam(value = "subject", required = false) String subject,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "from", required = false) String from,
			HttpSession session) {

		if (name != null && from != null) {
			emailService.sendEmail(subject, message, name, from);
		} else if (subject != null) {
			emailService.sendEmail(subject, message);
		} else {
			emailService.sendEmail(message);
		}

	}
}

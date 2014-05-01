package org.genemania.service.impl;

import org.apache.log4j.Logger;
import org.genemania.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class EmailServiceImpl implements EmailService {

	@Autowired
	private MailSender mailSender;

	@Autowired
	private SimpleMailMessage mailMessage;

	protected Logger logger = Logger.getLogger(getClass());

	@Override
	public void sendEmail(String message) {
		SimpleMailMessage msg = new SimpleMailMessage(this.mailMessage);
		msg.setText(message);
		mailSender.send(msg);
	}

	@Override
	public void sendEmail(String subject, String message) {
		SimpleMailMessage msg = new SimpleMailMessage(this.mailMessage);
		msg.setText(message);
		msg.setSubject(subject);
		mailSender.send(msg);
	}

	@Override
	public void sendEmail(String subject, String message, String name,
			String from) {
		SimpleMailMessage msg = new SimpleMailMessage(this.mailMessage);
		msg.setSubject(subject);
		msg.setText("The following message was sent on behalf of " + name
				+ " (" + from + ") by the GeneMANIA mailer\n--\n" + message);
		msg.setFrom(from);
		mailSender.send(msg);
	}

	public MailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	public SimpleMailMessage getMailMessage() {
		return mailMessage;
	}

	public void setMailMessage(SimpleMailMessage message) {
		this.mailMessage = message;
	}

	// test the email sending
	public void init() {
		try {
			this.sendEmail("This is a test of the email system on init of the GeneMANIA webserver.");
		} catch (Exception e) {
			logger.error("The test of the email system on init failed.", e);
		}
	}

}

package org.genemania.service.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.text.NumberFormatter;

import org.apache.log4j.Logger;
import org.genemania.domain.Statistics;
import org.genemania.service.EmailService;
import org.genemania.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class EmailServiceImpl implements EmailService {

	@Autowired
	private StatsService statsService;

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
			Statistics stats = statsService.getStats();
			String dbVersion = new java.text.SimpleDateFormat(
					"d MMMMM yyyy HH:mm:ss").format(stats.getDate());
			String initTime = new java.text.SimpleDateFormat(
					"d MMMMM yyyy HH:mm:ss").format(new Date());

			String message = "This is a test of the email system on init of the GeneMANIA webserver.  ";
			message += "The server successfully started on " + initTime;
			message += " and is serving "
					+ new DecimalFormat().format(stats.getOrganisms())
					+ " organisms";
			message += " and "
					+ new DecimalFormat().format(stats.getInteractions())
					+ " interactions among "
					+ new DecimalFormat().format(stats.getGenes()) + " genes.";

			this.sendEmail(message);
		} catch (Exception e) {
			logger.error("The test of the email system on init failed.", e);
		}
	}

	public StatsService getStatsService() {
		return statsService;
	}

	public void setStatsService(StatsService statsService) {
		this.statsService = statsService;
	}

}

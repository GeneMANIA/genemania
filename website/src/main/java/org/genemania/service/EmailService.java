package org.genemania.service;

/**
 * Provides a mechanism for sending email to the developers for feedback, bugs,
 * etc
 */
public interface EmailService {

	public void sendEmail(String subject, String message, String name,
			String from);

	public void sendEmail(String subject, String message);

	public void sendEmail(String message);

}

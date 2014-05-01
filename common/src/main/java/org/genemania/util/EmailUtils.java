/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * EmailUtils: TODO add description
 * Created Jun 22, 2009
 * @author Ovi Comes
 */
package org.genemania.util;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.genemania.Constants;

public class EmailUtils {

	public static void sendEmail(String feedbackTo, String subject,  String message) {
		SimpleEmail email = new SimpleEmail();
		try {
			email.setHostName(ApplicationConfig.getInstance().getProperty(Constants.CONFIG_PROPERTIES.FEEDBACK_EMAIL_HOST));
			email.addTo(feedbackTo);
			email.setFrom(ApplicationConfig.getInstance().getProperty(Constants.CONFIG_PROPERTIES.FEEDBACK_FROM));
			email.setSubject(subject);
			email.setMsg(message);
			email.send();
		} catch (EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

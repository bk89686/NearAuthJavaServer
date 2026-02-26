package com.humansarehuman.blue2factor.communication;

import java.util.ArrayList;
import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.apache.http.util.TextUtils;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.tables.EmailSentDbObj;
import com.humansarehuman.blue2factor.entities.tables.SolicitationDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class Emailer {
	private MailSender mailSender;

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	public boolean sendSolicitationEmails() {
		int successCount = 0;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		ArrayList<SolicitationDbObj> emailInfos = dataAccess.getUnsolicitedCompanies();
		String emailText;
		String emailAddress;
		String coName;
		String contactName;
		String subject = "Smart, effortless authentication";
		for (SolicitationDbObj emailInfo : emailInfos) {
			emailText = "I'm not sure if there's a chance for collaboration between our companies, "
					+ "but we provide amazingly easy and secure, proximity based authentication to "
					+ "MSPs. Let me know if you have any interest, and I'll send over some details.\nThank you,\n"
					+ "Chris McLain\n" + "CEO";
			emailAddress = emailInfo.getEmailAddress();
			if (emailAddress != null) {
				contactName = emailInfo.getContactName();
				coName = emailInfo.getCompanyName();
				if (!TextUtils.isEmpty(contactName)) {
					emailText = "Dear " + contactName + ":\n" + emailText;
				} else if (coName != null) {
					emailText = "Dear " + coName + ":\n" + emailText;
				} else {
					emailText = "Hello,\n" + emailText;
				}
				if (email2(emailAddress, subject, emailText)) {
					emailInfo.setContactCount(emailInfo.getContactCount() + 1);
					emailInfo.setLastContact(DateTimeUtilities.getCurrentTimestamp());
					dataAccess.updateSolicitationCompany(emailInfo);
					EmailSentDbObj emailSent = new EmailSentDbObj(GeneralUtilities.randomString(),
							emailInfo.getRecordId(), null, DateTimeUtilities.getCurrentTimestamp(), emailAddress,
							subject, emailText);
					dataAccess.addEmailSent(emailSent);
					successCount++;
					dataAccess.addLog("email successfully sent to " + emailAddress + " at " + coName);
				} else {
					dataAccess.addLog("email failed when sent to " + emailAddress + " at " + coName);
				}
			}
		}
		return successCount > 0;
	}

	public boolean email(String toEmail, String subject, String text) {
		boolean success = false;
		String from = "chris@NearAuth.ai";
		String host = "localhost";// or IP address

		// Get the session object
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", host);
		Session session = Session.getDefaultInstance(properties);
		DataAccess dataAccess = new DataAccess();
		// compose the message
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
			dataAccess.addLog("email", "email subject: " + subject + ", text: " + text);
			message.setSubject(subject);
			message.setText(text);

			// Send message
			Transport.send(message);
			dataAccess.addLog("email", "message sent successfully to " + toEmail);
			success = true;
		} catch (MessagingException mex) {
			dataAccess.addLog("email", mex);
		}
		return success;
	}

	public boolean email2(String toEmail, String subject, String text) {
		boolean success = false;
		DataAccess dataAccess = new DataAccess();
		try {
			String from = "chris@NearAuth.ai";
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(toEmail);
			message.setSubject(subject);
			message.setText(text);
			// sending message
			if (mailSender == null) {
				mailSender = new JavaMailSenderImpl();
			}
			mailSender.send(message);
			success = true;
		} catch (Exception ex) {
			dataAccess.addLog("problem sending email to: " + toEmail
					+ " - this occurs often and we're not sure if it's serious or not", LogConstants.WARNING);
			ex.printStackTrace();
		}
		return success;
	}

	@SuppressWarnings("ucd")
	public void sendInviteEmail(String userName, String email) {
		Emailer emailer = new Emailer();
		String emailTxt = "Dear " + userName + ", \r\n"
				+ "Your security team has requested that you register to use NearAuth.ai to enforce online safety. "
				+ "NearAuth.ai provides passwordless two-factor authentication.\r\n"
				+ "To begin the signup process, please go to the NearAuth.ai download page at "
				+ "<a href='www.NearAuth.ai/downloads'>https://www.NearAuth.ai/downloads</a> "
				+ "Or for more information, go to https://www.NearAuth.ai. You need to complete the "
				+ "signup process with " + Constants.NUMBER_OF_DAYS_TO_SIGN_UP
				+ " days to avoid an interuption in service\r\n\r\n\r\n"
				+ "Thank you,\r\n\r\nYour NearAuth.ai team\r\n\r\n";

		emailer.email2(email, "Welcome to NearAuth.ai", emailTxt);
	}

	public void sendAdminSignup(String userName, String email) {
		Emailer emailer = new Emailer();
		String emailTxt = "Dear " + userName + ", \r\n" + "Thank you for signing up for NearAuth.ai. "
				+ "NearAuth.ai provides passwordless two-factor authentication.\r\n\r\n"
				+ "To complete the registration process, please signin at: \r\n\r\n" + "<a href='" + Urls.getSecureUrl()
				+ "/company'>" + Urls.getSecureUrl() + "/company</a>"
				+ "\r\n\r\nThank you,\r\n\r\nYour NearAuth.ai team\r\n\r\n";

		emailer.email2(email, "Welcome to NearAuth.ai", emailTxt);
	}

	public boolean sendAppConfirmEmail(String email, String tempId) {
		boolean success = false;
		String url = "https://www.NearAuth.ai/asneuhsaetnhusaenthusaenhtuesnhtuseathusaeunth?email=" + email + "&tid="
				+ tempId;
		DataAccess dataAccess = new DataAccess();
		try {
			String resp = new Internet().sendGet(url, 1);
			dataAccess.addLog(resp, LogConstants.TRACE);
			success = true;
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}
}

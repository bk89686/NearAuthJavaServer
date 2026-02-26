package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

public class EmailSentDbObj {
	String emailId;
	String solicitationId;
	String mailingListId;
	Timestamp createDate;
	String emailAddress;
	String subject;
	String message;

	public EmailSentDbObj(String emailId, String solicitationId, String mailingListId, Timestamp createDate,
			String emailAddress, String subject, String message) {
		super();
		this.emailId = emailId;
		this.solicitationId = solicitationId;
		this.mailingListId = mailingListId;
		this.createDate = createDate;
		this.emailAddress = emailAddress;
		this.subject = subject;
		this.message = message;
	}

	public String getMailingListId() {
		return mailingListId;
	}

	public void setMailingListId(String mailingListId) {
		this.mailingListId = mailingListId;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getSolicitationId() {
		return solicitationId;
	}

	public void setSolicitationId(String solicitationId) {
		this.solicitationId = solicitationId;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}

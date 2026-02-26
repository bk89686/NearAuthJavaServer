package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class MailingList {
	private Timestamp createDate;
	private String email;
	private Boolean active;
	private String name;
	private Boolean beta;
	private String backend;
	private String emailId;

	public MailingList(Timestamp createDate, String email, String name, Boolean active, Boolean beta, String backend,
			String emailId) {
		this.createDate = createDate;
		this.email = email;
		this.active = active;
		this.name = name;
		this.beta = beta;
		this.backend = backend;
		this.emailId = emailId;
	}

	public MailingList(String email, Boolean active) {
		this.createDate = DateTimeUtilities.getCurrentTimestamp();
		this.email = email;
		this.name = "";
		this.active = active;
	}

	public MailingList(String email, String name, Boolean active) {
		this.createDate = DateTimeUtilities.getCurrentTimestamp();
		this.email = email;
		this.name = name;
		this.active = active;
	}

	public MailingList(String email) {
		this.createDate = DateTimeUtilities.getCurrentTimestamp();
		this.email = email;
		this.active = true;
	}

	public MailingList(String email, String name) {
		this.createDate = DateTimeUtilities.getCurrentTimestamp();
		this.email = email;
		this.name = name;
		this.active = true;
	}

	public String getBackend() {
		return backend;
	}

	public void setBackend(String backend) {
		this.backend = backend;
	}

	public Boolean getBeta() {
		return beta;
	}

	public void setBeta(Boolean beta) {
		this.beta = beta;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public String getEmail() {
		return email;
	}

	public Boolean getActive() {
		return active;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

}

package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

public class SolicitationDbObj {
	String recordId;
	Timestamp createDate;
	String contactName;
	String emailAddress;
	String companyName;
	Timestamp lastContact;
	int contactCount;

	public SolicitationDbObj(String recordId, Timestamp createDate, String contactName, String emailAddress,
			String companyName, Timestamp lastContact, int contactCount) {
		super();
		this.recordId = recordId;
		this.createDate = createDate;
		this.contactName = contactName;
		this.emailAddress = emailAddress;
		this.companyName = companyName;
		this.lastContact = lastContact;
		this.contactCount = contactCount;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
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

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public Timestamp getLastContact() {
		return lastContact;
	}

	public void setLastContact(Timestamp lastContact) {
		this.lastContact = lastContact;
	}

	public int getContactCount() {
		return contactCount;
	}

	public void setContactCount(int contactCount) {
		this.contactCount = contactCount;
	}

}

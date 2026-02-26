package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

public class TestUserDbObj {
	String testUserId;
	String companyName;
	boolean active;
	String email;
	Timestamp createDate;
	Timestamp lastCheck;
	int installs;
	int installLimit;

	public TestUserDbObj(String testUserId, String companyName, boolean active, String email, Timestamp createDate,
			Timestamp lastCheck, int installs, int installLimit) {
		super();
		this.testUserId = testUserId;
		this.companyName = companyName;
		this.active = active;
		this.email = email;
		this.createDate = createDate;
		this.lastCheck = lastCheck;
		this.installs = installs;
		this.installLimit = installLimit;
	}

	public String getTestUserId() {
		return testUserId;
	}

	public void setTestUserId(String testUserId) {
		this.testUserId = testUserId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public Timestamp getLastCheck() {
		return lastCheck;
	}

	public void setLastCheck(Timestamp lastCheck) {
		this.lastCheck = lastCheck;
	}

	public int getInstalls() {
		return installs;
	}

	public void setInstalls(int installs) {
		this.installs = installs;
	}

	public int getInstallLimit() {
		return installLimit;
	}

	public void setInstallLimit(int installLimit) {
		this.installLimit = installLimit;
	}

}

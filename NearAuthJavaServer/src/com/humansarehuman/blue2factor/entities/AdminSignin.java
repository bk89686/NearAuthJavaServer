package com.humansarehuman.blue2factor.entities;

import jakarta.servlet.http.HttpServletResponse;

import com.humansarehuman.blue2factor.entities.enums.UserType;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;

public class AdminSignin {
	private CompanyDbObj company;
	private boolean correctUserOrPw;
	private boolean foundLocally;
	private boolean admin;
	private GroupDbObj group;
	private String reason;
	private boolean allowed;
	private HttpServletResponse response;

	public AdminSignin(CompanyDbObj company, GroupDbObj group, boolean correctUserOrPw, boolean foundLocally,
			boolean admin, String reason, boolean allowed, HttpServletResponse response) {
		super();
		this.company = company;
		this.correctUserOrPw = correctUserOrPw;
		this.foundLocally = foundLocally;
		this.group = group;
		this.admin = admin;
		this.reason = reason;
		this.allowed = allowed;
		this.response = response;
	}

	public boolean canAdministerCodes() {
		boolean adminPerms = false;
		UserType userType = group.getUserType();
		if (userType == UserType.ADMIN || userType == UserType.SUPER_ADMIN) {
			adminPerms = true;
		}
		return adminPerms;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public CompanyDbObj getCompany() {
		return company;
	}

	public void setCompany(CompanyDbObj company) {
		this.company = company;
	}

	public boolean isCorrectUserOrPw() {
		return correctUserOrPw;
	}

	public void setCorrectUserOrPw(boolean correctUserOrPw) {
		this.correctUserOrPw = correctUserOrPw;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isFoundLocally() {
		return foundLocally;
	}

	public void setFoundLocally(boolean foundLocally) {
		this.foundLocally = foundLocally;
	}

	public GroupDbObj getGroup() {
		return group;
	}

	public void setGroup(GroupDbObj group) {
		this.group = group;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}

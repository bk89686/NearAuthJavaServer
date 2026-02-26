package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.entities.enums.UserType;

public class GroupDbObj {

	private String companyId;
	private String groupId;
	private String groupName;
	private int acceptedTypes;
	private boolean active;
	private Timestamp createDate;
	private int timeoutSecs;
	private String groupPw;
	private String salt;
	private int devicesAllowed;
	private int devicesInUse;
	private int permissions;
	private String username;
	private Timestamp tokenDate;
	private String uid;
	private UserType userType;
	private boolean userExempt;
	private boolean pushAllowed;
	private boolean textAllowed;

	public GroupDbObj(String companyId, String groupId, String groupName, int acceptedTypes, boolean active,
			Timestamp createDate, int timeoutSecs, String groupPw, String salt, int devicesAllowed, int devicesInUse,
			int permissions, String username, Timestamp tokenDate, String uid, UserType userType, boolean userExempt,
			boolean pushAllowed, boolean textAllowed) {
		super();
		this.companyId = companyId;
		this.groupId = groupId;
		this.groupName = groupName;
		this.acceptedTypes = acceptedTypes;
		this.active = active;
		this.createDate = createDate;
		this.timeoutSecs = timeoutSecs;
		this.groupPw = groupPw;
		this.salt = salt;
		this.devicesAllowed = devicesAllowed;
		this.devicesInUse = devicesInUse;
		this.permissions = permissions;
		this.username = username;
		this.tokenDate = tokenDate;
		this.setUid(uid);
		this.userType = userType;
		this.userExempt = userExempt;
		this.pushAllowed = pushAllowed;
		this.textAllowed = textAllowed;
	}

	public boolean isPushAllowed() {
		return pushAllowed;
	}

	public void setPushAllowed(boolean pushAllowed) {
		this.pushAllowed = pushAllowed;
	}

	public boolean isTextAllowed() {
		return textAllowed;
	}

	public void setTextAllowed(boolean textAllowed) {
		this.textAllowed = textAllowed;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getAcceptedTypes() {
		return acceptedTypes;
	}

	public void setAcceptedTypes(int acceptedTypes) {
		this.acceptedTypes = acceptedTypes;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	@Override
	public String toString() {
		return "GroupsDbObj [companyId=" + companyId + ", groupId=" + groupId + ", groupName=" + groupName
				+ ", acceptedTypes=" + acceptedTypes + ", active=" + active + ", createDate=" + createDate
				+ ", timeoutSecs=" + timeoutSecs + ", userType=" + userType.toString();
	}

	public int getTimeoutSecs() {
		return timeoutSecs;
	}

	public void setTimeoutSecs(int timeoutSecs) {
		this.timeoutSecs = timeoutSecs;
	}

	public String getGroupPw() {
		return groupPw;
	}

	public void setGroupPw(String groupPw) {
		this.groupPw = groupPw;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public int getDevicesAllowed() {
		return devicesAllowed;
	}

	public void setDevicesAllowed(int devicesAllowed) {
		this.devicesAllowed = devicesAllowed;
	}

	public int getDevicesInUse() {
		return devicesInUse;
	}

	public void setDevicesInUse(int devicesInUse) {
		this.devicesInUse = devicesInUse;
	}

	public int getPermissions() {
		return permissions;
	}

	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

	public Timestamp getTokenDate() {
		return tokenDate;
	}

	public void setTokenDate(Timestamp tokenDate) {
		this.tokenDate = tokenDate;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public boolean isUserExempt() {
		return userExempt;
	}

	public void setUserExempt(boolean userExempt) {
		this.userExempt = userExempt;
	}

}

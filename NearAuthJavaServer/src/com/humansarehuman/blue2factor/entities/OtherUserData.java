package com.humansarehuman.blue2factor.entities;

import java.io.Serializable;

public class OtherUserData implements Serializable {
	private static final long serialVersionUID = -1879867389137213973L;
	private String userName;
	private String email;
	private String groupId;

	public OtherUserData(String userName, String email, String groupId) {
		this.userName = userName;
		this.email = email;
		this.groupId = groupId;
	}

	public String getUserName() {
		return userName;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getEmail() {
		return email;
	}

}

package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

public class UpdateUserFlowRequest implements Serializable {
	private static final long serialVersionUID = 4356328596780988008L;
	private boolean addToAd;
	private boolean allowAllIdpUsers;

	public boolean isAddToAd() {
		return addToAd;
	}

	public void setAddToAd(boolean addToAd) {
		this.addToAd = addToAd;
	}

	public boolean isAllowAllIdpUsers() {
		return allowAllIdpUsers;
	}

	public void setAllowAllIdpUsers(boolean allowAllIdpUsers) {
		this.allowAllIdpUsers = allowAllIdpUsers;
	}

}

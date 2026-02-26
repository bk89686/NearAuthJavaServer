package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

public class UpdateNonMemberStrategyRequest implements Serializable {
	private static final long serialVersionUID = 2308452309697065662L;
	private boolean noConsoleAllowed;
	private boolean noDeviceAllowed;

	public boolean isNoConsoleAllowed() {
		return noConsoleAllowed;
	}

	public void setNoConsoleAllowed(boolean noConsoleAllowed) {
		this.noConsoleAllowed = noConsoleAllowed;
	}

	public boolean isNoDeviceAllowed() {
		return noDeviceAllowed;
	}

	public void setNoDeviceAllowed(boolean noDeviceAllowed) {
		this.noDeviceAllowed = noDeviceAllowed;
	}

}

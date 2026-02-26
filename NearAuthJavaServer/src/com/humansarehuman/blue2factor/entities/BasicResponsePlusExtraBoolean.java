package com.humansarehuman.blue2factor.entities;

import java.io.Serializable;

public class BasicResponsePlusExtraBoolean extends BasicResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5432066938596826509L;
	boolean extraBoolan;

	public BasicResponsePlusExtraBoolean(int outcome, String reason, String token, Boolean extraBoolean) {
		super(outcome, reason, token);
		this.extraBoolan = extraBoolean;
	}

	public boolean isExtraBoolan() {
		return extraBoolan;
	}

	public void setExtraBoolan(boolean extraBoolan) {
		this.extraBoolan = extraBoolan;
	}
}

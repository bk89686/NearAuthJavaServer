package com.humansarehuman.blue2factor.entities;

import java.io.Serializable;

import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;

public class BasicResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8900046097833603377L;

	private int outcome;
	private String reason;
	// in this context token is the new cookie (called b2fId elsewhere)
	private String token;
	private String instanceId;
	private Boolean connected;
	private long expireMillis;

	public BasicResponse(int outcome, String reason) {
		this(outcome, reason, "");

	}

	public BasicResponse(int outcome, String reason, String token) {
		// in this context token is the new cookie (called b2fId elsewhere)
		this(outcome, reason, token, "");
	}

	public BasicResponse(int outcome, String reason, String token, String instanceId) {
		this(outcome, reason, token, instanceId, 0);
	}

	public BasicResponse(int outcome, String reason, String token, String instanceId, long expireMillis) {
		this.outcome = outcome;
		this.reason = reason;
		this.token = token;
		this.instanceId = instanceId;
		this.expireMillis = expireMillis;
		if (outcome != Outcomes.SUCCESS) {
			new DataAccess().addLog("non successful response: " + outcome + " with reason: " + reason);
		}
	}

	public int getOutcome() {
		return outcome;
	}

	public void setOutcome(int outcome) {
		this.outcome = outcome;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		return "Responses [outcome=" + outcome + ", reason=" + reason + ", token=" + token + ", expireMillis="
				+ expireMillis + "]";
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public Boolean getConnected() {
		return connected;
	}

	public void setConnected(Boolean connected) {
		this.connected = connected;
	}

	public long getExpireMillis() {
		return expireMillis;
	}

	public void setExpireMillis(long expireMillis) {
		this.expireMillis = expireMillis;
	}

}

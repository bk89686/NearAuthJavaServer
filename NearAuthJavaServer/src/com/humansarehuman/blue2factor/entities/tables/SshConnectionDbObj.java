package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class SshConnectionDbObj {
	String sshId;
	String companyId;
	String deviceId;
	String serverId;
	String clientUser;
	String serverUser;
	Timestamp createDate;
	Timestamp completionDate;
	String localIpAddress;
	String serverIpAddress;
	int clientOutcome;
	int serverOutcome;

	public SshConnectionDbObj(String companyId, String deviceId, String serverId, String clientUser, String serverUser,
			Timestamp createDate, String localIpAddress, int clientOutcome) {
		super();
		this.sshId = GeneralUtilities.randomString();
		this.companyId = companyId;
		this.deviceId = deviceId;
		this.serverId = serverId;
		this.clientUser = clientUser;
		this.serverUser = serverUser;
		this.createDate = createDate;
		this.localIpAddress = localIpAddress;
		this.clientOutcome = clientOutcome;
		this.serverOutcome = Outcomes.UNKNOWN_STATUS;
	}

	public SshConnectionDbObj(String sshId, String companyId, String deviceId, String serverId, String clientUser,
			String serverUser, Timestamp createDate, Timestamp completionDate, String localIpAddress,
			String serverIpAddress, int clientOutcome, int serverOutcome) {
		super();
		this.sshId = sshId;
		this.companyId = companyId;
		this.deviceId = deviceId;
		this.serverId = serverId;
		this.clientUser = clientUser;
		this.serverUser = serverUser;
		this.createDate = createDate;
		this.completionDate = completionDate;
		this.localIpAddress = localIpAddress;
		this.serverIpAddress = serverIpAddress;
		this.clientOutcome = clientOutcome;
		this.serverOutcome = serverOutcome;
	}

	public String getSshId() {
		return sshId;
	}

	public void setSshId(String sshId) {
		this.sshId = sshId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getClientUser() {
		return clientUser;
	}

	public void setClientUser(String clientUser) {
		this.clientUser = clientUser;
	}

	public String getServerUser() {
		return serverUser;
	}

	public void setServerUser(String serverUser) {
		this.serverUser = serverUser;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public String getLocalIpAddress() {
		return localIpAddress;
	}

	public void setLocalIpAddress(String localIpAddress) {
		this.localIpAddress = localIpAddress;
	}

	public String getServerIpAddress() {
		return serverIpAddress;
	}

	public void setServerIpAddress(String serverIpAddress) {
		this.serverIpAddress = serverIpAddress;
	}

	public int getClientOutcome() {
		return clientOutcome;
	}

	public void setClientOutcome(int clientOutcome) {
		this.clientOutcome = clientOutcome;
	}

	public int getServerOutcome() {
		return serverOutcome;
	}

	public void setServerOutcome(int serverOutcome) {
		this.serverOutcome = serverOutcome;
	}

	public Timestamp getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(Timestamp completionDate) {
		this.completionDate = completionDate;
	}

}

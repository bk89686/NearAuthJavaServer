package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class CheckDbObj {
	private String checkId;
	private String instanceId;
	private boolean expired;
	private String peripheralDeviceId;
	private String centralDeviceId;
	private String serviceUuid;
	private String userId;
	private String centralBssid;
	private String centralSsid;
	private String peripheralBssid;
	private String peripheralSsid;
	private boolean completed;
	private int outcome;
	private Timestamp createDate;
	private Timestamp completionDate;
	private boolean verfiedReceipt;
	private CheckType checkType;
	private String centralInstanceId;
	private String peripheralInstanceId;
	private Timestamp expirationDate;

	public CheckDbObj(String checkId, String instanceId, String centralDeviceId, String peripheralDeviceId,
			String serviceUuid, String userId, String centralBssid, String centralSsid, String peripheralBssid,
			String peripheralSsid, boolean expired, boolean completed, Integer outcome, Timestamp createDate,
			Timestamp completionDate, boolean verfiedReceipt, CheckType checkType, String centralInstanceId,
			String peripheralInstanceId, Timestamp expirationDate) {
		super();
		this.checkId = checkId;
		this.instanceId = instanceId;
		this.peripheralDeviceId = peripheralDeviceId;
		this.centralDeviceId = centralDeviceId;
		this.serviceUuid = serviceUuid;
		this.userId = userId;
		this.centralBssid = centralBssid;
		this.centralSsid = centralSsid;
		this.peripheralBssid = peripheralBssid;
		this.peripheralSsid = peripheralSsid;
		this.expired = expired;
		this.completed = completed;
		this.outcome = outcome;
		this.createDate = createDate;
		this.completionDate = completionDate;
		this.verfiedReceipt = verfiedReceipt;
		this.checkType = checkType;
		this.centralInstanceId = centralInstanceId;
		this.peripheralInstanceId = peripheralInstanceId;
		this.expirationDate = expirationDate;
	}

	public CheckDbObj(String checkId, String instanceId, String centralDeviceId, String peripheralDeviceId,
			String serviceUuid, String userId, String centralBssid, String centralSsid, String peripheralBssid,
			String peripheralSsid, boolean expired, boolean completed, Integer outcome, Timestamp createDate,
			Timestamp completionDate, boolean verfiedReceipt, CheckType checkType, String centralInstanceId,
			String peripheralInstanceId) {
		super();
		this.checkId = checkId;
		this.instanceId = instanceId;
		this.peripheralDeviceId = peripheralDeviceId;
		this.centralDeviceId = centralDeviceId;
		this.serviceUuid = serviceUuid;
		this.userId = userId;
		this.centralBssid = centralBssid;
		this.centralSsid = centralSsid;
		this.peripheralBssid = peripheralBssid;
		this.peripheralSsid = peripheralSsid;
		this.expired = expired;
		this.completed = completed;
		this.outcome = outcome;
		this.createDate = createDate;
		this.completionDate = completionDate;
		this.verfiedReceipt = verfiedReceipt;
		this.checkType = checkType;
		this.centralInstanceId = centralInstanceId;
		this.peripheralInstanceId = peripheralInstanceId;
		this.expirationDate = DateTimeUtilities.getCurrentTimestampPlusDays(1);
	}

	public String getCheckId() {
		return checkId;
	}

	public void setCheckId(String checkId) {
		this.checkId = checkId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired, String src) {
		this.expired = expired;
	}

	public String getServiceUuid() {
		return serviceUuid;
	}

	public void setServiceUuid(String serviceUuid) {
		this.serviceUuid = serviceUuid;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPeripheralDeviceId() {
		return peripheralDeviceId;
	}

	public void setPeripheralDeviceId(String peripheralDeviceId) {
		this.peripheralDeviceId = peripheralDeviceId;
	}

	public String getCentralDeviceId() {
		return centralDeviceId;
	}

	public void setCentralDeviceId(String centralDeviceId) {
		this.centralDeviceId = centralDeviceId;
	}

	public String getCentralBssid() {
		return centralBssid;
	}

	public void setCentralBssid(String centralBssid) {
		this.centralBssid = centralBssid;
	}

	public String getCentralSsid() {
		return centralSsid;
	}

	public void setCentralSsid(String centralSsid) {
		this.centralSsid = centralSsid;
	}

	public String getPeripheralBssid() {
		return peripheralBssid;
	}

	public void setPeripheralBssid(String peripheralBssid) {
		this.peripheralBssid = peripheralBssid;
	}

	public String getPeripheralSsid() {
		return peripheralSsid;
	}

	public void setPeripheralSsid(String peripheralSsid) {
		this.peripheralSsid = peripheralSsid;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Integer getOutcome() {
		return outcome;
	}

	public void setOutcome(Integer outcome) {
		this.outcome = outcome;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public Timestamp getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(Timestamp completionDate) {
		this.completionDate = completionDate;
	}

	public boolean isVerfiedReceipt() {
		return verfiedReceipt;
	}

	public void setVerfiedReceipt(boolean verfiedReceipt) {
		this.verfiedReceipt = verfiedReceipt;
	}

	@Override
	public String toString() {
		return "ChecksDbObj [checkId=" + checkId + ", instanceId=" + instanceId + ", expired=" + expired
				+ ", receiverDeviceId=" + peripheralDeviceId + ", senderDeviceId=" + centralDeviceId + ", serviceUuid="
				+ serviceUuid + ", userId=" + userId + ", senderBssid=" + centralBssid + ", senderSsid=" + centralSsid
				+ ", receiverBssid=" + peripheralBssid + ", receiverSsid=" + peripheralSsid + ", completed=" + completed
				+ ", outcome=" + outcome + ", createDate=" + createDate + ", completionDate=" + completionDate
				+ ", verfiedReceipt=" + verfiedReceipt + "]";
	}

	public void setOutcome(int outcome) {
		this.outcome = outcome;
	}

	public CheckType getCheckType() {
		return checkType;
	}

	public void setCheckType(CheckType checkType) {
		this.checkType = checkType;
	}

	public String getCentralInstanceId() {
		return centralInstanceId;
	}

	public void setCentralInstanceId(String centralInstanceId) {
		this.centralInstanceId = centralInstanceId;
	}

	public String getPeripheralInstanceId() {
		return peripheralInstanceId;
	}

	public void setPeripheralInstanceId(String peripheralInstanceId) {
		this.peripheralInstanceId = peripheralInstanceId;
	}

	public Timestamp getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Timestamp expirationDate) {
		this.expirationDate = expirationDate;
	}

}

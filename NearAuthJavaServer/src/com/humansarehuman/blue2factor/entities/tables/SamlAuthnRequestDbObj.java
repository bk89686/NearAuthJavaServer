package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.enums.SignatureStatus;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class SamlAuthnRequestDbObj {
	private String tableId;
	private String identityProviderName;
	private Timestamp createDate;
	private String samlIdentityProviderId;
	private String outgoingRequestId;
	private String incomingRequestId;
	private String outgoingRelayState;
	private String incomingRelayState;
	private Timestamp issueInstance;
	private String issuer;
	private String outgoingAcsUrl;
	private String incomingAcsUrl;
	private SignatureStatus signatureStatus;
	private String encryptedAuthnRequest;
	private String ipAddress;
	private boolean expired;

	public String getOutgoingRelayState() {
		return outgoingRelayState;
	}

	public void setOutgoingRelayState(String outgoingRelayState) {
		this.outgoingRelayState = outgoingRelayState;
	}

	public String getIncomingRelayState() {
		return incomingRelayState;
	}

	public void setIncomingRelayState(String incomingRelayState) {
		this.incomingRelayState = incomingRelayState;
	}

	private String companyId;
	private String groupId;
	private String deviceId;
	private String token;
	private int outcome;
	private String sender;

	public SamlAuthnRequestDbObj(String tableId, String identityProviderName, Timestamp createDate,
			String samlIdentityProviderId, String outgoingRequestId, String incomingRequestId,
			String outgoingRelayState, String incomingRelayState, Timestamp issueInstance, String issuer,
			String outgoingAcsUrl, String incomingAcsUrl, SignatureStatus signatureStatus, String encryptedAuthnRequest,
			String ipAddress, boolean expired, String companyId, String groupId, String deviceId, String token,
			int outcome, String sender) {
		super();
		DataAccess dataAccess = new DataAccess();
		this.tableId = tableId;
		dataAccess.addLog("SamlAuthnRequestDbObj", "tableId: " + tableId);
		this.identityProviderName = identityProviderName;
		this.createDate = createDate;
		this.samlIdentityProviderId = samlIdentityProviderId;
		this.outgoingRequestId = outgoingRequestId;
		this.incomingRequestId = incomingRequestId;
		this.outgoingRelayState = outgoingRelayState;
		this.incomingRelayState = incomingRelayState;
		dataAccess.addLog("SamlAuthnRequestDbObj", "setting out relay state to " + outgoingRelayState);
		this.issueInstance = issueInstance;
		this.issuer = issuer;
		this.setOutgoingAcsUrl(outgoingAcsUrl);
		this.setIncomingAcsUrl(incomingAcsUrl);
		this.signatureStatus = signatureStatus;
		this.encryptedAuthnRequest = encryptedAuthnRequest;
		this.ipAddress = ipAddress;
		this.expired = expired;
		this.companyId = companyId;
		this.groupId = groupId;
		this.deviceId = deviceId;
		this.token = token;
		this.outcome = outcome;
		this.sender = sender;
		dataAccess.addLog("SamlAuthnRequestDbObj", "sender: " + sender);
	}

	public SamlAuthnRequestDbObj(String identityProviderName, Timestamp createDate, String samlIdentityProviderId,
			String outgoingRequestId, String incomingRequestId, String outgoingRelayState, String incomingRelayState,
			Timestamp issueInstance, String issuer, String outgoingAcsUrl, String incomingAcsUrl,
			SignatureStatus signatureStatus, String encryptedAuthnRequest, String ipAddress, boolean expired,
			String companyId, String groupId, String deviceId, String token, int outcome, String sender) {
		super();
		DataAccess dataAccess = new DataAccess();
		this.tableId = GeneralUtilities.randomString();
		dataAccess.addLog("SamlAuthnRequestDbObj", "tableId: " + tableId);
		this.identityProviderName = identityProviderName;
		this.createDate = createDate;
		this.samlIdentityProviderId = samlIdentityProviderId;
		this.outgoingRequestId = outgoingRequestId;
		this.incomingRequestId = incomingRequestId;
		dataAccess.addLog("SamlAuthnRequestDbObj", "incoming id: " + incomingRequestId);
		this.outgoingRelayState = outgoingRelayState;
		this.incomingRelayState = incomingRelayState;
		dataAccess.addLog("SamlAuthnRequestDbObj", "setting out relay state to " + outgoingRelayState);
		this.issueInstance = issueInstance;
		this.issuer = issuer;
		this.setOutgoingAcsUrl(outgoingAcsUrl);
		this.setIncomingAcsUrl(incomingAcsUrl);
		this.signatureStatus = signatureStatus;
		this.encryptedAuthnRequest = encryptedAuthnRequest;
		this.ipAddress = ipAddress;
		this.expired = expired;
		this.companyId = companyId;
		this.groupId = groupId;
		this.deviceId = deviceId;
		this.token = token;
		this.outcome = outcome;
		this.sender = sender;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public int getOutcome() {
		return outcome;
	}

	public void setOutcome(int outcome) {
		this.outcome = outcome;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getIdentityProviderName() {
		return identityProviderName;
	}

	public void setIdentityProviderName(String identityProviderName) {
		this.identityProviderName = identityProviderName;
	}

	public String getSamlIdentityProviderId() {
		return samlIdentityProviderId;
	}

	public void setSamlIdentityProviderId(String samlIdentityProviderId) {
		this.samlIdentityProviderId = samlIdentityProviderId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public SignatureStatus getSignatureStatus() {
		return signatureStatus;
	}

	public void setSignatureStatus(SignatureStatus signatureStatus) {
		this.signatureStatus = signatureStatus;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public String getOutgoingRequestId() {
		return outgoingRequestId;
	}

	public void setOutgoingRequestId(String outgoingRequestId) {
		this.outgoingRequestId = outgoingRequestId;
	}

	public String getIncomingRequestId() {
		return incomingRequestId;
	}

	public void setIncomingRequestId(String incomingRequestId) {
		this.incomingRequestId = incomingRequestId;
	}

	public Timestamp getIssueInstance() {
		return issueInstance;
	}

	public void setIssueInstance(Timestamp issueInstance) {
		this.issueInstance = issueInstance;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getEncryptedAuthnRequest() {
		return encryptedAuthnRequest;
	}

	public void setEncryptedAuthnRequest(String encryptedAuthnRequest) {
		this.encryptedAuthnRequest = encryptedAuthnRequest;
	}

	public String getOutgoingAcsUrl() {
		return outgoingAcsUrl;
	}

	public void setOutgoingAcsUrl(String outgoingAcsUrl) {
		this.outgoingAcsUrl = outgoingAcsUrl;
	}

	public String getIncomingAcsUrl() {
		return incomingAcsUrl;
	}

	public void setIncomingAcsUrl(String incomingAcsUrl) {
		this.incomingAcsUrl = incomingAcsUrl;
	}

}

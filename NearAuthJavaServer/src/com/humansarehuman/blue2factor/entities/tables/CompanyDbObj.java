package com.humansarehuman.blue2factor.entities.tables;

import java.util.Date;

import com.humansarehuman.blue2factor.entities.enums.AuthorizationMethod;
import com.humansarehuman.blue2factor.entities.enums.NonMemberStrategy;

public class CompanyDbObj {

	private String companyId;
	private String companyName;
	private String companySecret;
	private int acceptedTypes;
	private boolean active;
	private Date createDate;
	private String companyLoginToken;
	@SuppressWarnings("unused")
	private String companyCompletionUrl;
	private String companyBaseUrl;
	private String apiKey;
	private int licenseCount;
	private int licensesInUse;
	private String companyLoginUrl;
	private AuthorizationMethod f1Method;
	private AuthorizationMethod f2Method;
	private String logoutUrl;
	private String urlRegex;
	private NonMemberStrategy nonMemberStrategy;
	private boolean pushAllowed;
	private boolean textAllowed;
	private int textTimeoutSeconds;
	private int pushTimeoutSeconds;
	private int passkeyTimeoutSeconds;
	private boolean allowAllFromIdp;
	private boolean moveB2fUsersToIdp;
	private String emailDomain;
	private int adminCodeTimeoutSeconds;
	private String entityIdVal;

	public CompanyDbObj(String companyId, String companyName, String companySecret, int acceptedTypes, boolean active,
			Date createDate, String companyLoginToken, String companyCompletionUrl, String apiKey, int licenseCount,
			int licensesInUse, String companyBaseUrl, String companyLoginUrl, AuthorizationMethod f1Method,
			AuthorizationMethod f2Method, String logoutUrl, String urlRegex, NonMemberStrategy nonMemberStrategy,
			boolean pushAllowed, boolean textAllowed, int textTimeoutSeconds, int pushTimeoutSeconds,
			int passkeyTimeoutSeconds, boolean allowAllFromIdp, boolean moveB2fUsersToIdp, String emailDomain,
			int adminCodeTimeoutSeconds, String entityIdVal) {
		super();
		this.companyId = companyId;
		this.companyName = companyName;
		this.companySecret = companySecret;
		this.acceptedTypes = acceptedTypes;
		this.active = active;
		this.createDate = createDate;
		this.companyLoginToken = companyLoginToken;
		this.companyCompletionUrl = companyCompletionUrl;
		this.apiKey = apiKey;
		this.licenseCount = licenseCount;
		this.licensesInUse = licensesInUse;
		this.companyBaseUrl = companyBaseUrl;
		this.companyLoginUrl = companyLoginUrl;
		this.f1Method = f1Method;
		this.f2Method = f2Method;
		this.logoutUrl = logoutUrl;
		this.urlRegex = urlRegex;
		this.nonMemberStrategy = nonMemberStrategy;
		this.pushAllowed = pushAllowed;
		this.textAllowed = textAllowed;
		this.pushTimeoutSeconds = pushTimeoutSeconds;
		this.textTimeoutSeconds = textTimeoutSeconds;
		this.passkeyTimeoutSeconds = passkeyTimeoutSeconds;
		this.allowAllFromIdp = allowAllFromIdp;
		this.moveB2fUsersToIdp = moveB2fUsersToIdp;
		this.emailDomain = emailDomain;
		this.adminCodeTimeoutSeconds = adminCodeTimeoutSeconds;
		this.entityIdVal = entityIdVal;
	}

	public String getEntityIdVal() {
		return entityIdVal;
	}

	public void setEntityIdVal(String entityIdVal) {
		this.entityIdVal = entityIdVal;
	}

	public int getAdminCodeTimeoutSeconds() {
		return adminCodeTimeoutSeconds;
	}

	public void setAdminCodeTimeoutSeconds(int adminCodeTimeoutSeconds) {
		this.adminCodeTimeoutSeconds = adminCodeTimeoutSeconds;
	}

	public String getEmailDomain() {
		return emailDomain;
	}

	public void setEmailDomain(String emailDomain) {
		this.emailDomain = emailDomain;
	}

	public boolean isAllowAllFromIdp() {
		return allowAllFromIdp;
	}

	public void setAllowAllFromIdp(boolean allowAllFromIdp) {
		this.allowAllFromIdp = allowAllFromIdp;
	}

	public boolean isMoveB2fUsersToIdp() {
		return moveB2fUsersToIdp;
	}

	public void setMoveB2fUsersToIdp(boolean moveB2fUsersToIdp) {
		this.moveB2fUsersToIdp = moveB2fUsersToIdp;
	}

	public int getPasskeyTimeoutSeconds() {
		return passkeyTimeoutSeconds;
	}

	public void setPasskeyTimeoutSeconds(int passkeyTimeoutSeconds) {
		this.passkeyTimeoutSeconds = passkeyTimeoutSeconds;
	}

	public int getTextTimeoutSeconds() {
		return textTimeoutSeconds;
	}

	public void setTextTimeoutSeconds(int textTimeoutSeconds) {
		this.textTimeoutSeconds = textTimeoutSeconds;
	}

	public int getPushTimeoutSeconds() {
		return pushTimeoutSeconds;
	}

	public void setPushTimeoutSeconds(int pushTimeoutSeconds) {
		this.pushTimeoutSeconds = pushTimeoutSeconds;
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

	public NonMemberStrategy getNonMemberStrategy() {
		return nonMemberStrategy;
	}

	public void setNonMemberStrategy(NonMemberStrategy nonMemberStrategy) {
		this.nonMemberStrategy = nonMemberStrategy;
	}

	public String getUrlRegex() {
		return urlRegex;
	}

	public void setUrlRegex(String urlRegex) {
		this.urlRegex = urlRegex;
	}

	public AuthorizationMethod getF2Method() {
		return f2Method;
	}

	public void setF2Method(AuthorizationMethod f2Method) {
		this.f2Method = f2Method;
	}

	public AuthorizationMethod getF1Method() {
		return f1Method;
	}

	public void setF1Method(AuthorizationMethod f1Method) {
		this.f1Method = f1Method;
	}

	public String getCompanyLoginUrl() {
		return companyLoginUrl;
	}

	public String getCompleteCompanyLoginUrl() {
		return "https://" + companyBaseUrl + companyLoginUrl;
	}
	
	public String getCompanyLoginWithoutProtocol() {
		return companyBaseUrl + companyLoginUrl;
	}

	public void setCompanyLoginUrl(String companyLoginUrl) {
		this.companyLoginUrl = companyLoginUrl;
	}

	public String getCompanyBaseUrl() {
		return companyBaseUrl;
	}

	public void setCompanyBaseUrl(String companyBaseUrl) {
		this.companyBaseUrl = companyBaseUrl;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanySecret() {
		return companySecret;
	}

	public void setCompanySecret(String companySecret) {
		this.companySecret = companySecret;
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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Override
	public String toString() {
		return "CompanyDbObj [companyId=" + companyId + ", companyName=" + companyName + ", companySecret="
				+ companySecret + ", acceptedTypes=" + acceptedTypes + ", active=" + active + ", createDate="
				+ createDate + "]";
	}

	public String getCompanyLoginToken() {
		return companyLoginToken;
	}

	public void setCompanyLoginToken(String companyLoginToken) {
		this.companyLoginToken = companyLoginToken;
	}

	public String getCompanyCompletionUrl() {
		return "https://" + companyBaseUrl + "/" + companyLoginUrl;
//		return companyCompletionUrl;
	}

	public void setCompanyCompletionUrl(String companyCompletionUrl) {
		this.companyCompletionUrl = companyCompletionUrl;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public int getLicenseCount() {
		return licenseCount;
	}

	public void setLicenseCount(int licenseCount) {
		this.licenseCount = licenseCount;
	}

	public int getLicensesInUse() {
		return licensesInUse;
	}

	public void setLicensesInUse(int licensesInUse) {
		this.licensesInUse = licensesInUse;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public boolean equals(CompanyDbObj company2) {
		if (company2 == null) {
			return false;
		}
		return (this.companyId.equals(company2.companyId) && this.apiKey.equals(company2.apiKey));
	}

}

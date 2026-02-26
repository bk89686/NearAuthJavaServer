package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class LdapServerDbObj {
	String tableId;
	String companyId;
	Timestamp createDate;
	boolean active;
	String providerUrl;
	String searchBase;
	String jksFile;
	String jksPassword;
	String serviceUsername;
	String servicePassword;

	public LdapServerDbObj(String tableId, String companyId, Timestamp createDate, boolean active, String providerUrl,
			String searchBase, String jksFile, String jksPassword, String serviceUsername, String servicePassword) {
		super();
		this.tableId = tableId;
		this.companyId = companyId;
		this.createDate = createDate;
		this.active = active;
		this.providerUrl = providerUrl;
		this.searchBase = searchBase;
		this.jksFile = jksFile;
		this.jksPassword = jksPassword;
		this.serviceUsername = serviceUsername;
		this.servicePassword = servicePassword;
	}

//INSERT INTO B2F_LDAP_SERVER ('7E8C14C9-5F4A-4781-A208-9EB8393DDD2F', 'b2fd0fbd-2334-42a7-bfeb-d2760e2f8af7', '2022-09-15 18:30:06.662', true, 'ldaps://ldap.google.com:636', 'ou=Blue2Factor,ou=Users,dc=blue2factor,dc=com', '/var/java-application-ldap.jks', 'jT32J%d&22p');
	public LdapServerDbObj(String companyId, boolean active, String providerUrl, String searchBase, String jksFile,
			String jksPassword) {
		super();
		this.tableId = GeneralUtilities.randomString();
		this.companyId = companyId;
		this.createDate = DateTimeUtilities.getCurrentTimestamp();
		this.active = active;
		this.providerUrl = providerUrl;
		this.searchBase = searchBase;
		this.jksFile = jksFile;
		this.jksPassword = jksPassword;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getProviderUrl() {
		return providerUrl;
	}

	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}

	public String getSearchBase() {
		return searchBase;
	}

	public void setSearchBase(String searchBase) {
		this.searchBase = searchBase;
	}

	public String getJksFile() {
		return jksFile;
	}

	public void setJksFile(String jksFile) {
		this.jksFile = jksFile;
	}

	public String getJksPassword() {
		return jksPassword;
	}

	public void setJksPassword(String jksPassword) {
		this.jksPassword = jksPassword;
	}

	public String getServiceUsername() {
		return serviceUsername;
	}

	public void setServiceUsername(String serviceUsername) {
		this.serviceUsername = serviceUsername;
	}

	public String getServicePassword() {
		return servicePassword;
	}

	public void setServicePassword(String servicePassword) {
		this.servicePassword = servicePassword;
	}

}

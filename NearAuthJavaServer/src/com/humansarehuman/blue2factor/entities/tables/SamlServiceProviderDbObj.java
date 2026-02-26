package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;

public class SamlServiceProviderDbObj {
	private String tableId;
	private Timestamp createDate;
	private String serviceProviderEntityId;
	private String serviceProviderIssuer;
	private String serviceProviderName;
	private String serviceProviderUrl;
	private String acsUrl;
	private String serviceProviderMetadataUrl;
	private boolean active;
	private String referencingCompanyId;
	private String logoutUrl;
	private String serviceProviderMetadata;
	private String spEncryptionCert;

	public SamlServiceProviderDbObj(String tableId, Timestamp createDate, String serviceProviderEntityId,
			String serviceProviderIssuer, String serviceProviderName, String serviceProviderUrl,
			String serviceProviderMetadataUrl, boolean active, String referencingCompanyId, String acsUrl,
			String logoutUrl, String serviceProviderMetadata, String spEncryptionCert) {
		super();
		this.tableId = tableId;
		this.createDate = createDate;
		this.serviceProviderEntityId = serviceProviderEntityId;
		this.serviceProviderIssuer = serviceProviderIssuer;
		this.serviceProviderName = serviceProviderName;
		this.serviceProviderUrl = serviceProviderUrl;
		this.serviceProviderMetadataUrl = serviceProviderMetadataUrl;
		this.acsUrl = acsUrl;
		this.active = active;
		this.referencingCompanyId = referencingCompanyId;
		this.logoutUrl = logoutUrl;
		this.serviceProviderMetadata = serviceProviderMetadata;
		this.spEncryptionCert = spEncryptionCert;
		new DataAccess().addLog("looks good");
	}

	public String getSpEncryptionCert() {
		return spEncryptionCert;
	}

	public void setSpEncryptionCert(String spEncryptionCert) {
		this.spEncryptionCert = spEncryptionCert;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public String getServiceProviderMetadata() {
		return serviceProviderMetadata;
	}

	public void setServiceProviderMetadata(String serviceProviderMetadata) {
		this.serviceProviderMetadata = serviceProviderMetadata;
	}

	public String getAcsUrl() {
		return acsUrl;
	}

	public void setAcsUrl(String acsUrl) {
		this.acsUrl = acsUrl;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getReferencingCompanyId() {
		return referencingCompanyId;
	}

	public void setReferencingCompanyId(String referencingCompanyId) {
		this.referencingCompanyId = referencingCompanyId;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public String getServiceProviderIssuer() {
		return serviceProviderIssuer;
	}

	public void setServiceProviderIssuer(String serviceProviderIssuer) {
		this.serviceProviderIssuer = serviceProviderIssuer;
	}

//    public String getServiceProvider509Cert() {
//        return serviceProvider509Cert;
//    }
//
//    public void setServiceProvider509Cert(String serviceProvider509Cert) {
//        this.serviceProvider509Cert = serviceProvider509Cert;
//    }
//
//    public String getServiceProviderPrivateKey() {
//        return serviceProviderPrivateKey;
//    }
//
//    public void setServiceProviderPrivateKey(String serviceProviderPrivateKey) {
//        this.serviceProviderPrivateKey = serviceProviderPrivateKey;
//    }

	public String getServiceProviderMetadataUrl() {
		return serviceProviderMetadataUrl;
	}

	public void setServiceProviderMetadataUrl(String serviceProviderMetadataUrl) {
		this.serviceProviderMetadataUrl = serviceProviderMetadataUrl;
	}

	public String getServiceProviderEntityId() {
		return serviceProviderEntityId;
	}

	public void setServiceProviderEntityId(String serviceProviderEntityId) {
		this.serviceProviderEntityId = serviceProviderEntityId;
	}

	public String getServiceProviderName() {
		return serviceProviderName;
	}

	public void setServiceProviderName(String serviceProviderName) {
		this.serviceProviderName = serviceProviderName;
	}

	public String getServiceProviderUrl() {
		return serviceProviderUrl;
	}

	public void setServiceProviderUrl(String serviceProviderUrl) {
		this.serviceProviderUrl = serviceProviderUrl;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}

package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class SamlIdentityProviderDbObj {
    private String tableId;
    private Timestamp createDate;
    private String entityId;
    private String identityProviderName;
    private String signingCertificate;
    private String encryptingCertificate;
    private String nameIdFormat;
    private String redirectUrl;
    private String postUrl;
    private String artifactUrl;
    private boolean active;
    private String referencingCompany;
    private boolean encryptRequest;
    private boolean signRequest;
    private String acsUrl;
    private String destinationUrl;
    private String redirectBinding;
    private String postBinding;
    private String artifactBinding;
    private Timestamp validUntil;
    private String logoutUrl;

    public SamlIdentityProviderDbObj() {
        tableId = GeneralUtilities.randomString();
        createDate = DateTimeUtilities.getCurrentTimestamp();
    }

    public SamlIdentityProviderDbObj(String tableId, Timestamp createDate, String entityId,
            String identityProviderName, String signingCertificate, String encryptingCertificate,
            String nameIdFormat, String redirectUrl, String postUrl, String artifactUrl,
            boolean active, String referencingCompany, boolean encryptRequest, boolean signRequest,
            String acsUrl, String destinationUrl, String redirectBinding, String postBinding,
            String artifactBinding, Timestamp validUntil, String logoutUrl) {
        super();
        this.tableId = tableId;
        this.createDate = createDate;
        this.entityId = entityId;
        this.identityProviderName = identityProviderName;
        this.signingCertificate = signingCertificate;
        this.encryptingCertificate = encryptingCertificate;
        this.nameIdFormat = nameIdFormat;
        this.redirectUrl = redirectUrl;
        this.postUrl = postUrl;
        this.artifactUrl = artifactUrl;
        this.active = active;
        this.referencingCompany = referencingCompany;
        this.encryptRequest = encryptRequest;
        this.signRequest = signRequest;
        this.acsUrl = acsUrl;
        this.destinationUrl = destinationUrl;
        this.redirectBinding = redirectBinding;
        this.postBinding = postBinding;
        this.artifactBinding = artifactBinding;
        this.validUntil = validUntil;
        this.logoutUrl = logoutUrl;
    }

    public Timestamp getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Timestamp validUntil) {
        this.validUntil = validUntil;
    }

    public String getArtifactUrl() {
        return artifactUrl;
    }

    public void setArtifactUrl(String artifactUrl) {
        this.artifactUrl = artifactUrl;
    }

    public String getRedirectBinding() {
        return redirectBinding;
    }

    public void setRedirectBinding(String redirectBinding) {
        this.redirectBinding = redirectBinding;
    }

    public String getPostBinding() {
        return postBinding;
    }

    public void setPostBinding(String postBinding) {
        this.postBinding = postBinding;
    }

    public String getArtifactBinding() {
        return artifactBinding;
    }

    public void setArtifactBinding(String artifactBinding) {
        this.artifactBinding = artifactBinding;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public boolean isEncryptRequest() {
        return encryptRequest;
    }

    public void setEncryptRequest(boolean encryptRequest) {
        this.encryptRequest = encryptRequest;
    }

    public boolean isSignRequest() {
        return signRequest;
    }

    public void setSignRequest(boolean signRequest) {
        this.signRequest = signRequest;
    }

    public String getAcsUrl() {
        return acsUrl;
    }

    public void setAcsUrl(String acsUrl) {
        this.acsUrl = acsUrl;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }

    public void setDestinationUrl(String destinationUrl) {
        this.destinationUrl = destinationUrl;
    }

    public String getReferencingCompany() {
        return referencingCompany;
    }

    public void setReferencingCompany(String referencingCompany) {
        this.referencingCompany = referencingCompany;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getIdentityProviderName() {
        return identityProviderName;
    }

    public void setIdentityProviderName(String identityProviderName) {
        this.identityProviderName = identityProviderName;
    }

    public String getSigningCertificate() {
        return signingCertificate;
    }

    public void setSigningCertificate(String signingCertificate) {
        this.signingCertificate = signingCertificate;
    }

    public String getEncryptingCertificate() {
        return encryptingCertificate;
    }

    public void setEncryptingCertificate(String encryptingCertificate) {
        this.encryptingCertificate = encryptingCertificate;
    }

    public String getNameIdFormat() {
        return nameIdFormat;
    }

    public void setNameIdFormat(String nameIdFormat) {
        this.nameIdFormat = nameIdFormat;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

}

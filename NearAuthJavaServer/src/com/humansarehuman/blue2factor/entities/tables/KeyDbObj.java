package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class KeyDbObj {
	private String keyId;
	private String deviceId;
	private String browserId;
	private String companyId;
	private Timestamp createDate;
	private boolean active;
	private KeyType keyType;
	private String keyText;
	private boolean publicKey;
	private String algorithm;
	private String url;
	private Timestamp expireDate;
	private String groupId;

	public KeyDbObj(String deviceId, String browserId, String groupId, String companyId, KeyType keyType,
			String keyText, boolean publicKey, String algorithm, String url) {
		super();
		this.keyId = GeneralUtilities.randomString();
		this.deviceId = deviceId;
		this.browserId = browserId;
		this.setGroupId(groupId);
		this.companyId = companyId;
		this.createDate = DateTimeUtilities.getCurrentTimestamp();
		this.active = true;
		this.keyType = keyType;
		this.keyText = keyText;
		this.publicKey = publicKey;
		this.algorithm = algorithm;
		this.url = url;
		this.expireDate = DateTimeUtilities.getCurrentTimestampPlusDays(3652);
	}

	public KeyDbObj(String keyId, String deviceId, String browserId, String groupId, String companyId, KeyType keyType,
			String keyText, boolean publicKey, String algorithm, String url) {
		super();
		this.keyId = keyId;
		this.deviceId = deviceId;
		this.browserId = browserId;
		this.setGroupId(groupId);
		this.companyId = companyId;
		this.createDate = DateTimeUtilities.getCurrentTimestamp();
		this.active = true;
		this.keyType = keyType;
		this.keyText = keyText;
		this.publicKey = publicKey;
		this.algorithm = algorithm;
		this.url = url;
		this.expireDate = DateTimeUtilities.getCurrentTimestampPlusDays(3652);
	}

//    KeyDbObj(keyId, deviceId, browserId, companyId, createDate, active, keyType,
//            keyText, publicKey, algorithm, url, expireDate);
	public KeyDbObj(String keyId, String deviceId, String browserId, String groupId, String companyId, KeyType keyType,
			String keyText, boolean publicKey, String algorithm, String url, Timestamp expireDate) {
		super();
		this.keyId = keyId;
		this.deviceId = deviceId;
		this.browserId = browserId;
		this.setGroupId(groupId);
		this.companyId = companyId;
		this.createDate = DateTimeUtilities.getCurrentTimestamp();
		this.active = true;
		this.keyType = keyType;
		this.keyText = keyText;
		this.publicKey = publicKey;
		this.algorithm = algorithm;
		this.url = url;
		this.expireDate = expireDate;
	}

	public KeyDbObj(String keyId, String deviceId, String browserId, String groupId, String companyId,
			Timestamp createDate, boolean active, KeyType keyType, String keyText, boolean publicKey, String algorithm,
			String url, Timestamp expireDate) {
		super();
		this.keyId = keyId;
		this.deviceId = deviceId;
		this.browserId = browserId;
		this.setGroupId(groupId);
		this.companyId = companyId;
		this.createDate = createDate;
		this.active = active;
		this.keyType = keyType;
		this.keyText = keyText;
		this.publicKey = publicKey;
		this.algorithm = algorithm;
		this.url = url;
		this.expireDate = expireDate;
	}

	public Timestamp getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Timestamp expireDate) {
		this.expireDate = expireDate;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getBrowserId() {
		return browserId;
	}

	public void setBrowserId(String browserId) {
		this.browserId = browserId;
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
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

	public KeyType getKeyType() {
		return keyType;
	}

	public void setKeyType(KeyType keyType) {
		this.keyType = keyType;
	}

	public String getKeyText() {
		return keyText;
	}

	public void setKeyText(String keyText) {
		this.keyText = keyText;
	}

	public boolean isPublicKey() {
		return publicKey;
	}

	public void setPublicKey(boolean publicKey) {
		this.publicKey = publicKey;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

}

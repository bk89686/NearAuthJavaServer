package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;
import java.util.Date;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.enums.BluetoothType;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.OsClass;

public class DeviceDbObj {
	private String groupId;
	private String userId;
	private String deviceId;
	private int seed;
	private boolean active;
	private String fcmId;
	private String btAddress;
	private Date createDate;
	private Timestamp lastUpdate;
	private String deviceType;
	private OsClass operatingSystem;
	private String loginToken;
	private Date lastCompleteCheck;
	private Integer lastGmtOffset;
	private String osVersion;
	private String userLanguage;
	private String screenSize;
	private String rand;
	private Boolean showIcon;
	private Double devicePriority;
	private Boolean triggerUpdate;
	private int recentPushes;
	private Boolean unresponsive;
	private Timestamp lastPush;
	private boolean pushLoud;
	private boolean pushFailure;
	private String command;
	private String temp;
	private boolean central;
	private Timestamp lastReset;
	private boolean screensaverOn;
	private Timestamp lastVariableRetrieval;
	private boolean turnedOff;
	private DeviceClass deviceClass;
	private Boolean browserInstallComplete;
	private Boolean signedIn;
	private Timestamp lastSilentPush;
	private Timestamp lastSilentPushResponse;
	private Boolean hasBle;
	private Boolean turnOffFromInstaller;
	private String phoneNumber;
	private boolean multiUser;
	private boolean passkeyEnabled;
	private Integer txPower;
	private boolean terminate;

	public DeviceDbObj() {
	}

	public DeviceDbObj(String groupId, String userId, String deviceId, int seed, boolean active, String fcmId,
			String btAddress, Date createDate, Timestamp lastUpdate, String deviceType, OsClass operatingSystem,
			String loginToken, Date lastCompleteCheck, Integer lastGmtOffset, String osVersion, String userLanguage,
			String screenSize, String rand, Boolean showIcon, Double devicePriority, Boolean triggerUpdate,
			int recentPushes, Boolean unresponsive, Timestamp lastPush, boolean pushLoud, boolean pushFailure,
			String command, String temp, boolean central, Timestamp lastReset, boolean screensaverOn,
			Timestamp lastVariableRetrieval, boolean turnedOff, DeviceClass deviceClass, Boolean browserInstallComplete,
			Boolean signedIn, Timestamp lastSilentPush, Timestamp lastSilentPushResponse, Boolean hasBle,
			Boolean turnOffFromInstaller, String phoneNumber, boolean multiUser, boolean passkeyEnabled,
			Integer txPower, boolean terminate) {

		super();
		this.groupId = groupId;
		this.userId = userId;
		this.deviceId = deviceId;
		this.seed = seed;
		this.active = active;
		this.fcmId = fcmId;
		this.btAddress = btAddress;
		this.createDate = createDate;
		this.lastUpdate = lastUpdate;
		this.deviceType = deviceType;
		this.operatingSystem = operatingSystem;
		this.loginToken = loginToken;
		this.lastCompleteCheck = lastCompleteCheck;
		this.lastGmtOffset = lastGmtOffset;
		this.osVersion = osVersion;
		this.userLanguage = userLanguage;
		this.screenSize = screenSize;
		this.rand = rand;
		this.showIcon = showIcon;
		this.devicePriority = devicePriority;
		this.triggerUpdate = triggerUpdate;
		this.recentPushes = recentPushes;
		this.unresponsive = unresponsive;
		this.lastPush = lastPush;
		this.pushLoud = pushLoud;
		this.pushFailure = pushFailure;
		this.command = command;
		this.temp = temp;
		new DataAccess().addLog("DeviceDbObj", "central: " + central);
		this.central = central;
		this.lastReset = lastReset;
		this.screensaverOn = screensaverOn;
		this.lastVariableRetrieval = lastVariableRetrieval;
		this.turnedOff = turnedOff;
		this.deviceClass = deviceClass;
		this.browserInstallComplete = browserInstallComplete;
		this.signedIn = signedIn;
		this.lastSilentPush = lastSilentPush;
		this.lastSilentPushResponse = lastSilentPushResponse;
		this.hasBle = hasBle;
		this.turnOffFromInstaller = turnOffFromInstaller;
		this.phoneNumber = phoneNumber;
		this.multiUser = multiUser;
		this.passkeyEnabled = passkeyEnabled;
		this.txPower = txPower;
		this.terminate = terminate;
	}

	public boolean getTerminate() {
		return terminate;
	}

	public void setTerminate(boolean terminate) {
		this.terminate = terminate;
	}

	public Integer getTxPower() {
		if (txPower == null) {
			txPower = 127;
		}
		return txPower;
	}

	public boolean isPeripheralMobile() {
		boolean mobilePerf = false;
		if (!isCentral()) {
			mobilePerf = deviceClass.equals(DeviceClass.PHONE) || deviceClass.equals(DeviceClass.TABLET);
		}
		return mobilePerf;
	}

	public void setTxPower(Integer txPower) {
		this.txPower = txPower;
	}

	public Timestamp getLastSilentPushResponse() {
		return lastSilentPushResponse;
	}

	public void setLastSilentPushResponse(Timestamp lastSilentPushResponse) {
		this.lastSilentPushResponse = lastSilentPushResponse;
	}

	public boolean isMultiUser() {
		return multiUser;
	}

	public void setMultiUser(boolean multiUser) {
		this.multiUser = multiUser;
	}

	public boolean isPasskeyEnabled() {
		return passkeyEnabled;
	}

	public void setPasskeyEnabled(boolean passkeyEnabled) {
		this.passkeyEnabled = passkeyEnabled;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Boolean getTurnOffFromInstaller() {
		return turnOffFromInstaller;
	}

	public void setTurnOffFromInstaller(Boolean turnOffFromInstaller) {
		this.turnOffFromInstaller = turnOffFromInstaller;
	}

	public Boolean getBrowserInstallComplete() {
		return browserInstallComplete;
	}

	public Boolean getHasBle() {
		return hasBle;
	}

	public void setBrowserInstallComplete(Boolean browserInstallComplete) {
		this.browserInstallComplete = browserInstallComplete;
	}

	public void setHasBle(Boolean hasBle) {
		this.hasBle = hasBle;
	}

	public Timestamp getLastSilentPush() {
		return lastSilentPush;
	}

	public void setLastSilentPush(Timestamp lastSilentPush) {
		this.lastSilentPush = lastSilentPush;
	}

	public Boolean isBrowserInstallComplete() {
		return browserInstallComplete;
	}

	public void setBrowserInstallComplete(boolean browserInstallComplete) {
		this.browserInstallComplete = browserInstallComplete;
	}

	public DeviceClass getDeviceClass() {
		return deviceClass;
	}

	public void setDeviceClass(DeviceClass deviceClass) {
		this.deviceClass = deviceClass;
	}

	public Boolean getUnresponsive() {
		return unresponsive;
	}

	public void setUnresponsive(Boolean unresponsive) {
		this.unresponsive = unresponsive;
	}

	public boolean isTurnedOff() {
		return turnedOff;
	}

	public void setTurnedOff(boolean turnedOff) {
		this.turnedOff = turnedOff;
	}

	public Timestamp getLastVariableRetrieval() {
		return lastVariableRetrieval;
	}

	public void setLastVariableRetrieval(Timestamp lastVariableRetrieval) {
		this.lastVariableRetrieval = lastVariableRetrieval;
	}

	public boolean isScreensaverOn() {
		return screensaverOn;
	}

	public void setScreensaverOn(boolean screensaverOn) {
		this.screensaverOn = screensaverOn;
	}

	public Timestamp getLastReset() {
		return lastReset;
	}

	public void setLastReset(Timestamp lastReset) {
		this.lastReset = lastReset;
	}

	public boolean isCentral() {
		return central;
	}

	public void setCentral(boolean central) {
		new DataAccess().addLog("DeviceDbObj", "setting central: " + central);
		this.central = central;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public boolean isPushLoud() {
		return pushLoud;
	}

	public void setPushLoud(boolean pushLoud) {
		this.pushLoud = pushLoud;
	}

	public Timestamp getLastPush() {
		return lastPush;
	}

	public void setLastPush(Timestamp lastPush) {
		this.lastPush = lastPush;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getFcmId() {
		return fcmId;
	}

	public void setFcmId(String fcmId) {
		if (fcmId != null && !fcmId.equals("")) {
			// new DataAccess().addLog(this.deviceId, "setFcmId", "setting fcmId to " +
			// fcmId,
			// Constants.LOG_TRACE);
			this.fcmId = fcmId;
		}
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public OsClass getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(OsClass operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

	public String getBtAddress() {
		return btAddress;
	}

	public void setBtAddress(String btIp) {
		this.btAddress = btIp;
	}

	public Date getLastCompleteCheck() {
		return lastCompleteCheck;
	}

	public void setLastCompleteCheck(Date lastCompleteCheck) {
		this.lastCompleteCheck = lastCompleteCheck;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getUserLanguage() {
		return userLanguage;
	}

	public void setUserLanguage(String userLanguage) {
		this.userLanguage = userLanguage;
	}

	public Integer getLastGmtOffset() {
		return lastGmtOffset;
	}

	public void setLastGmtOffset(Integer lastGmtOffset) {
		this.lastGmtOffset = lastGmtOffset;
	}

	public String getScreenSize() {
		return screenSize;
	}

	public void setScreenSize(String screenSize) {
		this.screenSize = screenSize;
	}

	public String getRand() {
		return rand;
	}

	public void setRand(String rand) {
		this.rand = rand;
	}

	@Override
	public String toString() {
		return "DeviceDbObj [groupId=" + groupId + ", userId=" + userId + ", deviceId=" + deviceId + ", seed=" + seed
				+ ", active=" + active + ", fcmId=" + fcmId + ", btAddress=" + btAddress + ", createDate=" + createDate
				+ ", deviceType=" + deviceType + ", operatingSystem=" + operatingSystem + ", loginToken=" + loginToken
				+ ", lastCompleteCheck=" + lastCompleteCheck + ", lastGmtOffset=" + lastGmtOffset + ", osVersion="
				+ osVersion + ", userLanguage=" + userLanguage + ", screenSize=" + screenSize + ", rand=" + rand
				+ ", central=" + central + "]";
	}

	public Boolean getShowIcon() {
		return showIcon;
	}

	public void setShowIcon(Boolean showIcon) {
		this.showIcon = showIcon;
	}

	public Double getDevicePriority() {
		return devicePriority;
	}

	public void setDevicePriority(Double devicePriority) {
		this.devicePriority = devicePriority;
	}

	public Boolean getTriggerUpdate() {
		return triggerUpdate;
	}

	public void setTriggerUpdate(Boolean triggerUpdate) {
		this.triggerUpdate = triggerUpdate;
	}

	public int getRecentPushes() {
		return recentPushes;
	}

	public void setRecentPushes(int recentPushes) {
		this.recentPushes = recentPushes;
	}

	public boolean isPushFailure() {
		return pushFailure;
	}

	public void setPushFailure(boolean pushFailure) {
		this.pushFailure = pushFailure;
	}

	public BluetoothType getBluetoothType() {
		BluetoothType bluetoothType;
		if (central) {
			bluetoothType = BluetoothType.CENTRAL;
		} else {
			bluetoothType = BluetoothType.PERIPHERAL;
		}
		return bluetoothType;
	}

	public Boolean getSignedIn() {
		return signedIn;
	}

	public void setSignedIn(Boolean signedIn) {
		this.signedIn = signedIn;
	}

	public boolean equals(DeviceDbObj otherDevice) {
		boolean equal = false;
		if (otherDevice != null) {
			equal = otherDevice.getDeviceId().equals(deviceId);
		}
		return equal;
	}

}

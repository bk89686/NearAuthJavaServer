package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;

public class DeviceConnectionDbObj {
	private Timestamp createDate;
	private String connectionId;
	private String peripheralDeviceId;
	private String centralDeviceId;
	private String serviceUuid;
	private String characteristicUuid;
	private String groupId;
	private boolean active;
	private Timestamp lastCheck;
	private Timestamp lastSuccess;
	private Boolean peripheralConnected;
	private Timestamp lastPeripheralConnectionSuccess;
	private Boolean centralConnected;
	Timestamp lastCentralConnectionSuccess;
	private Timestamp advertisingPeripheral;
	private Timestamp connectingCentral;
	private boolean subscribed;
	private Timestamp lastSubscribed;
	private boolean installComplete;
	private String centralOsId;
	private Integer peripheralRssi;
	private Integer centralRssi;
	private Timestamp peripheralRssiTimestamp;
	private Timestamp centralRssiTimestamp;
	private Boolean needsPairing;
	private String peripheralIdentifier;

	public DeviceConnectionDbObj(Timestamp createDate, String connectionId, String peripheralDeviceId,
			String centralDeviceId, String serviceUuid, String characteristicUuid, String groupId, boolean active,
			Timestamp lastCheck, Timestamp lastSuccess, Boolean peripheralConnected,
			Timestamp lastPeripheralConnectionSuccess, Boolean centralConnected, Timestamp lastCentralConnectionSuccess,
			Timestamp advertisingPeripheral, Timestamp connectingCentral, boolean subscribed, Timestamp lastSubscribed,
			boolean installComplete, String centralOsId, Integer peripheralRssi, Integer centralRssi,
			Timestamp peripheralRssiTimestamp, Timestamp centralRssiTimestamp, boolean needsPairing,
			String peripheralIdentifier) {
		super();
		this.createDate = createDate;
		this.connectionId = connectionId;
		this.peripheralDeviceId = peripheralDeviceId;
		this.centralDeviceId = centralDeviceId;
		this.serviceUuid = serviceUuid;
		this.characteristicUuid = characteristicUuid;
		this.groupId = groupId;
		this.active = active;
		this.lastCheck = lastCheck;
		this.lastSuccess = lastSuccess;
		this.peripheralConnected = peripheralConnected;
		this.lastPeripheralConnectionSuccess = lastPeripheralConnectionSuccess;
		this.centralConnected = centralConnected;
		this.lastCentralConnectionSuccess = lastCentralConnectionSuccess;
		this.advertisingPeripheral = advertisingPeripheral;
		this.connectingCentral = connectingCentral;
		this.subscribed = subscribed;
		this.lastSubscribed = lastSubscribed;
		this.installComplete = installComplete;
		this.centralOsId = centralOsId;
		this.peripheralRssi = peripheralRssi;
		this.centralRssi = centralRssi;
		this.peripheralRssiTimestamp = peripheralRssiTimestamp;
		this.centralRssiTimestamp = centralRssiTimestamp;
		this.needsPairing = needsPairing;
		this.peripheralIdentifier = peripheralIdentifier;

		new DataAccess().addLog("DeviceConnectionDbObj", this.toString());
	}

	public String getPeripheralIdentifier() {
		return peripheralIdentifier;
	}

	public void setPeripheralIdentifier(String peripheralIdentifier) {
		this.peripheralIdentifier = peripheralIdentifier;
	}

	public Boolean getNeedsPairing() {
		return needsPairing;
	}

	public void setNeedsPairing(Boolean needsPairing) {
		this.needsPairing = needsPairing;
	}

	public Timestamp getPeripheralRssiTimestamp() {
		return peripheralRssiTimestamp;
	}

	public void setPeripheralRssiTimestamp(Timestamp peripheralRssiTimestamp) {
		this.peripheralRssiTimestamp = peripheralRssiTimestamp;
	}

	public Timestamp getCentralRssiTimestamp() {
		return centralRssiTimestamp;
	}

	public void setCentralRssiTimestamp(Timestamp centralRssiTimestamp) {
		this.centralRssiTimestamp = centralRssiTimestamp;
	}

	public Integer getPeripheralRssi() {
		if (peripheralRssi == null) {
			peripheralRssi = 127;
		}
		return peripheralRssi;
	}

	public void setPeripheralRssi(Integer peripheralRssi) {
		this.peripheralRssi = peripheralRssi;
	}

	public Integer getCentralRssi() {
		if (centralRssi == null) {
			centralRssi = 127;
		}
		return centralRssi;
	}

	public void setCentralRssi(Integer centralRssi) {
		this.centralRssi = centralRssi;
	}

	public Boolean getPeripheralConnected() {
		return peripheralConnected;
	}

	public void setPeripheralConnected(Boolean peripheralConnected) {
		this.peripheralConnected = peripheralConnected;
	}

	public Timestamp getLastPeripheralConnectionSuccess() {
		return lastPeripheralConnectionSuccess;
	}

	public void setLastPeripheralConnectionSuccess(Timestamp lastPeripheralConnectionSuccess) {
		this.lastPeripheralConnectionSuccess = lastPeripheralConnectionSuccess;
	}

	public Boolean getCentralConnected() {
		new DataAccess().addLog("getCentralConnected: " + centralConnected);
		return centralConnected;
	}

	public void setCentralConnected(Boolean centralConnected, String src) {
		new DataAccess().addLog("setCentralConnected to " + centralConnected + " from " + src);
		this.centralConnected = centralConnected;
	}

	public Timestamp getLastCentralConnectionSuccess() {
		return lastCentralConnectionSuccess;
	}

	public void setLastCentralConnectionSuccess(Timestamp lastCentralConnectionSuccess) {
		this.lastCentralConnectionSuccess = lastCentralConnectionSuccess;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setLastSuccess(Timestamp lastSuccess) {
		this.lastSuccess = lastSuccess;
	}

	public Timestamp getLastSubscribed() {
		return lastSubscribed;
	}

	public void setLastSubscribed(Timestamp lastSubscribed) {
		this.lastSubscribed = lastSubscribed;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
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

	public String getServiceUuid() {
		return serviceUuid;
	}

	public void setServiceUuid(String serviceUuid) {
		this.serviceUuid = serviceUuid;
	}

	public String getCharacteristicUuid() {
		return characteristicUuid;
	}

	public void setCharacteristicUuid(String characterUuid) {
		this.characteristicUuid = characterUuid;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active, String src) {
		new DataAccess().addLog("DeviceConnectionDbObj", "setActive: " + active + ", src: " + src);
		this.active = active;
	}

	public Timestamp getLastCheck() {
		return lastCheck;
	}

	public void setLastCheck(Timestamp lastCheck) {
		this.lastCheck = lastCheck;
	}

	public Timestamp getLastSuccess() {
		return lastSuccess;
	}

	public void setLastSuccess(Timestamp lastSuccess, String src) {
		this.lastSuccess = lastSuccess;
	}

	public Timestamp getAdvertisingPeripheral() {
		return advertisingPeripheral;
	}

	public void setAdvertisingPeripheral(Timestamp advertisingPeripheral) {
		this.advertisingPeripheral = advertisingPeripheral;
	}

	public Timestamp getConnectingCentral() {
		return connectingCentral;
	}

	public void setConnectingCentral(Timestamp connectingCentral) {
		this.connectingCentral = connectingCentral;
	}

	public boolean isSubscribed() {
		return subscribed;
	}

	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}

	public boolean isInstallComplete() {
		return installComplete;
	}

	public void setInstallComplete(boolean installComplete) {
		this.installComplete = installComplete;
	}

	public void setCentralConnected(Boolean centralConnected) {
		this.centralConnected = centralConnected;
	}

	public String getCentralOsId() {
		return centralOsId;
	}

	public void setCentralOsId(String centralOsId) {
		this.centralOsId = centralOsId.toUpperCase();
	}

	@Override
	public String toString() {
		return "DeviceConnectionDbObj [peripheralDeviceId=" + peripheralDeviceId + ", centralDeviceId="
				+ centralDeviceId + ", serviceUuid=" + serviceUuid + ", characteristicUuid=" + characteristicUuid
				+ ", active=" + active + ", lastCheck=" + lastCheck + ", lastSuccess=" + lastSuccess
				+ ", peripheralConnected=" + peripheralConnected + ", lastBluetoothSuccess="
				+ lastPeripheralConnectionSuccess + ", centralConnected=" + centralConnected
				+ ", lastCentralConnectionSuccess: " + lastCentralConnectionSuccess + ", advertisingPeripheral="
				+ advertisingPeripheral + ", connectingCentral=" + connectingCentral + ", subscribed=" + subscribed
				+ ", lastSubscribed=" + lastSubscribed + "]";
	}

}

package com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse;

import java.sql.Timestamp;

import org.apache.http.util.TextUtils;

import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class DeviceData {
	String deviceId;
	String serviceUuid;
	String connectionType;
	boolean connected;
	Timestamp connectionTime;
	String connectionTimeString;
	String deviceType;
	String operatingSystem;
	String deviceClass;
	boolean hasBle;
	boolean central;
	boolean multiuser;
	String userEmail;
	String companyName;
	Integer estimatedDistance;
	String estimatedDistanceString;
	String currentTime;
	String rssiTimeString;
	Timestamp distanceTimestamp;
	String connectionId;

	public DeviceData(String deviceId, String serviceUuid, String connectionType, boolean connected,
			Timestamp connectionTime, String deviceType, String operatingSystem, String deviceClass, boolean hasBle,
			boolean central, boolean multiuser, String userEmail, String companyName, Integer estimatedDistance,
			Timestamp distanceTimestamp, String connectionId) {
		super();
		this.deviceId = deviceId;
		this.serviceUuid = serviceUuid;
		this.connectionType = connectionType;
		this.connected = connected;
		this.connectionTime = connectionTime;
		this.deviceType = deviceType;
		this.operatingSystem = operatingSystem;
		this.deviceClass = deviceClass;
		this.hasBle = hasBle;
		this.central = central;
		this.multiuser = multiuser;
		this.userEmail = userEmail;
		this.companyName = companyName;
		this.connectionId = connectionId;
		if (central) {
			this.connectionTimeString = "look at the peripheral(s)";
		} else {
			if (connected) {
				this.connectionTimeString = DateTimeUtilities.hoursMinutesAndSecondsAgo(connectionTime);
			} else {
				this.connectionTimeString = " -- ";
			}
		}
		this.estimatedDistance = estimatedDistance;
		setEstimatedDistanceString(estimatedDistance);
		this.distanceTimestamp = distanceTimestamp;
		if (central) {
			this.rssiTimeString = "look at the peripheral(s)";
		} else {
			if (distanceTimestamp != null) {
				this.rssiTimeString = setTimeStringFromTime(distanceTimestamp);
			} else {
				this.rssiTimeString = "--";
			}
		}
	}

	public String getConnectionId() {
		if (!TextUtils.isBlank(connectionId) && connectionId.length() > 5) {
			return connectionId.substring(0, 5) + "...";
		} else {
			return "";
		}
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public Timestamp getDistanceTimestamp() {
		return distanceTimestamp;
	}

	public void setDistanceTimestamp(Timestamp distanceTimestamp) {
		this.distanceTimestamp = distanceTimestamp;
	}

	private String setTimeStringFromTime(Timestamp ts) {
		return DateTimeUtilities.hoursMinutesAndSecondsAgo(ts);
	}

	private void setEstimatedDistanceString(Integer estimatedDistance) {
		if (central) {
			estimatedDistanceString = "look at the peripheral(s)";
		} else {
			if (!connected || (!connectionType.equals(ConnectionType.PROX.name()))) {
				estimatedDistanceString = " -- ";
			} else {
				if (estimatedDistance == null || estimatedDistance < 0) {
					estimatedDistanceString = "distance could not be estimated";
				} else {
					if (estimatedDistance == 0) {
						estimatedDistanceString = "&lt; 1m";
					} else {
						estimatedDistanceString = estimatedDistance + "m";
					}
				}
			}
		}
	}

	public String getDeviceId() {
		return deviceId.substring(0, 5) + "...";
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getFullDeviceId() {
		return deviceId;
	}

	public String getServiceUuid() {
		if (!TextUtils.isBlank(serviceUuid) && serviceUuid.length() > 13) {
			return serviceUuid.substring(4, 13).toUpperCase() + "...";
		} else {
			return serviceUuid;
		}
	}

	public void setServiceUuid(String serviceUuid) {
		this.serviceUuid = serviceUuid;
	}

	public String getRssiTimeString() {
		return rssiTimeString;
	}

	public void setRssiTimeString(String rssiTimeString) {
		this.rssiTimeString = rssiTimeString;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	public void setEstimatedDistanceString(String estimatedDistanceString) {
		this.estimatedDistanceString = estimatedDistanceString;
	}

	public String getEstimatedDistanceString() {
		return estimatedDistanceString;
	}

	public Integer getEstimatedDistance() {
		return estimatedDistance;
	}

	public void setEstimatedDistance(Integer estimatedDistance) {
		this.estimatedDistance = estimatedDistance;
	}

	public String getConnectionTimeString() {
		return connectionTimeString;
	}

	public void setConnectionTimeString(String connectionTimeString) {
		this.connectionTimeString = connectionTimeString;
	}

	public Timestamp getConnectionTime() {
		return connectionTime;
	}

	public void setConnectionTime(Timestamp connectionTime) {
		this.connectionTime = connectionTime;
	}

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public String getDeviceClass() {
		return deviceClass;
	}

	public void setDeviceClass(String deviceClass) {
		this.deviceClass = deviceClass;
	}

	public boolean isHasBle() {
		return hasBle;
	}

	public void setHasBle(boolean hasBle) {
		this.hasBle = hasBle;
	}

	public boolean isCentral() {
		return central;
	}

	public void setCentral(boolean central) {
		this.central = central;
	}

	public boolean isMultiuser() {
		return multiuser;
	}

	public void setMultiuser(boolean multiuser) {
		this.multiuser = multiuser;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

}

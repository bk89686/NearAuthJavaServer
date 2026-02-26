package com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse;

import java.util.ArrayList;

import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class UsersDataApiResponse extends ApiResponse {
	private static final long serialVersionUID = -8420608415356759825L;
	ArrayList<DeviceData> deviceData;
	String currentTime;
	String companyName;
	String redirect;

	public UsersDataApiResponse() {
		super();
		this.deviceData = new ArrayList<>();
		this.currentTime = DateTimeUtilities.currentTimestampToReadableAltWithNyTimezone();
		this.companyName = "";
		this.redirect = "";
	}

	public UsersDataApiResponse(ArrayList<DeviceData> deviceData) {
		super();
		this.deviceData = deviceData;
		this.currentTime = DateTimeUtilities.currentTimestampToReadableAltWithNyTimezone();
		this.companyName = "";
		this.redirect = "";
	}

	public UsersDataApiResponse(DeviceData oneDeviceData) {
		super();
		this.deviceData = new ArrayList<DeviceData>();
		this.deviceData.add(oneDeviceData);
		this.currentTime = DateTimeUtilities.currentTimestampToReadableAltWithNyTimezone();
		this.companyName = "";
		this.redirect = "";
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public void addDevice(DeviceData oneDeviceData) {
		this.deviceData.add(oneDeviceData);
	}

	public ArrayList<DeviceData> getDeviceData() {
		return this.deviceData;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	public void setDeviceData(ArrayList<DeviceData> deviceData) {
		this.deviceData = deviceData;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

}

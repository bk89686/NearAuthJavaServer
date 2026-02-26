package com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class DeviceDataOneDevice {
	private String sDate;
	private String sDay;
	private String sTime;
	private String timeSpan;
	private boolean connected;
	private String sConnected;
	private String connectionType;
	private String source;
	private String description;
	private long secondsLong;
	private double fontSize;
	private long padding;
	private long cellHeight;
	private String endDate;

	public DeviceDataOneDevice(Timestamp ts, boolean connected, String connectionType, String timeSpan,
			long secondsLong, String source, String description) {
		super();
		String timeFormat = "h:mm a";
		String dateFormat = "M/d/yyyy";
		String dayFormat = "EEEE";
		this.sDate = DateTimeUtilities.timestampToString(ts, dateFormat);
		this.sDay = DateTimeUtilities.timestampToString(ts, dayFormat);
		this.sTime = DateTimeUtilities.timestampToString(ts, timeFormat);
		this.connected = connected;
		if (connected) {
			this.sConnected = "Connected";
		} else {
			this.sConnected = "Disconnected";
		}
		this.source = source;
		this.description = description;
		this.timeSpan = timeSpan;
		this.secondsLong = secondsLong;
		long secondLongDividedBy300 = secondsLong / 300;
		this.fontSize = Math.max(Math.min(secondLongDividedBy300, 1), 0.1);
		this.padding = Math.max(Math.min((secondsLong - 30) / 30, 10), 0);
		this.cellHeight = secondsLong / 40;
		this.connectionType = connectionType;
	}

	public String getsDay() {
		return sDay;
	}

	public void setsDay(String sDay) {
		this.sDay = sDay;
	}

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public long getCellHeight() {
		return cellHeight;
	}

	public void setCellHeight(long cellHeight) {
		this.cellHeight = cellHeight;
	}

	public long getPadding() {
		return padding;
	}

	public void setPadding(long padding) {
		this.padding = padding;
	}

	public double getFontSize() {
		return fontSize;
	}

	public void setFontSize(double fontSize) {
		this.fontSize = fontSize;
	}

	public long getSecondsLong() {
		return secondsLong;
	}

	public void setSecondsLong(long secondsLong) {
		this.secondsLong = secondsLong;
	}

	public String getTimeSpan() {
		return timeSpan;
	}

	public void setTimeSpan(String timeSpan) {
		this.timeSpan = timeSpan;
	}

	public String getSource() {
		return source;
	}

	public String getsDate() {
		return sDate;
	}

	public void setsDate(String sDate) {
		this.sDate = sDate;
	}

	public String getsTime() {
		return sTime;
	}

	public void setsTime(String sTime) {
		this.sTime = sTime;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getsConnected() {
		return sConnected;
	}

	public void setsConnected(String sConnected) {
		this.sConnected = sConnected;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSource(String source) {
		this.source = source;
	}

}

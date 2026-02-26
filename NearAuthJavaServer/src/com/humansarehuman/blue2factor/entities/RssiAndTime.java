package com.humansarehuman.blue2factor.entities;

import java.sql.Timestamp;

public class RssiAndTime {
	public int rssi;
	public Timestamp ts;

	public RssiAndTime(int rssi, Timestamp ts) {
		this.rssi = rssi;
		this.ts = ts;
	}
}
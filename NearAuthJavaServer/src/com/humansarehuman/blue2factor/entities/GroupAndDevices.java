package com.humansarehuman.blue2factor.entities;

import java.util.ArrayList;

import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;

public class GroupAndDevices {
	GroupDbObj group;
	ArrayList<DeviceDbObj> devices;

	public GroupAndDevices(GroupDbObj group, ArrayList<DeviceDbObj> devices) {
		this.group = group;
		this.devices = devices;
	}

	public GroupDbObj getGroup() {
		return group;
	}

	public void setGroup(GroupDbObj group) {
		this.group = group;
	}

	public ArrayList<DeviceDbObj> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<DeviceDbObj> devices) {
		this.devices = devices;
	}

}

package com.humansarehuman.blue2factor.entities;

import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;

public class CompanyGroupDevice {
	CompanyDbObj company;
	GroupDbObj group;
	DeviceDbObj device;

	public CompanyGroupDevice(CompanyDbObj company, GroupDbObj group, DeviceDbObj device) {
		this.company = company;
		this.group = group;
		this.device = device;
	}

	public CompanyDbObj getCompany() {
		return company;
	}

	public void setCompany(CompanyDbObj company) {
		this.company = company;
	}

	public GroupDbObj getGroup() {
		return group;
	}

	public void setGroup(GroupDbObj group) {
		this.group = group;
	}

	public DeviceDbObj getDevice() {
		return device;
	}

	public void setDevice(DeviceDbObj device) {
		this.device = device;
	}

}

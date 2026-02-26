package com.humansarehuman.blue2factor.entities;

import java.util.ArrayList;

import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;

public class CompanyGroupsDevices {
	CompanyDbObj company;
	ArrayList<GroupAndDevices> groupsAndDevices;

	public CompanyGroupsDevices(CompanyDbObj company, ArrayList<GroupAndDevices> groupsAndDevices) {
		this.company = company;
		this.groupsAndDevices = groupsAndDevices;
	}

	public CompanyDbObj getCompany() {
		return company;
	}

	public void setCompany(CompanyDbObj company) {
		this.company = company;
	}

	public ArrayList<GroupAndDevices> getGroupsAndDevices() {
		return groupsAndDevices;
	}

	public void setGroupsAndDevices(ArrayList<GroupAndDevices> groupsAndDevices) {
		this.groupsAndDevices = groupsAndDevices;
	}

}

package com.humansarehuman.blue2factor.entities;

import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;

public class IdentityObjectFromServer {
	CompanyDbObj company;
	DeviceDbObj device;
	BrowserDbObj browser = null;
	TokenDbObj browserToken = null;
	AccessCodeDbObj accessCode = null;
	GroupDbObj group;
	boolean fromJs = false;

	public IdentityObjectFromServer(CompanyDbObj company, DeviceDbObj device, BrowserDbObj browser,
			TokenDbObj browserToken, boolean fromJs) {
		super();
		this.company = company;
		this.browser = browser;
		this.device = device;
		this.browserToken = browserToken;
		this.fromJs = fromJs;
		this.group = getGroup();
	}

	public IdentityObjectFromServer(CompanyDbObj company, DeviceDbObj device, BrowserDbObj browser, boolean fromJs) {
		super();
		this.company = company;
		this.device = device;
		this.browser = browser;
		this.fromJs = fromJs;
		this.group = getGroup();
	}

	public IdentityObjectFromServer(CompanyDbObj company, GroupDbObj group, DeviceDbObj device, BrowserDbObj browser,
			boolean fromJs) {
		super();
		this.company = company;
		this.device = device;
		this.browser = browser;
		this.fromJs = fromJs;
		this.group = group;
	}

	public IdentityObjectFromServer(CompanyDbObj company, DeviceDbObj device, BrowserDbObj browser,
			AccessCodeDbObj accessCode) {
		super();
		this.company = company;
		this.device = device;
		this.browser = browser;
		this.accessCode = accessCode;
		this.group = getGroup();
	}

	public IdentityObjectFromServer(CompanyDbObj company, DeviceDbObj device, AccessCodeDbObj accessCode) {
		super();
		this.company = company;
		this.device = device;
		this.accessCode = accessCode;
		this.group = getGroup();
	}

	public IdentityObjectFromServer(BrowserDbObj browser, Boolean fromJs) {
		super();
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		if (browser != null) {
			this.company = dataAccess.getCompanyByDevId(browser.getDeviceId());
			this.device = dataAccess.getDeviceByBrowserId(browser.getBrowserId());
			this.browser = browser;
			this.fromJs = fromJs;
			this.group = getGroup();
		}
	}

	public void setGroup(GroupDbObj group) {
		this.group = group;
	}

	public GroupDbObj getGroup() {
		if (group == null) {
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			if (device != null) {
				group = dataAccess.getGroupById(device.getGroupId());
			}
		}
		return group;
	}

	public AccessCodeDbObj getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(AccessCodeDbObj accessCode) {
		this.accessCode = accessCode;
	}

	public CompanyDbObj getCompany() {
		return company;
	}

	public void setCompany(CompanyDbObj company) {
		this.company = company;
	}

	public BrowserDbObj getBrowser() {
		return browser;
	}

	public void setBrowser(BrowserDbObj browser) {
		this.browser = browser;
	}

	public DeviceDbObj getDevice() {
		return device;
	}

	public void setDevice(DeviceDbObj device) {
		this.device = device;
	}

	public TokenDbObj getBrowserToken() {
		return browserToken;
	}

	public void setBrowserToken(TokenDbObj browserToken) {
		this.browserToken = browserToken;
	}

	public boolean isFromJs() {
		return fromJs;
	}

	public void setFromJs(boolean fromJs) {
		this.fromJs = fromJs;
	}

}

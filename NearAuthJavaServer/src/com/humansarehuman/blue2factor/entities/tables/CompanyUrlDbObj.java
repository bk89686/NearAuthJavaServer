package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

public class CompanyUrlDbObj {
	private String tableId;
	private Timestamp createDate;
	private boolean active;
	private String companyId;
	private String urlRegex;

	public CompanyUrlDbObj(String tableId, Timestamp createDate, boolean active, String companyId, String urlRegex) {
		super();
		this.tableId = tableId;
		this.createDate = createDate;
		this.active = active;
		this.companyId = companyId;
		this.urlRegex = urlRegex;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getUrlRegex() {
		return urlRegex;
	}

	public void setUrlRegex(String urlRegex) {
		this.urlRegex = urlRegex;
	}

}

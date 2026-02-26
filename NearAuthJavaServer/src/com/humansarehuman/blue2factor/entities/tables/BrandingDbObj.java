package com.humansarehuman.blue2factor.entities.tables;

public class BrandingDbObj {
	String companyId;
	String iconPath;
	String backgroundColor;
	String foregroundColor;
	String titleImagePath;

	public BrandingDbObj(String companyId, String iconPath, String backgroundColor, String foregroundColor,
			String titleImagePath) {
		super();
		this.companyId = companyId;
		this.iconPath = iconPath;
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
		this.titleImagePath = titleImagePath;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(String foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public String getTitleImagePath() {
		return titleImagePath;
	}

	public void setTitleImagePath(String titleImagePath) {
		this.titleImagePath = titleImagePath;
	}

}

package com.humansarehuman.blue2factor.entities;

import java.io.Serializable;
import java.util.ArrayList;

public class AdminCodeResponse implements Serializable {
	private static final long serialVersionUID = -9135282747262769876L;
	ArrayList<String> codeList;
	// trigger upd

	public AdminCodeResponse(String code) {
		codeList = new ArrayList<String>();
		codeList.add(code);
	}

	public AdminCodeResponse(ArrayList<String> codeList) {
		this.codeList = codeList;
	}

	public ArrayList<String> getCodeList() {
		return codeList;
	}

	public void setCodeList(ArrayList<String> codeList) {
		this.codeList = codeList;
	}

	public void addCode(String code) {
		codeList.add(code);

	}
}

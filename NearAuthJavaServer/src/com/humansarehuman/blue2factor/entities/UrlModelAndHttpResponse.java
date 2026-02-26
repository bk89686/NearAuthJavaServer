package com.humansarehuman.blue2factor.entities;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;

import com.humansarehuman.blue2factor.constants.Outcomes;

public class UrlModelAndHttpResponse extends UrlAndModel {

	private HttpServletResponse httpResponse;
	private int outcome = Outcomes.UNKNOWN_STATUS;

	public UrlModelAndHttpResponse(UrlAndModel urlAndModel, HttpServletResponse httpResponse) {
		super(urlAndModel.getUrl(), urlAndModel.getModelMap());
		this.httpResponse = httpResponse;

	}

	public UrlModelAndHttpResponse(UrlAndModel urlAndModel, HttpServletResponse httpResponse, int outcome) {
		super(urlAndModel.getUrl(), urlAndModel.getModelMap());
		this.httpResponse = httpResponse;
		this.outcome = outcome;
	}

	public UrlModelAndHttpResponse(String url, ModelMap modelMap, HttpServletResponse httpResponse) {
		super(url, modelMap);
		this.httpResponse = httpResponse;

	}

	public UrlModelAndHttpResponse(String url, ModelMap modelMap, HttpServletResponse httpResponse, int outcome) {
		super(url, modelMap);
		this.httpResponse = httpResponse;
		this.outcome = outcome;
	}

	public int getOutcome() {
		return outcome;
	}

	public void setOutcome(int outcome) {
		this.outcome = outcome;
	}

	public HttpServletResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpServletResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

}

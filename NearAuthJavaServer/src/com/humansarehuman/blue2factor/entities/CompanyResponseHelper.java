package com.humansarehuman.blue2factor.entities;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;

public class CompanyResponseHelper {
    private HttpServletResponse httpResponse;
    private ModelMap model;
    private String nextPage;

    public CompanyResponseHelper(HttpServletResponse httpResponse, ModelMap model,
            String nextPage) {
        super();
        this.httpResponse = httpResponse;
        this.model = model;
        this.nextPage = nextPage;
    }

    public CompanyResponseHelper(HttpServletResponse httpResponse, ModelMap model, String nextPage,
            String reason) {
        super();
        this.httpResponse = httpResponse;
        this.model = model;
        this.nextPage = nextPage;
    }

    public HttpServletResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpServletResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public ModelMap getModel() {
        return model;
    }

    public void setModel(ModelMap model) {
        this.model = model;
    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

}

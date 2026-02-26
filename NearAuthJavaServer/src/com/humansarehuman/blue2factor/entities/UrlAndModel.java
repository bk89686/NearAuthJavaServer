package com.humansarehuman.blue2factor.entities;

import org.springframework.ui.ModelMap;

public class UrlAndModel {
    private String url;
    private ModelMap modelMap;

    public UrlAndModel(String url, ModelMap modelMap) {
        this.url = url;
        this.modelMap = modelMap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ModelMap getModelMap() {
        return modelMap;
    }

    public void setModelMap(ModelMap modelMap) {
        this.modelMap = modelMap;
    }

}

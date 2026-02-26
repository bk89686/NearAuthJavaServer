package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

public class CompanyCreation implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3235747730652418124L;
    String company;
    String email;
    String pw1;
    String fullName;

    public CompanyCreation() {
    }

    public CompanyCreation(String company, String email, String pw1, String fullName) {
        super();
        this.company = company;
        this.email = email;
        this.pw1 = pw1;
        this.fullName = fullName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPw1() {
        return pw1;
    }

    public void setPw1(String pw1) {
        this.pw1 = pw1;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

}

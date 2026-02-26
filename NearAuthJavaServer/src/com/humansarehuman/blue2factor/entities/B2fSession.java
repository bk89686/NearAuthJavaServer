package com.humansarehuman.blue2factor.entities;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("session")
public class B2fSession {
    String b2fSession;

    public String getB2fSession() {
        return b2fSession;
    }

    public void setB2fSession(String b2fSession) {
        this.b2fSession = b2fSession;
    }

}

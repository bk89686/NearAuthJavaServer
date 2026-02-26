package com.humansarehuman.blue2factor.authentication.passe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/billing")
@SuppressWarnings("ucd")
public class Billing {
    @RequestMapping(method = RequestMethod.GET)
    public String processGet(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model) {
        model.addAttribute("pageTitle", "Blue2Factor Billing");
        return "billingPage";
    }
}

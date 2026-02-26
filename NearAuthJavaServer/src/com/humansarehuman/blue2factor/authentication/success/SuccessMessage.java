package com.humansarehuman.blue2factor.authentication.success;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Urls;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.SUCCESS_MESSAGE)
@SuppressWarnings("ucd")
public class SuccessMessage {
    @RequestMapping(method = RequestMethod.GET)
    public String processGet(HttpServletRequest request, ModelMap model) {
        return "success";
    }
}

package com.humansarehuman.blue2factor.authentication.saml;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.utilities.saml.Saml;

@Controller
@RequestMapping(Urls.SAML_SP_METADATA)
@SuppressWarnings("ucd")
public class SamlSpMetadata {
    @RequestMapping(method = RequestMethod.GET)
    public String processGet(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model, @PathVariable("apiKey") String apiKey) {
        try {
            String metadata = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                    + new Saml().buildSpEntityDescriptor(apiKey);
            model.addAttribute("metadata", metadata);// .replace(">", ">\r\n"));
        } catch (Exception e) {
            new DataAccess().addLog("SamlSpMetadata", e);
        }
        return "samlMetadata";
    }
}

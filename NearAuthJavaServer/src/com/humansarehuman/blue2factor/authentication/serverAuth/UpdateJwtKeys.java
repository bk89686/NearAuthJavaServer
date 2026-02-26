package com.humansarehuman.blue2factor.authentication.serverAuth;

import java.sql.Timestamp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.jwt.JsonWebToken;

@Controller
@RequestMapping(Urls.UPDATE_JWT)
public class UpdateJwtKeys extends B2fApi {
    @RequestMapping(method = RequestMethod.GET)
    public String processGet(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model) {
        int outcome = Outcomes.FAILURE;
        String reason = "";
        boolean force = this.getRequestValue(request, "nthansh").equals("force");
        try {
            DataAccess dataAccess = new DataAccess();
            KeyDbObj currentKey = dataAccess.getJwsSigningKey();
            Timestamp tomorrow = DateTimeUtilities.getCurrentTimestampPlusDays(1);
            if (currentKey == null || force || currentKey.getExpireDate().before(tomorrow)) {
                new JsonWebToken().updateJwtKey();
                outcome = Outcomes.SUCCESS;
            } else {
                reason = "update not needed";
                dataAccess.addLog("UpdateJwtKeys", reason);
            }
        } catch (Exception e) {
            reason = e.getLocalizedMessage();
            new DataAccess().addLog("UpdateJwtKeys", e);
        }
        model.addAttribute("outcome", outcome);
        model.addAttribute("reason", reason);
        return "outcomeAndReason";
    }
}

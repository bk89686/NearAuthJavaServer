package com.humansarehuman.blue2factor.authentication;

import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;

@Controller
@RequestMapping(Urls.SAVE_PERIPHERAL_IDENTIFIER)
@SuppressWarnings("ucd")
public class SavePeripheralIdentifier extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String setPeripheralIdentifierProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String serviceUuid = this.getEncryptedRequestValue(request,
					Parameters.SAVE_PERIPHERAL_IDENTIFIER_SERVICE_ID, key, iv);
			String identifier = this.getEncryptedRequestValue(request, Parameters.SAVE_PERIPHERAL_IDENTIFIER_IDENTIFIER,
					key, iv);
			if (!TextUtils.isBlank(serviceUuid)) {
				DeviceConnectionDbObj connection = dataAccess.getConnectionByServiceUuid(serviceUuid);
				if (connection != null) {
					HashMap<String, Object> hm = new HashMap<String, Object>();
					hm.put("PERIPHERAL_IDENTIFIER", identifier);
					dataAccess.addLog("Updating peripheral reference to " + identifier);
					dataAccess.updateConnectionMap(connection, hm, "setActiveProcessPost");
					outcome = Outcomes.SUCCESS;
				} else {
					reason = Constants.CONNECTION_NOT_FOUND;
				}
			} else {
				reason = Constants.SERVICE_UUID_WAS_NULL;
				dataAccess.addLog("Service was empty ", LogConstants.WARNING);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

}

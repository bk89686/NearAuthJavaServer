package com.humansarehuman.blue2factor.authentication.api;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(value = Urls.REQUIRES_ENCRYPTION)
public class RequiresEncryption extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processRequiresEncryptionPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String serviceUuid = this.getEncryptedRequestValue(request,
					Parameters.SERVICE_REQUIRES_ENCRYPTION_PARAMETER, key, iv);
			boolean requiresEncryption = this.getEncryptedRequestBoolean(request,
					Parameters.REQUIRES_ENCRYPTION_PARAMETER, key, iv, true);
			DeviceConnectionDbObj conn = dataAccess.getConnectionByServiceUuid(serviceUuid);
			if (conn != null) {
				DeviceDbObj perf = dataAccess.getDeviceByDeviceId(conn.getPeripheralDeviceId());
				if (perf != null) {
					if (requiresEncryption) {
						perf.setTemp(Constants.REQUIRES_ENCRYPTION);
					} else {
						perf.setTemp("");
					}
					dataAccess.updateDevice(perf, "processRequiresEncryptionPost");
					outcome = Outcomes.SUCCESS;
				} else {
					reason = Constants.DEVICE_NOT_FOUND;
				}
			} else {
				reason = Constants.CONNECTION_NOT_FOUND;
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

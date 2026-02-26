package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.ADD_BROWSER_AUTH)
@SuppressWarnings("ucd")
public class AddBrowserAuth extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String addBrowserAuthProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		String deviceId = "";
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, "aseuha", key, iv);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "addBrowserAuth");
			if (device != null) {
				boolean central = device.isCentral();
				if (central) {
					// we are authorizing
					// we should check a signature
					dataAccess.updateDeviceBrowserSetupCompleteForAllPerfs(device, false);
					BasicResponse br = this.addAuthorization(device, true);
					outcome = br.getOutcome();
				} else {
					// we are checking for authorization
					if (!device.isBrowserInstallComplete()) {
						DeviceDbObj centralDevice = dataAccess.getCentralForPeripheral(device);
						if (dataAccess.didCentralDeviceAuthorizeAddBrowser(centralDevice)) {
							outcome = Outcomes.SUCCESS;
						} else {
							reason = "auth not given";
						}
					} else {
						reason = "auth not given";
					}
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(deviceId, e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";

	}

}

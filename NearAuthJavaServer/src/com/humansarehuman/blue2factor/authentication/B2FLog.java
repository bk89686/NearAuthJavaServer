package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(value = { Urls.B2F_LOG, "/b2fLog" })
@SuppressWarnings("ucd")
public class B2FLog extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String webLog(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = null;
			try {
				key = getKey(request);
			} catch (Exception e1) {

			}
			if (TextUtils.isEmpty(key)) {
				key = this.getRequestValue(request, "key");
			}
			String iv;
			String logValue;
			String devId;
			int logLevel;
			if (key.equals("none")) {
				devId = this.getRequestValue(request, "devId");
				logValue = this.getRequestValue(request, "b2fLog");
				logLevel = Integer.parseInt(this.getRequestValue(request, "lvl"));
			} else {
				iv = getInitVector(request);
				logValue = this.getEncryptedRequestValue(request, "b2fLog", key, iv);
				devId = this.getEncryptedRequestValue(request, "devId", key, iv);
				try {
					logLevel = Integer.parseInt(this.getEncryptedRequestValue(request, "lvl", key, iv));
				} catch (Exception e) {
					logLevel = LogConstants.DEBUG;
				}
			}
			// printAllRequestParams(request);
			dataAccess.addLog(devId, logValue, logLevel);
			// we don't go through here every time because it does a db lookup

			if (logValue != null && (logValue.startsWith("***") || logValue.startsWith("%2A%2A%2A"))) {
				DeviceDbObj device = dataAccess.getDeviceByDeviceId(devId);
				if (device != null && device.getUnresponsive()) {
//					dataAccess.setUnresponsive(device, true);
				} else {
					if (device == null) {
						dataAccess.addLog(devId, "device was null in webLog", LogConstants.DEBUG);
					} else {
						dataAccess.addLog(devId, "In webLog - unresponsive = " + device.getUnresponsive(),
								LogConstants.DEBUG);
					}
				}
			}
		} catch (Exception ex) {
			dataAccess.addLog(ex);
		}
		return "result";
	}
}

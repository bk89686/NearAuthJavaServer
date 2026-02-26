package com.humansarehuman.blue2factor.authentication.install;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;

@Controller
@RequestMapping(Urls.INSTALL_COMPLETE)
@SuppressWarnings("ucd")
public class SetIntallComplete extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String setInstallCompleteProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String serviceUuid = this.getEncryptedRequestValue(request, "shsN", key, iv);
			Boolean complete = this.getEncryptedRequestBoolean(request, "complete", key, iv, true);
			if (!TextUtils.isBlank(serviceUuid)) {
				DeviceConnectionDbObj conn = dataAccess.getConnectionByServiceUuid(serviceUuid);
				if (conn != null) {
					if (complete) {
						dataAccess.updateAsInstallComplete(conn);
					} else {
						// why the fuck would this happen
					}
					outcome = Outcomes.SUCCESS;
				} else {
					reason = Constants.CONNECTION_NOT_FOUND;
				}
			} else {
				reason = Constants.DEVICE_ID_WAS_BLANK;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

}

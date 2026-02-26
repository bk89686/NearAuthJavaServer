package com.humansarehuman.blue2factor.authentication.trigger;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.TRIGGER_PUSH_CRON)
@SuppressWarnings("ucd")
public class TriggerPushCron extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = runMinutelyCron();

		BasicResponse response = new BasicResponse(outcome, "");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String triggerPushProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		BasicResponse response = new BasicResponse(outcome, Constants.METHOD_NOT_ALLOWED);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private int runMinutelyCron() {
		int outcome = Outcomes.FAILURE;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		ArrayList<DeviceDbObj> devices = dataAccess.getAllActiveDevices();
		for (DeviceDbObj device : devices) {
			boolean connected = dataAccess.isAccessAllowed(device, "runMinutelyCron");
			if (dataAccess.updateQuickAccess(device.getDeviceId(), connected, "")) {
				outcome = Outcomes.SUCCESS;
			}
		}
		return outcome;
	}
}

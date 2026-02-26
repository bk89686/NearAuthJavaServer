package com.humansarehuman.blue2factor.authentication.internal;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.firebase.messaging.Message;
import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.communication.Emailer;
import com.humansarehuman.blue2factor.communication.PushNotifications;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.JsonUtilities;

@Controller
@RequestMapping(Urls.Q_AND_D)
@SuppressWarnings("ucd")
public class QandD extends B2fApi {

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		int outcome = Outcomes.FAILURE;
		QandD q = new QandD();
		try {
			int[] A = { 2, 1, 2, 3, 2, 2 };
			System.out.println(q.solution(A, 3));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int solution(int[] A, int R) {
		int maxDiffs = 0;
		int diff;
		for (int i = 0; i < A.length - R; i++) {
			diff = getNumberOfDiffs(i, R, A);
			System.out.println("diffs starting at " + i + " = " + diff);
			if (diff > maxDiffs) {
				maxDiffs = diff;
			}
		}
		return maxDiffs;
	}

	private int getNumberOfDiffs(int elementStart, int R, int[] A) {
		ArrayList<Integer> found = new ArrayList<>();
		for (int j = 0; j < elementStart; j++) {
			if (!found.contains(A[j])) {
				System.out.println("new val found: " + A[j]);
				found.add(A[j]);
			}
		}
		for (int k = elementStart + R; k < A.length; k++) {
			if (!found.contains(A[k])) {
				System.out.println("new val found: " + A[k]);
				found.add(A[k]);
			}
		}
		return found.size();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int i = 0;
//		CompanyDataAccess dataAccess = new CompanyDataAccess();
//		ArrayList<CompanyDbObj> companies = dataAccess.getAllActiveCompanies();
//		for (CompanyDbObj company : companies) {
//			company.setEntityIdVal(GeneralUtilities.getRandomUuid());
//			dataAccess.updateCompany(company);
//			i++;
//		}
		BasicResponse br = new BasicResponse(Outcomes.SUCCESS, Integer.toString(i));
		model = this.addBasicResponse(model, br);
		return "result";
	}

	public BasicResponse sendTestEmail() {
		String email = "chris@NearAuth.ai";
		Emailer emailer = new Emailer();
		int outcome = Outcomes.FAILURE;
		String reason = "";
		if (emailer.sendAppConfirmEmail(email, "teststr")) {
			outcome = Outcomes.SUCCESS;
		} else {
			reason = "wtf";
		}
		return new BasicResponse(outcome, reason);
	}

	String central = "XE7Bb0fZTLbf2Twq04mjlHea21whwdH10T4WELlZ";
	String perf = "kr7vHDbHJ9bqCLmLJBw38jTj745o2HuK03bK7kf4";
	String otherCentral = "b4hmdY2eAHlKN8LdQaTC2a56ifvKah8oIIoKEyc9";
	String fcmToken = "eV07iX5nME2Jq3qbYG1Alf:APA91bHAepJsS4o57ooz0tkvLmVT6XfCCHqcWcvhgNeX8QStk_xmJ-dGPqb486GUdRCw8qJKz1kP_ygEExhn2sdB1zxn_WdzcGqyH-9Ldkd9IeAZTt7rUvk";

	public BasicResponse testSilentPush2() {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {

			DeviceDbObj centralDevice = dataAccess.getDeviceByDeviceId(otherCentral);
			PushNotifications push = new PushNotifications();
			JsonUtilities jsonUtil = new JsonUtilities();
			String instanceId = GeneralUtilities.randomString(20);
			Message message = jsonUtil.getNewSilentMessageDataForCentral(centralDevice, false, instanceId, true);
			push.sendFcmMessage(message, centralDevice, false, false, true);
			outcome = Outcomes.SUCCESS;
		} catch (Exception e) {
			reason = e.getLocalizedMessage();
			dataAccess.addLog(e);
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		return response;
	}

	public BasicResponse testSilentPush() {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		DeviceDbObj perfDevice = dataAccess.getDeviceByDeviceId(perf);
		PushNotifications push = new PushNotifications();
		return push.sendPushToAllConnectedDevices(perfDevice, perf, false, false, true, true);
	}

	public BasicResponse testLoudPush() {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		DeviceDbObj centralDevice = dataAccess.getDeviceByDeviceId(central);
		PushNotifications push = new PushNotifications();
		return push.sendTestFcm(centralDevice);
//		ApiResponseWithToken apiResp = push.sendLoudPushByDevice(perfDevice, false);
//		return new BasicResponse(apiResp.getOutcome(), apiResp.getReason());
	}

	public BasicResponse testJwt() {
		int outcome = Outcomes.FAILURE;

		return new BasicResponse(outcome, "");
	}

}

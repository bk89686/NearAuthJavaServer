package com.humansarehuman.blue2factor.authentication.install;

import java.sql.Timestamp;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.communication.twilio.TextMessage;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.SUBMIT_PHONE_NUMBER)
@SuppressWarnings("ucd")
public class SubmitPhoneNumber extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String submitPhoneNumberProcessGet(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String submitPhoneNumberProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		getVersion(request);
		SamlDataAccess dataAccess = new SamlDataAccess();
		String key = getKey(request);
		String iv = getInitVector(request);
		String deviceId = this.getEncryptedRequestValue(request, Parameters.PHONE_NUMBER_DEVICE_ID, key, iv);
		boolean resend = this.getEncryptedRequestBoolean(request, Parameters.PHONE_NUMBER_RESEND, key, iv, false);
		String accessCode = this.getEncryptedRequestValue(request, Parameters.PHONE_NUMBER_ACCESS_CODE, key, iv);
		boolean addDevice = this.getEncryptedRequestBoolean(request, Parameters.PHONE_NUMBER_ADD_DEVICE, key, iv,
				false);
		dataAccess.addLog("accessCode: " + accessCode + ", addDevice: " + addDevice);
		try {
			DeviceDbObj peripheralDevice = dataAccess.getDeviceByDeviceId(deviceId);
			if (resend) {
				if (peripheralDevice != null) {
					dataAccess.addLog("perf found");
					DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(peripheralDevice, false);
					DeviceDbObj centralDevice = null;
					if (conn != null) {
						centralDevice = dataAccess.getDeviceByDeviceId(conn.getCentralDeviceId());
					} else {
						if (addDevice) {
							centralDevice = dataAccess.getCentralByGroupId(peripheralDevice.getGroupId());
						}
					}
					if (centralDevice != null) {
						if (!TextUtils.isBlank(centralDevice.getPhoneNumber())) {
							TextMessage text = new TextMessage();
							if (conn != null) {
								text.textCode(dataAccess.getCompanyByDevId(centralDevice.getDeviceId()), centralDevice,
										conn);
							} else {
								text.textCodeWithoutConnection(centralDevice, peripheralDevice, dataAccess);
							}
							dataAccess.addAccessCode(peripheralDevice.getDeviceId(), accessCode,
									"submitPhoneNumberProcessPost");
							outcome = Outcomes.SUCCESS;
						} else {
							reason = Constants.PHONE_NUMBER_BLANK;
						}
					} else {
						reason = Constants.CENTRAL_NOT_FOUND;
					}
				} else {
					reason = Constants.PERIPHERAL_NOT_FOUND;
				}
			} else {
				if (peripheralDevice != null) {
					dataAccess.addLog("perf not found");
					String phoneNumber = this.getEncryptedRequestValue(request, Parameters.PHONE_NUMBER, key, iv);
					DeviceConnectionDbObj conn = setupDumbPhone(peripheralDevice, phoneNumber, dataAccess);
					if (conn != null) {
						TextMessage textMessage = new TextMessage();
						textMessage.textCode(dataAccess.getCompanyByDevId(conn.getCentralDeviceId()), phoneNumber,
								conn);
						dataAccess.addAccessCode(peripheralDevice.getDeviceId(), accessCode,
								"submitPhoneNumberProcessPost");
						outcome = Outcomes.SUCCESS;
					} else {
						reason = "setup failed";
					}
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}

		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private DeviceConnectionDbObj setupDumbPhone(DeviceDbObj perf, String phoneNumber, DeviceDataAccess dataAccess) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String devId = GeneralUtilities.randomString();
		int seed = GeneralUtilities.randInt(0, 1000000000);
		Timestamp longTimeAgo = DateTimeUtilities.getBaseTimestamp();
		DeviceConnectionDbObj connection = null;
		try {
			dataAccess.addLog("add central");
			DeviceDbObj central = new DeviceDbObj(perf.getGroupId(), perf.getUserId(), devId, seed, false, null, null,
					new Date(), now, "dumbphone", OsClass.DUMBPHONE, null, new Date(), perf.getLastGmtOffset(), "",
					perf.getUserLanguage(), "", "", false, 100.0, false, 0, false, longTimeAgo, false, false, "", "",
					true, now, false, longTimeAgo, false, DeviceClass.PHONE, false, true, longTimeAgo, null, false,
					false, phoneNumber, false, false, null, false);
			dataAccess.addDevice(central);
			connection = dataAccess.getConnectionForPeripheral(perf, false);
			dataAccess.setCentralIdAndGroupId(devId, perf.getGroupId(), connection);
			dataAccess.addLog("connection found? " + (connection != null));
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return connection;
	}

}

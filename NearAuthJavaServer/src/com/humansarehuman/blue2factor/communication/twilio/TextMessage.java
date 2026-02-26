package com.humansarehuman.blue2factor.communication.twilio;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TextMessage {
	final String ACCOUNT_SID = "AC82c7e09ae119cc301978fb0434320831";
	final String AUTH_TOKEN = "77a9254ccca5aba99a1bac93dfa6384d";
	final String PHONE_NUMBER = "+18667381675";
	final String TEST_SID = "ACe1f8907736267a8d0bc59c2d86e0864f";
	final String TEST_TOKEN = "2b7ebba4a4989924b2f3c6c71b02b2b5";

	boolean TESTING = false;

	public void sendChrisText() {
		sendMessage("6072228641", "you dumb.");
	}

	public boolean sendAndroidLink(String phNo) {
		return sendMessage(phNo,
				"Please download the " + Constants.APP_NAME + " mobile app at " + Constants.GOOGLE_PLAY_LINK + ".");
	}

	public boolean sendIosLink(String phNo) {
		return sendMessage(phNo,
				"Please download the " + Constants.APP_NAME + " mobile app at " + Constants.APP_STORE_LINK + ".");
	}

	public boolean sendMessage(String phoneNumber, String msg) {
		boolean success = false;
		String username = TEST_SID;
		String password = TEST_TOKEN;
		DataAccess dataAccess = new DataAccess();
		if (!TESTING) {
			username = ACCOUNT_SID;
			password = AUTH_TOKEN;
		}
		try {
			Twilio.init(username, password);
			if (!phoneNumber.startsWith("+")) {
				if (phoneNumber.length() == 10) {
					phoneNumber = "+1" + phoneNumber;
				} else {
					phoneNumber = "+" + phoneNumber;
				}
			}
			dataAccess.addLog("sending message to " + phoneNumber);
			Message message = Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber("+18667381675"), msg)
					.create();
			com.twilio.rest.api.v2010.account.Message.Status status = message.getStatus();
			if (!message.getStatus().equals(Message.Status.FAILED) && !status.equals(Message.Status.UNDELIVERED)) {
				success = true;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}

	public boolean textCodeToCentral(IdentityObjectFromServer idObj, SamlDataAccess dataAccess) {
		DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(idObj.getDevice(), true);
		return textCode(idObj.getCompany(), idObj.getDevice(), conn);
	}

	public boolean textCodeToCentral(DeviceDbObj device, SamlDataAccess dataAccess) {
		DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(device, true);
		CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
		return textCode(company, device, conn);
	}

	public boolean textCode(CompanyDbObj company, DeviceDbObj device, DeviceConnectionDbObj conn) {
		return textCode(company, device.getPhoneNumber(), conn);
	}

	public boolean textCode(CompanyDbObj company, String phoneNumber, DeviceConnectionDbObj conn) {
		boolean success = false;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			dataAccess.expireChecksForCentral(conn.getCentralDeviceId(), "textCode");
			String checkId = GeneralUtilities.randomString();
			String instance = GeneralUtilities.randomString();
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			dataAccess.addLog("will text");
			if (company != null) {
				Timestamp threeMinutes = DateTimeUtilities
						.getCurrentTimestampPlusSeconds(company.getTextTimeoutSeconds());
				String code = GeneralUtilities.randomNumberString(6);

				CheckDbObj check = new CheckDbObj(checkId, instance, conn.getCentralDeviceId(),
						conn.getPeripheralDeviceId(), conn.getServiceUuid(), conn.getGroupId(), null, null, null, null,
						false, false, Outcomes.INCOMPLETE, now, null, false, CheckType.TXT, code, "", threeMinutes);

				dataAccess.addCheck(check);
				dataAccess.addLog("check added");
				String message = "Your NearAuth.ai verification code is: " + code;
				success = sendMessage(phoneNumber, message);

			} else {
				dataAccess.addLog("company not found", LogConstants.ERROR);
			}
		} catch (Exception e) {
			dataAccess.addLog("error");
			dataAccess.addLog(e);
		}
		return success;
	}

	public boolean textCodeWithoutConnection(DeviceDbObj central, DeviceDbObj peripheral,
			CompanyDataAccess dataAccess) {
		boolean success = false;
		try {
			String checkId = GeneralUtilities.randomString();
			String instance = GeneralUtilities.randomString();
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			dataAccess.addLog("will text");
			CompanyDbObj company = dataAccess.getCompanyByDevId(peripheral.getDeviceId());
			if (company != null) {
				Timestamp threeMinutes = DateTimeUtilities
						.getCurrentTimestampPlusSeconds(company.getTextTimeoutSeconds());
				String code = GeneralUtilities.randomNumberString(6);

				CheckDbObj check = new CheckDbObj(checkId, instance, central.getDeviceId(), peripheral.getDeviceId(),
						"", peripheral.getGroupId(), null, null, null, null, false, false, Outcomes.INCOMPLETE, now,
						null, false, CheckType.TXT, code, "", threeMinutes);

				dataAccess.addCheck(check);
				dataAccess.addLog("check added, phone number: " + central.getPhoneNumber());
				String message = "Your NearAuth.ai verification code is: " + code;
				success = sendMessage(central.getPhoneNumber(), message);

			} else {
				dataAccess.addLog("company not found", LogConstants.ERROR);
			}
		} catch (Exception e) {
			dataAccess.addLog("error");
			dataAccess.addLog(e);
		}
		return success;
	}

//	public CheckDbObj(String checkId, String instanceId, String centralDeviceId, String peripheralDeviceId,
//			String serviceUuid, String userId, String centralBssid, String centralSsid, String peripheralBssid,
//			String peripheralSsid, boolean expired, boolean completed, Integer outcome, Timestamp createDate,
//			Timestamp completionDate, boolean verfiedReceipt, CheckType checkType, String centralInstanceId,
//			String peripheralInstanceId, Timestamp expirationDate) {
}

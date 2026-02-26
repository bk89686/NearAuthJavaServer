package com.humansarehuman.blue2factor.authentication.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.AccessAllowedWithAccessType;
import com.humansarehuman.blue2factor.entities.OtherUserData;
import com.humansarehuman.blue2factor.entities.RssiAndTime;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.DeviceData;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.UsersDataApiResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = Urls.DEVICE_DATA)
@SuppressWarnings("ucd")
public class GetDeviceData extends B2fApi {

	CompanyDataAccess dataAccess = new CompanyDataAccess();

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getDeviceData(HttpServletRequest request, HttpServletResponse response, ModelMap model) {

		int outcome = Outcomes.FAILURE;
		String reason = "";
		String redirect = "";
		UsersDataApiResponse responseData = new UsersDataApiResponse();
		try {
			TokenDbObj token = this.getPersistentTokenObj(request);
			if (token != null) {
				CompanyDbObj company;
				GroupDbObj displayGroup;
				GroupDbObj myGroup = dataAccess.getActiveGroupFromToken(token);
				if (myGroup != null) {
					DeviceDbObj device = dataAccess.getDeviceByDeviceId(token.getDeviceId(), true, "getDeviceData");
					if (device != null && dataAccess.isAccessAllowed(device)) {
						boolean admin = dataAccess.userIsAdmin(myGroup);
						request.setAttribute("admin", admin);
						String incomingGroupId = this.getRequestValue(request, "uid");
						if (!incomingGroupId.equals("")) {
							displayGroup = dataAccess.getActiveGroupById(incomingGroupId);
							if (!userHasAccess(myGroup, displayGroup, admin)) {
								displayGroup = myGroup;
							}
						} else {
							displayGroup = myGroup;
						}
						if (admin) {
							ArrayList<OtherUserData> otherUsers = getOtherUsersData(myGroup, displayGroup);
							request.setAttribute("otherUsersData", otherUsers);
							request.setAttribute("otherUserCount", otherUsers.size());
						}
						company = dataAccess.getCompanyById(displayGroup.getCompanyId());
						responseData.setCompanyName(company.getCompanyName());
						responseData = this.getOneUsersDevices(request, responseData, company, displayGroup);
						outcome = Outcomes.SUCCESS;
					} else {
						company = dataAccess.getCompanyById(myGroup.getCompanyId());
						reason = Constants.DEVICE_NOT_CONNECTED;

						redirect = Urls.getSecureUrl() + "/failure/" + myGroup.getCompanyId() + "/reset";
					}
				} else {
					reason = Constants.GROUP_NOT_FOUND;
				}
			} else {
				dataAccess.addLog("browser token not found", LogConstants.WARNING);
				reason = "Your browser needs to be resynchronized with NearAuth.ai to view this data.";
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		responseData.setOutcome(outcome);
		responseData.setReason(reason);
		responseData.setRedirect(redirect);
		request.setAttribute("deviceData", responseData);
		request.setAttribute("pageTitle", "NearAuth.ai Device Data");
		return "displayDeviceData";
	}

	private boolean userHasAccess(GroupDbObj myGroup, GroupDbObj otherGroup, boolean admin) {
		boolean hasAccess = false;
		if (admin) {
			hasAccess = myGroup.getCompanyId().equals(otherGroup.getCompanyId());
		}
		return hasAccess;
	}

	private ArrayList<OtherUserData> getOtherUsersData(GroupDbObj adminGroup, GroupDbObj currGroup) {
		return dataAccess.getAllActiveCompanyGroupsWithDevices(adminGroup.getCompanyId(), currGroup);
	}

	private UsersDataApiResponse getOneUsersDevices(HttpServletRequest request, UsersDataApiResponse responseData,
			CompanyDbObj company, GroupDbObj group) {
		ArrayList<DeviceDbObj> devices = dataAccess.getDevicesFromGroupDesc(group, true);
		dataAccess.addLog("deviceCount: " + devices.size(), LogConstants.TRACE);
		for (DeviceDbObj device : devices) {
			DeviceData deviceData = getOneDeviceData(request, company, group, device);
			responseData.addDevice(deviceData);
		}
		return responseData;
	}

	private DeviceData getOneDeviceData(HttpServletRequest request, CompanyDbObj company, GroupDbObj group,
			DeviceDbObj device) {
		DeviceData oneDeviceData = null;
		Timestamp checkTime;
		AccessAllowedWithAccessType accessData = dataAccess.isProximateWithData(device, false);
		if (!accessData.isAccessAllowed()) {
			accessData = dataAccess.didGiveAccessWithReturnType(device);
			if (!accessData.isAccessAllowed()) {
				accessData = dataAccess.isBtConnectedWithRecentTransferWithData(device);
			}
		}
		if (accessData.getConnectionType() != null) {
			checkTime = accessData.getCheckTime();
		} else {
			checkTime = DateTimeUtilities.getBaseTimestamp();
		}
		Integer txPower = device.getTxPower();
		Set<Integer> zeroVals = Set.of(-999, 0, 127);
		if (txPower == null || zeroVals.contains(txPower)) {
			txPower = -59;
		}

		ConnectionInfo connectionInfo = null;
		if (!device.isCentral()) {
			connectionInfo = getPeripheralConnectionInfo(device, accessData);
		} else {
			accessData = setCentralAccessData(accessData, device);
		}
		int estimatedDistance = -1;
		try {
			if (connectionInfo != null && connectionInfo.getRssi() != null) {
				estimatedDistance = GeneralUtilities.estimateDistance(device, connectionInfo.getRssi());
			}
		} catch (Exception wtf) {
			dataAccess.addLog(wtf);
		}
		request.setAttribute("username", group.getUsername());
		String sConnType = null;
		if (accessData.getConnectionType() != null) {
			sConnType = accessData.getConnectionType().toString();
		}
		String deviceType = device.getDeviceType();
		if (!device.isActive()) {
			deviceType += " (inactive)";
		}
		String connectionId = null;
		String serviceUuid = null;
		Timestamp rssiTimestamp = null;
		if (connectionInfo != null) {
			connectionId = connectionInfo.getConnectionId();
			serviceUuid = connectionInfo.getServiceUuid();
			rssiTimestamp = connectionInfo.getRssiTimestamp();
		}
		oneDeviceData = new DeviceData(device.getDeviceId(), serviceUuid, sConnType, accessData.isAccessAllowed(),
				checkTime, deviceType, device.getOperatingSystem().toString(),
				device.getDeviceClass().deviceClassName(), device.getHasBle(), device.isCentral(), device.isMultiUser(),
				group.getGroupName(), company.getCompanyName(), estimatedDistance, rssiTimestamp, connectionId);

		return oneDeviceData;
	}

	private AccessAllowedWithAccessType setCentralAccessData(AccessAllowedWithAccessType accessData,
			DeviceDbObj device) {
		ArrayList<DeviceConnectionDbObj> connections = dataAccess.getConnectionsForCentral(device);
		DeviceDbObj perf;
		boolean perfAllowed = false;
		for (DeviceConnectionDbObj conn : connections) {
			perf = dataAccess.getDeviceByDeviceId(conn.getPeripheralDeviceId());
			if (dataAccess.isAccessAllowed(perf)) {
				perfAllowed = true;
				break;
			}
		}
		accessData.setAccessAllowed(perfAllowed);
		return accessData;
	}

	private ConnectionInfo getPeripheralConnectionInfo(DeviceDbObj device, AccessAllowedWithAccessType accessData) {
		DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(device, true);
		String serviceUuid = null;
		RssiAndTime rssiAndTime = null;
		Integer rssi = null;
		Timestamp rssiTimestamp = null;
		String connectionId = "";
		if (connection != null) {
			serviceUuid = connection.getServiceUuid();
			connectionId = connection.getConnectionId();
		}
		if (accessData.getConnectionType() != null && (accessData.getConnectionType().equals(ConnectionType.PROX))) {

			if (connection != null) {
				rssiAndTime = dataAccess.getRssiForPeripheral(device, connection);
				if (rssiAndTime != null) {
					rssi = rssiAndTime.rssi;
					rssiTimestamp = rssiAndTime.ts;
				}
			} else {
				dataAccess.addLog("connection was null for " + device.getDeviceId(), LogConstants.WARNING);
			}
		}
		return new ConnectionInfo(connectionId, serviceUuid, rssiAndTime, rssi, rssiTimestamp);
	}

	class ConnectionInfo {
		private String connectionId;
		private String serviceUuid;
		private RssiAndTime rssiAndTime;
		private Integer rssi;
		private Timestamp rssiTimestamp;

		public ConnectionInfo(String connectionId, String serviceUuid, RssiAndTime rssiAndTime, Integer rssi,
				Timestamp rssiTimestamp) {
			super();
			this.connectionId = connectionId;
			this.serviceUuid = serviceUuid;
			this.rssiAndTime = rssiAndTime;
			this.rssi = rssi;
			this.rssiTimestamp = rssiTimestamp;
		}

		public String getConnectionId() {
			return connectionId;
		}

		public String getServiceUuid() {
			return serviceUuid;
		}

		public RssiAndTime getRssiAndTime() {
			return rssiAndTime;
		}

		public Integer getRssi() {
			return rssi;
		}

		public Timestamp getRssiTimestamp() {
			return rssiTimestamp;
		}
	}
}

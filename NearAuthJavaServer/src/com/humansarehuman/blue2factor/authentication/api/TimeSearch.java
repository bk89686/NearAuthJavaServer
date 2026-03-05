package com.humansarehuman.blue2factor.authentication.api;

import java.sql.Timestamp;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.ConnectionLogDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.TIME_SEARCH)
@SuppressWarnings("ucd")
public class TimeSearch extends B2fApi {
	CompanyDataAccess dataAccess = new CompanyDataAccess();
	int logLevel = LogConstants.TRACE;
	int connCount = 0;

	@RequestMapping(method = RequestMethod.POST)
	public String postTimeSearch(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		String reason = "";
		int outcome = Outcomes.FAILURE;
		TokenDbObj token = this.getPersistentTokenObj(request);
		Timestamp startTs = null;
		Timestamp endTs = null;
		ArrayList<ConnectionData> connEvts = new ArrayList<>();
		int offsetMinutes = 0;
		String tz = "";
		try {
			if (token != null) {
				GroupDbObj myGroup = dataAccess.getActiveGroupFromToken(token);
				CompanyDbObj company = dataAccess.getCompanyByGroupId(myGroup.getGroupId());
				boolean admin = dataAccess.userIsAdmin(myGroup);
				if (admin) {
					Timestamp now = DateTimeUtilities.getCurrentTimestamp();
					startTs = buildTime(request, true);
					endTs = buildTime(request, false);
					offsetMinutes = -1 * Integer.valueOf(getRequestValue(request, "tzOffset"));
					tz = getRequestValue(request, "tzString");
					if (startTs.after(now)) {
						startTs = now;
					}
					String useRange = this.getRequestValue(request, "rangeSearch");
					dataAccess.addLog("useRange: " + useRange, LogConstants.TRACE);
					if (endTs != null && useRange.equals("on")) {
						if (endTs.after(now)) {
							endTs = now;
						}
						if (endTs.equals(startTs)) {
							endTs = null;
						}
					} else {
						endTs = null;
					}
					if (endTs == null || endTs.after(startTs)) {
						dataAccess.addLog("endTs: " + endTs, LogConstants.TRACE);
						ArrayList<GroupDbObj> groups = dataAccess.getGroupsByCompanyId(myGroup.getCompanyId());
						dataAccess.addLog("group count: " + groups.size(), LogConstants.TRACE);
						for (GroupDbObj group : groups) {
							ArrayList<DeviceConnectionDbObj> conns = dataAccess.getConnectionsForGroup(group);
							dataAccess.addLog("group " + group.getUsername() + " has " + conns.size() + " connections",
									LogConstants.TRACE);
							if (endTs == null) {
								connEvts = getConnectionEventsForSingleTime(conns, company, myGroup, connEvts, startTs,
										offsetMinutes);
							} else {
								connEvts = getConnectionEventsForRange(conns, company, myGroup, connEvts, startTs,
										endTs, offsetMinutes);
							}
						}
						outcome = Outcomes.SUCCESS;
					} else {
						reason = "The end time you selected was before the start time.";
					}

				} else {
					reason = "You need to be an admin to do this.";
				}

			}
		} catch (Exception e) {
			String eStr = this.getStackTrace(e);
			dataAccess.addLog(eStr);
			reason = eStr;
		}
		model.put("outcome", outcome);
		model.put("groupConnectionEvents", connEvts);
		model.put("tzString", tz);
		model.put("startDate", DateTimeUtilities.timestampToString(startTs, "M/d/yyyy", offsetMinutes));
		model.put("startTime", DateTimeUtilities.timestampToString(startTs, "h:mm:ss a", offsetMinutes));
		if (endTs != null) {
			model.put("endDate", DateTimeUtilities.timestampToString(endTs, "M/d/yyyy", offsetMinutes));
			model.put("endTime", DateTimeUtilities.timestampToString(endTs, "h:mm:ss a", offsetMinutes));
		}
		model.put("reason", reason);
		model.put("connCount", connCount);
		model.put("pageTitle", "Search Authentications By Time");
		return "timeSearch";
	}

	private ArrayList<ConnectionData> getConnectionEventsForRange(ArrayList<DeviceConnectionDbObj> conns,
			CompanyDbObj company, GroupDbObj userGroup, ArrayList<ConnectionData> connEvts, Timestamp startTs,
			Timestamp endTs, int offsetMinutes) {
		ConnectionData currentConnectionData;
		DeviceAndConnLog deviceAndConn;
		ConnectionLogDbObj startConnLog;
		ConnectionLogDbObj endConnLog;
		DeviceDbObj peripheralDevice;
		DeviceDbObj centralDevice = null;
		String centralId = "";
		ArrayList<String> connIdList = new ArrayList<>();
		ArrayList<DeviceAndConnLog> devicesAndConns = new ArrayList<>();
		ArrayList<ConnectionLogDbObj[]> connectDisconnectPairs;
		boolean hasConnected = false;
		for (DeviceConnectionDbObj conn : conns) {
			if (centralDevice == null & conn.getCentralDeviceId() != null) {
				centralId = conn.getCentralDeviceId();
				centralDevice = dataAccess.getDeviceByDeviceId(centralId, true, "postTimeSearch");
			}
			connIdList.add(conn.getConnectionId());
			connectDisconnectPairs = dataAccess.getConnectionEventsInRange(company, conn, startTs, endTs);
			for (ConnectionLogDbObj[] connDisconn : connectDisconnectPairs) {
				startConnLog = connDisconn[0];
				endConnLog = connDisconn[1];
				hasConnected = true;
				connCount++;
				peripheralDevice = dataAccess.getDeviceByDeviceId(conn.getPeripheralDeviceId(), true, "postTimeSearch");
				dataAccess.addLog("device: " + peripheralDevice.getDeviceType() + " is connected.",
						LogConstants.TRACE);

				deviceAndConn = new DeviceAndConnLog(peripheralDevice, centralDevice, startConnLog, endConnLog,
						offsetMinutes);
				devicesAndConns.add(deviceAndConn);
			}
		}
		if (conns.size() > 0) {
			connectDisconnectPairs = dataAccess.getConnectionEventsInRangeForCentral(company, centralId, startTs,
					endTs);
			for (ConnectionLogDbObj[] connDisconn : connectDisconnectPairs) {
				startConnLog = connDisconn[0];
				endConnLog = connDisconn[1];
				if (startConnLog != null && startConnLog.isConnected()) {
					hasConnected = true;
					deviceAndConn = new DeviceAndConnLog(null, centralDevice, startConnLog, endConnLog, offsetMinutes);
					devicesAndConns.add(deviceAndConn);
				}
			}
		}
		if (hasConnected) {
			currentConnectionData = new ConnectionData(userGroup, devicesAndConns, centralDevice);
			connEvts.add(currentConnectionData);
		}
		return connEvts;
	}

	private ArrayList<ConnectionData> getConnectionEventsForSingleTime(ArrayList<DeviceConnectionDbObj> conns,
			CompanyDbObj company, GroupDbObj userGroup, ArrayList<ConnectionData> connEvts, Timestamp startTs,
			int offsetMinutes) {
		boolean connected = false;
		ConnectionData currentConnectionData;
		DeviceAndConnLog deviceAndConn;
		ConnectionLogDbObj startConnLog;
		ConnectionLogDbObj endConnLog;
		ConnectionLogDbObj[] connLogs;
		DeviceDbObj peripheralDevice;
		DeviceDbObj centralDevice = null;
		String centralId = "";
		ArrayList<String> connIdList = new ArrayList<>();
		ArrayList<DeviceAndConnLog> devicesAndConns = new ArrayList<>();
		for (DeviceConnectionDbObj conn : conns) {
			if (centralDevice == null) {
				centralId = conn.getCentralDeviceId();
				centralDevice = dataAccess.getDeviceByDeviceId(centralId, true, "postTimeSearch");
			}
			connIdList.add(conn.getConnectionId());
			connLogs = dataAccess.getConnectionEventsAtTime(company, conn, startTs);
			dataAccess.addLog("after getting logs", LogConstants.TRACE);
			startConnLog = connLogs[0];
			endConnLog = connLogs[1];
			if (startConnLog != null) {
				if (startConnLog.isConnected()) {
					connected = true;
					connCount++;
					peripheralDevice = dataAccess.getDeviceByDeviceId(conn.getPeripheralDeviceId(), true,
							"postTimeSearch");
					dataAccess.addLog("device: " + peripheralDevice.getDeviceType() + " is connected.",
							LogConstants.TRACE);

					deviceAndConn = new DeviceAndConnLog(peripheralDevice, centralDevice, startConnLog, endConnLog,
							offsetMinutes);
					devicesAndConns.add(deviceAndConn);
				}
			}
		}
		if (conns.size() > 0) {
			connLogs = dataAccess.getConnectionEventsAtTimeForCentral(company, centralId, startTs);
			startConnLog = connLogs[0];
			endConnLog = connLogs[1];
			if (startConnLog != null && startConnLog.isConnected()) {
				connected = true;
				deviceAndConn = new DeviceAndConnLog(null, centralDevice, startConnLog, endConnLog, offsetMinutes);
				devicesAndConns.add(deviceAndConn);
			}
		}
		if (connected) {
			dataAccess.addLog("device and conns size: " + devicesAndConns.size(), LogConstants.TRACE);
			currentConnectionData = new ConnectionData(userGroup, devicesAndConns, centralDevice);
			connEvts.add(currentConnectionData);
		}
		return connEvts;
	}

	private Timestamp buildTime(HttpServletRequest request, boolean start) {
		Timestamp ts = null;
		String prefix;
		if (start) {
			prefix = "start";
		} else {
			prefix = "end";
		}
		try {
			String tzOffset = this.getRequestValue(request, "tzOffset");
			String sTime = this.getRequestValue(request, prefix + "Time");
			String sDate = this.getRequestValue(request, prefix + "Date");
			String sSeconds = this.getRequestValue(request, prefix + "Seconds");
			String sMillis = this.getRequestValue(request, prefix + "Millis");
			dataAccess.addLog(prefix + " date: " + sDate, LogConstants.TRACE);
			if (sDate != "" && sTime != "") {
				int iSeconds;
				if (sMillis == "") {
					iSeconds = 0;
				} else {
					iSeconds = Integer.valueOf(sSeconds);
				}
				int iMillis;
				if (sMillis == "") {
					iMillis = 0;
				} else {
					iMillis = Integer.valueOf(sMillis);
				}
				ts = DateTimeUtilities.buildTimestampFromTimeDateOffsetWithSeconds(sTime, iSeconds, iMillis, sDate,
						-1 * Integer.parseInt(tzOffset));
			}
		} catch (Exception e) {
			dataAccess.addLog(prefix + " time not found", LogConstants.TRACE);
		}
		return ts;
	}

	public class ConnectionData {
		GroupDbObj group;
		ArrayList<DeviceAndConnLog> devicesAndConns;
		DeviceDbObj centralDevice;

		public ConnectionData(GroupDbObj group, ArrayList<DeviceAndConnLog> devicesAndConns,
				DeviceDbObj centralDevice) {
			super();
			this.group = group;
			this.devicesAndConns = devicesAndConns;
			this.centralDevice = centralDevice;
		}

		public GroupDbObj getGroup() {
			return group;
		}

		public void setGroup(GroupDbObj group) {
			this.group = group;
		}

		public ArrayList<DeviceAndConnLog> getDevicesAndConns() {
			return devicesAndConns;
		}

		public void setDevicesAndConns(ArrayList<DeviceAndConnLog> devicesAndConns) {
			this.devicesAndConns = devicesAndConns;
		}

		public DeviceDbObj getCentralDevice() {
			return centralDevice;
		}

		public void setCentralDevice(DeviceDbObj centralDevice) {
			this.centralDevice = centralDevice;
		}

	}

	public class DeviceAndConnLog {
		DeviceDbObj peripheralDevice;
		DeviceDbObj centralDevice;
		ConnectionLogDbObj startConnLog;
		ConnectionLogDbObj endConnLog;
		String startConnectionTypeString;
		String startConnectionTimeString;
		String endConnectionTimeString;
		int offsetMinutes;

		public DeviceAndConnLog(DeviceDbObj peripheralDevice, DeviceDbObj centralDevice,
				ConnectionLogDbObj startConnLog, ConnectionLogDbObj endConnLog, int offsetMinutes) {
			super();
			this.peripheralDevice = peripheralDevice;
			this.startConnLog = startConnLog;
			this.endConnLog = endConnLog;
			this.offsetMinutes = offsetMinutes;
			this.computeTimeAndType(startConnLog, endConnLog, offsetMinutes);
		}

		private void computeTimeAndType(ConnectionLogDbObj startConnLog, ConnectionLogDbObj endConnLog,
				int offsetMinutes) {
			this.startConnectionTypeString = computeConnectionTypeString(startConnLog);
			this.startConnectionTimeString = computeConnectionTimeString(startConnLog, offsetMinutes);
			this.endConnectionTimeString = computeConnectionTimeString(endConnLog, offsetMinutes);
		}

		public String computeConnectionTimeString(ConnectionLogDbObj connLog, int offsetMinutes) {
			String time = "unknown";
			if (connLog == null) {
				time = "the present";
			} else {
				try {
					time = DateTimeUtilities.timestampToString(connLog.getEventTimestamp(), "h:mm:ss a 'on' MM/dd/yyyy",
							offsetMinutes);
				} catch (Exception e) {
					dataAccess.addLog(e);
				}
			}
			return time;
		}

		public String computeConnectionTypeString(ConnectionLogDbObj connLog) {
			String connStr = "unknown";
			switch (connLog.getConnectionType()) {
			case ADMIN_CODE:
				connStr = "using an admin code";
				break;
			case PASSKEY:
				connStr = "using a passkey";
				break;
			case PROX:
				connStr = "via proximity detection";
				break;
			case PUSH:
				connStr = "by responding to a push notification";
				break;
			case NONE:
				connStr = "not connected";
				break;
			case TXT:
				connStr = "by entering a texted code";
				break;
			case NONMEMBER_ACCESS:
				connStr = "non-member access";
				break;
			}
			return connStr;
		}

		public String getStartConnectionTypeString() {
			return startConnectionTypeString;
		}

		public String getStartConnectionTimeString() {
			return startConnectionTimeString;
		}

		public String getEndConnectionTimeString() {
			return endConnectionTimeString;
		}

		public DeviceDbObj getPeripheralDevice() {
			return peripheralDevice;
		}

		public void setPeripheralDevice(DeviceDbObj peripheralDevice) {
			this.peripheralDevice = peripheralDevice;
		}

		public DeviceDbObj getCentralDevice() {
			return centralDevice;
		}

		public void setCentralDevice(DeviceDbObj centralDevice) {
			this.centralDevice = centralDevice;
		}

		public ConnectionLogDbObj getStartConnLog() {
			return startConnLog;
		}

		public ConnectionLogDbObj getEndConnLog() {
			return endConnLog;
		}

		public void setStartConnLog(ConnectionLogDbObj connLog) {
			this.startConnLog = connLog;
		}
	}

}

package com.humansarehuman.blue2factor.dataAndAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.http.util.TextUtils;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.entities.AccessAllowedWithAccessType;
import com.humansarehuman.blue2factor.entities.AdminCodeResponse;
import com.humansarehuman.blue2factor.entities.ConnectedAndConnectionType;
import com.humansarehuman.blue2factor.entities.DeviceAndInstanceId;
import com.humansarehuman.blue2factor.entities.RssiAndTime;
import com.humansarehuman.blue2factor.entities.SuccessAndConnection;
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.enums.NonMemberStrategy;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.ConnectionLogDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.QuickAccessDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class DeviceDataAccess extends DeviceConnectionDataAccess {

	public ConnectionLogDbObj[] getConnectionEventsAtTime(CompanyDbObj company, DeviceConnectionDbObj conn,
			Timestamp ts) {
		ConnectionLogDbObj startEvent = null;
		ConnectionLogDbObj endEvent = null;
		ArrayList<ConnectionLogDbObj> connLogs = getConnLogsAfterTimestampPlusOneBeforePeripheral(company,
				conn.getConnectionId(), conn.getPeripheralDeviceId(), ts);
		this.addLog("connLogs found starting from " + ts.toString() + ": " + connLogs.size(),
				LogConstants.TRACE);
//		int i = 0;
//		for (ConnectionLogDbObj connLog : connLogs) {
//			i++;
//			this.addLog(i + ": connected: " + connLog.isConnected() + " @ " + connLog.getEventTimestamp(),
//					LogConstants.TEMPORARILY_IMPORTANT);
//		}
		if (connLogs.size() > 0) {
			if (connLogs.get(0).isConnected()) {
				this.addLog("the first event before " + ts.toString() + " was a connection at "
						+ connLogs.get(0).getEventTimestamp(), LogConstants.TRACE);
				startEvent = connLogs.get(0);
				for (int j = 1; j < connLogs.size(); j++) {
					if (!connLogs.get(j).isConnected()) {
						endEvent = connLogs.get(j);
						this.addLog(j + ": and then we disconnected at " + connLogs.get(j).getEventTimestamp(),
								LogConstants.TRACE);
						break;
					} else {
						this.addLog(j + ": still connected at " + connLogs.get(j).getEventTimestamp());
					}
				}
				if (endEvent == null) {
					this.addLog("and we are still connected", LogConstants.TRACE);
				}
			} else {
				this.addLog("the first event before " + ts.toString() + " was a disconnect at "
						+ connLogs.get(0).getEventTimestamp(), LogConstants.TRACE);
			}
		}
		if (startEvent != null) {
			addLog(startEvent.getEventTimestamp().toString(), LogConstants.TRACE);
		}
		if (endEvent != null) {
			addLog(" to " + endEvent.getEventTimestamp().toString(), LogConstants.TRACE);
		}
		ConnectionLogDbObj[] events = { startEvent, endEvent };
		return events;
	}

	public ArrayList<ConnectionLogDbObj[]> getConnectionEventsInRange(CompanyDbObj company, DeviceConnectionDbObj conn,
			Timestamp startTs, Timestamp endTs) {
		ConnectionLogDbObj startEvent = null;
		ConnectionLogDbObj endEvent = null;
		ArrayList<ConnectionLogDbObj[]> startAndEndEvents = new ArrayList<>();
		ArrayList<ConnectionLogDbObj> connLogs = getConnLogsAfterTimestampPlusOneBeforePeripheral(company,
				conn.getConnectionId(), conn.getPeripheralDeviceId(), startTs);
		boolean first = true;
		boolean previouslyDisconnected = true;

		for (ConnectionLogDbObj connLog : connLogs) {
			if (first) {
				if (connLog.isConnected()) {
					first = false;
				}
			}
			if (!first) {
				if (connLog.isConnected()) {
					if (previouslyDisconnected) {
						startEvent = connLog;
						previouslyDisconnected = false;
					}
				} else {
					previouslyDisconnected = true;
					endEvent = connLog;
					ConnectionLogDbObj[] startAndEnd = { startEvent, endEvent };
					startAndEndEvents.add(startAndEnd);
					startEvent = null;
					endEvent = null;
				}
			}
		}
		if (startEvent != null) {
			ConnectionLogDbObj[] startAndEnd = { startEvent, null };
			startAndEndEvents.add(startAndEnd);
		}
		return startAndEndEvents;
	}

	public ConnectionLogDbObj[] getConnectionEventsAtTimeForCentral(CompanyDbObj company, String centralId,
			Timestamp ts) {
		ArrayList<ConnectionLogDbObj> connLogs = new ArrayList<>();
		ConnectionLogDbObj startEvent = null;
		ConnectionLogDbObj endEvent = null;
		connLogs = getConnLogsAfterTimestampPlusOneBeforeCentral(company, centralId, ts);
		if (connLogs.size() > 0) {
			if (connLogs.get(0).isConnected()) {
				startEvent = connLogs.get(0);
				if (connLogs.size() > 1) {
					endEvent = connLogs.get(1);
				}
			}
		}
		ConnectionLogDbObj[] events = { startEvent, endEvent };
		return events;
	}

	public ArrayList<ConnectionLogDbObj[]> getConnectionEventsInRangeForCentral(CompanyDbObj company, String centralId,
			Timestamp startTs, Timestamp endTs) {
		ConnectionLogDbObj startEvent = null;
		ConnectionLogDbObj endEvent = null;
		ArrayList<ConnectionLogDbObj[]> startAndEndEvents = new ArrayList<>();
		ArrayList<ConnectionLogDbObj> connLogs = getConnLogsAfterTimestampPlusOneBeforeCentral(company, centralId,
				startTs);
		boolean first = true;
		boolean previouslyDisconnected = true;

		for (ConnectionLogDbObj connLog : connLogs) {
			if (first) {
				if (connLog.isConnected()) {
					first = false;
				}
			}
			if (!first) {
				if (connLog.isConnected()) {
					if (previouslyDisconnected) {
						startEvent = connLog;
						previouslyDisconnected = false;
					}
				} else {
					previouslyDisconnected = true;
					endEvent = connLog;
					ConnectionLogDbObj[] startAndEnd = { startEvent, endEvent };
					startAndEndEvents.add(startAndEnd);
					startEvent = null;
					endEvent = null;
				}
			}
		}
		if (startEvent != null) {
			ConnectionLogDbObj[] startAndEnd = { startEvent, null };
			startAndEndEvents.add(startAndEnd);
		}
		return startAndEndEvents;
	}

	public RssiAndTime getRssiForPeripheral(DeviceDbObj device, DeviceConnectionDbObj conn) {
		Integer rssi = 0;
		Timestamp ts = null;
		Timestamp perfTs = conn.getPeripheralRssiTimestamp();
		Timestamp centralTs = conn.getCentralRssiTimestamp();
		if (centralTs != null ) {
			if (perfTs == null || centralTs.after(perfTs)) {
				ts = centralTs;
				rssi = conn.getCentralRssi();
			} else {
				ts = perfTs;
				rssi = conn.getPeripheralRssi();
			}
		} else if (perfTs != null) {
			ts = perfTs;
			rssi = conn.getPeripheralRssi();
		}
		
		this.addLog("rssi timestamp: " + ts, LogConstants.TEMPORARILY_IMPORTANT);
		return new RssiAndTime(rssi, ts);
	}

	public boolean addQuickAccessDevice(QuickAccessDbObj quickAccess) {
		boolean success = false;
		String query = "INSERT INTO B2F_QUICK_ACCESS VALUES (?, ?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, quickAccess.getQuickCheckId());
			prepStmt.setString(2, quickAccess.getDeviceId());
			prepStmt.setBoolean(3, quickAccess.getConnected());
			prepStmt.setTimestamp(4, quickAccess.getLastUpdate());
			prepStmt.setString(5, quickAccess.getMethod());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public boolean updateQuickAccess(String deviceId, boolean connected, String method) {
		boolean success = false;
		QuickAccessDbObj quickAccess = getQuickAccessByDeviceId(deviceId);
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		if (quickAccess == null) {
			quickAccess = new QuickAccessDbObj(GeneralUtilities.randomString(), deviceId, connected, now, method);
			success = addQuickAccessDevice(quickAccess);
		} else {
			quickAccess.setConnected(connected);
			quickAccess.setLastUpdate(now);
			quickAccess.setMethod(method);
			success = updateQuickAccess(quickAccess);
		}
		return success;
	}

	public boolean updateQuickAccess(QuickAccessDbObj quickAccess) {
		boolean success = false;
		String query = "UPDATE B2F_QUICK_ACCESS SET CONNECTED = ?, LAST_UPDATE = ?, METHOD = ? WHERE DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, quickAccess.getConnected());
			prepStmt.setTimestamp(2, quickAccess.getLastUpdate());
			prepStmt.setString(3, quickAccess.getMethod());
			prepStmt.setString(4, quickAccess.getDeviceId());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public QuickAccessDbObj getQuickAccessByDeviceId(String deviceId) {
		QuickAccessDbObj quickAccess = null;
		String query = "SELECT * FROM B2F_QUICK_ACCESS WHERE DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				quickAccess = recordToQuickAccess(rs);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return quickAccess;
	}

	private QuickAccessDbObj recordToQuickAccess(ResultSet rs) {
		QuickAccessDbObj quickAccess = null;
		try {
			String quickAccessId = rs.getString("QUICK_ACCESS_ID");
			String deviceId = rs.getString("DEVICE_ID");
			Boolean connected = rs.getBoolean("CONNECTED");
			Timestamp lastUpdate = rs.getTimestamp("LAST_UPDATE");
			String method = rs.getString("METHOD");
			quickAccess = new QuickAccessDbObj(quickAccessId, deviceId, connected, lastUpdate, method);
		} catch (SQLException e) {
			this.addLog(e);
		}
		return quickAccess;
	}

	public boolean disconnectQuickAccessForConnection(DeviceDbObj device, DeviceConnectionDbObj connection) {
		boolean success = false;
		DeviceDbObj peripheral;
		DeviceDbObj central;
		try {
			if (device.isCentral()) {
				central = device;
				peripheral = this.getDeviceByDeviceId(connection.getPeripheralDeviceId());
			} else {
				peripheral = device;
				central = this.getCentralForPeripheral(device);
			}
			boolean isConnectedToOther = false;
			ArrayList<DeviceConnectionDbObj> connections = this.getAllConnectionsForCentral(central, true);
			for (DeviceConnectionDbObj otherConnection : connections) {
				if (!otherConnection.getConnectionId().equals(connection.getConnectionId())) {
					if (this.isAccessAllowed(connection, central, true)) {
						isConnectedToOther = true;
					}
				}
			}
			if (!isConnectedToOther) {
				this.updateQuickAccess(central, false);
			}
			this.updateQuickAccess(peripheral, false);
			success = true;
		} catch (Exception e) {
			this.addLog(e);
		}
		return success;
	}

	public void disconnectQuickAccessForDevice(DeviceDbObj device) {
		if (device.isCentral()) {
			ArrayList<DeviceDbObj> devices = this.getPeripheralsDevicesFromCentral(device);
			for (DeviceDbObj peripheralDevice : devices) {
				this.updateQuickAccess(peripheralDevice, false);
			}
		} else {
			DeviceConnectionDbObj conn = this.getConnectionForPeripheral(device, true);
			if (conn != null) {
				DeviceDbObj central = this.getDeviceByDeviceId(conn.getCentralDeviceId(),
						"disconnectQuickAccessForDevice");
				if (central != null) {
					boolean isConnectedToOther = false;
					ArrayList<DeviceConnectionDbObj> connections = this.getAllConnectionsForCentral(central, true);
					for (DeviceConnectionDbObj otherConnection : connections) {
						if (!otherConnection.getConnectionId().equals(conn.getConnectionId())) {
							if (this.isAccessAllowed(conn, central)) {
								isConnectedToOther = true;
							}
						}
					}
					if (!isConnectedToOther) {
						updateQuickAccess(central, false);
					}
				}
			}

		}
		this.updateQuickAccess(device, false);
	}

	public boolean quickIsAccessAllowed(String deviceId) {
		boolean connected = false;
		String query = "SELECT * FROM B2F_QUICK_ACCESS WHERE DEVICE_ID = ? "
				+ "AND LAST_UPDATE > ? ORDER BY LAST_UPDATE DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			Timestamp fiveMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(5);
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setTimestamp(2, fiveMinutesAgo);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				connected = rs.getBoolean("CONNECTED");
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connected;
	}

	public boolean updateQuickAccess(DeviceDbObj device, boolean connected) {
		return updateQuickAccess(device.getDeviceId(), connected);
	}

	public boolean updateQuickAccess(String deviceId, boolean connected) {
		String query = "UPDATE B2F_QUICK_ACCESS SET LAST_UPDATE = ?, CONNECTED = ? WHERE " + "DEVICE_ID = ?";
		boolean success = false;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setTimestamp(1, now);
			prepStmt.setBoolean(2, connected);
			prepStmt.setString(3, deviceId);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public void updateAllQuickAccess() {
		ArrayList<DeviceDbObj> devices = this.getAllActiveDevices();
		boolean currDeviceConnected;
		int i = 0;
		for (DeviceDbObj device : devices) {
			currDeviceConnected = this.isAccessAllowed(device, "updateAllQuickAccess");
			updateQuickAccess(device.getDeviceId(), currDeviceConnected);
			i++;
		}
		addLog(i + " devices were updated in quick access");
	}

	/**
	 * We may want to limit this by device class and to those devices
	 */
	public void shittyHackToUpdateConnectionStatusOnWindows() {
		Timestamp oneMinuteishAgo = DateTimeUtilities.getCurrentTimestampMinusSeconds(65);
		String query = "SELECT * FROM B2F_DEVICE WHERE CENTRAL = ? AND LAST_VARIABLE_RETRIEVAL < ? AND "
				+ "ACTIVE = ? AND SCREENSAVER_ON = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setTimestamp(2, oneMinuteishAgo);
			prepStmt.setBoolean(3, true);
			prepStmt.setBoolean(4, false);
			logQuery("shittyHackToUpdateConnectionStatusOnWindows", prepStmt);
			rs = executeQuery(prepStmt);
			DeviceDbObj device;
			int i = 0;
			while (rs.next()) {
				device = this.recordToDevice(rs);
				updateDeviceAwake(device, false, false);
				i++;
			}
			if (i > 0) {
				this.addLog(i + " devices were marked as asleep because they were inactive", LogConstants.IMPORTANT);
			}
		} catch (Exception e) {
			this.addLog("pushStalePeripherals", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	public void updateLastSilentPushResponse(String centralId, String peripheralId) {
		this.updateLastSilentPushResponse(centralId);
		this.updateLastSilentPushResponse(peripheralId);
	}

	public void updateLastSilentPushResponse(String deviceId) {
		DeviceDbObj device = this.getDeviceByDeviceId(deviceId, "updateLastSilentPushResponse");
		if (device != null) {
			this.updateLastSilentPushResponse(device);
		} else {
			this.addLog(deviceId, "device was null", LogConstants.WARNING);
		}
	}

	public void updateLastSilentPushResponse(DeviceDbObj device) {
		setLastSilentPushResponse(device, DateTimeUtilities.getCurrentTimestamp());
	}

	public ArrayList<DeviceDbObj> getPeripheralsDevicesFromCentral(DeviceDbObj central) {
		ArrayList<DeviceConnectionDbObj> conns = this.getConnectionsForCentral(central);
		ArrayList<DeviceDbObj> peripheralDevices = new ArrayList<>();
		DeviceDbObj currDev;
		for (DeviceConnectionDbObj conn : conns) {
			currDev = this.getDeviceByDeviceId(conn.getPeripheralDeviceId(), "getConnectedPeripheralsFromCentral");
			if (currDev.isActive()) {
				peripheralDevices.add(currDev);
			}
		}
		return peripheralDevices;
	}

	public DeviceDbObj getOtherDeviceInConnection(DeviceDbObj device, DeviceConnectionDbObj connection) {
		DeviceDbObj otherDevice;
		if (device.isCentral()) {
			otherDevice = this.getDeviceByDeviceId(connection.getPeripheralDeviceId(), "getOtherDeviceInConnection");
		} else {
			otherDevice = this.getDeviceByDeviceId(connection.getCentralDeviceId(), "getOtherDeviceInConnection");
		}
		return otherDevice;
	}

	public void setupAddBrowser(DeviceDbObj device) {
		this.updateDeviceBrowserSetupComplete(device, false);
		expireAddBrowser(device);
	}

	protected void expireAddBrowser(final DeviceDbObj device) {
		final Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		final DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("called");
		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				if (!device.isBrowserInstallComplete()) {
					if (device.getLastUpdate().before(now)) {
						dataAccess.addLog("should expiring Add");
						// dataAccess.updateDeviceBrowserSetupComplete(device, true);
					} else {
						dataAccess.addLog("not expiring Add due to recent change");
					}
				} else {
					dataAccess.addLog("not expiring Add b/c it was already changed");
				}
			}
		}, 480 * 1000);
	}

	/**
	 * This could cause problems if the device signed up, reset all devices and
	 * signed up again, I think - cjm
	 * 
	 * @param device
	 * @return
	 */
	public boolean hasPreviouslyConnectedWithin24Hours(final String perfDeviceId, String centralDeviceId) {
		boolean success = false;
		String query = "SELECT * FROM B2F_CHECK WHERE PERIPHERAL_DEVICE_ID = ?" + " AND CENTRAL_DEVICE_ID = ?"
				+ " AND COMPLETED = ? AND OUTCOME = ? AND VERIFIED_RECEIPT = ?"
				+ " AND CHECK_TYPE = ? AND COMPLETION_DATE > ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			Timestamp oneDayAgo = DateTimeUtilities.getCurrentTimestampMinusHours(24);
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, perfDeviceId);
			prepStmt.setString(2, centralDeviceId);
			prepStmt.setBoolean(3, true);
			prepStmt.setInt(4, 0);
			prepStmt.setBoolean(5, true);
			prepStmt.setString(6, CheckType.PROX.checkTypeName());
			prepStmt.setTimestamp(7, oneDayAgo);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				success = true;
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public DeviceDbObj getPeripheralByServiceUuid(String serviceUuid) {
		DeviceDbObj device = null;
		String query = "SELECT dev.* FROM B2F_DEVICE dev JOIN B2F_DEVICE_CONNECTION con "
				+ "ON dev.DEVICE_ID = con.PERIPHERAL_DEVICE_ID WHERE con.SERVICE_UUID = ? " + "AND dev.ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, serviceUuid);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				device = this.recordToDevice(rs);
			}
		} catch (SQLException e) {
			this.addLog("getPeripheralByServiceUuid", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		this.addLog("found: " + (device != null));
		return device;
	}

	public void updateDeviceBrowserSetupComplete(DeviceDbObj device, boolean installComplete) {
		addLog("setting browser install complete to " + installComplete);
		setBrowserInstallComplete(device, installComplete);
	}

	public boolean updateDeviceBrowserSetupCompleteForAllPerfs(DeviceDbObj device, boolean installComplete) {
		boolean success = false;
		addLog("setting browser install complete to " + installComplete);
		String query;
		if (device.isCentral()) {
			query = "UPDATE B2F_DEVICE dev2 SET BROWSER_INSTALL_COMPLETE = ? "
					+ "WHERE dev2.DEVICE_ID IN (SELECT con1.PERIPHERAL_DEVICE_ID FROM "
					+ "B2F_DEVICE_CONNECTION con1 WHERE con1.CENTRAL_DEVICE_ID = ?)";
		} else {
			query = "UPDATE B2F_DEVICE dev2 SET BROWSER_INSTALL_COMPLETE = ? "
					+ "WHERE dev2.DEVICE_ID IN (SELECT con1.PERIPHERAL_DEVICE_ID FROM "
					+ "B2F_DEVICE_CONNECTION con1 WHERE con1.CENTRAL_DEVICE_ID IN "
					+ "(SELECT con.CENTRAL_DEVICE_ID FROM B2F_DEVICE_CONNECTION con "
					+ "WHERE con.PERIPHERAL_DEVICE_ID = ?))";
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, installComplete);
			prepStmt.setString(2, device.getDeviceId());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			this.addLog("updateDeviceBrowserSetupCompleteForAllPerfs", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public ArrayList<GroupDbObj> getGroupsWithActiveConnections() {
		ArrayList<GroupDbObj> groups = new ArrayList<>();
		String query = "SELECT DISTINCT gr.* FROM B2F_GROUP gr JOIN B2F_DEVICE_CONNECTION con "
				+ "ON gr.GROUP_ID = con.GROUP_ID WHERE con.ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			GroupDbObj gp;
			while (rs.next()) {
				gp = this.recordToGroup(rs);
				groups.add(gp);
			}
		} catch (SQLException e) {
			this.addLog("getGroupsWithActiveConnections", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return groups;
	}

	public List<DeviceDbObj> getAllDevices() {
		List<DeviceDbObj> deviceList = new ArrayList<DeviceDbObj>();
		String query = "SELECT " + deviceFields + " FROM B2F_DEVICE";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			int i = 0;
			while (rs.next()) {
				deviceList.add(i, recordToDevice(rs));
				i++;
			}
		} catch (SQLException e) {
			this.addLog("getAllDevices", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return deviceList;
	}

	/*
	 * "CREATE_DATE, CONNECTION_ID, PERIPHERAL_DEVICE_ID, CENTRAL_DEVICE_ID, " +
	 * "SERVICE_UUID, CHARACTER_UUID, GROUP_ID, ACTIVE, LAST_CHECK, LAST_SUCCESS, "
	 * +
	 * "ADVERTISING_PERIPHERAL, CONNECTING_CENTRAL, SUBSCRIBED, INSTALL_COMPLETE, "
	 * + "PERIPHERAL_CONNECTED, LAST_PERIPHERAL_CONNECTION_SUCCESS," +
	 * "CENTRAL_CONNECTED, LAST_CENTRAL_CONNECTION_SUCCESS";
	 */
	public ArrayList<DeviceDbObj> getPeripheralFcmDevicesWithActiveConnections(Timestamp lastConnectionEarliest,
			Timestamp lastConnectionLatest) {
		ArrayList<DeviceDbObj> devices = new ArrayList<>();
		String query = "SELECT pdev.* FROM B2F_DEVICE_CONNECTION conn JOIN B2F_DEVICE pdev"
				+ " ON conn.PERIPHERAL_DEVICE_ID = pdev.DEVICE_ID WHERE conn.ACTIVE = ? AND"
				+ " conn.PERIPHERAL_CONNECTED = ? AND conn.LAST_SUCCESS > ? AND conn.LAST_SUCCESS < ?"
				+ " AND pdev.FCM_ID IS NOT NULL";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setBoolean(2, true);
			prepStmt.setTimestamp(3, lastConnectionEarliest);
			prepStmt.setTimestamp(4, lastConnectionLatest);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			DeviceDbObj device;
			while (rs.next()) {
				device = recordToDevice(rs);
				devices.add(device);
			}
			addLog(devices.size() + " devices found");
		} catch (Exception e) {
			addLog(e);
		}
		return devices;
	}

	public ArrayList<DeviceDbObj> getDisconnectedDevices() {
		ArrayList<DeviceDbObj> devices = getPeripheralFcmDevicesWithDisconnectedConnections();
		ArrayList<DeviceDbObj> centrals = getCentralFcmDevicesWithDisconnectedConnections();
		devices.addAll(centrals);

		return devices;
	}

	public ArrayList<DeviceDbObj> getPeripheralFcmDevicesWithDisconnectedConnections() {
		ArrayList<DeviceDbObj> devices = new ArrayList<>();
		Timestamp twentyMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(20);
		String query = "SELECT pdev.* FROM B2F_DEVICE_CONNECTION conn JOIN B2F_DEVICE pdev"
				+ " ON conn.PERIPHERAL_DEVICE_ID = pdev.DEVICE_ID WHERE conn.ACTIVE = ? AND"
				+ " (conn.PERIPHERAL_CONNECTED = ? OR conn.LAST_SUCCESS < ?)" + " AND pdev.FCM_ID IS NOT NULL";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setBoolean(2, false);
			prepStmt.setTimestamp(3, twentyMinutesAgo);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			DeviceDbObj device;
			while (rs.next()) {
				device = recordToDevice(rs);
				devices.add(device);
			}
			addLog(devices.size() + " devices found");
		} catch (Exception e) {
			addLog("getPeripheralFcmDevicesWithDisconnectedConnections", e);
		}
		return devices;
	}

	public ArrayList<DeviceDbObj> getCentralFcmDevicesWithDisconnectedConnections() {
		ArrayList<DeviceDbObj> devices = new ArrayList<>();
		Timestamp twentyMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(20);
		String query = "SELECT DISTINCT cdev.* FROM B2F_DEVICE_CONNECTION conn JOIN B2F_DEVICE cdev"
				+ " ON conn.CENTRAL_DEVICE_ID = cdev.DEVICE_ID WHERE conn.ACTIVE = ? AND"
				+ " (conn.CENTRAL_CONNECTED = ? OR conn.LAST_SUCCESS < ?) " + " AND cdev.FCM_ID IS NOT NULL";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setBoolean(2, false);
			prepStmt.setTimestamp(3, twentyMinutesAgo);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			DeviceDbObj device;
			while (rs.next()) {
				device = recordToDevice(rs);
				devices.add(device);
			}
			addLog(devices.size() + " devices found");
		} catch (Exception e) {
			addLog("getCentralFcmDevicesWithDisconnectedConnections", e);
		}
		return devices;
	}

	public ArrayList<DeviceDbObj> getCentralFcmDevicesWithActiveConnections(Timestamp lastConnectionEarliest,
			Timestamp lastConnectionLatest) {
		ArrayList<DeviceDbObj> devices = new ArrayList<>();
		String query = "SELECT cdev.* FROM B2F_DEVICE_CONNECTION conn JOIN B2F_DEVICE cdev"
				+ " ON conn.CENTRAL_DEVICE_ID = cdev.DEVICE_ID WHERE conn.ACTIVE = ? AND"
				+ " conn.CENTRAL_CONNECTED = ? AND conn.LAST_SUCCESS > ? AND conn.LAST_SUCCESS < ?"
				+ " AND cdev.FCM_ID IS NOT NULL";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setBoolean(2, true);
			prepStmt.setTimestamp(3, lastConnectionEarliest);
			prepStmt.setTimestamp(4, lastConnectionLatest);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			DeviceDbObj device;
			while (rs.next()) {
				device = recordToDevice(rs);
				devices.add(device);
			}
			addLog(devices.size() + " devices found");
		} catch (Exception e) {
			addLog("getCentralFcmDevicesWithActiveConnections", e);
		}
		return devices;
	}

	public ArrayList<DeviceDbObj> getFcmDevicesWithActiveConnections(Timestamp lastConnectionEarliest,
			Timestamp lastConnectionLatest) {
		ArrayList<DeviceDbObj> devices = getPeripheralFcmDevicesWithActiveConnections(lastConnectionEarliest,
				lastConnectionLatest);
		ArrayList<DeviceDbObj> centrals = getCentralFcmDevicesWithActiveConnections(lastConnectionEarliest,
				lastConnectionLatest);
		devices.addAll(centrals);
		addLog("from " + lastConnectionEarliest + " no " + lastConnectionLatest + ": " + devices.size()
				+ " devices found");
		return devices;
	}

	public ArrayList<DeviceDbObj> getFcmDevicesThatRecentlyBecameInactive() {
		ArrayList<DeviceDbObj> devices = getFcmCentralDevicesThatRecentlyBecameInactive();
		ArrayList<DeviceDbObj> perfs = getFcmPeripheralDevicesThatRecentlyBecameInactive();
		devices.addAll(perfs);
		addLog(devices.size() + " devices found");
		return devices;
	}

	public ArrayList<DeviceDbObj> getFcmCentralDevicesThatRecentlyBecameInactive() {
		ArrayList<DeviceDbObj> devices = new ArrayList<>();
		Timestamp twoMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(2);
		String query = "SELECT cdev.* FROM B2F_DEVICE_CONNECTION conn JOIN B2F_DEVICE cdev"
				+ " ON conn.CENTRAL_DEVICE_ID = cdev.DEVICE_ID WHERE conn.ACTIVE = ? AND"
				+ " conn.CENTRAL_CONNECTED = ? AND conn.LAST_SUCCESS > ?" + " AND cdev.FCM_ID IS NOT NULL";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setBoolean(2, false);
			prepStmt.setTimestamp(3, twoMinutesAgo);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			DeviceDbObj device;
			while (rs.next()) {
				device = recordToDevice(rs);
				devices.add(device);
			}
			addLog(devices.size() + " devices found");
		} catch (Exception e) {
			addLog("getFcmCentralDevicesThatRecentlyBecameInactive", e);
		}
		return devices;
	}

	public ArrayList<DeviceDbObj> getFcmPeripheralDevicesThatRecentlyBecameInactive() {
		ArrayList<DeviceDbObj> devices = new ArrayList<>();
		Timestamp twoMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(2);
		String query = "SELECT pdev.* FROM B2F_DEVICE_CONNECTION conn JOIN B2F_DEVICE pdev"
				+ " ON conn.PERIPHERAL_DEVICE_ID = pdev.DEVICE_ID WHERE conn.ACTIVE = ? AND"
				+ " conn.PERIPHERAL_CONNECTED = ? AND conn.LAST_SUCCESS > ?" + " AND pdev.FCM_ID IS NOT NULL";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setBoolean(2, false);
			prepStmt.setTimestamp(3, twoMinutesAgo);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			DeviceDbObj device;
			while (rs.next()) {
				device = recordToDevice(rs);
				devices.add(device);
			}
			addLog(devices.size() + " devices found");
		} catch (Exception e) {
			addLog("getFcmPeripheralDevicesThatRecentlyBecameInactive", e);
		}
		return devices;
	}

	public DeviceDbObj updateAsTurnedOn(DeviceDbObj device) {
		setTurnedOffFromInstaller(device, false);
		return device;
	}

	public ArrayList<DeviceAndInstanceId> getPendingRequests(DeviceDbObj device) {
		ArrayList<DeviceAndInstanceId> devicesAndInstanceIds = new ArrayList<>();
		if (device.isCentral()) {
			Timestamp oneMinuteAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(1);
			String query = "SELECT DISTINCT PERIPHERAL_DEVICE_ID, CENTRAL_INSTANCE_ID, CREATE_DATE "
					+ "FROM B2F_CHECK WHERE PERIPHERAL_DEVICE_ID IN (SELECT PERIPHERAL_DEVICE_ID "
					+ "FROM B2F_DEVICE_CONNECTION bdc WHERE bdc.CENTRAL_DEVICE_ID = ? AND "
					+ "bdc.ACTIVE = ?) AND CREATE_DATE > ? AND COMPLETED = ? AND EXPIRED = ? AND "
					+ "(CHECK_TYPE = ? OR CHECK_TYPE = ?) ORDER BY CREATE_DATE";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, device.getDeviceId());
				prepStmt.setBoolean(2, true);
				prepStmt.setTimestamp(3, oneMinuteAgo);
				prepStmt.setBoolean(4, false);
				prepStmt.setBoolean(5, false);
				prepStmt.setString(6, CheckType.PUSH.toString());
				prepStmt.setString(7, CheckType.SSHPUSH.toString());
				logQuery(getMethodName(), prepStmt);
				rs = executeQuery(prepStmt);
				String peripheralId;
				DeviceDbObj peripheralDevice;
				String centralInstanceId;
				Timestamp createDate;
				Timestamp expireDate;
				while (rs.next()) {
					peripheralId = rs.getString(1);
					centralInstanceId = rs.getString(2);
					createDate = rs.getTimestamp(3);
					Encryption encryption = new Encryption();
					String encryptedId = encryption.encryptStringWithPublicKey(device, centralInstanceId);
					expireDate = DateTimeUtilities.addSeconds(createDate, 600);
					addLog("found notification: " + peripheralId + ", " + centralInstanceId + ", " + expireDate);
					peripheralDevice = this.getDeviceByDeviceId(peripheralId);
					if (peripheralDevice != null && !this.isAccessAllowed(peripheralDevice, "getPendingRequests")) {
						devicesAndInstanceIds
								.add(new DeviceAndInstanceId(device, peripheralDevice, encryptedId, expireDate));
					}
				}
			} catch (SQLException e) {
				this.addLog("getPendingRequests", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}
		addLog("pendingRequestsFound: " + devicesAndInstanceIds.size());
		return devicesAndInstanceIds;
	}

	public boolean isCentralDumbphone(DeviceDbObj device) {
		boolean dumbphone = false;
		if (device != null) {
			dumbphone = device.getDeviceType().equalsIgnoreCase("dumbphone");
		}
		return dumbphone;
	}

	public DeviceDbObj getCentralForPeripheral(DeviceDbObj device) {
		return getCentralForPeripheral(device.getDeviceId());
	}

	public DeviceDbObj getCentralForPeripheral(String peripheralId) {
		DeviceDbObj device = null;
		if (!TextUtils.isEmpty(peripheralId)) {
			String query = "SELECT dev.* FROM B2F_DEVICE_CONNECTION con JOIN B2F_DEVICE dev "
					+ "ON con.CENTRAL_DEVICE_ID = dev.DEVICE_ID WHERE con.PERIPHERAL_DEVICE_ID = ? AND "
					+ "con.ACTIVE = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, peripheralId);
				prepStmt.setBoolean(2, true);
				logQuery(getMethodName(), prepStmt);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					device = recordToDevice(rs);
				}
			} catch (SQLException e) {
				this.addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (device == null) {
				addLog("device is null", LogConstants.WARNING);
			}
		}
		return device;
	}

	public DeviceDbObj getCentralByGroupId(String groupId) {
		DeviceDbObj device = null;
		if (!TextUtils.isEmpty(groupId)) {
			String query = "SELECT * FROM B2F_DEVICE dev WHERE GROUP_ID = ? AND CENTRAL = ? AND ACTIVE = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, groupId);
				prepStmt.setBoolean(2, true);
				prepStmt.setBoolean(3, true);
				logQuery(getMethodName(), prepStmt);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					device = recordToDevice(rs);
				}
			} catch (SQLException e) {
				this.addLog("getCentralByGroupId", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (device == null) {
				addLog("getCentralByGroupId", "device is null", LogConstants.WARNING);
			}
		}
		return device;
	}

	public void resetBrowsersForGroupId(String groupId) {
		// this is wholy inefficient
		int expired = 0;
		ArrayList<DeviceDbObj> devices = this.getDevicesByGroupId(groupId, false);
		for (DeviceDbObj device : devices) {
			expired += expireDeviceBrowsers(device);
		}
		this.addLog(expired + " browsers were expired");
	}

	public int expireDeviceBrowsers(DeviceDbObj device) {
		int expired = 0;
		ArrayList<BrowserDbObj> browsers = this.getBrowsersForDevice(device);
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		for (BrowserDbObj browser : browsers) {
			browser.setExpireDate(now);
			updateBrowser(browser);
			expired++;
		}
		return expired;
	}

	public ArrayList<BrowserDbObj> getBrowsersForDevice(DeviceDbObj device) {
		ArrayList<BrowserDbObj> browsers = new ArrayList<>();
		String query = "SELECT * FROM B2F_BROWSER WHERE DEVICE_ID = ? AND ACTIVE = ?";
		BrowserDbObj browser;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, device.getDeviceId());
			prepStmt.setBoolean(2, true);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				browser = this.recordToBrowser(rs);
				browsers.add(browser);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return browsers;
	}

	public DeviceDbObj getDeviceByBrowserToken(String browserToken) {
		DeviceDbObj device = null;
		if (!TextUtils.isEmpty(browserToken)) {
			String query = "SELECT device.* FROM B2F_DEVICE device JOIN B2F_TOKEN token "
					+ "ON device.DEVICE_ID = token.DEVICE_ID WHERE token.TOKEN_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, browserToken);
				logQueryImportant(getMethodName(), prepStmt);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					device = recordToDevice(rs);
				}
			} catch (SQLException e) {
				this.addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (device == null) {
				addLog("device is null", LogConstants.WARNING);
			}
		}
		return device;
	}

	public DeviceDbObj getDeviceByBrowserId(String browserId) {
		DeviceDbObj device = null;
		if (!TextUtils.isEmpty(browserId)) {
			String query = "SELECT b2fd.* FROM B2F_BROWSER b2fb JOIN B2F_DEVICE b2fd "
					+ "ON b2fb.DEVICE_ID = b2fd.DEVICE_ID WHERE b2fb.BROWSER_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, browserId);
				logQuery(getMethodName(), prepStmt);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					device = recordToDevice(rs);
				}
			} catch (SQLException e) {
				this.addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (device == null) {
				addLog("device is null for browserId: " + browserId, LogConstants.WARNING);
			}
		}
		return device;
	}

	public DeviceDbObj getDeviceByDeviceId(String deviceId, String src) {
		return getDeviceByDeviceId(deviceId, false, src);
	}

	public DeviceDbObj getDeviceByDeviceId(String deviceId) {
		return getDeviceByDeviceId(deviceId, false, "");
	}

	public DeviceDbObj getDeviceByDeviceId(String deviceId, boolean activeOnly, String src) {
		DeviceDbObj device = null;
		if (!TextUtils.isEmpty(deviceId)) {
			String query = "SELECT * FROM B2F_DEVICE WHERE DEVICE_ID = ?";
			if (activeOnly) {
				query += " AND ACTIVE = ?";
			}
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, deviceId);
				if (activeOnly) {
					prepStmt.setBoolean(2, true);
				}
				this.addLog("from: " + src);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					device = recordToDevice(rs);
				}
			} catch (SQLException e) {
				this.addLog("getDeviceByDeviceId", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (device == null) {
				addLog(deviceId, "device with id '" + deviceId + "' is null -> src: " + src, LogConstants.WARNING);
			}
		}
		return device;
	}

	public DeviceDbObj getExpiredDevice(String deviceId) {
		DeviceDbObj device = null;
		try {
			if (!TextUtils.isEmpty(deviceId) && deviceId.length() > 18) {
				String query = "SELECT * FROM B2F_DEVICE WHERE DEVICE_ID LIKE ?";
				Connection conn = null;
				PreparedStatement prepStmt = null;
				ResultSet rs = null;
				try {
					conn = MySqlConn.getConnection();
					prepStmt = conn.prepareStatement(query);
					prepStmt.setString(1, "%" + deviceId);
					logQuery(getMethodName(), prepStmt);
					rs = executeQuery(prepStmt);
					if (rs.next()) {
						device = recordToDevice(rs);
					}
				} catch (SQLException e) {
					this.addLog("getExpiredDevice", e);
				} finally {
					MySqlConn.close(rs, prepStmt, conn);
				}
				if (device == null) {
					addLog(deviceId, "expired device not found", LogConstants.WARNING);
				}
			}
		} catch (Exception e) {
			this.addLog("getExpiredDevice", e);
		}
		return device;
	}

	public ArrayList<DeviceDbObj> getActiveDevicesFromGroup(GroupDbObj group) {
		return getDevicesFromGroup(group, true);
	}

	public ArrayList<DeviceDbObj> getDevicesFromGroup(GroupDbObj group, boolean activeOnly) {
		return getDevicesByGroupId(group.getGroupId(), activeOnly);
	}

	public ArrayList<DeviceDbObj> getDevicesFromGroupDesc(GroupDbObj group, boolean activeOnly) {
		return getDevicesByGroupIdDesc(group.getGroupId(), activeOnly);
	}

	public ArrayList<DeviceDbObj> getActiveDevicesByGroupId(String groupId) {
		return getDevicesByGroupId(groupId, true);
	}

	public ArrayList<DeviceDbObj> getNonTempDevicesByGroupId(String groupId, boolean activeOnly) {
		ArrayList<DeviceDbObj> otherDevices = new ArrayList<DeviceDbObj>();
		String query = "SELECT " + deviceFields + " FROM B2F_DEVICE WHERE GROUP_ID = ? AND DEVICE_CLASS != ? ";
		if (activeOnly) {
			query += "AND ACTIVE = ?";
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, groupId);
			prepStmt.setString(2, DeviceClass.TEMP.toString());
			if (activeOnly) {
				prepStmt.setBoolean(3, true);
			}
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			int i = 0;
			while (rs.next()) {
				otherDevices.add(i, recordToDevice(rs));
				i++;
			}
		} catch (SQLException e) {
			addLog("getDevicesByGroupId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return otherDevices;
	}

	public ArrayList<DeviceDbObj> getDevicesByGroupId(String groupId, boolean activeOnly) {
		ArrayList<DeviceDbObj> otherDevices = new ArrayList<DeviceDbObj>();
		String query = "SELECT " + deviceFields + " FROM B2F_DEVICE WHERE GROUP_ID = ? ";
		if (activeOnly) {
			query += "AND ACTIVE = ? ";
		}
		query += "ORDER BY CENTRAL";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, groupId);
			if (activeOnly) {
				prepStmt.setBoolean(2, true);
			}
			logQueryImportant(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			int i = 0;
			while (rs.next()) {
				otherDevices.add(i, recordToDevice(rs));
				i++;
			}
		} catch (SQLException e) {
			addLog("getDevicesByGroupId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return otherDevices;
	}

	public ArrayList<DeviceDbObj> getDevicesByGroupIdDesc(String groupId, boolean activeOnly) {
		ArrayList<DeviceDbObj> otherDevices = new ArrayList<DeviceDbObj>();
		String query = "SELECT " + deviceFields
				+ " FROM B2F_DEVICE WHERE (GROUP_ID = ? OR GROUP_ID = ?) AND DEVICE_TYPE IS NOT NULL ";
		if (activeOnly) {
			query += "AND ACTIVE = ? ";
		}
		query += "ORDER BY CENTRAL DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, groupId);
			prepStmt.setString(2, "X-" + groupId);
			if (activeOnly) {
				prepStmt.setBoolean(3, true);
			}
			logQueryImportant(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			int i = 0;
			while (rs.next()) {
				otherDevices.add(i, recordToDevice(rs));
				i++;
			}
		} catch (SQLException e) {
			addLog("getDevicesByGroupId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return otherDevices;
	}

	public int getCheckCount(DeviceDbObj device) {
		int checkCount = 0;
		ArrayList<DeviceConnectionDbObj> connections = this.getConnectionsForDevice(device, true);
		addLog("number of connections = " + connections.size());
		for (DeviceConnectionDbObj connection : connections) {
			ArrayList<CheckDbObj> checks = this.getAllChecksByDeviceIds(connection.getCentralDeviceId(),
					connection.getPeripheralDeviceId());
			checkCount += checks.size();
		}
		addLog("number of checks = " + checkCount);
		return checkCount;
	}

	public void deactivateAllDevicesForGroup(String groupId) {
		new Thread(() -> {
			String query = "UPDATE B2F_DEVICE SET ACTIVE = ?, LOGIN_TOKEN = ?, FCM_ID = ?, "
					+ "GROUP_ID = CONCAT('X-', GROUP_ID), DEVICE_ID = CONCAT('X-', DEVICE_ID) "
					+ "WHERE GROUP_ID = ? AND ACTIVE = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, false);
				prepStmt.setString(2, "");
				prepStmt.setString(3, "");
				prepStmt.setString(4, groupId);
				prepStmt.setBoolean(5, true);
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (Exception e) {
				addLog("deactivateAllDevicesForGroup", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void deactivateDevice(DeviceDbObj device) {
		new Thread(() -> {
			String query = "UPDATE B2F_DEVICE SET ACTIVE = ?, LOGIN_TOKEN = ?, "
					+ "DEVICE_ID = CONCAT('X-', DEVICE_ID) WHERE DEVICE_ID = ? AND ACTIVE = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, false);
				prepStmt.setString(2, "");
				prepStmt.setString(3, device.getDeviceId());
				prepStmt.setBoolean(4, true);

				logQuery("deactivateDevice", prepStmt);
				prepStmt.executeUpdate();
			} catch (Exception e) {
				addLog(device.getDeviceId(), "deactivateDevice", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void deactivateConnectionForDevice(DeviceDbObj device) {
		new Thread(() -> {
			String query = "UPDATE B2F_DEVICE_CONNECTION SET ACTIVE = ?, LAST_SUBSCRIBED = ?, "
					+ "LAST_BLUETOOTH_SUCCESS = ?, INSTALL_COMPLETE = ? WHERE PERIPHERAL_DEVICE_ID = ? "
					+ "OR CENTRAL_DEVICE_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				Timestamp baseTime = DateTimeUtilities.getBaseTimestamp();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, false);
				prepStmt.setTimestamp(2, baseTime);
				prepStmt.setTimestamp(3, baseTime);
				prepStmt.setBoolean(4, false);
				prepStmt.setString(5, device.getDeviceId());
				prepStmt.setString(6, device.getDeviceId());
				logQuery(prepStmt);
				prepStmt.executeUpdate();
			} catch (Exception e) {
				addLog(device.getDeviceId(), "deactivateConnectionForDevice", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	public void deactivateAllConnectionsForGroup(String groupId) {
		new Thread(() -> {
			String query = "UPDATE B2F_DEVICE_CONNECTION SET ACTIVE = ?, LAST_SUBSCRIBED = ?, "
					+ "LAST_BLUETOOTH_SUCCESS = ?, INSTALL_COMPLETE = ? WHERE GROUP_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				Timestamp baseTime = DateTimeUtilities.getBaseTimestamp();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, false);
				prepStmt.setTimestamp(2, baseTime);
				prepStmt.setTimestamp(3, baseTime);
				prepStmt.setBoolean(4, false);
				prepStmt.setString(5, groupId);
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (Exception e) {
				addLog("deactivateAllConnectionsForGroup", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	public DeviceDbObj getDeviceByDeviceIdAandUser(String deviceId, String userId) {
		DeviceDbObj device = null;
		String query = "SELECT " + deviceFields + " FROM B2F_DEVICE WHERE DEVICE_ID = ? AND USER_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setString(2, userId);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				device = recordToDevice(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getDeviceByDeviceIdAandUser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		if (device == null) {
			addLog("deviceId", "device is null");
		}
		return device;
	}

	public boolean expireChecksForDevice(DeviceDbObj device) {
		boolean success = false;
		String sql;
		if (device.isCentral()) {
			sql = "UPDATE B2F_CHECK SET EXPIRED = ? WHERE CENTRAL_DEVICE_ID = ?";
		} else {
			sql = "UPDATE B2F_CHECK SET EXPIRED = ? WHERE PERIPHERAL_DEVICE_ID = ?";
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setBoolean(1, true);
			prepStmt.setString(2, device.getDeviceId());
			this.logQueryImportant("expireChecksForDevice", prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			this.addLog("expireChecksForDevice", e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public void deactivateTempDevice(DeviceDbObj device) {
		if (device.getDeviceClass() == DeviceClass.TEMP) {
			updateQuickAccess(device, false);
			setActive(device, false);
//			updateDevice(device, "deactivateTempDevice");
		}
	}

	public boolean expirePushAndBioChecksForCentralDevice(DeviceDbObj device) {
		boolean success = false;
		String sql;
		if (!device.isCentral()) {
			sql = "UPDATE B2F_CHECK SET EXPIRED = ? WHERE CENTRAL_DEVICE_ID = ? AND CHECK_TYPE != ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(sql);
				prepStmt.setBoolean(1, true);
				prepStmt.setString(2, device.getDeviceId());
				prepStmt.setString(3, CheckType.PROX.toString().toLowerCase());
				prepStmt.executeUpdate();
				success = true;
			} catch (Exception e) {
				this.addLog(e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}
		return success;
	}

	public boolean expirePushAndBioChecksForPeripheralDevice(DeviceDbObj device) {
		boolean success = false;
		String sql;
		if (!device.isCentral()) {
			sql = "UPDATE B2F_CHECK SET EXPIRED = ? WHERE PERIPHERAL_DEVICE_ID = ? AND CHECK_TYPE != ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(sql);
				prepStmt.setBoolean(1, true);
				prepStmt.setString(2, device.getDeviceId());
				prepStmt.setString(3, CheckType.PROX.toString().toLowerCase());
				prepStmt.executeUpdate();
				success = true;
			} catch (Exception e) {
				this.addLog(e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}
		return success;
	}

	public boolean expirePushAndBioChecksForConnection(DeviceConnectionDbObj connection) {
		boolean success = false;
		String sql = "UPDATE B2F_CHECK SET EXPIRED = ? WHERE PERIPHERAL_DEVICE_ID = ? "
				+ "AND CENTRAL_DEVICE_ID = ? AND CHECK_TYPE != ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setBoolean(1, true);
			prepStmt.setString(2, connection.getPeripheralDeviceId());
			prepStmt.setString(3, connection.getCentralDeviceId());
			prepStmt.setString(4, CheckType.PROX.toString().toLowerCase());
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}

		return success;
	}

	public boolean deviceIsTemp(DeviceDbObj device) {
		boolean temp = false;
		if (device != null) {
			if (device.getDeviceClass() != null) {
				if (device.getDeviceClass().toString().equals(DeviceClass.TEMP.toString())) {
					temp = true;
				}
			}
		}
		this.addLog("temp: " + temp);
		return temp;
	}

	public boolean isCentralDeviceAuthorized(DeviceDbObj centralDevice) {
		boolean authed = false;
		Timestamp authTimeLimit = getAuthTimeLimit();
		String sql = "SELECT " + authFields + " FROM B2F_AUTHORIZATION WHERE "
				+ "CENTRAL_DEVICE = ? AND REQUESTING_DEVICE != ? AND AUTHORIZATION_COMPLETED = ? AND "
				+ "USER_ID = ? AND AUTHORIZATION_TIME > ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, centralDevice.getDeviceId());
			prepStmt.setString(2, centralDevice.getDeviceId());
			prepStmt.setBoolean(3, false);
			prepStmt.setString(4, centralDevice.getUserId());
			prepStmt.setTimestamp(5, authTimeLimit);
			logQueryImportant(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				authed = true;
			}
		} catch (SQLException e) {
			this.addLog("isCentralDeviceAuthorized", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		addLog(centralDevice.getDeviceId(), "auth: " + authed);
		return authed;
	}

	public BrowserDbObj getBrowserForDevice(DeviceDbObj device) {
		return getBrowserForDevice(device.getDeviceId());
	}

	// TODO: is this dangerous? - cjm
	private BrowserDbObj getBrowserForDevice(String deviceId) {
		BrowserDbObj browser = null;
		String sql = "SELECT * FROM B2F_BROWSER WHERE DEVICE_ID = ? AND EXPIRE_DATE > ? ORDER BY CREATE_DATE DESC";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, deviceId);
			prepStmt.setTimestamp(2, now);
			rs = executeQuery(prepStmt);
			int i = 0;
			while (rs.next()) { // this is awkward - cjm
				if (i == 0) {
					addLog("browserFound");
					browser = recordToBrowser(rs);
				} else {
					addLog("another browserFound");
					browser = null;
					break;
				}
				i++;
			}
		} catch (SQLException e) {
			this.addLog("didCentralDeviceAuthorizeAddBrowser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return browser;
	}

	public boolean didCentralDeviceAuthorizeAddBrowser(DeviceDbObj centralDevice) {
		boolean authed = false;
		Timestamp authTimeLimit = getAuthTimeLimit();
		String sql = "SELECT " + authFields + " FROM B2F_AUTHORIZATION WHERE "
				+ "CENTRAL_DEVICE = ? AND REQUESTING_DEVICE = ? AND AUTHORIZATION_COMPLETED = ? AND "
				+ "USER_ID = ? AND AUTHORIZATION_TIME > ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, centralDevice.getDeviceId());
			prepStmt.setString(2, centralDevice.getDeviceId());
			prepStmt.setBoolean(3, false);
			prepStmt.setString(4, centralDevice.getUserId());
			prepStmt.setTimestamp(5, authTimeLimit);
			logQueryImportant(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				authed = true;
			}
		} catch (SQLException e) {
			this.addLog("didCentralDeviceAuthorizeAddBrowser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		addLog(centralDevice.getDeviceId(), "auth: " + authed);
		return authed;
	}

	public boolean isPeripheralDeviceAuthorized(DeviceDbObj peripheralDevice) {
		DeviceDbObj centralDevice = this.getConnectedCentral(peripheralDevice);
		boolean authed = false;
		Timestamp authTimeLimit = getAuthTimeLimit();
		String sql = "SELECT " + authFields + " FROM B2F_AUTHORIZATION WHERE "
				+ "CENTRAL_DEVICE = ? AND AUTHORIZATION_COMPLETED = ? AND USER_ID = ? AND AUTHORIZATION_TIME > ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, centralDevice.getDeviceId());
			prepStmt.setBoolean(2, false);
			prepStmt.setString(3, peripheralDevice.getUserId());

			prepStmt.setTimestamp(4, authTimeLimit);
			logQueryImportant(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				authed = true;
			}
		} catch (SQLException e) {
			this.addLog("isPeripheralDeviceAuthorized", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return authed;
	}

	public boolean isPeripheralDeviceOn(DeviceConnectionDbObj conn) {
		boolean deviceOn = false;
		DeviceDbObj device = this.getDeviceByDeviceId(conn.getPeripheralDeviceId(), true, "isDeviceOn");
		if (device != null) {
			deviceOn = !device.isScreensaverOn() && !device.isTurnedOff();
		}
		return deviceOn;
	}

	public boolean wasRecentPush(DeviceDbObj device) {
		Timestamp lastPush = device.getLastPush();
		long secondsAgo = DateTimeUtilities.timestampSecondAgo(lastPush);
		return secondsAgo < 120;
	}

	public OsClass getCentralType(DeviceConnectionDbObj conn) {
		String centralId = conn.getCentralDeviceId();
		DeviceDbObj central = this.getDeviceByDeviceId(centralId, "getCentralType");
		OsClass osClass = OsClass.UNKNOWN;
		if (central != null) {
			osClass = central.getOperatingSystem();
		}
		return osClass;
	}

	public DeviceDbObj getConnectedCentral(DeviceDbObj device) {
		DeviceDbObj central = null;
		DeviceConnectionDbObj connection = this.getConnectionForPeripheral(device, true);
		if (connection != null) {
			central = new DeviceDataAccess().getDeviceByDeviceId(connection.getCentralDeviceId(),
					"getConnectedCentral");
		}
		return central;
	}

	/*
	 * "CREATE_DATE, CONNECTION_ID, PERIPHERAL_DEVICE_ID, CENTRAL_DEVICE_ID, " +
	 * "SERVICE_UUID, CHARACTER_UUID, GROUP_ID, ACTIVE, LAST_CHECK, LAST_SUCCESS, "
	 * +
	 * "ADVERTISING_PERIPHERAL, CONNECTING_CENTRAL, SUBSCRIBED, INSTALL_COMPLETE, "
	 * + "PERIPHERAL_CONNECTED, LAST_PERIPHERAL_CONNECTION_SUCCESS," +
	 * "CENTRAL_CONNECTED, LAST_CENTRAL_CONNECTION_SUCCESS";
	 */
	public boolean deactivateConnections(DeviceDbObj device) {
		boolean success = false;
		String query = "UPDATE B2F_DEVICE_CONNECTION SET ACTIVE = 0, SUBSCRIBED = 0, "
				+ "CENTRAL_CONNECTED = 0, PERIPHERAL_CONNECTED = 0 WHERE PERIPHERAL_DEVICE_ID = ? "
				+ "OR CENTRAL_DEVICE_ID = ?";
		int count = 0;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, device.getDeviceId());
			prepStmt.setString(2, device.getDeviceId());
			logQuery(getMethodName(), prepStmt);
			count = prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		addLog("expired " + count + " connections");
		return success;
	}

	public boolean expireKeysForUser(String groupId) {
		boolean success = false;
		ArrayList<DeviceDbObj> deviceList = this.getDevicesByGroupId(groupId, false);
		String qMarks = "";
		int i = 0;
		for (i = 0; i < deviceList.size(); i++) {
			if (i > 0) {
				qMarks += ", ";
			}
			qMarks += "?";
		}
		String query = "UPDATE B2F_KEY SET ACTIVE = ? WHERE DEVICE_ID IN (" + qMarks + ")";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			int index = 2;
			for (DeviceDbObj currDevice : deviceList) {
				prepStmt.setString(index++, currDevice.getDeviceId());
			}
			logQueryImportant(getMethodName(), prepStmt);
			if (i > 0) {
				prepStmt.executeUpdate();
			}
			success = true;
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		addLog("expiring keys " + success);
		return success;
	}

	public boolean deactivateKeyTypeForDeviceAndUrlExcept(KeyType keyType, String deviceId, String url,
			String exceptId) {
		boolean success = false;
		String query = "UPDATE B2F_KEY SET ACTIVE = ? WHERE DEVICE_ID = ? AND KEY_TYPE = ? "
				+ "AND URL = ? AND KEY_ID != ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, deviceId);
			prepStmt.setString(3, keyType.toString());
			prepStmt.setString(4, url);
			prepStmt.setString(5, exceptId);
			logQueryImportant(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public boolean deactivateKeyTypeForBrowserAndUrlExcept(KeyType keyType, String browserId, String url,
			String exceptId) {
		boolean success = false;
		String query = "UPDATE B2F_KEY SET ACTIVE = ? WHERE BROWSER_ID = ? AND KEY_TYPE = ? "
				+ "AND URL = ? AND KEY_ID != ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, browserId);
			prepStmt.setString(3, keyType.toString());
			prepStmt.setString(4, url);
			prepStmt.setString(5, exceptId);
			logQueryImportant(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public boolean expireKeysForDevice(ArrayList<DeviceDbObj> deviceList) {
		boolean success = false;

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < deviceList.size(); i++) {
			builder.append("?,");
		}
		String query = "UPDATE B2F_KEYS SET ACTIVE = ? WHERE DEVICE_ID IN ("
				+ builder.deleteCharAt(builder.length() - 1).toString() + ")";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			int index = 1;
			for (DeviceDbObj device : deviceList) {
				prepStmt.setString(index++, device.getDeviceId());
				prepStmt.executeUpdate();
				logQueryImportant(getMethodName(), prepStmt);
				success = true;
			}
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public boolean expireChecksForCentral(DeviceDbObj device, String src) {
		boolean success;
		if (device.isCentral()) {
			success = expireChecksForCentral(device.getDeviceId(), src);
		} else {
			success = expireChecksForCentral(this.getConnectedCentral(device), src);
		}
		return success;
	}

	public boolean expireChecksForCentral(String deviceId, String src) {
		boolean success = false;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "UPDATE B2F_CHECK SET EXPIRATION_DATE = ?, EXPIRED = ? WHERE (EXPIRATION_DATE > ? || EXPIRED = "
				+ "?) AND CENTRAL_DEVICE_ID = ? ";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setTimestamp(1, now);
			prepStmt.setBoolean(2, true);
			prepStmt.setTimestamp(3, now);
			prepStmt.setBoolean(4, false);
			prepStmt.setString(5, deviceId);
			this.logQueryImportant("expireChecksForCentral", prepStmt);
			prepStmt.executeUpdate();
			success = true;
			this.addLog(deviceId, "success");
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public Timestamp getLastSuccess(DeviceDbObj device) {
		Timestamp lastSuccess = null;
		if (device.isCentral()) {
			ArrayList<DeviceConnectionDbObj> connections = getConnectionsForCentral(device);
			for (DeviceConnectionDbObj connection : connections) {
				if (lastSuccess == null) {
					lastSuccess = connection.getLastSuccess();
				} else {
					if (connection.getLastSuccess() != null && connection.getLastSuccess().after(lastSuccess)) {
						lastSuccess = connection.getLastSuccess();
					}
				}
			}
		} else {
			DeviceConnectionDbObj connection = getConnectionForPeripheral(device, true);
			if (connection != null) {
				lastSuccess = connection.getLastSuccess();
			}
		}
		return lastSuccess;
	}

	public boolean isConnecting(DeviceDbObj device) {
		boolean isConnecting = false;
		if (device.isCentral()) {
			ArrayList<DeviceConnectionDbObj> connections = getConnectionsForCentral(device);
			for (DeviceConnectionDbObj connection : connections) {
				if (connection.getAdvertisingPeripheral() != null
						&& DateTimeUtilities.timestampSecondAgo(connection.getAdvertisingPeripheral()) < 30) {
					isConnecting = true;
					break;
				}
			}
		} else {
			DeviceConnectionDbObj conn = getConnectionForPeripheral(device, true);
			if (conn != null) {
				if (conn.getAdvertisingPeripheral() != null
						&& DateTimeUtilities.timestampSecondAgo(conn.getAdvertisingPeripheral()) < 30) {
					isConnecting = true;
				}
			} else {
				addLog(device.getDeviceId(), "connection was null", LogConstants.WARNING);
			}
		}
		return isConnecting;
	}

	public Timestamp getConnectingCentral(DeviceDbObj device) {
		Timestamp connectingCentral = null;
		if (device.isCentral()) {
			ArrayList<DeviceConnectionDbObj> connections = getConnectionsForCentral(device);
			for (DeviceConnectionDbObj connection : connections) {
				if (connectingCentral == null) {
					connectingCentral = connection.getConnectingCentral();
				} else {
					if (connection.getConnectingCentral() != null
							&& connection.getConnectingCentral().after(connectingCentral)) {
						connectingCentral = connection.getConnectingCentral();
					}
				}
			}
		} else {
			DeviceConnectionDbObj connection = getConnectionForPeripheral(device, true);
			connectingCentral = connection.getConnectingCentral();
		}
		return connectingCentral;
	}

	public boolean getConnecting(DeviceDbObj device) {
		boolean connecting = false;
		if (device.isCentral()) {
			ArrayList<DeviceConnectionDbObj> conns = this.getConnectionsForCentral(device);
			for (DeviceConnectionDbObj connection : conns) {
				if (getConnecting(connection)) {
					connecting = true;
					break;
				}
			}
		} else {
			DeviceConnectionDbObj conn = this.getConnectionForPeripheral(device, true);
			connecting = getConnecting(conn);
		}
		return connecting;
	}

	boolean updateDeviceSuccesses(DeviceDbObj device1, DeviceDbObj device2, Boolean fromCentral) {
		DeviceConnectionDbObj connection = getConnectionForDevices(device1, device2);
		return updateProximateDeviceSuccesses(connection, fromCentral);
	}

	public void updateLastVariableRetrieval(DeviceDbObj device, boolean computerAwake) {
		new Thread(() -> {
			String query = "UPDATE B2F_DEVICE SET LAST_VARIABLE_RETRIEVAL = ?, TURNED_OFF = ?, "
					+ "PUSH_FAILURE = ?, SCREENSAVER_ON = ? WHERE DEVICE_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, DateTimeUtilities.getCurrentTimestamp());
				prepStmt.setBoolean(2, false);
				prepStmt.setBoolean(3, false);
				prepStmt.setBoolean(4, !computerAwake);
				prepStmt.setString(5, device.getDeviceId());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();

			} catch (SQLException e) {
				this.addLog(device.getDeviceId(), e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	public boolean updateDeviceAwake(DeviceDbObj device, boolean computerAwake, boolean ignoreAccess) {
		boolean success = true;
		if (!device.isCentral()) {
			String query = "UPDATE B2F_DEVICE SET SCREENSAVER_ON = ?, LAST_VARIABLE_RETRIEVAL = ? WHERE DEVICE_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			try {

				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, !computerAwake);
				prepStmt.setTimestamp(2, now);
				prepStmt.setString(3, device.getDeviceId());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				this.addLog(device.getDeviceId(), "set screensaver on to " + !computerAwake, LogConstants.IMPORTANT);
				if (!ignoreAccess) {
					device.setScreensaverOn(!computerAwake);
					if (computerAwake) {
						isAccessAllowed(device, "updateDeviceAwake");
					} else {
						validateWithConnectionLog(device, false, null);
//						this.addConnectionLogIfNeeded(device, false, query, query, null);
					}
				}
				success = true;
			} catch (SQLException e) {
				this.addLog(device.getDeviceId(), e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}
		return success;
	}

	public boolean updateDeviceAwake(DeviceConnectionDbObj connection, DeviceDbObj device, boolean computerAwake,
			boolean ignoreAccess) {
		boolean success = true;
		if (!device.isCentral()) {
			String query = "UPDATE B2F_DEVICE SET SCREENSAVER_ON = ?, LAST_VARIABLE_RETRIEVAL = ? WHERE DEVICE_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			try {

				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, !computerAwake);
				prepStmt.setTimestamp(2, now);
				prepStmt.setString(3, device.getDeviceId());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				this.addLog(device.getDeviceId(), "set screensaver on to " + !computerAwake, LogConstants.IMPORTANT);
				if (!ignoreAccess) {
					device.setScreensaverOn(!computerAwake);
					if (computerAwake) {
						isAccessAllowed(device, "updateDeviceAwake");
					} else {
						validateWithConnectionLog(connection, device, false, null);
					}
				}
				success = true;
			} catch (SQLException e) {
				this.addLog(device.getDeviceId(), e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}
		return success;
	}

	/*
	 * Use of this fution should be limited to setup
	 */
	public boolean updateDevice(DeviceDbObj device, String src) {
		boolean success = false;
		String query = "UPDATE B2F_DEVICE SET GROUP_ID = ?, USER_ID = ?, SEED = ?, ACTIVE = ?, " + "FCM_ID = ?, "
				+ "BT_ADDRESS = ?, DEVICE_TYPE = ?, OPERATING_SYSTEM= ?, LOGIN_TOKEN = ?, "
				+ "OS_VERSION = ?, USER_LANGUAGE = ?, LAST_GMT_OFFSET = ?, "
				+ "SCREEN_SIZE= ?, RAND = ?, SHOW_ICON = ?, "
				+ "DEVICE_PRIORITY = ?, TRIGGER_UPDATE = ?, RECENT_PUSHES = ?, "
				+ "UNRESPONSIVE = ?, LAST_PUSH = ?, PUSH_LOUD = ?, PUSH_FAILURE = ?, "
				+ "COMMAND = ?, TEMP = ?, CENTRAL = ?, SCREENSAVER_ON = ?, "
				+ "DEVICE_CLASS = ?, LAST_UPDATE = ?, HAS_BLE = ?, TERMINATE = ?";

		query += " WHERE DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, device.getGroupId());
			prepStmt.setString(2, device.getUserId());
			prepStmt.setInt(3, device.getSeed());
			prepStmt.setBoolean(4, device.isActive());
			prepStmt.setString(5, device.getFcmId());
			prepStmt.setString(6, device.getBtAddress());
			prepStmt.setString(7, device.getDeviceType());
			prepStmt.setString(8, device.getOperatingSystem().osClassName());
			prepStmt.setString(9, device.getLoginToken());
			prepStmt.setString(10, device.getOsVersion());
			prepStmt.setString(11, device.getUserLanguage());
			prepStmt.setInt(12, device.getLastGmtOffset());
			prepStmt.setString(13, device.getScreenSize());
			prepStmt.setString(14, device.getRand());
			prepStmt.setBoolean(15, device.getShowIcon());
			prepStmt.setDouble(16, device.getDevicePriority());
			prepStmt.setBoolean(17, device.getTriggerUpdate());
			prepStmt.setInt(18, device.getRecentPushes());
			prepStmt.setBoolean(19, device.getUnresponsive());

			prepStmt.setString(20, DateTimeUtilities.utilDateToSqlStringDt(device.getLastPush()));
			prepStmt.setBoolean(21, device.isPushLoud());
			prepStmt.setBoolean(22, device.isPushFailure());
			prepStmt.setString(23, device.getCommand());
			prepStmt.setString(24, device.getTemp());
			prepStmt.setBoolean(25, device.isCentral());
			prepStmt.setBoolean(26, device.isScreensaverOn());
			prepStmt.setString(27, device.getDeviceClass().deviceClassName());
			prepStmt.setTimestamp(28, DateTimeUtilities.getCurrentTimestamp());
			prepStmt.setBoolean(29, device.getHasBle());
			prepStmt.setBoolean(30, device.getTerminate());

			prepStmt.setString(31, device.getDeviceId());

			logQueryImportant(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			this.addLog(device.getDeviceId(), device.getDeviceType() + " called from " + src);
			success = true;
		} catch (SQLException e) {
			this.addLog(device.getDeviceId(), e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public boolean updateDeviceMap(DeviceDbObj device, HashMap<String, Object> hm) {
		boolean success = false;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			StringBuilder query = new StringBuilder("UPDATE B2F_DEVICE SET ");
			boolean first = true;
			for (String field : hm.keySet()) {
				if (!first) {
					query.append(", ");
				} else {
					first = false;
				}
				query.append(field + " = ?");
			}
			query.append(" WHERE DEVICE_ID = ?");
			prepStmt = conn.prepareStatement(query.toString());
			int j = 1;
			for (Object value : hm.values()) {
				if (value instanceof String) {
					prepStmt.setString(j, (String) value);
				} else if (value instanceof Boolean) {
					prepStmt.setBoolean(j, (Boolean) value);
				} else if (value instanceof Timestamp) {
					prepStmt.setTimestamp(j, (Timestamp) value);
				} else if (value instanceof Date) {
					prepStmt.setDate(j, (java.sql.Date) value);
				} else if (value instanceof Integer) {
					prepStmt.setInt(j, (Integer) value);
				} else {
					MySqlConn.close(prepStmt, conn);
					throw new SQLException("could not update: " + value + ": bad type " + value.getClass().getName()
							+ " in the map: " + getHashMapAsString(hm));
				}
				j++;
			}
			prepStmt.setString(j, device.getDeviceId());
			this.logQueryImportant("updateDeviceMap", prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (SQLException e) {
			this.addLog(device.getDeviceId(), "could not update some value in the map: " + getHashMapAsString(hm)
					+ " -- msg: " + e.getLocalizedMessage(), LogConstants.ERROR);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public void updateDeviceMapAsync(DeviceDbObj device, HashMap<String, Object> hm) {
		new Thread(() -> {
			updateDeviceMap(device, hm);
		}).start();
	}

	/**
	 * isPathThroughAllowed - This has to deal with how to handle how unknown users
	 * are handled - If the user has has been signed up, but has no devices
	 * registered, then we allow them through if ALLOW_NO_DEVICE is the non-member
	 * strategy - If the user does not exist within the company, then we allow them
	 * through if the non-member strategy is ALLOW_NOT_SIGNED_UP
	 * 
	 * @param company
	 * @param group
	 * @return
	 */
	public boolean isPassThroughAllowed(CompanyDbObj company, GroupDbObj group) {
		boolean passThrough = false;
		if (company != null) {
			NonMemberStrategy nms = company.getNonMemberStrategy();
			try {
				if (group == null && (nms == NonMemberStrategy.ALLOW_NOT_SIGNED_UP
						|| nms == NonMemberStrategy.ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE)) {
					passThrough = true;
				} else if (group.isUserExempt() && !group.getGroupName().equals(Constants.ANONYMOUS_GROUP)) {
					passThrough = true;
				} else if (group != null
						&& (nms == NonMemberStrategy.ALLOW_NO_DEVICE
								|| nms == NonMemberStrategy.ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE)
						&& this.getNonTempDevicesByGroupId(group.getGroupId(), true).size() == 0) {
					passThrough = true;
				}
			} catch (Exception e) {
				addLog("error getting passthrough could be because the device id used is no longer active");
				addLog(e);
			}
			addLog("passThrough: " + passThrough + " because non-member strategy = " + nms.name());
		}
		return passThrough;
	}

//	public DeviceDbObj updateScreenSaverStatus(DeviceDbObj device, boolean screensaverOn) {
//		// update as bluetooth subscribed or unsubscribed from peripheral
//		setScreensaverOn(device, screensaverOn);
//		addLog(device.getDeviceId(), "ss on: " + screensaverOn, LogConstants.DEBUG);
//		if (screensaverOn) {
//			if (!device.isCentral()) {
//				DeviceConnectionDataAccess dataAccess = new DeviceConnectionDataAccess();
//				DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(device, true);
//				if (connection != null) {
//					dataAccess.addConnectionLogIfNeeded(connection, false, device.getDeviceId(),
//							"screensaverActivated");
//				}
//			}
//		}
//		return device;
//	}

	public boolean updateStringForDevice(DeviceDbObj device, String fieldName, String value) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put(fieldName, value);
		return updateDeviceMap(device, hm);
	}

	public boolean updateIntForDevice(DeviceDbObj device, String fieldName, int value) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put(fieldName, value);
		return updateDeviceMap(device, hm);
	}

	public boolean updateBooleanForDevice(DeviceDbObj device, String fieldName, boolean value) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put(fieldName, value);
		return updateDeviceMap(device, hm);
	}

	public boolean updateDateForDevice(DeviceDbObj device, String fieldName, Date value) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put(fieldName, value);
		return updateDeviceMap(device, hm);
	}

	public boolean updateTimestampForDevice(DeviceDbObj device, String fieldName, Timestamp value) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put(fieldName, value);
		return updateDeviceMap(device, hm);
	}

	public boolean setTurnedOff(DeviceDbObj device, boolean turnedOff) {
		return updateBooleanForDevice(device, "TURNED_OFF", turnedOff);
	}

	public boolean setTurnedOffFromInstaller(DeviceDbObj device, boolean turnOffFromInstaller) {
		return updateBooleanForDevice(device, "TURN_OFF_FROM_INSTALLER", turnOffFromInstaller);
	}

	public boolean setScreensaverOn(DeviceDbObj device, boolean screensaverOn) {
		return updateBooleanForDevice(device, "SCREENSAVER_ON", screensaverOn);
	}

	public boolean setActive(DeviceDbObj device, boolean active) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("ACTIVE", active);
		return updateDeviceMap(device, hm);
	}

	public boolean setActive(DeviceDbObj central, DeviceDbObj peripheral, boolean active) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("ACTIVE", active);
		updateDeviceMap(peripheral, hm);
		return updateDeviceMap(central, hm);
	}

	public boolean setActiveAndCentralLastReset(DeviceDbObj central, DeviceDbObj peripheral, boolean active,
			Timestamp ts) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("ACTIVE", active);
		updateDeviceMap(peripheral, hm);
		hm.put("LAST_RESET", ts);
		return updateDeviceMap(central, hm);
	}

	public boolean setLastSilentPushResponse(DeviceDbObj device, Timestamp ts) {
		boolean outcome = true;
		Timestamp lastTs = device.getLastSilentPushResponse();
		if (lastTs == null || DateTimeUtilities.absoluteTimestampDifferenceInSeconds(ts, lastTs) > 30) {
			outcome = updateTimestampForDevice(device, "LAST_SILENT_PUSH_RESPONSE", ts);
			HashMap<String, Object> hm = new HashMap<String, Object>();
			hm.put("LAST_SILENT_PUSH_RESPONSE", ts);
			hm.put("PUSH_FAILURE", false);
			outcome = updateDeviceMap(device, hm);
		}
		return outcome;
	}

	public boolean setBrowserInstallComplete(DeviceDbObj device, boolean browserInstallComplete) {
		return updateBooleanForDevice(device, "BROWSER_INSTALL_COMPLETE", browserInstallComplete);
	}

	public boolean setActiveAndInstallComplete(DeviceDbObj device, boolean active, boolean installComplete) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("INSTALL_COMPLETE", installComplete);
		hm.put("ACTIVE", active);
		return updateDeviceMap(device, hm);
	}

	public boolean setGroupId(DeviceDbObj device, String groupId) {
		return updateStringForDevice(device, "GROUP_ID", groupId);
	}

	public boolean setBtAddress(DeviceDbObj device, String btAddress) {
		return updateStringForDevice(device, "BT_ADDRESS", btAddress);
	}

	public boolean setGroupIdAndActive(DeviceDbObj device, String groupId, boolean active) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("GROUP_ID", groupId);
		hm.put("ACTIVE", active);
		return updateDeviceMap(device, hm);
	}

	public boolean setTurnOffFromInstallerAndLastSilentPushResponse(DeviceDbObj device, boolean turnOffFromInstaller,
			Timestamp responseTs) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("TURN_OFF_FROM_INSTALLER", turnOffFromInstaller);
		hm.put("LAST_SILENT_PUSH_RESPONSE", responseTs);
		return updateDeviceMap(device, hm);
	}

	public boolean setDeviceIdAndActive(DeviceDbObj device, String deviceId, boolean active) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("ACTIVE", active);
		hm.put("DEVICE_ID", deviceId);
		return updateDeviceMap(device, hm);
	}

	public boolean setGroupIdUserIdAndActive(DeviceDbObj device, String groupId, String userId, boolean active) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("GROUP_ID", groupId);
		hm.put("USER_ID", userId);
		hm.put("ACTIVE", active);
		return updateDeviceMap(device, hm);
	}

	public boolean setLastCompleteCheck(DeviceDbObj device, Date lastCompleteCheck) {
		return updateDateForDevice(device, "LAST_COMPLETE_CHECK", lastCompleteCheck);
	}

	public boolean setLastReset(DeviceDbObj device, Timestamp lastReset) {
		return updateTimestampForDevice(device, "LAST_RESET", lastReset);
	}

	public boolean setCommand(DeviceDbObj device, String command) {
		return updateStringForDevice(device, "COMMAND", command);
	}

	public boolean setTxPower(DeviceDbObj device, int txPower) {
		return updateIntForDevice(device, "TX_POWER", txPower);
	}

	public boolean setUnresponsive(DeviceDbObj device, boolean unresponsive) {
		return updateBooleanForDevice(device, "UNRESPONSIVE", unresponsive);
	}

	public boolean setPushFailure(DeviceDbObj device, boolean pushFailure) {
		return updateBooleanForDevice(device, "PUSH_FAILURE", pushFailure);
	}

	public boolean setTemp(DeviceDbObj device, String temp) {
		return updateStringForDevice(device, "TEMP", temp);
	}

	public boolean setFcmAndPushFailure(DeviceDbObj device, String fcm, boolean pushFailure) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("FCM_ID", fcm);
		hm.put("PUSH_FAILURE", pushFailure);
		return updateDeviceMap(device, hm);
	}

	public boolean setFcmAndUnresponsive(DeviceDbObj device, String fcm, boolean unresponsive) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("FCM_ID", fcm);
		hm.put("UNRESPONSIVE", unresponsive);
		return updateDeviceMap(device, hm);
	}

	public boolean setFcm(DeviceDbObj device, String fcm) {
		return updateStringForDevice(device, "FCM_ID", fcm);
	}

	public boolean setLoginToken(DeviceDbObj device, String loginToken) {
		return updateStringForDevice(device, "LOGIN_TOKEN", loginToken);
	}

	public boolean setShowIcon(DeviceDbObj device, boolean showIcon) {
		return updateBooleanForDevice(device, "SHOW_ICON", showIcon);
	}

	public boolean setSignedIn(DeviceDbObj device, boolean signedIn) {
		return updateBooleanForDevice(device, "SIGNED_IN", signedIn);
	}

	public boolean setHasBle(DeviceDbObj device, boolean hasBle) {
		return updateBooleanForDevice(device, "HAS_BLE", hasBle);
	}

	public boolean recordSilentPush(DeviceDbObj device) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("TRIGGER_UPDATE", true);
		hm.put("UNRESPONSIVE", true);
		hm.put("RECENT_PUSHES", device.getRecentPushes() + 1);
		hm.put("LAST_SILENT_PUSH", now);
		return updateDeviceMap(device, hm);
	}

	public boolean recordLoudPush(DeviceDbObj device) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("TRIGGER_UPDATE", true);
		hm.put("UNRESPONSIVE", true);
		hm.put("LAST_PUSH", now);
		return updateDeviceMap(device, hm);
	}

	public boolean setPushResponse(DeviceDbObj device) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("LAST_SILENT_PUSH_RESPONSE", now);
		hm.put("UNRESPONSIVE", false);
		return updateDeviceMap(device, hm);
	}

	public boolean setPushFailed(DeviceDbObj device, boolean failed) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("PUSH_FAILURE", failed);
		return updateDeviceMap(device, hm);
	}

	public boolean setPushFailedFromCheck(CheckDbObj check, boolean failed) {
		boolean success = false;
		DeviceDbObj central = this.getDeviceByDeviceId(check.getCentralDeviceId());
		if (central != null) {
			success = setPushFailed(central, failed);
		}
		return success;
	}

	public boolean setPushFailedFromConnection(DeviceConnectionDbObj connection, boolean failed) {
		boolean success = false;
		DeviceDbObj central = this.getDeviceByDeviceId(connection.getCentralDeviceId());
		if (central != null) {
			success = setPushFailed(central, failed);
		}
		return success;
	}

	public boolean isVeryRecentAccessAllowed(DeviceDbObj device) {
		boolean success = false;
		if (isProximate(device, false)) {
			addLog(device.getDeviceId(), "yes, proximate");
			success = true;
		} else {
			addLog(device.getDeviceId(), "not proximate");
			success = didVeryRecentlyGiveAccess(device);
			if (success) {
				addLog(device.getDeviceId(), "access given through " + "fingerprint or push");
			} else {
				addLog(device.getDeviceId(), "access not given");
				success = isBtConnectedWithRecentTransfer(device);
			}
		}
		if (success && !device.getSignedIn()) {
			success = false;
			addLog(device.getDeviceId(), "device " + device.getDeviceType() + " was not signed in");
		}
		return success;
	}

	public boolean isAccessAllowed(DeviceDbObj device) {
		return isAccessAllowed(device, "");
	}

	public boolean isAccessAllowed(DeviceDbObj device, String caller) {
		return isAccessAllowed(device, caller, false);
	}

	public boolean isAccessAllowedIgnoringSleep(DeviceConnectionDbObj connection, DeviceDbObj device) {
		return isAccessAllowed(connection, device, true);

	}

	public boolean isAccessAllowed(DeviceConnectionDbObj connection, DeviceDbObj device) {
		return isAccessAllowed(connection, device, false);
	}
	
	public AccessAllowedWithAccessType isAccessAllowedWithConnectionMethod(DeviceConnectionDbObj connection, DeviceDbObj device) {
		return isAccessAllowedWithConnectionMethod(connection, device, false);
	}

	public boolean isAccessAllowed(DeviceDbObj device, String caller, boolean deactivateFingerprint) {
		boolean success = false;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		ConnectionType connType = null;
		if (device != null) {
			if (!device.isScreensaverOn()) {
				if (isProximate(device, false)) {
					addLog(device.getDeviceId(), "prox", LogConstants.TEMPORARILY_IMPORTANT);
					connType = ConnectionType.PROX;
					success = true;
				} else {
					addLog(device.getDeviceId(), "not prox", LogConstants.TEMPORARILY_IMPORTANT);
					ConnectedAndConnectionType cct = didGiveAccess(device, deactivateFingerprint);
					success = cct.isConnected();
					connType = cct.getConnectionType();
					if (!success) {
						addLog(device.getDeviceId(), "did not give access", LogConstants.TEMPORARILY_IMPORTANT);
						success = isBtConnectedWithRecentTransfer(device);
						if (success) {
							connType = ConnectionType.PROX;
						}
					}
				}
				if (success && !device.getSignedIn()) {
					addLog(device.getDeviceId(), caller + ": device " + device.getDeviceType() + " was not signed in",
							LogConstants.WARNING);
				}
				if (!success) {
					if (device.getSignedIn()) {
						success = isPassThroughAllowed(dataAccess.getCompanyByDevId(device.getDeviceId()),
								dataAccess.getGroupByDeviceId(device.getDeviceId()));
					}
				} else {
					if (!device.isCentral()) {
						addLog(device.getDeviceId(), "checking for communication", LogConstants.TRACE);
						success = hasPeripheralHadCommunication(device);
					}
				}
			} else {
				addLog(device.getDeviceId(), "screensaver was on", LogConstants.IMPORTANT);
			}
			addLog(device.getDeviceId(), "success: " + success + "\nconnection type:  " + connType,
					LogConstants.IMPORTANT);
		} else {
			addLog(caller + ": device was null", LogConstants.WARNING);
		}
		validateWithConnectionLog(device, success, connType);
		return success;
	}
	
	public AccessAllowedWithAccessType isAccessAllowedWithConnectionMethod(DeviceDbObj device, 
			String caller, boolean deactivateFingerprint) {
		AccessAllowedWithAccessType accessData = isProximateWithData(device, false);
		if (!accessData.isAccessAllowed()) {
			accessData = didGiveAccessWithReturnType(device);
			if (!accessData.isAccessAllowed()) {
				accessData = isBtConnectedWithRecentTransferWithData(device);
			}
		}
		return accessData;
	}

	public boolean isAccessAllowed(DeviceConnectionDbObj connection, DeviceDbObj device, boolean ignoreSleep) {
		boolean success = false;
		ConnectionType connType = null;
		if (connection != null) {
			try {
				if (device.isCentral() || !device.isScreensaverOn()) {
					if (isProximate(connection, ignoreSleep)) {
						connType = ConnectionType.PROX;
						success = true;
					} else {
						AccessAllowedWithAccessType cct = didGiveAccess(connection, device.isCentral());
						success = cct.isAccessAllowed();
						connType = cct.getConnectionType();
						if (!success) {
							Boolean peripheralMobile = null;
							if (device.isCentral()) {
								DeviceDbObj peripheral = this.getDeviceByDeviceId(connection.getPeripheralDeviceId());
								if (peripheral != null) {
									peripheralMobile = peripheral.isPeripheralMobile();
								} else {
									this.addLog(device.getDeviceId(),
											"Peripheral was null for device connection Id: "
													+ connection.getConnectionId() + " the perfId was "
													+ connection.getPeripheralDeviceId(),
											LogConstants.ERROR);
								}
							} else {
								peripheralMobile = device.isPeripheralMobile();
							}
							if (peripheralMobile != null) {
								success = connectionIsWithinTimePeriod(connection, peripheralMobile);
								if (success) {
									connType = ConnectionType.PROX;
								}
							}
//							if (success) {
//								if (cct.getConnectionType() != null) {
//									connType = cct.getConnectionType();
//								} else {
//									connType = ConnectionType.PROX;
//								}
//							}
						}
					}

					if (success) {
						if (!device.getSignedIn()) {
							// success = false;
							addLog(device.getDeviceId(),
									"withConnection: device " + device.getDeviceType() + " was not signed in",
									LogConstants.ERROR);
						}
						if (!ignoreSleep) {
							success = hasPeripheralHadCommunication(connection, device);
						}
					}
				}
			} catch (Exception e) {
				addLog(e);
			}
		} else {
			addLog(device.getDeviceId(), "withConnection: connection was null", LogConstants.ERROR);
		}
		addLog(device.getDeviceId(), "connected: " + success + ", connType: " + connType + " (with device)",
				LogConstants.TRACE);
		if (!ignoreSleep) {
			validateWithConnectionLog(connection, device, success, connType);
		}
		return success;
	}
	
	public AccessAllowedWithAccessType isAccessAllowedWithConnectionMethod(DeviceConnectionDbObj connection, 
			DeviceDbObj device, boolean ignoreSleep) {
		AccessAllowedWithAccessType accessAllowedWithAccessType = new AccessAllowedWithAccessType(false, ConnectionType.NONE,
				DateTimeUtilities.getCurrentTimestamp());
		if (connection != null) {
			try {
				if (device.isCentral() || !device.isScreensaverOn()) {
					accessAllowedWithAccessType = isProximateWithData(connection, false);
					if (!accessAllowedWithAccessType.isAccessAllowed()) {
						accessAllowedWithAccessType = didGiveAccess(connection, device.isCentral());;
						if (!accessAllowedWithAccessType.isAccessAllowed()) {
							Boolean peripheralMobile = null;
							if (device.isCentral()) {
								DeviceDbObj peripheral = this.getDeviceByDeviceId(connection.getPeripheralDeviceId());
								if (peripheral != null) {
									peripheralMobile = peripheral.isPeripheralMobile();
								} else {
									this.addLog(device.getDeviceId(),
											"Peripheral was null for device connection Id: "
													+ connection.getConnectionId() + " the perfId was "
													+ connection.getPeripheralDeviceId(),
											LogConstants.ERROR);
								}
							} else {
								peripheralMobile = device.isPeripheralMobile();
							}
							if (peripheralMobile != null) {
								accessAllowedWithAccessType = connectionIsWithinTimePeriodWithConnectionMethod(connection, peripheralMobile);
							}
						}
					}

					if (accessAllowedWithAccessType.isAccessAllowed()) {
						if (!device.getSignedIn()) {
							// success = false;
							addLog(device.getDeviceId(),
									"withConnection: device " + device.getDeviceType() + " was not signed in",
									LogConstants.ERROR);
						}
						if (!ignoreSleep) {
							accessAllowedWithAccessType.setAccessAllowed(hasPeripheralHadCommunication(connection, device));
						}
					}
				}
			} catch (Exception e) {
				addLog(e);
			}
		} else {
			addLog(device.getDeviceId(), "withConnection: connection was null", LogConstants.ERROR);
		}
		addLog(device.getDeviceId(), "connected: " + accessAllowedWithAccessType.isAccessAllowed() + 
				", connType: " + accessAllowedWithAccessType.getConnectionType() + " (with device)",
				LogConstants.TRACE);
		if (!ignoreSleep) {
			validateWithConnectionLog(connection, device, accessAllowedWithAccessType);
		}
		return accessAllowedWithAccessType;
	}
	
	

	public boolean isAccessAllowed(DeviceConnectionDbObj connection, boolean ignoreSleep) {
		boolean success = false;
		ConnectionType connType = null;
		DeviceDbObj device = null;
		if (connection != null) {
			try {
				device = this.getDeviceByDeviceId(connection.getPeripheralDeviceId());
				if (!device.isScreensaverOn() && !device.isTurnedOff()) {
					if (isProximate(connection, ignoreSleep)) {
						connType = ConnectionType.PROX;
						success = true;
					} else {
						AccessAllowedWithAccessType cct = didGiveAccess(connection, false);
						success = cct.isAccessAllowed();
						connType = cct.getConnectionType();
						if (!success) {
							success = connectionIsWithinTimePeriod(connection, device.isPeripheralMobile());
							if (success) {
								connType = ConnectionType.PROX;
							}
						}
					}
				}
			} catch (Exception e) {
				addLog(e);
			}
			if (success) {
				success = hasPeripheralHadCommunication(connection, null);
			}
			addLog(connection.getPeripheralDeviceId(),
					"connected: " + success + ", connType: " + connType + " (without device)", LogConstants.DEBUG);
		} else {
			addLog("withConnection: connection was null", LogConstants.ERROR);
		}
		validateWithConnectionLog(connection, success, connType);

		return success;
	}

	private void validateWithConnectionLog(DeviceConnectionDbObj connection, boolean connected,
			ConnectionType connectionType) {
		DeviceDbObj device = this.getDeviceByDeviceId(connection.getPeripheralDeviceId(), true,
				"validateWithConnectionLog");
		validateWithConnectionLog(connection, device, connected, connectionType);
	}

	public void validateWithConnectionLog(DeviceDbObj device, boolean connected, ConnectionType connectionType) {
		if (!device.isCentral()) {
			if (!device.isScreensaverOn() || !connected) {
				this.addLog(device.getDeviceId(), "adding connection log - screensaver on: " + device.isScreensaverOn()
						+ ", connected: " + connected, LogConstants.TRACE);
				DeviceConnectionDbObj connection = this.getConnectionForPeripheral(device, true);
				this.validateWithConnectionLog(connection, device, connected, connectionType);
			} else {
				this.addLog(device.getDeviceId(), "screensaver is on -> not logging connection",
						LogConstants.TRACE);
			}
		} else {
			if (!connected) {
				this.addLog("conn change for central to connected = " + connected, LogConstants.IMPORTANT);
				this.logCurrentStackTrace(device.getDeviceId(), "validateWithConnectionLog");
//				ArrayList<DeviceConnectionDbObj> connections = this.getConnectionsForCentral(device);
//				for (DeviceConnectionDbObj connection : connections) {
//					validateWithConnectionLog(connection, device, false, null);
//				}
			}
		}
	}

	private void validateWithConnectionLog(DeviceConnectionDbObj connection, DeviceDbObj device, 
			AccessAllowedWithAccessType accessAllowedWithAccessType) {
		validateWithConnectionLog(connection, device, accessAllowedWithAccessType.isAccessAllowed(), 
				accessAllowedWithAccessType.getConnectionType());
	}
	private void validateWithConnectionLog(DeviceConnectionDbObj connection, DeviceDbObj device, boolean connected,
			ConnectionType connectionType) {
		boolean added = false;
		try {
			DeviceDbObj peripheralDevice = null;
			Timestamp ts = DateTimeUtilities.getCurrentTimestamp();
			if (device.isCentral()) {
				if (connection != null) {
					peripheralDevice = this.getDeviceByDeviceId(connection.getPeripheralDeviceId());
				} else {
					if (!connected) {
						ArrayList<DeviceConnectionDbObj> conns = this.getAllConnectionsForCentral(device, true);
						for (DeviceConnectionDbObj conn : conns) {
							ConnectionLogDbObj connLog = getLastConnectionStatusFromLog(connection);
							if (connLog == null || connected != connLog.isConnected()) {
								ConnectionLogDbObj centralConnectionLog = new ConnectionLogDbObj(conn.getConnectionId(),
										device.getDeviceId(), connected, ts, device.getDeviceId(), "isAccessAllowed",
										connectionType);
								this.addConnectionLog(centralConnectionLog);
								added = true;
							}
						}
					} else {
						ConnectionLogDbObj centralConnectionLog = new ConnectionLogDbObj(null, device.getDeviceId(),
								connected, ts, device.getDeviceId(), "isAccessAllowed", connectionType);
						this.addConnectionLog(centralConnectionLog);
						added = true;
					}
				}
			} else {
				peripheralDevice = device;
			}
			if (peripheralDevice != null) {
				ConnectionLogDbObj connLog = getLastConnectionStatusFromLog(connection, device);
				if (!peripheralDevice.isScreensaverOn()) {
					addLog(device.getDeviceId(), "peripheralDevice: screensaver is off", LogConstants.TRACE);
					if (connLog == null || connected != connLog.isConnected()
							|| connLog.getConnectionType() != connectionType) {
						addLog(device.getDeviceId(), "new status (or connection type), connected = " + connected,
								LogConstants.TRACE);
						if ((connLog == null || !connLog.getDescription().equals("isAccessAllowed")) && !added) {
							ConnectionLogDbObj connectionLog = new ConnectionLogDbObj(connection.getConnectionId(),
									null, connected, ts, connection.getPeripheralDeviceId(), "isAccessAllowed",
									connectionType);
							this.addConnectionLog(connectionLog);
						}
					} else {
						if (connLog.getConnectionType() != connectionType && !added) {
							ConnectionLogDbObj connectionLog = new ConnectionLogDbObj(connLog.getConnectionId(), null,
									connected, ts, connection.getPeripheralDeviceId(), "isAccessAllowed",
									connectionType);
							this.addConnectionLog(connectionLog);
						}
					}
				} else {
					if (connLog != null && connLog.isConnected() && !added) {
						addLog(device.getDeviceId(), "we're asleep", LogConstants.TRACE);
						ConnectionLogDbObj connectionLog = new ConnectionLogDbObj(connection.getConnectionId(), null,
								false, ts, connection.getPeripheralDeviceId(), "screenSaver on", null);
						this.addConnectionLog(connectionLog);
					} else {
						if (!added) {
							addLog(device.getDeviceId(), "not adding a log because we were already disconnected",
									LogConstants.TRACE);
						}
					}
				}
			} else {
				addLog(device.getDeviceId(), "perf was null", LogConstants.ERROR);
			}
		} catch (Exception e) {
			this.addLog(e);
		}
	}

	public boolean hasPeripheralHadCommunication(DeviceDbObj peripheralDevice) {
		boolean hasHadCommunication = false;
		if (!peripheralDevice.isCentral()) {
			if (peripheralDevice.getDeviceClass().equals(DeviceClass.COMPUTER)) {
				hasHadCommunication = DateTimeUtilities
						.timeDifferenceInSecondsFromNow(peripheralDevice.getLastVariableRetrieval()) < 120;
			} else {
				hasHadCommunication = true;
			}
			if (!hasHadCommunication) {
				this.addLog(peripheralDevice.getDeviceId(),
						"the peripheral, which is a computer has not been in contact recently enough",
						LogConstants.WARNING);
			}
		}
		return hasHadCommunication;
	}

	public boolean hasPeripheralHadCommunication(DeviceConnectionDbObj connection, DeviceDbObj device) {
		boolean hasHadCommunication = false;
		DeviceDbObj perf;
		if (device != null && !device.isCentral()) {
			perf = device;
		} else {
			perf = this.getDeviceByDeviceId(connection.getPeripheralDeviceId(), true, "hasPeripheralHadCommunication");
		}
		if (perf.getDeviceClass().equals(DeviceClass.COMPUTER)) {
			hasHadCommunication = DateTimeUtilities
					.timeDifferenceInSecondsFromNow(perf.getLastVariableRetrieval()) < 120;
		} else {
			hasHadCommunication = true;
		}
		if (!hasHadCommunication) {
			this.addLog(perf.getDeviceId(),
					"the peripheral, which is a computer has not been in contact recently enough",
					LogConstants.WARNING);
		}
		return hasHadCommunication;
	}

	/* we really only want to check if the one device is signed in */
	public boolean isSignedIn(DeviceConnectionDbObj connection) {
		boolean signedIn = false;
		ArrayList<DeviceDbObj> devices = this.getDevicesByConnection(connection);
		for (DeviceDbObj device : devices) {
			signedIn = device.getSignedIn();
			if (!signedIn) {
				break;
			}
		}
		this.addLog("signed in: " + signedIn);
		return signedIn;
	}

	public ArrayList<DeviceDbObj> getDevicesByConnection(DeviceConnectionDbObj connection) {
		ArrayList<DeviceDbObj> devices = new ArrayList<>();
		String query = "SELECT * FROM B2F_DEVICE WHERE DEVICE_ID IN (?, ?)";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connection.getCentralDeviceId());
			prepStmt.setString(2, connection.getPeripheralDeviceId());
			rs = executeQuery(prepStmt);
			DeviceDbObj device;
			while (rs.next()) {
				device = this.recordToDevice(rs);
				devices.add(device);
			}
		} catch (Exception e) {
			this.addLog("getDevicesByConnection", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return devices;
	}

	public DeviceDbObj getCentralDeviceByConnection(DeviceConnectionDbObj connection) {
		DeviceDbObj device = null;
		String query = "SELECT * FROM B2F_DEVICE WHERE DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connection.getCentralDeviceId());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				device = this.recordToDevice(rs);
			}
		} catch (Exception e) {
			this.addLog("getCentralDeviceByConnection", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return device;
	}

	public DeviceDbObj getPeripheralDeviceByConnection(DeviceConnectionDbObj connection) {
		DeviceDbObj device = null;
		String query = "SELECT * FROM B2F_DEVICE WHERE DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connection.getPeripheralDeviceId());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				device = this.recordToDevice(rs);
			}
		} catch (Exception e) {
			this.addLog("getPeripheralDeviceByConnection", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return device;
	}

	/**
	 * Use isAccessAllowedByAuthToken(String tokenId, boolean isCentral) rather than
	 * this whenever possible
	 * 
	 * @param tokenId
	 * @return
	 */
	public boolean isAccessAllowedByAuthToken(DeviceDbObj device, String tokenId) {
		return isAccessAllowedByToken(device, tokenId, TokenDescription.AUTHENTICATION);
	}
	
	public AccessAllowedWithAccessType isAccessAllowedByAuthTokenWithConnectionMethod(DeviceDbObj device, String tokenId) {
		return isAccessAllowedByTokenWithConnectionMethod(device, tokenId, TokenDescription.AUTHENTICATION);
	}
	
	

	public boolean isAccessAllowedByJwt(DeviceDbObj device, String tokenId) {
		return isAccessAllowedByToken(device, tokenId, TokenDescription.JWT);
	}

	public void expireToken(String tokenId) {
		new Thread(() -> {
			if (!TextUtils.isEmpty(tokenId)) {
				Timestamp yesterday = DateTimeUtilities.getCurrentTimestampMinusHours(24);
				String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE TOKEN_ID = ?";
				Connection conn = null;
				PreparedStatement prepStmt = null;
				try {
					conn = MySqlConn.getConnection();
					prepStmt = conn.prepareStatement(query);
					prepStmt.setTimestamp(1, yesterday);
					prepStmt.setString(2, tokenId);
					logQuery(getMethodName(), prepStmt);
					prepStmt.executeUpdate();
				} catch (Exception e) {
					this.addLog("expireToken", e);
				} finally {
					MySqlConn.close(prepStmt, conn);
				}
			}
		}).start();
	}

	public void expireToken(TokenDbObj token) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		token.setExpireTime(now);
		token.setLastUpdate(now);
		updateToken(token);
	}

	public void expireTwoTokens(String tokenId1, String tokenId2) {
		new Thread(() -> {
			if (!TextUtils.isEmpty(tokenId1) && !TextUtils.isEmpty(tokenId2)) {
				Timestamp yesterday = DateTimeUtilities.getCurrentTimestampMinusHours(24);
				String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE TOKEN_ID IN (?, ?)";
				Connection conn = null;
				PreparedStatement prepStmt = null;
				try {
					conn = MySqlConn.getConnection();
					prepStmt = conn.prepareStatement(query);
					prepStmt.setTimestamp(1, yesterday);
					prepStmt.setString(2, tokenId1);
					prepStmt.setString(3, tokenId2);
					logQuery(getMethodName(), prepStmt);
					prepStmt.executeUpdate();
				} catch (Exception e) {
					this.addLog("expireTwoTokens", e);
				} finally {
					MySqlConn.close(prepStmt, conn);
				}
			}
		}).start();
	}

	public void expireAllJwtsForToken(String tokenId) {
		new Thread(() -> {
			if (!TextUtils.isEmpty(tokenId)) {
				TokenDbObj token = this.getTokenByDescriptionAndTokenId(TokenDescription.JWT, tokenId);
				if (token != null) {
					Timestamp yesterday = DateTimeUtilities.getCurrentTimestampMinusHours(24);
					String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE DEVICE_ID = ? AND " + "DESCRIPTION = ?";
					Connection conn = null;
					PreparedStatement prepStmt = null;
					try {
						conn = MySqlConn.getConnection();
						prepStmt = conn.prepareStatement(query);
						prepStmt.setTimestamp(1, yesterday);
						prepStmt.setString(2, token.getDeviceId());
						prepStmt.setString(3, TokenDescription.JWT.toString());
						logQuery(getMethodName(), prepStmt);
						prepStmt.executeUpdate();
					} catch (Exception e) {
						this.addLog("expireAllJwtsForToken", e);
					} finally {
						MySqlConn.close(prepStmt, conn);
					}
				}
			}
		}).start();
	}

	public boolean isAccessAllowedByToken(String tokenId, DeviceDbObj device, boolean isCentral,
			TokenDescription description) {
		boolean success = false;
		if (isCentral) {
			ArrayList<DeviceConnectionDbObj> connections = this.getConnectionsForCentralToken(tokenId, description);
			for (DeviceConnectionDbObj connection : connections) {
				if (this.isAccessAllowed(connection, device)) {
					success = true;
					break;
				}
			}
		} else {
			DeviceConnectionDbObj connection = this.getConnectionByPeripheralToken(tokenId, description);
			if (connection != null) {
				success = isAccessAllowed(connection, device);
			}
		}
		return success;
	}

	/**
	 * Use isAccessAllowedByAuthToken(String tokenId, boolean isCentral) whenever
	 * possible
	 * 
	 * @param tokenId
	 * @return
	 */
	public boolean isAccessAllowedByToken(DeviceDbObj device, String tokenId, TokenDescription description) {
		boolean success = false;
		addLog("start");
		if (!device.isCentral()) {
			addLog("peripheralConnection found");
			DeviceConnectionDbObj connection = this.getConnectionByPeripheralToken(tokenId, description);
			success = isAccessAllowed(connection, device);
		} else {
			addLog("peripheralConnection NOT found");
			ArrayList<DeviceConnectionDbObj> connections = this.getConnectionsForCentralToken(tokenId, description);
			for (DeviceConnectionDbObj devConn : connections) {
				addLog("will check if access is allowed with device acting as central");
				if (this.isAccessAllowed(devConn, device)) {
					success = true;
					break;
				}
			}
		}
		addLog("accessAllowed: " + success);
		return success;
	}
	
	public AccessAllowedWithAccessType isAccessAllowedByTokenWithConnectionMethod(DeviceDbObj device, String tokenId, TokenDescription description) {
		boolean success = false;
		addLog("start");
		AccessAllowedWithAccessType accessAllowedWithAccessType = new AccessAllowedWithAccessType(false, ConnectionType.NONE,
				DateTimeUtilities.getCurrentTimestamp());
		if (!device.isCentral()) {
			addLog("peripheralConnection found");
			DeviceConnectionDbObj connection = this.getConnectionByPeripheralToken(tokenId, description);
			accessAllowedWithAccessType = isAccessAllowedWithConnectionMethod(connection, device);
		} else {
			addLog("peripheralConnection NOT found");
			ArrayList<DeviceConnectionDbObj> connections = this.getConnectionsForCentralToken(tokenId, description);
			for (DeviceConnectionDbObj devConn : connections) {
				addLog("will check if access is allowed with device acting as central");
				accessAllowedWithAccessType = isAccessAllowedWithConnectionMethod(devConn, device);
				if (accessAllowedWithAccessType.isAccessAllowed()) {
					break;
				}
			}
		}
		addLog("accessAllowed: " + success);
		return accessAllowedWithAccessType;
	}
	

	public SuccessAndConnection isAccessAllowedByTokenWithConnection(DeviceDbObj device, String tokenId,
			TokenDescription description) {
		boolean success = false;
		DeviceConnectionDbObj foundConnection = null;
		if (!device.isCentral()) {
			addLog("peripheralConnection found");
			DeviceConnectionDbObj connection = this.getConnectionByPeripheralToken(tokenId, description);
			// is there any point in doing this if we're just going to requery when its
			// null? -cjm
			if (connection == null) {
				connection = this.getConnectionForPeripheral(device, true);
			}
			success = isAccessAllowed(connection, device);
			foundConnection = connection;
		} else {
			addLog("this is a central device");
			ArrayList<DeviceConnectionDbObj> connections = this.getConnectionsForCentralToken(tokenId, description);
			for (DeviceConnectionDbObj devConn : connections) {
				addLog("will check if access is allowed with device acting as central");
				if (this.isAccessAllowed(devConn, device)) {
					foundConnection = devConn;
					success = true;
					break;
				}
			}
		}
		addLog("accessAllowed: " + success);
		return new SuccessAndConnection(success, foundConnection);
	}

	public boolean isProximateAccessAllowed(DeviceConnectionDbObj connection, boolean ignoreSleep) {
		boolean success = false;
		if (isProximate(connection, ignoreSleep)) {
			addLog("yes, proximate");
			success = true;
		} else {
			addLog("not proximate");
			DeviceDbObj peripheral = this.getDeviceByDeviceId(connection.getPeripheralDeviceId());
			boolean peripheralMobile = peripheral.isPeripheralMobile();
			success = connectionIsWithinTimePeriod(connection, peripheralMobile);
			if (success) {
				addLog(connection.getPeripheralDeviceId(), "bluetooth connected with recent transfer");
			} else {
				addLog(connection.getPeripheralDeviceId(), "not bluetooth connected with recent transfer");
			}
		}
		return success;
	}

	public boolean isProximateAccessAllowed(DeviceDbObj device, boolean ignoreSleep) {
		boolean success = false;
		if (isProximate(device, ignoreSleep)) {
			addLog(device.getDeviceId(), "paa: We are proximate");
			success = true;
		} else {
			success = isBtConnectedWithRecentTransfer(device);
			if (success) {
				addLog(device.getDeviceId(), "paa: bluetooth connected with recent transfer");
			}
		}
		if (!success) {
			addLog(device.getDeviceId(), "paa: access denied", LogConstants.WARNING);
		}
		return success;
	}

	public boolean isProximate(DeviceDbObj device, boolean ignoreSleep) {
		boolean connected = false;
		if (!device.isTurnedOff()) {
			String query;
			if (device.isCentral()) {
				query = "SELECT * FROM B2F_CHECK ck JOIN B2F_DEVICE_CONNECTION dc "
						+ "ON ck.CENTRAL_DEVICE_ID = dc.CENTRAL_DEVICE_ID "
						+ "WHERE ck.CENTRAL_DEVICE_ID = ? AND ck.COMPLETED = ? AND (ck.CHECK_TYPE = ? "
						+ "OR ck.CHECK_TYPE = ?) "
						+ "AND ck.OUTCOME = ? AND ck.EXPIRED = ? AND dc.SERVICE_UUID IS NOT NULL "
						+ "ORDER BY ck.COMPLETION_DATE DESC LIMIT 1";
			} else {
				query = "SELECT * FROM B2F_CHECK ck JOIN B2F_DEVICE_CONNECTION dc "
						+ "ON ck.PERIPHERAL_DEVICE_ID = dc.PERIPHERAL_DEVICE_ID "
						+ "WHERE ck.PERIPHERAL_DEVICE_ID = ? AND ck.COMPLETED = ? AND (ck.CHECK_TYPE = ? "
						+ "OR ck.CHECK_TYPE = ?) "
						+ "AND ck.OUTCOME = ? AND ck.EXPIRED = ? AND dc.SERVICE_UUID IS NOT NULL "
						+ "ORDER BY ck.COMPLETION_DATE DESC LIMIT 1";
			}
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, device.getDeviceId());
				prepStmt.setBoolean(2, true);
				prepStmt.setString(3, CheckType.PROX.checkTypeName().toLowerCase());
				prepStmt.setString(4, CheckType.CONNECTION_FROM_PERIPHERAL.checkTypeName().toLowerCase());
				prepStmt.setInt(5, Outcomes.SUCCESS);
				prepStmt.setBoolean(6, false);
				logQuery(getMethodName(), prepStmt);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					CheckDbObj check = this.recordToCheck(rs);
					DeviceConnectionDbObj connection = this.getConnectionByDeviceIds(check.getCentralDeviceId(),
							check.getPeripheralDeviceId());
					if (connection != null) {
						if (!areDevicesTurnedOffOrAsleep(connection, ignoreSleep)
								&& connection.getPeripheralConnected()) {
							Timestamp completionDate = check.getCompletionDate();
							if (completionDate != null) {
								long seconds = DateTimeUtilities.timestampSecondAgo(check.getCompletionDate());

								if (seconds < 20 * 60) {
									addLog(check.getPeripheralDeviceId(), "prox is true",
											LogConstants.TRACE);
									connected = true;
								}
							} else {
								addLog(check.getPeripheralDeviceId(), "completion date is null", LogConstants.WARNING);
							}
						} else {
							addLog(check.getPeripheralDeviceId(), "peripheral not connected");
						}
					} else {
						addLog(check.getPeripheralDeviceId(), "connection not found for central: "
								+ check.getCentralDeviceId() + " and peripheral: " + check.getPeripheralDeviceId(),
								LogConstants.ERROR);
					}
				} else {
					addLog(device.getDeviceId(), "current successful check not found", LogConstants.WARNING);
				}
			} catch (Exception e) {
				this.addLog(device.getDeviceId(), e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}
		return connected;
	}

	public AccessAllowedWithAccessType isProximateWithData(DeviceConnectionDbObj connection, boolean ignoreSleep) {
		AccessAllowedWithAccessType accessAllowedWithAccessType = new AccessAllowedWithAccessType(false, ConnectionType.NONE,
				DateTimeUtilities.getCurrentTimestamp());
		if (!areDevicesTurnedOffOrAsleep(connection, ignoreSleep)) {
			if (connection.getPeripheralConnected()) {
				String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_DEVICE_ID = ? AND "
						+ "PERIPHERAL_DEVICE_ID = ? AND COMPLETED = ? AND OUTCOME = ? AND "
						+ "EXPIRED = ? AND CHECK_TYPE IN (?, ?) ORDER BY COMPLETION_DATE DESC";
				Connection conn = null;
				PreparedStatement prepStmt = null;
				ResultSet rs = null;
				try {
					conn = MySqlConn.getConnection();
					prepStmt = conn.prepareStatement(query);
					prepStmt.setString(1, connection.getCentralDeviceId());
					prepStmt.setString(2, connection.getPeripheralDeviceId());
					prepStmt.setBoolean(3, true);
					prepStmt.setInt(4, Outcomes.SUCCESS);
					prepStmt.setBoolean(5, false);
					prepStmt.setString(6, CheckType.PROX.checkTypeName().toLowerCase());
					prepStmt.setString(7, CheckType.CONNECTION_FROM_PERIPHERAL.checkTypeName().toLowerCase());
					rs = executeQuery(prepStmt);
					logQuery(getMethodName(), prepStmt);
					if (rs.next()) {
						Timestamp completionDate = rs.getTimestamp("COMPLETION_DATE");
						if (completionDate != null) {
							long seconds = DateTimeUtilities.timestampSecondAgo(completionDate);
							addLog(connection.getPeripheralDeviceId(), "proximity seconds: " + seconds,
									LogConstants.DEBUG);
							if (seconds < 20 * 60) {
								accessAllowedWithAccessType.setAccessAllowed(true);
								accessAllowedWithAccessType.setConnectionType(ConnectionType.PROX);
							}
						}
						addLog(connection.getCentralDeviceId(), "success: " + accessAllowedWithAccessType.isAccessAllowed());
					} else {
						addLog(connection.getCentralDeviceId(), "none found");
					}
				} catch (Exception e) {
					this.addLog(connection.getCentralDeviceId(), e);
				} finally {
					MySqlConn.close(rs, prepStmt, conn);
				}
			}
		} else {
			accessAllowedWithAccessType.setReason(Constants.DEVICE_ASLEEP);
			addLog(connection.getPeripheralDeviceId(), "one of the devices is turned off or asleep",
					LogConstants.WARNING);
		}
		return accessAllowedWithAccessType;
	}
	
	public AccessAllowedWithAccessType isProximateWithData(DeviceDbObj device, boolean ignoreSleep) {
		boolean connected = this.isAccessAllowed(device);
		Timestamp completionDate = null;
		if (!device.isTurnedOff() && !device.isScreensaverOn()) {
			String query;
			if (device.isCentral()) {
				query = "SELECT * FROM B2F_CHECK ck JOIN B2F_DEVICE_CONNECTION dc "
						+ "ON ck.CENTRAL_DEVICE_ID = dc.CENTRAL_DEVICE_ID "
						+ "WHERE ck.CENTRAL_DEVICE_ID = ? AND ck.COMPLETED = ? AND (ck.CHECK_TYPE = ? "
						+ "OR ck.CHECK_TYPE = ?) "
						+ "AND ck.OUTCOME = ? AND ck.EXPIRED = ? AND dc.SERVICE_UUID IS NOT NULL "
						+ "ORDER BY ck.COMPLETION_DATE DESC LIMIT 1";
			} else {
				query = "SELECT * FROM B2F_CHECK ck JOIN B2F_DEVICE_CONNECTION dc "
						+ "ON ck.PERIPHERAL_DEVICE_ID = dc.PERIPHERAL_DEVICE_ID "
						+ "WHERE ck.PERIPHERAL_DEVICE_ID = ? AND ck.COMPLETED = ? AND (ck.CHECK_TYPE = ? "
						+ "OR ck.CHECK_TYPE = ?) "
						+ "AND ck.OUTCOME = ? AND ck.EXPIRED = ? AND dc.SERVICE_UUID IS NOT NULL "
						+ "ORDER BY ck.COMPLETION_DATE DESC LIMIT 1";
			}
			ResultSet rs = null;
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, device.getDeviceId());
				prepStmt.setBoolean(2, true);
				prepStmt.setString(3, CheckType.PROX.checkTypeName().toLowerCase());
				prepStmt.setString(4, CheckType.CONNECTION_FROM_PERIPHERAL.checkTypeName().toLowerCase());
				prepStmt.setInt(5, Outcomes.SUCCESS);
				prepStmt.setBoolean(6, false);
				logQueryImportant(getMethodName(), prepStmt);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					CheckDbObj check = this.recordToCheck(rs);
					DeviceConnectionDbObj connection = this.getConnectionByDeviceIds(check.getCentralDeviceId(),
							check.getPeripheralDeviceId());
					if (connection != null) {
						addLog("connection found");
						if (!areDevicesTurnedOffOrAsleep(connection, ignoreSleep)
								&& connection.getPeripheralConnected()) {
							completionDate = check.getCompletionDate();
							if (completionDate != null) {
								long seconds = DateTimeUtilities.timestampSecondAgo(completionDate);
								this.addLog(check.getPeripheralDeviceId(), "proximity seconds: " + seconds,
										LogConstants.DEBUG);
								if (seconds < 20 * 60) {
									connected = true;
								}
							} else {
								addLog(check.getPeripheralDeviceId(), "completion dhate is null", LogConstants.INFO);
							}
						} else {
							addLog(check.getPeripheralDeviceId(), "peripheral not connected");
						}
					} else {
						addLog(check.getPeripheralDeviceId(), "connection not found for central: "
								+ check.getCentralDeviceId() + " and peripheral: " + check.getPeripheralDeviceId(),
								LogConstants.ERROR);
					}
				} else {
					addLog(device.getDeviceId(), "current successful check not found", LogConstants.INFO);
				}
				if (connected && !device.isCentral()) {
					connected = hasPeripheralHadCommunication(device);
				}
			} catch (Exception e) {
				this.addLog(device.getDeviceId(), e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}
		return new AccessAllowedWithAccessType(connected, ConnectionType.PROX, completionDate);
	}

	/**
	 * Have we either fingerprinted, responded to a push, or are we bluetooth
	 * connected and have decrypted data within the last 24 hours
	 * 
	 * @param device
	 * @return true if prox was proven
	 */
	public boolean isBtConnectedWithRecentTransfer(DeviceDbObj device) {
		boolean connected = false;
		if (device.isActive()) {
			if (device.isCentral()) {
				ArrayList<DeviceConnectionDbObj> connections = this.getConnectionsForCentral(device);
				addLog("connections: " + connections.size());
				for (DeviceConnectionDbObj connection : connections) {
					DeviceDbObj peripheral = this.getDeviceByDeviceId(connection.getPeripheralDeviceId());
					if (peripheral != null) {
						boolean mobile = peripheral.isPeripheralMobile();
						connected = connectionIsWithinTimePeriod(connection, mobile);
						if (connected) {
							deactivateFingerprint(device);
							break;
						}
					}
				}
			} else {
				DeviceConnectionDbObj perfConnection = this.getConnectionForPeripheral(device, true);

				boolean mobile = device.isPeripheralMobile();
				connected = connectionIsWithinTimePeriod(perfConnection, mobile);
				if (connected) {
					deactivateFingerprint(device);
				}
			}
		} else {

		}
		return connected;
	}

	public AccessAllowedWithAccessType isBtConnectedWithRecentTransferWithData(DeviceDbObj device) {
		boolean connected = false;
		ConnectionType connectionType = ConnectionType.NONE;
		Timestamp completionDate = null;
		AccessAllowedWithAccessType accessAllowedWithAccessType = new AccessAllowedWithAccessType(connected,
				connectionType, completionDate);
		if (device.isActive() && !device.isScreensaverOn()) {
			if (device.isCentral()) {
				ArrayList<DeviceConnectionDbObj> connections = this.getConnectionsForCentral(device);
				addLog(device.getDeviceId() + " connections: " + connections.size());
				for (DeviceConnectionDbObj connection : connections) {
					DeviceDbObj peripheral = this.getDeviceByDeviceId(connection.getPeripheralDeviceId());
					boolean mobile = peripheral.isPeripheralMobile();
					accessAllowedWithAccessType = connectionIsWithinTimePeriodWithData(device, connection, mobile);
					if (accessAllowedWithAccessType.isAccessAllowed()) {
						break;
					}
				}
			} else {
				DeviceConnectionDbObj perfConnection = this.getConnectionForPeripheral(device, true);
				boolean mobile = device.isPeripheralMobile();
				accessAllowedWithAccessType = connectionIsWithinTimePeriodWithData(device, perfConnection, mobile);
			}
		}
		return accessAllowedWithAccessType;
	}

	public boolean connectionIsWithinTimePeriod(DeviceConnectionDbObj connection, boolean mobilePeripheral) {
		boolean connected = false;
		if (connection != null && (connection.getCentralConnected() && connection.getPeripheralConnected())) {
			Timestamp connMinimum;
			int timeLimit;
			if (mobilePeripheral) {
				timeLimit = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS_MOBILE_PERIPHERAL;
			} else {
				timeLimit = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS;
			}
			connMinimum = DateTimeUtilities.getCurrentTimestampMinusSeconds(timeLimit);
			if (connection.getLastSuccess() != null) {
				if (connection.getLastSuccess().after(connMinimum)) {
					connected = true;
				}
			}
		} else {
			if (connection == null) {
				this.addLog("connection was null", LogConstants.ERROR);
			}
		}
		if (!connected) {
			// is it possible we want to exclude centrals from this
			connected = isSubscribedWithinTimePeriod(connection, mobilePeripheral);
		} else {
			this.addLog("connected = true");
		}
		return connected;
	}
	
	public AccessAllowedWithAccessType connectionIsWithinTimePeriodWithConnectionMethod(DeviceConnectionDbObj connection, boolean mobilePeripheral) {
		AccessAllowedWithAccessType accessAllowedWithAccessType = new AccessAllowedWithAccessType(false, ConnectionType.NONE,
				DateTimeUtilities.getCurrentTimestamp());
		if (connection != null && (connection.getCentralConnected() && connection.getPeripheralConnected())) {
			Timestamp connMinimum;
			int timeLimit;
			if (mobilePeripheral) {
				timeLimit = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS_MOBILE_PERIPHERAL;
			} else {
				timeLimit = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS;
			}
			connMinimum = DateTimeUtilities.getCurrentTimestampMinusSeconds(timeLimit);
			if (connection.getLastSuccess() != null) {
				if (connection.getLastSuccess().after(connMinimum)) {
					accessAllowedWithAccessType = new AccessAllowedWithAccessType(true, ConnectionType.PROX, connection.getLastSuccess());
				}
			}
		} else {
			if (connection == null) {
				accessAllowedWithAccessType.setReason(Constants.CONNECTION_NOT_FOUND);
				this.addLog("connection was null", LogConstants.ERROR);
			}
		}
		if (!accessAllowedWithAccessType.isAccessAllowed()) {
			// is it possible we want to exclude centrals from this
			accessAllowedWithAccessType = isSubscribedWithinTimePeriodWithConnectionMethod(connection, mobilePeripheral);
		} else {
			this.addLog("connected = true");
		}
		return accessAllowedWithAccessType;
	}

	private boolean isSubscribedWithinTimePeriod(DeviceConnectionDbObj connection, boolean mobilePeripheral) {
		boolean connected = false;
		if (connection != null && (connection.isSubscribed())) {
			Timestamp lastSubscribed = connection.getLastSubscribed();
			long secondsAgo = DateTimeUtilities.timestampSecondAgo(lastSubscribed);
			if (Constants.BLUETOOTH_CONNECTION_TIME_PERIOD > secondsAgo) {
				long lastSuccessSecondsAgo;
				if (mobilePeripheral) {
					lastSuccessSecondsAgo = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS_MOBILE_PERIPHERAL;
				} else {
					lastSuccessSecondsAgo = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS;
				}
				if (connection.getLastSuccess() != null) {
					lastSuccessSecondsAgo = DateTimeUtilities.timestampSecondAgo(connection.getLastSuccess());
				}
				if (Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS > lastSuccessSecondsAgo) {
					connected = true;
				} else {
					this.addLog(connection.getPeripheralDeviceId(),
							"subscribed but not transferred within the last 24 hours - lastSuccess: "
									+ lastSuccessSecondsAgo + "seconds ago",
							LogConstants.IMPORTANT);
				}
			} else {
				this.addLog(connection.getPeripheralDeviceId(), "not Bluetooth connect within the lasn 20 minutes.",
						LogConstants.IMPORTANT);
			}
		}
		return connected;
	}
	
	private AccessAllowedWithAccessType isSubscribedWithinTimePeriodWithConnectionMethod(DeviceConnectionDbObj connection, boolean mobilePeripheral) {
		AccessAllowedWithAccessType accessAllowedWithAccessType = new AccessAllowedWithAccessType(false, ConnectionType.NONE,
				DateTimeUtilities.getCurrentTimestamp());
		if (connection != null && (connection.isSubscribed())) {
			Timestamp lastSubscribed = connection.getLastSubscribed();
			long secondsAgo = DateTimeUtilities.timestampSecondAgo(lastSubscribed);
			if (Constants.BLUETOOTH_CONNECTION_TIME_PERIOD > secondsAgo) {
				long lastSuccessSecondsAgo;
				if (mobilePeripheral) {
					lastSuccessSecondsAgo = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS_MOBILE_PERIPHERAL;
				} else {
					lastSuccessSecondsAgo = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS;
				}
				if (connection.getLastSuccess() != null) {
					lastSuccessSecondsAgo = DateTimeUtilities.timestampSecondAgo(connection.getLastSuccess());
				}
				if (Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS > lastSuccessSecondsAgo) {
					accessAllowedWithAccessType = new AccessAllowedWithAccessType(true, ConnectionType.PROX,
							connection.getLastSuccess());
				} else {
					accessAllowedWithAccessType.setReason(Constants.NO_RECENT_COMMUNICATION);
					this.addLog(connection.getPeripheralDeviceId(),
							"subscribed but not transferred within the last 24 hours - lastSuccess: "
									+ lastSuccessSecondsAgo + "seconds ago",
							LogConstants.IMPORTANT);
				}
			} else {
				accessAllowedWithAccessType.setReason(Constants.NO_RECENT_COMMUNICATION);
				this.addLog(connection.getPeripheralDeviceId(), "not Bluetooth connect within the lasn 20 minutes.",
						LogConstants.IMPORTANT);
			}
		}
		return accessAllowedWithAccessType;
	}

	public AccessAllowedWithAccessType connectionIsWithinTimePeriodWithData(DeviceDbObj device,
			DeviceConnectionDbObj connection, boolean mobilePeripheral) {
		boolean connected = false;
		Timestamp lastConnection = null;
		ConnectionType connectionType = ConnectionType.NONE;

		if (connection != null && (connection.getCentralConnected() && connection.getPeripheralConnected())) {
			int timeLimit;
			if (mobilePeripheral) {
				timeLimit = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS_MOBILE_PERIPHERAL;
			} else {
				timeLimit = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS;
			}
			Timestamp connMinimum = DateTimeUtilities.getCurrentTimestampMinusSeconds(timeLimit);
			if (connection.getLastSuccess().after(connMinimum)) {
				connected = true;
				connectionType = ConnectionType.PROX;
				lastConnection = getLastConnection(connection);
			} else {
				this.addLog(connection.getPeripheralDeviceId(),
						"last connection was too long ago: "
								+ DateTimeUtilities.timestampToReadableAltWithNyTimezone(lastConnection),
						LogConstants.TRACE);
				lastConnection = connection.getLastSuccess();
			}
		} else {
			if (connection == null) {
				this.addLog(device.getDeviceId(), "connection was null", LogConstants.ERROR);
			} else {
				lastConnection = connection.getLastSuccess();
				this.addLog(connection.getPeripheralDeviceId(), "either central or peripheral is not connected",
						LogConstants.TRACE);
			}
		}
		if (!connected && connection != null) {
			this.addLog(connection.getPeripheralDeviceId(), "not bluetooth connected", LogConstants.TRACE);
			AccessAllowedWithAccessType accessData = isSubscribedWithinTimePeriodWithData(connection, mobilePeripheral);
			connected = accessData.isAccessAllowed();
			connectionType = accessData.getConnectionType();
			lastConnection = accessData.getCheckTime();
		}
		return new AccessAllowedWithAccessType(connected, connectionType, lastConnection);
	}

	private Timestamp getLastConnection(DeviceConnectionDbObj connection) {
		Timestamp lastConnection;
		if (connection.getLastCentralConnectionSuccess().after(connection.getLastPeripheralConnectionSuccess())) {
			lastConnection = connection.getLastCentralConnectionSuccess();
		} else {
			lastConnection = connection.getLastPeripheralConnectionSuccess();
		}
		if (connection.getLastSuccess().after(lastConnection)) {
			lastConnection = connection.getLastSuccess();
		}
		return lastConnection;
	}

	private AccessAllowedWithAccessType isSubscribedWithinTimePeriodWithData(DeviceConnectionDbObj connection,
			boolean mobilePeripheral) {
		boolean connected = false;
		ConnectionType connectionType = ConnectionType.NONE;
		Timestamp lastConnection = null;
		if (connection != null && (connection.isSubscribed())) {
			Timestamp lastSubscribed = connection.getLastSubscribed();
			long secondsAgo = DateTimeUtilities.timestampSecondAgo(lastSubscribed);
			this.addLog(connection.getPeripheralDeviceId(), "lastConnection was " + secondsAgo + " seconds ago.",
					LogConstants.IMPORTANT);
			Timestamp timePeriod = DateTimeUtilities
					.getCurrentTimestampMinusSeconds(Constants.BLUETOOTH_CONNECTION_TIME_PERIOD);
			this.addLog("is " + lastSubscribed + " after " + timePeriod);
			if (Constants.BLUETOOTH_CONNECTION_TIME_PERIOD > secondsAgo) {

				long lastSuccessSecondsAgo = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS + 1;
				if (connection.getLastSuccess() != null) {
					lastSuccessSecondsAgo = DateTimeUtilities.timestampSecondAgo(connection.getLastSuccess());
				}
				long timeLimit;
				if (mobilePeripheral) {
					timeLimit = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS_MOBILE_PERIPHERAL;
				} else {
					timeLimit = Constants.PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS;
				}
				if (timeLimit > lastSuccessSecondsAgo) {
					connected = true;
					connectionType = ConnectionType.PROX;
					lastConnection = lastSubscribed;
				} else {
					this.addLog(connection.getPeripheralDeviceId(),
							"subscribed but not transferred within the last 24 hours", LogConstants.DEBUG);
				}
			} else {
				this.addLog(connection.getPeripheralDeviceId(), "not Bluetooth connected within the last 20 minutes.",
						LogConstants.DEBUG);
			}
		} else {
			this.addLog(connection.getPeripheralDeviceId(), "we are not subscribed", LogConstants.DEBUG);
		}
		return new AccessAllowedWithAccessType(connected, connectionType, lastConnection);
	}

	public boolean isRecentSuccessOrConnecting(DeviceDbObj device) {
		boolean success = isProximate(device, false);
		if (!success) {
			success = isConnecting(device);
		}
		return success;
	}

	public AdminCodeResponse generateAccessCodes() {
		return generateAccessCodes(10);
	}

	public AdminCodeResponse generateAccessCodes(int count) {
		CheckDbObj check;
		ArrayList<String> codes = new ArrayList<>();
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Timestamp expiration = DateTimeUtilities.getCurrentTimestampPlusSeconds(Constants.ADMIN_CODE_DURATION_SECONDS);
		for (int i = 0; i < count; i++) {
			String adminCode = UUID.randomUUID().toString();
			codes.add(adminCode);
			check = new CheckDbObj(GeneralUtilities.randomString(), null, null, null, null, null, null, null, null,
					null, false, false, Outcomes.INCOMPLETE, now, null, false, CheckType.ADMIN_CODE, adminCode,
					adminCode, expiration);
			this.addCheck(check);
		}
		addLog("added " + count + "admin codes");
		return new AdminCodeResponse(codes);
	}

	public AccessAllowedWithAccessType didGiveAccess(DeviceConnectionDbObj connection, boolean isCentral) {
		boolean connected = false;
		ConnectionType connType = ConnectionType.NONE;
		String query = "SELECT * FROM B2F_CHECK WHERE ";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Timestamp connTime = now;
		if (isCentral) {
			query += "CENTRAL_DEVICE_ID = ? ";
		} else {
			query += "PERIPHERAL_DEVICE_ID = ? ";
		}
		query += "AND COMPLETED = ? AND OUTCOME = ? AND EXPIRED = ? "
				+ "AND (CHECK_TYPE = ? OR CHECK_TYPE = ? OR CHECK_TYPE = ? OR CHECK_TYPE = ?) "
				+ "AND EXPIRATION_DATE > ? ORDER BY COMPLETION_DATE DESC LIMIT 1";
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			if (isCentral) {
				prepStmt.setString(1, connection.getCentralDeviceId());
			} else {
				prepStmt.setString(1, connection.getPeripheralDeviceId());
			}
			prepStmt.setBoolean(2, true);
			prepStmt.setInt(3, Outcomes.SUCCESS);
			prepStmt.setBoolean(4, false);

			prepStmt.setString(5, CheckType.PUSH.checkTypeName().toLowerCase());
			prepStmt.setString(6, CheckType.PASSKEY.checkTypeName().toLowerCase());
			prepStmt.setString(7, CheckType.TXT.checkTypeName());
			prepStmt.setString(8, CheckType.ADMIN_CODE.checkTypeName().toLowerCase());
			prepStmt.setTimestamp(9, now);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				CheckDbObj check = this.recordToCheck(rs);
				connType = check.getCheckType().getConnectionType();
				connTime = check.getCompletionDate();
				connected = true;
			}
		} catch (Exception e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return new AccessAllowedWithAccessType(connected, connType, connTime);
	}

	public boolean didVeryRecentlyGiveAccess(DeviceDbObj device) {
		boolean connected = false;
		if (!device.isTurnedOff()) {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			String query = "SELECT * FROM B2F_CHECK WHERE ";
			if (device.isCentral()) {
				query += "CENTRAL_DEVICE_ID = ? ";
			} else {
				query += "PERIPHERAL_DEVICE_ID = ? ";
			}
			query += "AND COMPLETED = ? AND OUTCOME = ? AND EXPIRED = ? "
					+ "AND (CHECK_TYPE = ? OR CHECK_TYPE = ? OR CHECK_TYPE = ?) "
					+ "AND EXPIRATION_DATE > ? ORDER BY COMPLETION_DATE DESC LIMIT 1";
			ResultSet rs = null;
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, device.getDeviceId());
				prepStmt.setBoolean(2, true);
				prepStmt.setInt(3, Outcomes.SUCCESS);
				prepStmt.setBoolean(4, false);

				prepStmt.setString(5, CheckType.TXT.checkTypeName().toLowerCase());
				prepStmt.setString(6, CheckType.PASSKEY.checkTypeName().toLowerCase());
				prepStmt.setString(7, CheckType.SSHPUSH.checkTypeName().toLowerCase());
				prepStmt.setTimestamp(8, now);
				logQuery(getMethodName(), prepStmt);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					CheckDbObj check = recordToCheck(rs);
					Timestamp completionDate = check.getCompletionDate();
					if (completionDate != null) {
						long seconds = DateTimeUtilities.timestampSecondAgo(completionDate);
						if (seconds < Constants.VERY_RECENT_FINGERPRINT_OR_PUSH_MOBILE_TIMEOUT_SECS) {
							addLog("checkType: " + check.getCheckType().toString().toUpperCase());
							if (check.getCheckType().toString().toUpperCase()
									.equals(CheckType.SSHPUSH.checkTypeName())) {
								check.setExpired(true, "didVeryRecentlyGiveAccess");
								updateCheck(check);
							}
							connected = true;
						}
						addLog(device.getDeviceId(), "gave access " + seconds + " seconds ago.");
					}
				}
			} catch (Exception e) {
				this.addLog(device.getDeviceId(), e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}

		} else {
			this.addLog(device.getDeviceId(), "device was turned off");
		}
		this.addLog(device.getDeviceId(), "connected: " + connected);
		return connected;
	}

	public void expireFingerprintAndPush(DeviceDbObj device) {
		String query = "UPDATE B2F_CHECK SET EXPIRATION_DATE = ?, EXPIRED = ? WHERE CHECK_TYPE "
				+ "IN (?, ?, ?, ?) AND EXPIRATION_DATE > ? ";
		if (device.isCentral()) {
			query += "AND CENTRAL_DEVICE_ID = ? ";
		} else {
			query += "AND PERIPHERAL_DEVICE_ID = ? ";
		}
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setTimestamp(1, now);
			prepStmt.setBoolean(2, true);
			prepStmt.setString(3, CheckType.PASSKEY.toString());
			prepStmt.setString(4, CheckType.PUSH.toString());
			prepStmt.setString(5, CheckType.TXT.toString());
			prepStmt.setString(6, CheckType.SSHPUSH.toString());
			prepStmt.setTimestamp(7, now);
			prepStmt.setString(8, device.getDeviceId());
			logQueryImportant(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
		} catch (Exception e) {
			this.addLog(device.getDeviceId(), e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
	}

	public ConnectedAndConnectionType didGiveAccess(DeviceDbObj device) {
		return didGiveAccess(device, false);
	}

	public void deactivateFingerprint(DeviceDbObj device) {
		new Thread(() -> {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			String query = "UPDATE B2F_CHECK SET EXPIRATION_DATE = ?, EXPIRED = ? WHERE EXPIRED = ? AND "
					+ "CHECK_TYPE = ? AND ";
			if (device.isCentral()) {
				query += "CENTRAL_DEVICE_ID = ?";
			} else {
				query += "PERIPHERAL_DEVICE_ID = ?";
			}
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, now);
				prepStmt.setBoolean(2, true);
				prepStmt.setBoolean(3, false);
				prepStmt.setString(4, CheckType.PASSKEY.name());
				prepStmt.setString(5, device.getDeviceId());
				prepStmt.executeUpdate();
			} catch (Exception e) {
				this.addLog(device.getDeviceId(), e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}

		}).start();
	}

	public ConnectedAndConnectionType didGiveAccess(DeviceDbObj device, boolean deactivateRecord) {
		boolean connected = false;
		ConnectionType connType = null;
		if (!device.isTurnedOff()) {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			String query = "SELECT * FROM B2F_CHECK WHERE ";
			if (device.isCentral()) {
				query += "CENTRAL_DEVICE_ID = ? ";
			} else {
				query += "PERIPHERAL_DEVICE_ID = ? ";
			}
			query += "AND COMPLETED = ? AND OUTCOME = ? AND EXPIRED = ? "
					+ "AND (CHECK_TYPE = ? OR CHECK_TYPE = ? OR CHECK_TYPE = ? OR CHECK_TYPE = ?) "
					+ "AND EXPIRATION_DATE > ? ORDER BY COMPLETION_DATE DESC LIMIT 1";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, device.getDeviceId());
				prepStmt.setBoolean(2, true);
				prepStmt.setInt(3, Outcomes.SUCCESS);
				prepStmt.setBoolean(4, false);

				prepStmt.setString(5, CheckType.PUSH.checkTypeName().toLowerCase());
				prepStmt.setString(6, CheckType.PASSKEY.checkTypeName().toLowerCase());
				prepStmt.setString(7, CheckType.TXT.checkTypeName().toLowerCase());
				prepStmt.setString(8, CheckType.ADMIN_CODE.checkTypeName().toLowerCase());
				prepStmt.setTimestamp(9, now);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					connected = true;
					CheckDbObj check = recordToCheck(rs);
					connType = check.getCheckType().getConnectionType();
					if (deactivateRecord) {
						check.setExpired(connected, "didGiveAccess");
						check.setExpirationDate(now);
						this.updateCheck(check);
					}
				}
			} catch (Exception e) {
				this.addLog(device.getDeviceId(), e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}

		} else {
			this.addLog(device.getDeviceId(), "device was turned off", LogConstants.WARNING);
		}
		this.addLog(device.getDeviceId(), "connected: " + connected + "; connType: " + connType,
				LogConstants.TRACE);
		return new ConnectedAndConnectionType(connected, connType);
	}

	public AccessAllowedWithAccessType didGiveAccessWithReturnType(DeviceDbObj device) {
		boolean connected = false;
		ConnectionType connectionType = ConnectionType.NONE;
		Timestamp completionDate = null;
		if (!device.isTurnedOff() && !device.isScreensaverOn()) {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			String query = "SELECT * FROM B2F_CHECK WHERE ";
			if (device.isCentral()) {
				query += "CENTRAL_DEVICE_ID = ? ";
			} else {
				query += "PERIPHERAL_DEVICE_ID = ? ";
			}
			query += "AND COMPLETED = ? AND OUTCOME = ? AND EXPIRED = ? "
					+ "AND (CHECK_TYPE = ? OR CHECK_TYPE = ? OR CHECK_TYPE = ? OR CHECK_TYPE = ?) "
					+ "AND EXPIRATION_DATE > ? ORDER BY COMPLETION_DATE DESC LIMIT 1";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, device.getDeviceId());
				prepStmt.setBoolean(2, true);
				prepStmt.setInt(3, Outcomes.SUCCESS);
				prepStmt.setBoolean(4, false);

				prepStmt.setString(5, CheckType.PUSH.checkTypeName().toLowerCase());
				prepStmt.setString(6, CheckType.PASSKEY.checkTypeName().toLowerCase());
				prepStmt.setString(7, CheckType.TXT.checkTypeName().toLowerCase());
				prepStmt.setString(8, CheckType.ADMIN_CODE.checkTypeName().toLowerCase());
				prepStmt.setTimestamp(9, now);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					CheckDbObj check = this.recordToCheck(rs);
					connectionType = check.getCheckType().getConnectionType();
					completionDate = check.getCompletionDate();
					connected = true;
				}
			} catch (Exception e) {
				this.addLog(device.getDeviceId(), e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}

		} else {
			this.addLog(device.getDeviceId(), "device was turned off or screensaver on");
		}
		this.addLog(device.getDeviceId(), "connected: " + connected);
		return new AccessAllowedWithAccessType(connected, connectionType, completionDate);
	}

	public DeviceDbObj getDeviceByAccessCode(String accessCode) {
		DeviceDbObj device = null;
		String query = "SELECT device.* FROM B2F_ACCESS_STRINGS access JOIN B2F_DEVICE device "
				+ "ON access.DEVICE_ID = device.DEVICE_ID WHERE access.ACCESS_CODE = ? AND " + "access.ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, accessCode);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				device = this.recordToDevice(rs);
			}
			this.addLog(accessCode, "found: " + (device != null));
		} catch (Exception e) {
			this.addLog("getDeviceByAccessCode", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return device;
	}

	public boolean expireAccessCode(String accessCode) {
		boolean success = false;
		String query = "UPDATE B2F_ACCESS_STRINGS SET ACTIVE = ? WHERE ACCESS_CODE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, accessCode);
			logQuery(getMethodName(), prepStmt);
			success = prepStmt.executeUpdate() > 0;
			addLog("setting accessCode to false, success: " + success);
		} catch (Exception e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public boolean expireAccessCodesBesides(String accessCode, String browserId) {
		boolean success = false;
		String query = "UPDATE B2F_ACCESS_STRINGS SET ACTIVE = ? WHERE ACCESS_CODE != ? AND BROWSER_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, accessCode);
			prepStmt.setString(3, browserId);
			logQuery(getMethodName(), prepStmt);
			success = prepStmt.executeUpdate() > 0;
			addLog("setting accessCode to false, success: " + success);
		} catch (Exception e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public DeviceDbObj getDeviceByToken(String token) {
		return getDeviceByToken(token, null);
	}

	/**
	 * Used in resyncing browser
	 * 
	 * @param token
	 * @return
	 */
	public DeviceDbObj getDeviceBySessionTokenIgnoringExpiration(String token) {
		DeviceDbObj device = null;
		String query = "SELECT device.* FROM B2F_TOKEN token JOIN B2F_DEVICE device "
				+ "ON token.DEVICE_ID = device.DEVICE_ID WHERE token.TOKEN_ID = ? AND token.DESCRIPTION = ?";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, token);
			prepStmt.setString(2, TokenDescription.BROWSER_SESSION.toString());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				device = this.recordToDevice(rs, "getDeviceBySessionTokenIgnoringExpiration");
				this.addLog("device was found");
			}
			this.addLog("found: " + (device != null));
		} catch (Exception e) {
			this.addLog("getDeviceBySessionTokenIgnoringExpiration", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return device;
	}

	public DeviceDbObj getDeviceByTokenIgnoringExpiration(String token) {
		return getDeviceByTokenIgnoringExpiration(token, null);
	}

	public DeviceDbObj getDeviceByTokenIgnoringExpiration(String token, String src) {
		DeviceDbObj device = null;
		String query = "SELECT device.* FROM B2F_TOKEN token JOIN B2F_DEVICE device "
				+ "ON token.DEVICE_ID = device.DEVICE_ID WHERE token.TOKEN_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, token);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				device = this.recordToDevice(rs);
				this.addLog("device was set");
			}
			String srcStr;
			if (!TextUtils.isEmpty(src)) {
				srcStr = " -- from: " + src;
			} else {
				srcStr = " -- source not given";
			}
			this.addLog("getDeviceByTokenIgnoringExpiration", "found: " + (device != null) + srcStr);
		} catch (Exception e) {
			this.addLog("getDeviceByTokenIgnoringExpiration", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return device;
	}

	public DeviceDbObj getActiveDeviceByToken(String token, String src) {
		DeviceDbObj device = null;
		String now = DateTimeUtilities.getLastTimestampString();

		String query = "SELECT device.* FROM B2F_TOKEN token JOIN B2F_DEVICE device "
				+ "ON token.DEVICE_ID = device.DEVICE_ID WHERE token.TOKEN_ID = ? AND "
				+ "(token.EXPIRE_TIME IS NULL OR token.EXPIRE_TIME > ?) AND device.ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, token);
			prepStmt.setString(2, now);
			prepStmt.setBoolean(3, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				device = this.recordToDevice(rs);
				this.addLog("device was set");
			}
			String srcStr;
			if (!TextUtils.isEmpty(src)) {
				srcStr = " -- from: " + src;
			} else {
				srcStr = " -- source not given";
			}
			this.addLog("found: " + (device != null) + srcStr);
		} catch (Exception e) {
			this.addLog("getDeviceByToken", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return device;
	}

	public DeviceDbObj getDeviceByToken(String token, String src) {
		DeviceDbObj device = null;
		String now = DateTimeUtilities.getLastTimestampString();

		String query = "SELECT device.* FROM B2F_TOKEN token JOIN B2F_DEVICE device "
				+ "ON token.DEVICE_ID = device.DEVICE_ID WHERE token.TOKEN_ID = ? AND "
				+ "(token.EXPIRE_TIME IS NULL OR token.EXPIRE_TIME > ?)";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, token);
			prepStmt.setString(2, now);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				device = this.recordToDevice(rs);
				this.addLog("device was set");
			}
			String srcStr;
			if (!TextUtils.isEmpty(src)) {
				srcStr = " -- from: " + src;
			} else {
				srcStr = " -- source not given";
			}
			this.addLog("found: " + (device != null) + srcStr);
		} catch (Exception e) {
			this.addLog("getDeviceByToken", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return device;
	}

	public CompanyDbObj getCompanyByToken(String token, String src) {
		CompanyDbObj company = null;
		String now = DateTimeUtilities.getLastTimestampString();

		String query = "SELECT company.* FROM B2F_TOKEN token JOIN B2F_GROUP grp ON "
				+ "grp.GROUP_ID = token.GROUP_ID JOIN " + "B2F_COMPANY company ON "
				+ "company.COMPANY_ID = grp.COMPANY_ID " + "WHERE token.TOKEN_ID = ? AND (token.EXPIRE_TIME IS NULL OR "
				+ "token.EXPIRE_TIME > ?)";

		if (!src.equals("")) {
			this.addLog("src: " + src);
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, token);
			prepStmt.setString(2, now);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				company = this.recordToCompany(rs);
			}
			String srcStr;
			if (!TextUtils.isEmpty(src)) {
				srcStr = " -- from: " + src;
			} else {
				srcStr = " -- source not given";
			}
			this.addLog("found: " + (company != null) + srcStr);
		} catch (Exception e) {
			this.addLog("getCompanyByToken", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return company;
	}

	public DeviceDbObj getDeviceByFcm(String fcmId) {
		DeviceDbObj device = null;
		String query = "SELECT " + deviceFields + " FROM B2F_DEVICE WHERE FCM_ID = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, fcmId);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				device = recordToDevice(rs);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return device;
	}

	public boolean addDevice(DeviceDbObj device) {
		boolean success = false;
		String query = "INSERT INTO B2F_DEVICE (" + deviceFields + ") VALUES ("
				+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
				+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, device.getGroupId());
			prepStmt.setString(2, device.getUserId());
			prepStmt.setString(3, device.getDeviceId());
			prepStmt.setInt(4, device.getSeed());
			prepStmt.setBoolean(5, device.isActive());
			prepStmt.setString(6, device.getFcmId());
			prepStmt.setString(7, device.getBtAddress());
			prepStmt.setTimestamp(8, DateTimeUtilities.getCurrentTimestamp());
			prepStmt.setTimestamp(9, DateTimeUtilities.getCurrentTimestamp());
			prepStmt.setString(10, device.getDeviceType());
			if (device.getOperatingSystem() != null) {
				prepStmt.setString(11, device.getOperatingSystem().osClassName());
			} else {
				prepStmt.setString(11, "");
			}
			prepStmt.setString(12, device.getLoginToken());
			prepStmt.setDate(13, null);
			prepStmt.setString(14, device.getOsVersion());
			prepStmt.setString(15, device.getUserLanguage());
			prepStmt.setInt(16, device.getLastGmtOffset());

			prepStmt.setString(17, device.getScreenSize());
			prepStmt.setString(18, device.getRand());
			prepStmt.setBoolean(19, device.getShowIcon());
			prepStmt.setDouble(20, device.getDevicePriority());
			prepStmt.setBoolean(21, device.getTriggerUpdate());
			prepStmt.setInt(22, device.getRecentPushes());
			prepStmt.setBoolean(23, device.getUnresponsive());
			prepStmt.setTimestamp(24, device.getLastPush());
			prepStmt.setBoolean(25, device.isPushLoud());
			prepStmt.setBoolean(26, device.isPushFailure());
			prepStmt.setString(27, device.getCommand());
			/*
			 * "TEMP, CENTRAL, LAST_RESET, SCREENSAVER_ON, LAST_VARIABLE_RETRIEVAL, TURNED_OFF, DEVICE_CLASS, "
			 * //7 +
			 * "BROWSER_INSTALL_COMPLETE, SIGNED_IN, LAST_SILENT_PUSH, HAS_BLE, TURN_OFF_FROM_INSTALLER, PHONE_NUMBER,"
			 * //6 + "MULTI_USER, PASSKEY_ENABLED"; //2
			 */
			prepStmt.setString(28, device.getTemp());
			prepStmt.setBoolean(29, device.isCentral());
			prepStmt.setTimestamp(30, device.getLastReset());
			prepStmt.setBoolean(31, device.isScreensaverOn());
			prepStmt.setTimestamp(32, device.getLastVariableRetrieval());
			prepStmt.setBoolean(33, device.isTurnedOff());
			prepStmt.setString(34, device.getDeviceClass().deviceClassName());
			prepStmt.setBoolean(35, device.isBrowserInstallComplete());
			prepStmt.setBoolean(36, device.getSignedIn());
			prepStmt.setTimestamp(37, device.getLastSilentPush());
			prepStmt.setTimestamp(38, device.getLastSilentPushResponse());
			prepStmt.setBoolean(39, device.getHasBle());
			prepStmt.setBoolean(40, device.getTurnOffFromInstaller());
			prepStmt.setString(41, device.getPhoneNumber());
			prepStmt.setBoolean(42, device.isMultiUser());
			prepStmt.setBoolean(43, device.isPasskeyEnabled());
			prepStmt.setInt(44, device.getTxPower());
			prepStmt.setBoolean(45, device.getTerminate());
			logQueryImportant(getMethodName(), prepStmt);
			addLog(device.getDeviceId(), "signedIn: " + device.getSignedIn());
			prepStmt.executeUpdate();
			success = true;
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public boolean deleteDevice(DeviceDbObj device) {
		return deleteDeviceByDevId(device.getDeviceId());
	}

	private boolean deleteDeviceByDevId(String deviceId) {
		boolean success = false;
		if (deviceId.endsWith("-old")) {
			String query = "UPDATE B2F_DEVICE SET DEVICE_ID = ? WHERE DEVICE_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, deviceId + "-old");
				prepStmt.setString(2, deviceId);
				logQuery(getMethodName(), prepStmt);
				if (prepStmt.executeUpdate() > 0) {
					success = deleteConnectionsWithDeviceId(deviceId);
				}
				success = true;
			} catch (SQLException e) {
				addLog(e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		} else {
			success = true;
		}
		return success;
	}

	private boolean deleteConnectionsWithDeviceId(String deviceId) {
		boolean sucCent = deleteCentralConnectionsWithDeviceId(deviceId);
		boolean sucPerf = deletePeripheralConnectionsWithDeviceId(deviceId);
		return sucCent || sucPerf;
	}

	private boolean deleteCentralConnectionsWithDeviceId(String deviceId) {
		boolean success = false;
		String query = "UPDATE B2F_DEVICE_CONNECTION SET CENTRAL_DEVICE_ID = ?, ACTIVE = ? "
				+ "WHERE CENTRAL_DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId + "-old");
			prepStmt.setBoolean(2, false);
			prepStmt.setString(3, deviceId);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	private boolean deletePeripheralConnectionsWithDeviceId(String deviceId) {
		boolean success = false;
		String query = "UPDATE B2F_DEVICE_CONNECTION SET PERIPHERAL_DEVICE_ID = ?, ACTIVE = ? "
				+ "WHERE PERIPHERAL_DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId + "-old");
			prepStmt.setBoolean(2, false);
			prepStmt.setString(3, deviceId);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
		return success;
	}

	public DeviceDbObj getTestComputer() {
		Timestamp ts = DateTimeUtilities.getCurrentTimestampMinusMinutes(10000);
		return new DeviceDbObj("8JIZJNtflFTPQfHAi3cBSzMeIBHjGLwugYNBHYar", "eHDjf6pngBMeKZ8CCOszghGRsMMTxYgk62ZIWdBv",
				"OKgcZY2kMobCzKVd89pQqms0FlFewYxsIram7HAB", 428937312, true, "", "", new Date(),
				DateTimeUtilities.getCurrentTimestamp(), "", OsClass.OSX, "C02V81MKHV2H", new Date(), -18000, "10_14_6",
				"en-US", "1440900%4019201200", "", true, 650.0, false, 0, false, ts, false, false, "", "", false, ts,
				false, ts, false, DeviceClass.COMPUTER, false, true, ts, null, true, false, null, false, true, null,
				false);
	}
}

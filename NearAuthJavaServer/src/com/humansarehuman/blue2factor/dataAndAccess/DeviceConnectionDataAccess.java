package com.humansarehuman.blue2factor.dataAndAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.util.TextUtils;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.ConnectionLogDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class DeviceConnectionDataAccess extends DataAccess {

	public ArrayList<DeviceConnectionDbObj> getConnectionsForDevice(String deviceId) {
		ArrayList<DeviceConnectionDbObj> connections = new ArrayList<>();
		DeviceConnectionDbObj connection = null;
		ResultSet rs = null;
		String query = "SELECT *  FROM B2F_DEVICE_CONNECTION WHERE (CENTRAL_DEVICE_ID = ? || PERIPHERAL_DEVICE_ID = ?) "
				+ "AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setString(2, deviceId);
			prepStmt.setBoolean(3, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connection = recordToConnection(rs);
				connections.add(connection);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getAllActiveConnections", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connections;
	}

	public boolean updateAsProximatelyConnected(DeviceConnectionDbObj connection, Boolean fromCentral) {
		return updateAsProximatelyConnected(connection, null, fromCentral);
	}

	public boolean updateAsProximatelyConnected(DeviceConnectionDbObj connection, String centralOsId,
			Boolean fromCentral) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("LAST_SUCCESS", now);
		hm.put("LAST_CENTRAL_CONNECTION_SUCCESS", now);
		hm.put("CENTRAL_CONNECTED", true);
		hm.put("PERIPHERAL_CONNECTED", true);
		hm.put("LAST_PERIPHERAL_CONNECTION_SUCCESS", now);
		this.addLog("connectionChanged to connected for " + connection.getPeripheralDeviceId());
		String from = "";
		if (fromCentral == true) {
			from = " from central";
		} else if (fromCentral == false) {
			from = " from peripheral";
		}
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.setPushFailedFromConnection(connection, false);
		if (peripheralIsAwake(connection)) {
			addConnectionLogIfNeeded(connection, true, DateTimeUtilities.getCurrentTimestamp(), centralOsId,
					"updateAsConnected", ConnectionType.PROX);
		}
		return updateConnectionMap(connection, hm, "updateAsConnected" + from);
	}

	public boolean peripheralIsAwake(DeviceConnectionDbObj connection) {
		boolean awake = false;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		DeviceDbObj peripheral = dataAccess.getDeviceByDeviceId(connection.getPeripheralDeviceId(),
				"peripheralIsAwake");
		if (peripheral != null) {
			if (peripheral.isActive() && !peripheral.isScreensaverOn()) {
				dataAccess.addLog("peripheral is awake", LogConstants.TRACE);
				awake = true;
			}
		} else {
			dataAccess.addLog("peripheral is sleeping", LogConstants.TRACE);
			dataAccess.addLog("peripheral is null for conId: " + connection.getConnectionId());
		}
		return awake;
	}

	public ArrayList<DeviceConnectionDbObj> getAllActiveConnections() {
		ArrayList<DeviceConnectionDbObj> connections = new ArrayList<>();
		DeviceConnectionDbObj connection = null;

		String query = "SELECT *  FROM B2F_DEVICE_CONNECTION WHERE ACTIVE = ?";
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connection = recordToConnection(rs);
				connections.add(connection);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getAllActiveConnections", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connections;
	}

	public boolean deactivateConnection(DeviceConnectionDbObj connection) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("CENTRAL_DEVICE_ID", "Deactivated-" + connection.getCentralDeviceId());
		hm.put("PERIPHERAL_DEVICE_ID", "Deactivated-" + connection.getPeripheralDeviceId());
		hm.put("GROUP_ID", "Deactivated-" + connection.getGroupId());
		hm.put("ACTIVE", false);
		addConnectionLogIfNeeded(connection, false, DateTimeUtilities.getCurrentTimestamp(),
				connection.getCentralDeviceId(), "deactivateConnection", null);
		return updateConnectionMap(connection, hm, null);
	}

	public boolean setCentralIdGroupIdAndActive(String centralDeviceId, String groupId,
			DeviceConnectionDbObj connection) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("CENTRAL_DEVICE_ID", centralDeviceId);
		hm.put("GROUP_ID", groupId);
		hm.put("ACTIVE", true);
		hm.put("INSTALL_COMPLETE", true);
		return updateConnectionMap(connection, hm, "setCentralIdGroupIdAndActive");
	}

	public boolean setCentralIdGroupIdActiveAndInstallComplete(String centralDeviceId, String groupId,
			DeviceConnectionDbObj connection) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("CENTRAL_DEVICE_ID", centralDeviceId);
		hm.put("GROUP_ID", groupId);
		hm.put("ACTIVE", true);
		hm.put("INSTALL_COMPLETE", true);
		return updateConnectionMap(connection, hm, "setCentralIdGroupIdActiveAndInstallComplete");
	}

	public boolean setCentralIdAndGroupId(String centralDeviceId, String groupId, DeviceConnectionDbObj connection) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("CENTRAL_DEVICE_ID", centralDeviceId);
		hm.put("GROUP_ID", groupId);
		return updateConnectionMap(connection, hm, "setCentralIdAndGroupId");
	}

	public void resetServiceAndCharacteristicForConnection(DeviceConnectionDbObj connection) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("SERVICE_UUID", createNewB2fUuid());
		hm.put("CHARACTER_UUID", createNewB2fUuid());
		this.addLog("reseting UUIDs for " + connection.getPeripheralDeviceId(), LogConstants.WARNING);
		updateConnectionMap(connection, hm, "resetServiceAndCharacteristicForConnection");
	}

	public void resetServicesAndCharacteristics() {
		new Thread(() -> {
			ArrayList<DeviceConnectionDbObj> conns = getAllActiveConnections();
			for (DeviceConnectionDbObj connection : conns) {
				if (!this.isProximate(connection, false)) {
					resetServiceAndCharacteristicForConnection(connection);
				}
			}
		}).start();
	}

	public void resetServicesAndCharacteristicsForDevice(String deviceId) {
		new Thread(() -> {
			ArrayList<DeviceConnectionDbObj> conns = this.getConnectionsForDevice(deviceId);
			for (DeviceConnectionDbObj connection : conns) {
				if (!this.isProximate(connection, false)) {
					resetServiceAndCharacteristicForConnection(connection);
				}
			}
		}).start();
	}

	public String createNewB2fUuid() {
		UUID uuid = UUID.randomUUID();
		String uuidStr = uuid.toString();
		uuidStr = uuidStr.replace(uuidStr.substring(0, 8), "0000b2f" + GeneralUtilities.randomHex());
		return uuidStr.toLowerCase();
	}

	public ArrayList<DeviceDbObj> getAllActiveDevices() {
		ArrayList<DeviceDbObj> devices = new ArrayList<>();
		DeviceDbObj device = null;
		String query = "SELECT * FROM B2F_DEVICE WHERE ACTIVE = ? ORDER BY GROUP_ID, CENTRAL DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				device = recordToDevice(rs);
				devices.add(device);
			}
		} catch (SQLException e) {
			this.addLog("getAllActiveDevices", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return devices;
	}

	public boolean doesConnectionExistByEmail(String email) {
		boolean connected = false;
		String query = "SELECT * FROM B2F_GROUP bg JOIN B2F_DEVICE bd ON bg.GROUP_ID = bd.GROUP_ID "
				+ "JOIN B2F_DEVICE_CONNECTION bdc ON bd.DEVICE_ID = bdc.CENTRAL_DEVICE_ID WHERE bg.GROUP_NAME = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, email);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				connected = true;
			}
		} catch (SQLException e) {
			this.addLog("getConnectionByDeviceIds", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		this.addLog("connection exists: " + connected);
		return connected;
	}

	public Timestamp getLastConnection(DeviceDbObj device) {
		Timestamp lastSuccessfulConnection = null;
		String query = "SELECT LAST_SUCCESS FROM B2F_DEVICE_CONNECTION WHERE ACTIVE = ? AND ";
		if (device.isCentral()) {
			query += "CENTRAL_DEVICE_ID = ? ";
		} else {
			query += "PERIPHERAL_DEVICE_ID = ? ";
		}
		query += "ORDER BY LAST_SUCCESS DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setString(2, device.getDeviceId());
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				lastSuccessfulConnection = rs.getTimestamp("LAST_SUCCESS");
			}
		} catch (Exception e) {
			this.addLog("getLastConnection", e);
		}
		return lastSuccessfulConnection;
	}

	public DeviceConnectionDbObj getConnectionByDeviceIds(String centralInstanceId, String peripheralInstanceId) {
		DeviceConnectionDbObj connection = null;

		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE CENTRAL_DEVICE_ID = ? "
				+ "AND PERIPHERAL_DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, centralInstanceId);
			prepStmt.setString(2, peripheralInstanceId);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connection = recordToConnection(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(centralInstanceId, e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connection;
	}

	public DeviceConnectionDbObj getConnectionByPeripheralId(String peripheralInstanceId) {
		DeviceConnectionDbObj connection = null;

		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE PERIPHERAL_DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, peripheralInstanceId);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connection = recordToConnection(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(peripheralInstanceId, e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connection;
	}

	public ArrayList<DeviceConnectionDbObj> getAllConnectionsForCentral(DeviceDbObj central, boolean active) {
		ArrayList<DeviceConnectionDbObj> connections = new ArrayList<>();
		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE CENTRAL_DEVICE_ID = ? AND SERVICE_UUID IS NOT "
				+ "NULL AND ACTIVE = ? ORDER BY CENTRAL_CONNECTED ASC, LAST_CENTRAL_CONNECTION_SUCCESS DESC";
		String centralId = "";
		if (central != null) {
			centralId = central.getDeviceId();
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, central.getDeviceId());
			prepStmt.setBoolean(2, active);
			rs = executeQuery(prepStmt);
			DeviceConnectionDbObj connection;
			while (rs.next()) {
				connection = recordToConnection(rs);
				connections.add(connection);
			}
		} catch (SQLException e) {
			this.addLog(centralId, e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connections;
	}

	public ArrayList<DeviceConnectionDbObj> getConnectionsForDevice(DeviceDbObj device, boolean activeOnly) {
		ArrayList<DeviceConnectionDbObj> connections;
		if (!device.isCentral()) {
			connections = new ArrayList<>();
			DeviceConnectionDbObj connection = getConnectionForPeripheral(device, activeOnly);
			if (connection != null) {
				this.addLog("connection found");
				connections.add(connection);
			}
		} else {
			connections = getAllConnectionsForCentral(device, activeOnly);
		}
		return connections;
	}

	public ArrayList<DeviceConnectionDbObj> getAllUsersConnectionsFromPeripheral(DeviceDbObj device,
			boolean activeOnly) {
		ArrayList<DeviceConnectionDbObj> connections;
		if (!device.isCentral()) {
			connections = new ArrayList<>();
			DeviceConnectionDbObj connection = getConnectionForPeripheral(device, activeOnly);
			if (connection != null) {
				this.addLog("connection found");
				connections.add(connection);
			}
		} else {
			connections = getAllConnectionsForCentral(device, activeOnly);
		}
		return connections;
	}

	public ArrayList<DeviceConnectionDbObj> getAllUsersConnections(DeviceDbObj device, boolean activeOnly) {
		ArrayList<DeviceConnectionDbObj> connections = new ArrayList<>();
		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE GROUP_ID = ?";
		if (activeOnly) {
			query += " AND ACTIVE = ?";
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, device.getGroupId());
			prepStmt.setBoolean(2, true);
			rs = executeImportantQuery(prepStmt);
			while (rs.next()) {
				DeviceConnectionDbObj connection = recordToConnection(rs);
				connections.add(connection);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connections;
	}

	public DeviceConnectionDbObj getConnectionByConnectionId(String connectionId) {
		DeviceConnectionDbObj connection = null;
		String query = "SELECT *  FROM B2F_DEVICE_CONNECTION WHERE CONNECTION_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connectionId);
			this.logQuery("getConnectionByConnectionId", prepStmt);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connection = recordToConnection(rs);
				break;
			}
		} catch (SQLException e) {
			addLog("DeviceConnectionDbObj",
					"SELECT *  FROM B2F_DEVICE_CONNECTION WHERE CONNECTION_ID = '" + connectionId + "'", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connection;
	}

	public boolean wasPreviouslyConnected(DeviceConnectionDbObj connection) {
		return connection.getLastSuccess() == DateTimeUtilities.getBaseTimestamp();
	}

	public DeviceConnectionDbObj getConnectionForPeripheral(DeviceDbObj device, boolean activeOnly) {
		return getConnectionForPeripheral(device.getDeviceId(), activeOnly);
	}

	public DeviceConnectionDbObj getConnectionForPeripheral(String peripheralId, boolean activeOnly) {
		DeviceConnectionDbObj connection = null;
		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE PERIPHERAL_DEVICE_ID = ? AND "
				+ "SERVICE_UUID IS NOT NULL";
		if (activeOnly) {
			query += " AND ACTIVE = ?";
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, peripheralId);
			if (activeOnly) {
				prepStmt.setBoolean(2, true);
			}
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				addLog(peripheralId, "record found", LogConstants.DEBUG);
				connection = recordToConnection(rs);
				break;
			}
		} catch (SQLException e) {
			addLog("getDeviceByDeviceId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		addLog(peripheralId, "connectionExists: " + (connection != null), LogConstants.DEBUG);
		return connection;
	}

	public ArrayList<DeviceConnectionDbObj> getConnectionsForCentralToken(String tokenId,
			TokenDescription description) {
		String query = "SELECT conn.* FROM B2F_DEVICE_CONNECTION conn JOIN B2F_TOKEN tok ON "
				+ "conn.CENTRAL_DEVICE_ID = tok.DEVICE_ID WHERE tok.TOKEN_ID = ? AND tok.DESCRIPTION = ? "
				+ " AND tok.EXPIRE_TIME > ? AND conn.ACTIVE = ? ";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		ArrayList<DeviceConnectionDbObj> connections = new ArrayList<>();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tokenId);
			prepStmt.setString(2, description.toString());
			prepStmt.setTimestamp(3, now);
			prepStmt.setBoolean(4, true);
			rs = executeQuery(prepStmt);
			DeviceConnectionDbObj connection;
			while (rs.next()) {
				addLog("record found");
				connection = recordToConnection(rs);
				connections.add(connection);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connections;
	}

	public DeviceConnectionDbObj getConnectionByPeripheralToken(String tokenId, TokenDescription description) {
		DeviceConnectionDbObj connection = null;
		String query = "SELECT conn.* FROM B2F_DEVICE_CONNECTION conn JOIN B2F_TOKEN tok ON "
				+ "conn.PERIPHERAL_DEVICE_ID = tok.DEVICE_ID WHERE tok.TOKEN_ID = ? AND tok.DESCRIPTION = ? "
				+ " AND tok.EXPIRE_TIME > ? AND conn.ACTIVE = ? ";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tokenId);
			prepStmt.setString(2, description.toString());
			prepStmt.setTimestamp(3, now);
			prepStmt.setBoolean(4, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("record found");
				connection = recordToConnection(rs);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connection;

	}

	public ArrayList<DeviceConnectionDbObj> getConnectionsForGroup(GroupDbObj group) {
		return getConnectionsForGroupId(group.getGroupId());
	}

	public ArrayList<DeviceConnectionDbObj> getConnectionsForGroupId(String groupId) {
		ArrayList<DeviceConnectionDbObj> connections = new ArrayList<>();
		DeviceConnectionDbObj connection;
		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE GROUP_ID = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, groupId);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connection = recordToConnection(rs);
				connections.add(connection);
			}
		} catch (SQLException e) {
			addLog("getConnectionsForGroup", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		this.addLog("connections: " + connections.size());
		return connections;
	}

	public ArrayList<DeviceConnectionDbObj> getConnectionsForCentral(DeviceDbObj device) {
		return getConnectionsForCentral(device.getDeviceId());
	}

	public ArrayList<DeviceConnectionDbObj> getConnectionsForCentral(String deviceId) {
		ArrayList<DeviceConnectionDbObj> connections = new ArrayList<>();
		DeviceConnectionDbObj connection;
		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE CENTRAL_DEVICE_ID = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			int i = 0;
			while (rs.next()) {
				connection = recordToConnection(rs);
				connections.add(connection);
				i++;
			}
			addLog(i + " connections found.");
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connections;
	}

	public void updateMostRecestIncompleteConnectionForCentral(DeviceDbObj device) {
		String query = "UPDATE B2F_DEVICE_CONNECTION SET INSTALL_COMPLETE = ? WHERE "
				+ "CENTRAL_DEVICE_ID = ? AND ACTIVE = ? "
				+ "AND INSTALL_COMPLETE = ? ORDER BY CREATE_DATE DESC LIMIT 1";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setString(2, device.getDeviceId());
			prepStmt.setBoolean(3, true);
			prepStmt.setBoolean(4, false);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();

		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	public DeviceConnectionDbObj getConnectionByServiceUuid(String serviceUuid) {
		DeviceConnectionDbObj connection = null;
		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE SERVICE_UUID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, serviceUuid.toUpperCase());
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connection = recordToConnection(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(serviceUuid, e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connection;
	}

	public DeviceConnectionDbObj getConnectionByCentralReference(String identifier) {
		DeviceConnectionDbObj connection = null;
		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE PERIPHERAL_IDENTIFIER = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, identifier.toUpperCase());
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connection = recordToConnection(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connection;
	}

	DeviceConnectionDbObj getConnectionForDevices(DeviceDbObj device1, DeviceDbObj device2) {
		DeviceConnectionDbObj connection = null;
		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE "
				+ " (CENTRAL_DEVICE_ID = ? AND PERIPHERAL_DEVICE_ID = ?) OR"
				+ " (CENTRAL_DEVICE_ID = ? AND PERIPHERAL_DEVICE_ID = ?) AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, device1.getDeviceId());
			prepStmt.setString(2, device2.getDeviceId());
			prepStmt.setString(3, device2.getDeviceId());
			prepStmt.setString(4, device1.getDeviceId());
			prepStmt.setBoolean(5, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connection = recordToConnection(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(device1.getDeviceId(), e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connection;
	}

	public boolean isRecentlySubscribed(DeviceConnectionDbObj connection) {
		boolean recent = false;
		Timestamp lastBtConnection = connection.getLastSubscribed();
		long seconds = DateTimeUtilities.timestampSecondAgo(lastBtConnection);
		this.addLog(connection.getCentralDeviceId(), "seconds since lastConn: " + seconds);
		if (seconds < 45 * 60) { // 45 minutes
			recent = true;
		}
		return recent;
	}

	public DeviceConnectionDbObj getConnectionFromCheck(CheckDbObj check) {
		DeviceConnectionDbObj connection = null;
		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE CENTRAL_DEVICE_ID = ? "
				+ "AND PERIPHERAL_DEVICE_ID = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, check.getCentralDeviceId());
			prepStmt.setString(2, check.getPeripheralDeviceId());
			prepStmt.setBoolean(3, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connection = recordToConnection(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(check.getPeripheralDeviceId(), e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connection;
	}

	public boolean isProximate(DeviceConnectionDbObj connection, boolean ignoreSleep) {
		boolean connected = false;
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
								connected = true;
							}
						}
						addLog(connection.getCentralDeviceId(), "success: " + connected);
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
			addLog(connection.getPeripheralDeviceId(), "one of the devices is turned off or asleep",
					LogConstants.INFO);
		}
		return connected;
	}

	/**
	 * 
	 * @param connection
	 * @return true if either of the devices are not found or turned off
	 */
	protected boolean areDevicesTurnedOffOrAsleep(DeviceConnectionDbObj connection, boolean ignoreSleep) {
		boolean turnedOffOrAsleep = false;
		DeviceDbObj peripheral = new DeviceDataAccess().getDeviceByDeviceId(connection.getPeripheralDeviceId());
		if (peripheral == null || peripheral.isTurnedOff() || (peripheral.isScreensaverOn() && !ignoreSleep)) {
			turnedOffOrAsleep = true;
		} else {
			DeviceDbObj central = new DeviceDataAccess().getDeviceByDeviceId(connection.getCentralDeviceId());
			if (central == null || central.isTurnedOff()) {
				turnedOffOrAsleep = true;
			}
		}
		if (turnedOffOrAsleep) {
			this.addLog(connection.getPeripheralDeviceId(), "turnedOffOrAsleep: " + turnedOffOrAsleep,
					LogConstants.INFO);
		}
		return turnedOffOrAsleep;
	}

	protected boolean updateProximateDeviceSuccesses(DeviceConnectionDbObj connection, Boolean fromCentral) {
		return updateAsProximatelyConnected(connection, fromCentral);
	}

	public boolean hasPreviouslyConnected(DeviceConnectionDbObj connection) {
		boolean previouslyConnected = false;
		if (connection.getLastCentralConnectionSuccess() != null) {
			previouslyConnected = true;
		}
		return previouslyConnected;
	}

	public boolean updateCentralProximateConnection(DeviceConnectionDbObj connection, boolean connected) {

		return updateCentralProximateConnection(connection, connected, null);
	}

	public boolean updateAsInstallComplete(DeviceConnectionDbObj connection) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("INSTALL_COMPLETE", true);
		return updateConnectionMap(connection, hm, "updateAsInstallComplete");
	}

	public boolean updateAsInstallCompleteAndActive(DeviceConnectionDbObj connection) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("ACTIVE", true);
		hm.put("INSTALL_COMPLETE", true);
		return updateConnectionMap(connection, hm, "updateAsInstallCompleteAndActive");
	}

	public boolean updateAsInactive(DeviceConnectionDbObj connection) {
		return updateActiveStatus(connection, false);
	}

	public boolean updateAsActive(DeviceConnectionDbObj connection) {
		return updateActiveStatus(connection, true);
	}

	public boolean updateActiveStatus(DeviceConnectionDbObj connection, boolean active) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("ACTIVE", active);
		return updateConnectionMap(connection, hm, "updateActiveStatus");
	}

	public boolean updateAsConnecting(DeviceConnectionDbObj connection) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("CONNECTING_CENTRAL", DateTimeUtilities.getCurrentTimestamp());
		return updateConnectionMap(connection, hm, "updateAsConnecting");
	}

	public boolean updateRssi(DeviceConnectionDbObj connection, int rssi, boolean isCentral) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		if (isCentral) {
			hm.put("CENTRAL_RSSI", rssi);
			hm.put("CENTRAL_RSSI_TIMESTAMP", now);
		} else {
			hm.put("PERIPHERAL_RSSI", rssi);
			hm.put("PERIPHERAL_RSSI_TIMESTAMP", now);
		}
		return updateConnectionMap(connection, hm, "updateRssi");
	}

	public boolean updateAsCentralConnected(DeviceConnectionDbObj connection) {
		return updateAsCentralProximatelyConnected(connection, null);
	}

	public boolean updateAsCentralProximatelyConnected(DeviceConnectionDbObj connection, String centralOsId) {
		boolean success = false;
		if (this.peripheralIsAwake(connection)) {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			HashMap<String, Object> hm = new HashMap<String, Object>();
			hm.put("CENTRAL_CONNECTED", true);
			hm.put("PERIPHERAL_CONNECTED", true);
			hm.put("LAST_CENTRAL_CONNECTION_SUCCESS", now);
			if (!TextUtils.isBlank(centralOsId)) {
				hm.put("PERIPHERAL_IDENTIFIER", centralOsId);
				this.addLog(connection.getPeripheralDeviceId(), "changing peripheral identifier to " + centralOsId,
						LogConstants.TRACE);
			}

			DeviceDbObj peripheral = new DeviceDataAccess().getDeviceByDeviceId(connection.getPeripheralDeviceId());
			if (peripheral != null && !peripheral.isScreensaverOn()) {
				addConnectionLogIfNeeded(connection, true, now, connection.getCentralDeviceId(),
						"updateAsCentralConnected", ConnectionType.PROX);
			}
			success = updateConnectionMap(connection, hm, "updateAsCentralConnected");
		}
		return success;
	}

	public void addConnectionLogIfNeeded(CheckDbObj check, boolean connected, String src, String description,
			ConnectionType connectionType) {
		this.addConnectionLogIfNeeded(check, connected, src, description, false, connectionType);
	}

	public void addConnectionLogIfNeeded(CheckDbObj check, boolean connected, String src, String description,
			boolean skipCheck, ConnectionType connectionType) {
		addConnectionLogIfNeeded(check, connected, DateTimeUtilities.getCurrentTimestamp(), src, description, skipCheck,
				connectionType);
	}

	public void addConnectionLogIfNeeded(DeviceConnectionDbObj connection, boolean connected, String src,
			String description, ConnectionType connectionType) {
		addConnectionLogIfNeeded(connection, connected, DateTimeUtilities.getCurrentTimestamp(), src, description,
				false, connectionType);
	}

	public void addConnectionLogIfNeeded(DeviceConnectionDbObj connection, boolean connected, String src,
			String description, boolean skipCheck, ConnectionType connectionType) {
		addConnectionLogIfNeeded(connection, connected, DateTimeUtilities.getCurrentTimestamp(), src, description,
				skipCheck, connectionType);
	}

	public void addConnectionLogIfNeeded(CheckDbObj check, boolean connected, Timestamp ts, String src,
			String description, ConnectionType connectionType) {
		this.addConnectionLogIfNeeded(check, connected, src, description, false, connectionType);
	}

	public void addConnectionLogIfNeeded(CheckDbObj check, boolean connected, Timestamp ts, String src,
			String description, boolean skipCheck, ConnectionType connectionType) {
		new Thread(() -> {

			DeviceConnectionDbObj connection = this.getConnectionByDeviceIds(check.getCentralDeviceId(),
					check.getPeripheralDeviceId());
			if (connection != null) {
				if (!connected || this.peripheralIsAwake(connection)) {
					DeviceDbObj device = new DeviceDataAccess().getDeviceByDeviceId(connection.getPeripheralDeviceId());
					addToConnectionLogIfNeededSync(connection, device, connected, ts, src, description, skipCheck,
							connectionType);
				}
			}
		}).start();
	}

	public void addConnectionLogIfNeeded(DeviceConnectionDbObj connection, boolean connected, Timestamp ts, String src,
			String description, ConnectionType connectionType) {
		this.addConnectionLogIfNeeded(connection, connected, src, description, false, connectionType);
	}

	public void addConnectionLogIfNeeded(DeviceConnectionDbObj connection, boolean connected, Timestamp ts, String src,
			String description, boolean skipCheck, ConnectionType connectionType) {
		DeviceDbObj device = new DeviceDataAccess().getDeviceByDeviceId(connection.getPeripheralDeviceId());
		if (device == null || !device.isScreensaverOn() || !connected) {
			new Thread(() -> {
				addToConnectionLogIfNeededSync(connection, device, connected, ts, src, description, skipCheck,
						connectionType);
			}).start();
		}
	}

	public void addConnectionLogIfNeeded(DeviceDbObj device, boolean connected, String src, String description,
			ConnectionType connectionType) {
		addConnectionLogIfNeeded(device, connected, DateTimeUtilities.getCurrentTimestamp(), src, description, false,
				connectionType);
	}

	public void addConnectionLogIfNeeded(DeviceDbObj device, boolean connected, String src, String description,
			boolean skipCheck, ConnectionType connectionType) {
		addConnectionLogIfNeeded(device, connected, DateTimeUtilities.getCurrentTimestamp(), src, description,
				skipCheck, connectionType);
	}

	public void addConnectionLogIfNeeded(DeviceDbObj device, boolean connected, Timestamp ts, String src,
			String description, ConnectionType connectionType) {
		this.addConnectionLogIfNeeded(device, connected, src, description, false, connectionType);
	}

	public void addConnectionLogIfNeeded(DeviceDbObj device, boolean connected, Timestamp ts, String src,
			String description, boolean skipCheck, ConnectionType connectionType) {
		if (device != null && (!device.isScreensaverOn() || !connected)) {
			new Thread(() -> {
				addToConnectionLogIfNeededSync(null, device, connected, ts, src, description, skipCheck,
						connectionType);
			}).start();
		}
	}

	public void addConnectionLogIfNeeded(DeviceConnectionDbObj connection, DeviceDbObj device, boolean connected,
			Timestamp ts, String src, String description, boolean skipCheck, ConnectionType connectionType) {
		if (device == null || !device.isScreensaverOn() || !connected) {
			new Thread(() -> {
				addToConnectionLogIfNeededSync(connection, device, connected, ts, src, description, skipCheck,
						connectionType);
			}).start();
		}
	}

	public void addToConnectionLogIfNeededSync(DeviceConnectionDbObj connection, DeviceDbObj device, boolean connected,
			Timestamp ts, String src, String description, boolean skipCheck, ConnectionType connectionType) {
		try {
			if (connection != null) {
				ConnectionLogDbObj connLog = getLastConnectionStatusFromLog(connection);
				if (connLog == null || connected != connLog.isConnected()) {
					if (!connected || !skipCheck) {
						this.addLog("adding a connLog entry with connection not null",
								LogConstants.TRACE);
						ConnectionLogDbObj connectionLog = new ConnectionLogDbObj(connection.getConnectionId(), null,
								connected, ts, src, description, connectionType);
						addConnectionLog(connectionLog);
					} else {
						this.addLog("will check isAccessAllow to see if we add a connLog entry"
								+ " with connection not null", LogConstants.TRACE);
						new DeviceDataAccess().isAccessAllowed(connection, device);
					}
				}
			} else {
				if (device != null) {
					ConnectionLogDbObj connLog = getLastConnectionStatusFromLog(device);
					if (connLog == null || connected != connLog.isConnected()) {
						if (!connected || !skipCheck) {
							if (!connected || device.isScreensaverOn()) {
								this.addLog(device.getDeviceId(), "adding a connLog entry",
										LogConstants.TRACE);
								ConnectionLogDbObj connectionLog = new ConnectionLogDbObj(null, device.getDeviceId(),
										connected, ts, src, description, connectionType);
								addConnectionLog(connectionLog);
							}
						} else {
							this.addLog(device.getDeviceId(),
									"will check isAccessAllow to see if we add a connLog entry",
									LogConstants.TRACE);
							new DeviceDataAccess().isAccessAllowed(device, "addToConnectionLogIfNeededSync");
						}
					} else {
						this.addLog(device.getDeviceId(), "status was the same; connected: " + connected,
								LogConstants.TRACE);
					}
				} else {
					this.addLog("device and connection were both null", LogConstants.ERROR);
				}
			}
		} catch (Exception e) {
			this.addLog(e);
		}
	}

	public ConnectionLogDbObj getLastConnectionStatusFromLog(DeviceDbObj device) {
		ConnectionLogDbObj connLog = null;
		if (device.isCentral()) {
			String query = "SELECT * FROM B2F_CONNECTION_LOG WHERE DEVICE_ID = ? ORDER BY "
					+ "EVENT_TIMESTAMP DESC LIMIT 1";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, device.getDeviceId());
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					connLog = this.rsToConnectionLog(rs);
				}
			} catch (SQLException e) {
				addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		} else {
			DeviceConnectionDbObj connection = this.getConnectionByPeripheralId(device.getDeviceId());
			connLog = getLastConnectionStatusFromLog(connection, device);
		}
		return connLog;
	}

	public ConnectionLogDbObj getLastConnectionStatusFromLog(DeviceConnectionDbObj connection, DeviceDbObj device) {
		String query = "SELECT * FROM B2F_CONNECTION_LOG WHERE CONNECTION_ID = ? UNION ALL "
				+ "SELECT * FROM B2F_CONNECTION_LOG WHERE DEVICE_ID = ? ORDER BY EVENT_TIMESTAMP DESC LIMIT 1";
		ConnectionLogDbObj connLog = null;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connection.getConnectionId());
			prepStmt.setString(2, device.getDeviceId());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				connLog = this.rsToConnectionLog(rs);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connLog;
	}

	public ConnectionLogDbObj getLastConnectionStatusFromLog(DeviceConnectionDbObj connection) {
		String query = "SELECT * FROM B2F_CONNECTION_LOG WHERE CONNECTION_ID = ? ORDER BY "
				+ "EVENT_TIMESTAMP DESC LIMIT 1";
		ConnectionLogDbObj connLog = null;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connection.getConnectionId());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				connLog = this.rsToConnectionLog(rs);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connLog;
	}

	public ArrayList<ConnectionLogDbObj> getConnLogsAfterTimestampPlusOneBeforePeripheral(CompanyDbObj company,
			String connectionId, String deviceId, Timestamp ts) {
		ArrayList<ConnectionLogDbObj> importantEvents = new ArrayList<>();
		String query = "(SELECT * FROM B2F_CONNECTION_LOG WHERE (CONNECTION_ID = ? OR DEVICE_ID = ?) "
				+ "AND EVENT_TIMESTAMP < ? ORDER BY "
				+ "EVENT_TIMESTAMP DESC LIMIT 1) UNION ALL (SELECT * FROM B2F_CONNECTION_LOG WHERE "
				+ "(CONNECTION_ID = ? OR DEVICE_ID = ?) AND EVENT_TIMESTAMP > ?) ORDER BY EVENT_TIMESTAMP ";
		ArrayList<ConnectionLogDbObj> allEvents = new ArrayList<>();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		ConnectionLogDbObj event;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connectionId);
			prepStmt.setString(2, deviceId);
			prepStmt.setTimestamp(3, ts);
			prepStmt.setString(4, connectionId);
			prepStmt.setString(5, deviceId);
			prepStmt.setTimestamp(6, ts);
			rs = executeImportantQuery(prepStmt);
			while (rs.next()) {
				event = this.rsToConnectionLog(rs);
				allEvents.add(event);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		addLog("connEvents before removing unneded: " + allEvents.size(), LogConstants.TRACE);
		if (allEvents.size() > 1) {
			importantEvents = removeUnneededLogs(company, allEvents, false, false);
		} else if (allEvents.size() == 1) {
			importantEvents = allEvents;
		}
		addLog("connEvents after removing unnedded: " + importantEvents.size(), LogConstants.TRACE);
		return importantEvents;
	}

	public ArrayList<ConnectionLogDbObj> getConnLogsAfterTimestampPlusOneBeforeCentral(CompanyDbObj company,
			String deviceId, Timestamp ts) {
		ArrayList<ConnectionLogDbObj> importantEvents = new ArrayList<>();
		String query = "(SELECT * FROM B2F_CONNECTION_LOG WHERE DEVICE_ID = ?) AND EVENT_TIMESTAMP < ? ORDER BY "
				+ "EVENT_TIMESTAMP DESC LIMIT 1) UNION ALL (SELECT * FROM B2F_CONNECTION_LOG WHERE "
				+ "DEVICE_ID = ?) AND EVENT_TIMESTAMP > ?) ORDER BY EVENT_TIMESTAMP ";
		ArrayList<ConnectionLogDbObj> allEvents = new ArrayList<>();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		ConnectionLogDbObj event;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			int j = 1;
			prepStmt.setString(j++, deviceId);
			prepStmt.setTimestamp(j++, ts);
			prepStmt.setString(j++, deviceId);
			prepStmt.setTimestamp(j++, ts);
			logQueryImportant("getConnLogsAfterTimestampPlusOneBeforeCentral", prepStmt);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				event = this.rsToConnectionLog(rs);
				allEvents.add(event);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		if (allEvents.size() > 1) {
			importantEvents = removeUnneededLogs(company, allEvents, true, false);
		} else if (allEvents.size() == 1) {
			importantEvents = allEvents;
		}
		return importantEvents;
	}

//	public ArrayList<ConnectionLogDbObj> getConnLogsAfterTimestampPlusOneBeforeAndAfterPeripheral(CompanyDbObj company,
//			String connectionId, String deviceId, Timestamp startTime, Timestamp endTime) {
//		ArrayList<ConnectionLogDbObj> importantEvents = new ArrayList<>();
//		String query = "(SELECT * FROM B2F_CONNECTION_LOG WHERE (CONNECTION_ID = ? OR DEVICE_ID = ?) AND EVENT_TIMESTAMP < ? ORDER BY "
//				+ "EVENT_TIMESTAMP DESC LIMIT 1) UNION ALL (SELECT * FROM B2F_CONNECTION_LOG WHERE (CONNECTION_ID = ? OR DEVICE_ID = ?) "
//				+ "AND EVENT_TIMESTAMP > ? AND EVENT_TIMESTAMP < ?) UNION ALL (SELECT * FROM B2F_CONNECTION_LOG WHERE (CONNECTION_ID = ? "
//				+ "OR DEVICE_ID = ?) AND EVENT_TIMESTAMP > ? ORDER BY EVENT_TIMESTAMP DESC LIMIT 1) "
//				+ "ORDER BY EVENT_TIMESTAMP ";
//		ArrayList<ConnectionLogDbObj> allEvents = new ArrayList<>();
//		Connection conn = null;
//		PreparedStatement prepStmt = null;
//		ResultSet rs = null;
//		ConnectionLogDbObj event;
//		try {
//			conn = MySqlConn.getConnection();
//			prepStmt = conn.prepareStatement(query);
//			prepStmt.setString(1, connectionId);
//			prepStmt.setString(2, deviceId);
//			prepStmt.setTimestamp(3, startTime);
//			prepStmt.setString(4, connectionId);
//			prepStmt.setString(5, deviceId);
//			prepStmt.setTimestamp(6, startTime);
//			prepStmt.setTimestamp(7, endTime);
//			prepStmt.setString(8, connectionId);
//			prepStmt.setString(9, deviceId);
//			prepStmt.setTimestamp(10, endTime);
//			rs = executeQuery(prepStmt);
//			if (rs.next()) {
//				event = this.rsToConnectionLog(rs);
//				allEvents.add(event);
//			}
//		} catch (SQLException e) {
//			addLog(e);
//		} finally {
//			MySqlConn.close(rs, prepStmt, conn);
//		}
//		if (allEvents.size() > 1) {
//			importantEvents = removeUnneededLogs(company, allEvents, false, false);
//		} else if (allEvents.size() == 1) {
//			importantEvents = allEvents;
//		}
//		return importantEvents;
//	}
//
//	public ArrayList<ConnectionLogDbObj> getConnLogsAfterTimestampPlusOneBeforeAndAfterCentral(CompanyDbObj company,
//			ArrayList<String> connId, String deviceId, Timestamp startTime, Timestamp endTime) {
//		StringBuilder placeholders = new StringBuilder();
//		ArrayList<ConnectionLogDbObj> importantEvents = new ArrayList<>();
//		if (connId.size() > 0) {
//			for (int i = 0; i < connId.size(); i++) {
//				placeholders.append("?");
//				if (i < connId.size() - 1) {
//					placeholders.append(", ");
//				}
//			}
//			String query = "(SELECT * FROM B2F_CONNECTION_LOG WHERE (CONNECTION_ID IN (" + placeholders
//					+ ") OR DEVICE_ID = ?) AND EVENT_TIMESTAMP < ? ORDER BY "
//					+ "EVENT_TIMESTAMP DESC LIMIT 1) UNION ALL (SELECT * FROM B2F_CONNECTION_LOG WHERE (CONNECTION_ID IN ("
//					+ placeholders + ") OR DEVICE_ID = ?) "
//					+ "AND EVENT_TIMESTAMP > ?)";
//			ArrayList<ConnectionLogDbObj> allEvents = new ArrayList<>();
//			Connection conn = null;
//			PreparedStatement prepStmt = null;
//			ResultSet rs = null;
//			ConnectionLogDbObj event;
//			try {
//				conn = MySqlConn.getConnection();
//				prepStmt = conn.prepareStatement(query);
//				int j = 1;
//				for (int i = 0; i < connId.size(); i++) {
//					prepStmt.setString(j++, connId.get(i));
//				}
//				prepStmt.setString(j++, deviceId);
//				prepStmt.setTimestamp(j++, startTime);
//				for (int i = 0; i < connId.size(); i++) {
//					prepStmt.setString(j++, connId.get(i));
//				}
//				prepStmt.setString(j++, deviceId);
//				prepStmt.setTimestamp(j++, startTime);
//				rs = executeQuery(prepStmt);
//				while (rs.next()) {
//					event = this.rsToConnectionLog(rs);
//					allEvents.add(event);
//				}
//				rs = executeQuery(prepStmt);
//				if (rs.next()) {
//					event = this.rsToConnectionLog(rs);
//					allEvents.add(event);
//				}
//			} catch (SQLException e) {
//				addLog(e);
//			} finally {
//				MySqlConn.close(rs, prepStmt, conn);
//			}
//			if (allEvents.size() > 1) {
//				importantEvents = removeUnneededLogs(company, allEvents, true, false);
//			} else if (allEvents.size() == 1) {
//				importantEvents = allEvents;
//			}
//		}
//		return importantEvents;
//	}

	public ArrayList<ConnectionLogDbObj> removeUnneededLogs(CompanyDbObj company,
			ArrayList<ConnectionLogDbObj> connLogs, boolean periodIncludesFirstLog, boolean dontSetIgnore) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int logLevel = LogConstants.TRACE;
		ArrayList<ConnectionLogDbObj> neededLogs = new ArrayList<>();
		ConnectionLogDbObj baseLog;
		ConnectionLogDbObj currentLog;
		ConnectionLogDbObj nextLog;
		long seconds = -1;
		int startIndex = getStartingTime(connLogs, periodIncludesFirstLog);
		neededLogs.add(connLogs.get(startIndex));
		boolean connectionChanged;
		ConnectionLogDbObj finalConnLog = null;
		if (startIndex != -1) {
			baseLog = connLogs.get(startIndex);
			int i = startIndex;
			dataAccess.addLog("starting at: " + i, logLevel);
			while (i < connLogs.size()) {
				int j = 1;
				currentLog = connLogs.get(i);
				if (logStatusHasChanged(baseLog, currentLog, i)) {
					// we may have to add a record if the previousRecord was passkey, txt, or
					// admincode and it timed out
					ConnectionType connType = baseLog.getConnectionType();
					if (connType == ConnectionType.ADMIN_CODE || connType == ConnectionType.PASSKEY
							|| connType == ConnectionType.PUSH || connType == ConnectionType.TXT) {
						dataAccess.addLog("new connType: " + connType.connectionTypeName(), logLevel);
						if (didPreviousEventTimeout(company, baseLog, currentLog)) {
							neededLogs = addDisconnectRecord(company, neededLogs, baseLog);
						}
					}
					// now see if it lasts long enough
					seconds = Constants.minLogRecordLength + 1;
					dataAccess.addLog("comparing: " + currentLog.getEventTimestamp() + " - " + currentLog.isConnected()
							+ " to " + j + " to " + (connLogs.size() - 1), logLevel);
					if (i != connLogs.size() - 1) {
						connectionChanged = false;
						while (!connectionChanged && ((i + j) != (connLogs.size()))) {
							nextLog = connLogs.get(i + j);
							if (nextLog.isConnected() != currentLog.isConnected()) {
								connectionChanged = true;
								seconds = getRecordLength(currentLog, nextLog);
							} else {
								dataAccess.addLog(nextLog.isConnected() + " vs. " + currentLog.isConnected(), logLevel);
							}
							j++;
						}
					} else {
						// this is the last record
						if (currentLog.isConnected()) {
							if (currentLog.getConnectionId() != null) {
								DeviceConnectionDbObj lastConn = dataAccess
										.getConnectionByConnectionId(currentLog.getConnectionId());
								if (!dataAccess.isAccessAllowed(lastConn, false)) {
									finalConnLog = dataAccess.getLastConnectionStatusFromLog(lastConn);
								}
							} else {
								DeviceDbObj device = dataAccess.getDeviceByDeviceId(currentLog.getDeviceId());
								if (!dataAccess.isAccessAllowed(device, "")) {
									finalConnLog = dataAccess.getLastConnectionStatusFromLog(device);
								}
							}
						}
					}
					if (seconds > Constants.minLogRecordLength || i == connLogs.size() - 1) {
						neededLogs.add(currentLog);
						baseLog = currentLog;
						dataAccess.addLog("using connLog @ " + currentLog.getEventTimestamp() + ", connected: "
								+ currentLog.isConnected(), logLevel);
					} else {
						if (!dontSetIgnore) {
							dataAccess.updateIgnoreConnectionRecord(currentLog);
							dataAccess.addLog("not using connLog @ " + currentLog.getEventTimestamp()
									+ " because it is too short: " + seconds + " seconds.", logLevel);
						}
					}
				} else {
					if (!dontSetIgnore) {
						dataAccess.updateIgnoreConnectionRecord(currentLog);
						dataAccess.addLog("not using connLog @ " + currentLog.getEventTimestamp()
								+ " because the connection status for both is " + currentLog.isConnected()
								+ " when compared with " + baseLog.getEventTimestamp(), logLevel);
					}
				}
				i++;
			}
		}
		if (finalConnLog != null) {
			neededLogs.add(finalConnLog);
		}
		dataAccess.addLog("log # before removal: " + connLogs.size() + "; after: " + neededLogs.size(), logLevel);
		printLogs("neededLogs", neededLogs);
		return neededLogs;
	}

	private void printLogs(String name, ArrayList<ConnectionLogDbObj> connLogs) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int logLevel = LogConstants.TRACE;
		for (ConnectionLogDbObj connLog : connLogs) {
			dataAccess.addLog(name + ": " + connLog.toString(), logLevel);
		}
	}

	private boolean logStatusHasChanged(ConnectionLogDbObj baseLog, ConnectionLogDbObj currentLog, int iteration) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int logLevel = LogConstants.TRACE;
		dataAccess.addLog("comparing: " + currentLog.getEventTimestamp() + " - " + currentLog.isConnected() + " via "
				+ currentLog.getConnectionType() + " to " + baseLog.getEventTimestamp() + " - " + baseLog.isConnected()
				+ " via " + baseLog.getConnectionType(), logLevel);
		return iteration == 0 || (currentLog.isConnected() != baseLog.isConnected())
				|| ((currentLog.getConnectionType() != baseLog.getConnectionType() && currentLog.isConnected()));
	}

	private ArrayList<ConnectionLogDbObj> addDisconnectRecord(CompanyDbObj company,
			ArrayList<ConnectionLogDbObj> neededLogs, ConnectionLogDbObj previousLog) {

		Timestamp ts = getTimeoutTimestamp(company, previousLog);
		ConnectionLogDbObj newLog = new ConnectionLogDbObj(previousLog.getConnectionId(), previousLog.getDeviceId(),
				false, ts, previousLog.getSrc(), previousLog.getDescription(), null);
		neededLogs.add(newLog);
		return neededLogs;
	}

	private Timestamp getTimeoutTimestamp(CompanyDbObj company, ConnectionLogDbObj previousLog) {
		ConnectionType connType = previousLog.getConnectionType();
		long timeoutSeconds = 0;
		switch (connType) {
		case ADMIN_CODE:
		case PASSKEY:
			timeoutSeconds = company.getPasskeyTimeoutSeconds();
			break;
		case PUSH:
			timeoutSeconds = company.getPushTimeoutSeconds();
			break;
		case TXT:
			timeoutSeconds = company.getTextTimeoutSeconds();
			break;
		default:
			break;
		}
		return DateTimeUtilities.addSeconds(previousLog.getEventTimestamp(), timeoutSeconds);
	}

	private boolean didPreviousEventTimeout(CompanyDbObj company, ConnectionLogDbObj previousLog,
			ConnectionLogDbObj currentLog) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int logLevel = LogConstants.TRACE;
		boolean timedOut = false;
		long timeDifferenceInSeconds = DateTimeUtilities.timestampDifferenceInSeconds(previousLog.getEventTimestamp(),
				currentLog.getEventTimestamp());
		if (previousLog.getConnectionType() == ConnectionType.ADMIN_CODE) {
			timedOut = timeDifferenceInSeconds > company.getTextTimeoutSeconds();
		} else if (previousLog.getConnectionType() == ConnectionType.PASSKEY) {
			timedOut = timeDifferenceInSeconds > company.getPasskeyTimeoutSeconds();
			dataAccess.addLog(
					"is " + timeDifferenceInSeconds + " > " + company.getPasskeyTimeoutSeconds() + "? " + timedOut,
					logLevel);
		} else if (previousLog.getConnectionType() == ConnectionType.PUSH) {
			timedOut = timeDifferenceInSeconds > company.getPushTimeoutSeconds();
		} else if (previousLog.getConnectionType() == ConnectionType.TXT) {
			timedOut = timeDifferenceInSeconds > company.getTextTimeoutSeconds();
		}
		dataAccess.addLog("connType: " + previousLog.getConnectionType().connectionTypeName() + " timed out: "
				+ timedOut + " because the difference was " + timeDifferenceInSeconds + " seconds between "
				+ previousLog.getEventTimestamp() + " and " + currentLog.getEventTimestamp(), logLevel);
		return timedOut;
	}

	private long getRecordLength(ConnectionLogDbObj firstRecord, ConnectionLogDbObj secondRecord) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int logLevel = LogConstants.TRACE;
		long seconds = DateTimeUtilities.absoluteTimestampDifferenceInSeconds(firstRecord.getEventTimestamp(),
				secondRecord.getEventTimestamp());
		dataAccess.addLog("difference between " + firstRecord.getEventTimestamp() + " and "
				+ secondRecord.getEventTimestamp() + " = " + seconds + " seconds", logLevel);
		return seconds;
	}

	private int getStartingTime(ArrayList<ConnectionLogDbObj> connLogs, boolean periodIncludesFirstLog) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int logLevel = LogConstants.TRACE;
		int startIndex = -1;
		ConnectionLogDbObj nextLog;
		ConnectionLogDbObj currentLog;
		for (int i = 0; i < connLogs.size(); i++) {
			currentLog = connLogs.get(i);
			if (connLogs.size() > i) {
				nextLog = connLogs.get(i + 1);
				if (currentLog.isConnected() != nextLog.isConnected() || periodIncludesFirstLog) {
					if (DateTimeUtilities.absoluteTimestampDifferenceInSeconds(currentLog.getEventTimestamp(),
							nextLog.getEventTimestamp()) > 30 || periodIncludesFirstLog) {
						startIndex = i;
						dataAccess.addLog("starting time: " + currentLog.getEventTimestamp() + " at index: " + i,
								logLevel);
						break;
					}
				}
			}
		}
		return startIndex;
	}

	public ConnectionLogDbObj[] getConnectionLogPriorToForCentral(CompanyDbObj company, Timestamp ts,
			ArrayList<String> connIdList, String deviceId) {
		ConnectionLogDbObj startConnEvent = null;
		ConnectionLogDbObj endConnEvent = null;
		String query = "SELECT * FROM B2F_CONNECTION_LOG WHERE DEVICE_ID = ? AND EVENT_TIMESTAMP < ? "
				+ "AND CONNECTION_TYPE != ? ORDER BY " + "EVENT_TIMESTAMP DESC LIMIT 1";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setTimestamp(2, ts);
			prepStmt.setString(3, ConnectionType.PROX.toString());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				startConnEvent = this.rsToConnectionLog(rs);
				if (startConnEvent.isConnected()) {
					// TODO: this sucks, but I'm pretty fucking lazy right now -cjm
					for (String connId : connIdList) {
						endConnEvent = getDisconnectEventForNonProx(company, startConnEvent, connId, deviceId);
						if (endConnEvent.getEventTimestamp().before(ts)) {
							startConnEvent = null;
							endConnEvent = null;
							break;
						}
					}
				}
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}

		ConnectionLogDbObj[] evts = { startConnEvent, endConnEvent };
		return evts;
	}

	private ConnectionLogDbObj getDisconnectEventForNonProx(CompanyDbObj company, ConnectionLogDbObj connEvent,
			String connectionId, String deviceId) {
		ConnectionLogDbObj disconnectEvent = null;
		Timestamp disconnectTime = null;
		Timestamp start = connEvent.getEventTimestamp();
		ConnectionLogDbObj connLog;
		switch (connEvent.getConnectionType()) {
		case PUSH:
			disconnectTime = DateTimeUtilities.getCurrentTimestampMinusSeconds(company.getPushTimeoutSeconds());
			break;
		case TXT:
			disconnectTime = DateTimeUtilities.getCurrentTimestampMinusSeconds(company.getTextTimeoutSeconds());
			break;
		case PASSKEY:
			disconnectTime = DateTimeUtilities.getCurrentTimestampMinusSeconds(company.getPasskeyTimeoutSeconds());
			break;
		case ADMIN_CODE:
			disconnectTime = DateTimeUtilities.getCurrentTimestampMinusSeconds(company.getAdminCodeTimeoutSeconds());
			break;
		default:
			break;
		}
		if (disconnectTime != null) {
			disconnectEvent = new ConnectionLogDbObj(connectionId, deviceId, false, disconnectTime, "", "", null);
		}
		String query = "SELECT * FROM B2F_CONNECTION_LOG WHERE EVENT_TIMESTAMP > ? AND EVENT_TIMESTAMP < ? AND (";
		if (connectionId != null) {
			query += "CONNECTION_ID = ? ";
		}
		if (deviceId != null) {
			if (connectionId != null) {
				query += "OR ";
			}
			query += "DEVICE_ID = ? ";
		}
		query += ") ORDER BY EVENT_TIMESTAMP";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		boolean reconnected = false;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			int i = 1;
			prepStmt.setTimestamp(i++, start);
			prepStmt.setTimestamp(i++, disconnectTime);
			if (connectionId != null) {
				prepStmt.setString(i++, connectionId);
			}
			if (deviceId != null) {
				prepStmt.setString(i++, deviceId);
			}
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				connLog = this.rsToConnectionLog(rs);
				if (connLog.isConnected()) {
					reconnected = true;
				} else {
					if (reconnected) {
						disconnectEvent = connLog;
						break;
					}
				}
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return disconnectEvent;
	}

//	private boolean didWeDisconnectAfter(CompanyDbObj company, ConnectionLogDbObj connEvent, Timestamp end,
//			String connectionId) {
//		boolean disconnectedAfter = false;
//		Timestamp start = connEvent.getEventTimestamp();
//		switch (connEvent.getConnectionType()) {
//		case PUSH:
//			disconnectedAfter = start
//					.before(DateTimeUtilities.getCurrentTimestampMinusSeconds(company.getPushTimeoutSeconds()));
//			break;
//		case TXT:
//			disconnectedAfter = start
//					.before(DateTimeUtilities.getCurrentTimestampMinusSeconds(company.getTextTimeoutSeconds()));
//			break;
//		case PASSKEY:
//			disconnectedAfter = start
//					.before(DateTimeUtilities.getCurrentTimestampMinusSeconds(company.getPasskeyTimeoutSeconds()));
//			break;
//		case ADMIN_CODE:
//			disconnectedAfter = start
//					.before(DateTimeUtilities.getCurrentTimestampMinusSeconds(company.getAdminCodeTimeoutSeconds()));
//			break;
//		default:
//			break;
//		}
//		if (!disconnectedAfter) {
//			ConnectionLogDbObj connLog;
//			String query = "SELECT * FROM B2F_CONNECTION_LOG WHERE CONNECTION_ID = ? AND EVENT_TIMESTAMP > ? AND EVENT_TIMESTAMP < ? ORDER BY "
//					+ "EVENT_TIMESTAMP";
//			Connection conn = null;
//			PreparedStatement prepStmt = null;
//			ResultSet rs = null;
//			boolean reconnected = false;
//			try {
//				conn = MySqlConn.getConnection();
//				prepStmt = conn.prepareStatement(query);
//				prepStmt.setString(1, connectionId);
//				prepStmt.setTimestamp(2, start);
//				prepStmt.setTimestamp(3, end);
//				rs = executeQuery(prepStmt);
//				while (rs.next()) {
//					connLog = this.rsToConnectionLog(rs);
//					if (connLog.isConnected()) {
//						reconnected = true;
//					} else {
//						if (reconnected) {
//							disconnectedAfter = true;
//							break;
//						}
//					}
//				}
//			} catch (SQLException e) {
//				addLog(e);
//			} finally {
//				MySqlConn.close(rs, prepStmt, conn);
//			}
//		}
//		return disconnectedAfter;
//	}

	public ConnectionLogDbObj getPreviousConnectionLog(ConnectionLogDbObj connLog) {
		String query = "SELECT * FROM B2F_CONNECTION_LOG WHERE CONNECTION_ID = ? AND EVENT_TIMESTAMP < ? ORDER BY "
				+ "EVENT_TIMESTAMP DESC LIMIT 1";
		ConnectionLogDbObj newConnLog = null;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connLog.getConnectionId());
			prepStmt.setTimestamp(2, connLog.getEventTimestamp());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				newConnLog = this.rsToConnectionLog(rs);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return newConnLog;
	}

	public Timestamp getInitialConnectionLogsTimestamp(DeviceConnectionDbObj connection, int count) {
		Timestamp initialTs = null;
		ConnectionLogDbObj currConn = null;
		String query = "SELECT * FROM B2F_CONNECTION_LOG WHERE CONNECTION_ID = ? ORDER BY " + "EVENT_TIMESTAMP LIMIT ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connection.getConnectionId());
			prepStmt.setInt(2, 1);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				currConn = this.rsToConnectionLog(rs);
				initialTs = currConn.getEventTimestamp();
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return initialTs;
	}

	public Timestamp getInitialConnectionLogsTimestamp(ArrayList<DeviceConnectionDbObj> connections, int count) {
		Timestamp initialTs = null;
		ConnectionLogDbObj currConn = null;
		String qMarks = "";
		for (int i = 0; i < connections.size(); i++) {
			qMarks += ("?");
			if (i < connections.size() - 1) {
				qMarks += (", ");
			}
		}
		String query = "SELECT * FROM B2F_CONNECTION_LOG WHERE CONNECTION_ID IN (" + qMarks + ") ORDER BY "
				+ "EVENT_TIMESTAMP LIMIT ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			int j;
			for (j = 1; j <= connections.size(); j++) {
				prepStmt.setString(j, connections.get(j - 1).getConnectionId());
			}
			prepStmt.setInt(j++, 1);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				currConn = this.rsToConnectionLog(rs);
				initialTs = currConn.getEventTimestamp();
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return initialTs;
	}

	/*
	 */
//	public ArrayList<ConnectionLogDbObj> getLastDaysConnectionLogs(DeviceConnectionDbObj connection,
//			DeviceDbObj device, int days) {
//		ArrayList<ConnectionLogDbObj> connLogs = new ArrayList<>();
//		ArrayList<ConnectionLogDbObj> newConnLogs;
//		if (connection != null) {
//			connLogs = getLastDaysConnectionLogs(connection, device, days);
//		}
//		return connLogs;
//	}

	/**
	 * This is faster than using the query with CONNECTION_ID IN (...
	 * 
	 * @param connection
	 * @param device
	 * @param days
	 * @return
	 */
	public ArrayList<ConnectionLogDbObj> getLastDaysConnectionLogs(DeviceConnectionDbObj connection, DeviceDbObj device,
			int days, String centralId) {
		ArrayList<ConnectionLogDbObj> connLogs = new ArrayList<>();
		if (device != null) {
			ConnectionLogDbObj currConn = null;
			String query = "SELECT * FROM B2F_CONNECTION_LOG WHERE (CONNECTION_ID = ? OR DEVICE_ID = ? ";
			query += ") AND IGNORE_EVENT = ? AND "
					+ "EVENT_TIMESTAMP > NOW() - INTERVAL ? DAY ORDER BY EVENT_TIMESTAMP";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, connection.getConnectionId());
				if (centralId == null) {
					prepStmt.setString(2, device.getDeviceId());
				} else {
					prepStmt.setString(2, centralId);
				}
				prepStmt.setBoolean(3, false);
				prepStmt.setInt(4, days);
				logQuery(getMethodName(), prepStmt);
				rs = executeQuery(prepStmt);
				while (rs.next()) {
					currConn = this.rsToConnectionLog(rs);
					connLogs.add(currConn);
				}
			} catch (SQLException e) {
				addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		} else {
			this.addLog("device or connection was null", LogConstants.ERROR);
		}
		return connLogs;
	}

	public ArrayList<ConnectionLogDbObj> getAllConnectionLogs() {
		ArrayList<ConnectionLogDbObj> connLogs = new ArrayList<>();
		ConnectionLogDbObj currConn = null;
		String query = "SELECT * FROM B2F_CONNECTION_LOG";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				currConn = this.rsToConnectionLog(rs);
				connLogs.add(currConn);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return connLogs;
	}

	public void addConnectionLog(ConnectionLogDbObj connectionLog) {
		String query = "INSERT INTO B2F_CONNECTION_LOG (CONNECTION_LOG_ID, CONNECTION_ID, "
				+ "DEVICE_ID, EVENT_TIMESTAMP, CONNECTED, SRC, "
				+ "EVENT_DESCRIPTION, CONNECTION_TYPE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connectionLog.getConnectionLogId());
			prepStmt.setString(2, connectionLog.getConnectionId());
			prepStmt.setString(3, connectionLog.getDeviceId());
			prepStmt.setTimestamp(4, connectionLog.getEventTimestamp());
			prepStmt.setBoolean(5, connectionLog.isConnected());
			prepStmt.setString(6, connectionLog.getSrc());
			prepStmt.setString(7, connectionLog.getDescription());
			if (connectionLog.getConnectionType() != null) {
				prepStmt.setString(8, connectionLog.getConnectionType().name());
			} else {
				prepStmt.setString(8, null);
			}
			this.logQuery("addConnectionLog", prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			addLog(connectionLog.getSrc(), e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	public ConnectionLogDbObj rsToConnectionLog(ResultSet rs) {
		ConnectionLogDbObj connectionLog = null;
		try {
			String connectionLogId = rs.getString("CONNECTION_LOG_ID");
			String connectionId = rs.getString("CONNECTION_ID");
			String deviceId = rs.getString("DEVICE_ID");
			Timestamp eventTimestamp = rs.getTimestamp("EVENT_TIMESTAMP");
			boolean connected = rs.getBoolean("CONNECTED");
			String src = rs.getString("SRC");
			String desc = rs.getString("EVENT_DESCRIPTION");
			String connType = rs.getString("CONNECTION_TYPE");
			ConnectionType connectionType = ConnectionType.fromString(connType);
			boolean ignoreEvent = rs.getBoolean("IGNORE_EVENT");
			connectionLog = new ConnectionLogDbObj(connectionLogId, connectionId, deviceId, connected, eventTimestamp,
					src, desc, connectionType, ignoreEvent);
		} catch (SQLException e) {
			addLog(e);
		}
		return connectionLog;
	}

	public void updateIgnoreConnectionRecord(ConnectionLogDbObj connLog) {
		this.addLog("ignore " + connLog.toString());
		new Thread(() -> {
			String query = "UPDATE B2F_CONNECTION_LOG SET IGNORE_EVENT = ? WHERE CONNECTION_LOG_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, true);
				prepStmt.setString(2, connLog.getConnectionLogId());
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public boolean updateCentralProximateConnection(DeviceConnectionDbObj connection, boolean connected,
			String centralOsId) {
		boolean success = false;
		if (connected) {
			success = updateAsCentralProximatelyConnected(connection, centralOsId);
		} else {
			new DataAccess().addLog("connected: " + connected, LogConstants.TRACE);
			success = updateAsDisconnected(connection, true);
		}
		return success;
	}

	public boolean updateAsDisconnected(DeviceConnectionDbObj connection, Boolean fromCentral) {
		boolean success = false;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		if (connection.isSubscribed() || connection.getCentralConnected() || connection.getPeripheralConnected()) {

			HashMap<String, Object> hm = new HashMap<String, Object>();
			if (connection.isSubscribed()) {
				hm.put("LAST_SUBSCRIBED", now);
				hm.put("SUBSCRIBED", false);
			}
			if (connection.getCentralConnected()) {
				hm.put("LAST_CENTRAL_CONNECTION_SUCCESS", now);
				hm.put("CENTRAL_CONNECTED", false);
			}
			if (connection.getPeripheralConnected()) {
				hm.put("LAST_PERIPHERAL_CONNECTION_SUCCESS", now);
				hm.put("PERIPHERAL_CONNECTED", false);
			}
			success = updateConnectionMap(connection, hm, "updateAsDisconnected");
			this.addLog("connectionChanged to DISconnected for " + connection.getPeripheralDeviceId(),
					LogConstants.IMPORTANT);
		}
		String from = "";
		if (fromCentral == true) {
			from = " from central";
		} else if (fromCentral == false) {
			from = " from peripheral";
		}
		addConnectionLogIfNeeded(connection, false, now, connection.getPeripheralDeviceId(),
				"from updateAsDisconnected" + from, null);
		return success;
	}

	public boolean updateAsCentralSubscribed(DeviceConnectionDbObj connection) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("CENTRAL_CONNECTED", true);
		hm.put("PERIPHERAL_CONNECTED", true);
		hm.put("LAST_CENTRAL_CONNECTION_SUCCESS", now);
		hm.put("LAST_SUBSCRIBED", now);
		this.addLog("subscribed = true for " + connection.getPeripheralDeviceId());
		addConnectionLogIfNeeded(connection, true, now, connection.getCentralDeviceId(),
				"from updateAsCentralSubscribed", ConnectionType.PROX);
		return updateConnectionMap(connection, hm, "updateAsCentralSubscribed");
	}

	public boolean updateAsPeripheralSubscribed(DeviceConnectionDbObj connection, DeviceDbObj device) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("CENTRAL_CONNECTED", true);
		hm.put("PERIPHERAL_CONNECTED", true);
		hm.put("LAST_PERIPHERAL_CONNECTION_SUCCESS", now);
		hm.put("LAST_SUBSCRIBED", now);
		this.addLog("subscribed = true for " + connection.getPeripheralDeviceId());
		addConnectionLogIfNeeded(connection, device, true, now, connection.getPeripheralDeviceId(),
				"from updateAsPeripheralSubscribed", false, ConnectionType.PROX);
		return updateConnectionMap(connection, hm, "updateAsPeripheralSubscribed");
	}

	public boolean updateCentralSubscribed(DeviceConnectionDbObj connection, DeviceDbObj device, boolean connected) {
		boolean success = false;
		if (connected) {
			success = updateAsCentralSubscribed(connection);
		} else {
			success = updateAsUnsubscribed(connection, device, true);
		}
		return success;
	}

	public boolean updateAsUnsubscribed(DeviceConnectionDbObj connection, DeviceDbObj device, Boolean fromCentral) {
		boolean success = false;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		if (connection.isSubscribed() || connection.getCentralConnected() || connection.getPeripheralConnected()) {
			HashMap<String, Object> hm = new HashMap<String, Object>();
			if (connection.isSubscribed()) {
				hm.put("LAST_SUBSCRIBED", now);
				hm.put("SUBSCRIBED", false);
			}
			if (connection.getCentralConnected()) {
				hm.put("LAST_CENTRAL_CONNECTION_SUCCESS", now);
				hm.put("CENTRAL_CONNECTED", false);
			}
			this.addLog("subscribed = false for " + connection.getPeripheralDeviceId());
			success = updateConnectionMap(connection, hm, "updateAsUnsubscribed");
		}
		String from = "";
		if (fromCentral == true) {
			from = " from central";
		} else if (fromCentral == false) {
			from = " from peripheral";
		}
		addConnectionLogIfNeeded(connection, device, false, now, connection.getPeripheralDeviceId(),
				"from updateAsUnsubscribed" + from, false, null);
		return success;
	}

//	public boolean updatePeripheralSubscribed(DeviceConnectionDbObj connection, DeviceDbObj device,
//			boolean subscribed) {
//		boolean success = false;
//		if (subscribed) {
//			success = updateAsPeripheralSubscribed(connection, device);
//		} else {
//			success = updateAsUnsubscribed(connection, device, false);
//		}
//		return success;
//	}

	public boolean updateAsPeripheralConnected(DeviceConnectionDbObj connection) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("CENTRAL_CONNECTED", true);
		hm.put("PERIPHERAL_CONNECTED", true);
		hm.put("LAST_PERIPHERAL_CONNECTION_SUCCESS", now);
		this.addLog("connected = true for " + connection.getPeripheralDeviceId());
		addConnectionLogIfNeeded(connection, true, now, connection.getPeripheralDeviceId(),
				"from updateAsPeripheralConnected", ConnectionType.PROX);
		return updateConnectionMap(connection, hm, "updateAsPeripheralConnected");
	}

	public boolean updatePeripheralConnected(DeviceConnectionDbObj connection, boolean connected) {

		boolean success = false;
		if (connected) {
			success = updateAsPeripheralConnected(connection);
		} else {
			new DataAccess().addLog("connected: " + connected, LogConstants.TRACE);
			success = updateAsDisconnected(connection, false);
		}
		return success;
	}

	public boolean updateNeedPairing(DeviceConnectionDbObj connection, boolean needsPairing) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("NEEDS_PAIRING", needsPairing);
		return updateConnectionMap(connection, hm, "updateNeedPairing");
	}

	public boolean updateConnectionMap(DeviceConnectionDbObj connection, HashMap<String, Object> hm, String caller) {
		boolean success = false;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			StringBuilder query = new StringBuilder("UPDATE B2F_DEVICE_CONNECTION SET ");
			boolean first = true;
			for (String field : hm.keySet()) {
				if (!first) {
					query.append(", ");
				} else {
					first = false;
				}
				query.append(field + " = ?");
			}
			query.append(" WHERE CONNECTION_ID = ?");
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
					MySqlConn.close(rs, prepStmt, conn);
					throw new SQLException("could not update: " + value + ": bad type " + value.getClass().getName()
							+ " in the map: " + getHashMapAsString(hm));
				}
				j++;
			}
			prepStmt.setString(j, connection.getConnectionId());
			logQuery("updateConnectionMap", prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (SQLException e) {
			this.addLog(connection.getPeripheralDeviceId(), e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	protected String getHashMapAsString(HashMap<String, Object> hm) {
		StringBuilder sb = new StringBuilder("");
		for (Map.Entry<String, Object> entry : hm.entrySet()) {
			sb.append(entry.getKey() + " = " + entry.getValue().toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public boolean getConnecting(DeviceConnectionDbObj connection) {
		boolean connecting = false;
		if (connection != null) {
			Timestamp connectingCentral = connection.getConnectingCentral();
			Timestamp advertisingPeripheral = connection.getAdvertisingPeripheral();
			if (connectingCentral != null && advertisingPeripheral != null
					&& !connectingCentral.before(advertisingPeripheral)) {
				if (DateTimeUtilities.timestampSecondAgo(connectingCentral) < 300) {
					if (connection.getLastSuccess().before(connectingCentral)) {
						connecting = true;
					}
				}
			}
			addLog(connection.getCentralDeviceId(), "is Connecting: " + connecting);
		} else {
			addLog("connection null", LogConstants.ERROR);
		}
		return connecting;
	}

	public boolean addConnection(DeviceConnectionDbObj connection) {
		boolean success = false;
		if (connection != null) {
			String connectionFields = "CREATE_DATE, CONNECTION_ID, PERIPHERAL_DEVICE_ID, CENTRAL_DEVICE_ID, "
					+ "SERVICE_UUID, CHARACTER_UUID, GROUP_ID, ACTIVE, LAST_CHECK, LAST_SUCCESS, "
					+ "ADVERTISING_PERIPHERAL, CONNECTING_CENTRAL, SUBSCRIBED, INSTALL_COMPLETE, "
					+ "PERIPHERAL_CONNECTED, LAST_PERIPHERAL_CONNECTION_SUCCESS,"
					+ "CENTRAL_CONNECTED, LAST_CENTRAL_CONNECTION_SUCCESS, CENTRAL_OS_ID, PERIPHERAL_RSSI, "
					+ "CENTRAL_RSSI, PERIPHERAL_RSSI_TIMESTAMP, CENTRAL_RSSI_TIMESTAMP, NEEDS_PAIRING, PERIPHERAL_IDENTIFIER";
			String query = "INSERT INTO B2F_DEVICE_CONNECTION (" + connectionFields + ")"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, connection.getCreateDate());
				prepStmt.setString(2, connection.getConnectionId());
				prepStmt.setString(3, connection.getPeripheralDeviceId());
				prepStmt.setString(4, connection.getCentralDeviceId());
				prepStmt.setString(5, connection.getServiceUuid());
				prepStmt.setString(6, connection.getCharacteristicUuid());
				prepStmt.setString(7, connection.getGroupId());
				prepStmt.setBoolean(8, connection.isActive());
				prepStmt.setTimestamp(9, connection.getLastCheck());
				prepStmt.setTimestamp(10, connection.getLastSuccess());
				prepStmt.setTimestamp(11, connection.getAdvertisingPeripheral());
				prepStmt.setTimestamp(12, connection.getConnectingCentral());
				prepStmt.setBoolean(13, connection.isSubscribed());
				prepStmt.setBoolean(14, connection.isInstallComplete());
				prepStmt.setBoolean(15, connection.getPeripheralConnected());
				Timestamp lastPeripheralConnection = connection.getLastPeripheralConnectionSuccess();
				if (lastPeripheralConnection == null) {
					lastPeripheralConnection = DateTimeUtilities.getBaseTimestamp();
				}
				prepStmt.setTimestamp(16, lastPeripheralConnection);
				prepStmt.setBoolean(17, connection.getCentralConnected());
				Timestamp lastCentralConnection = connection.getLastCentralConnectionSuccess();
				if (lastCentralConnection == null) {
					lastCentralConnection = DateTimeUtilities.getBaseTimestamp();
				}
				prepStmt.setTimestamp(18, lastCentralConnection);
				prepStmt.setString(19, connection.getCentralOsId());
				prepStmt.setInt(20, connection.getPeripheralRssi());
				prepStmt.setInt(21, connection.getCentralRssi());
				prepStmt.setTimestamp(22, connection.getPeripheralRssiTimestamp());
				prepStmt.setTimestamp(23, connection.getCentralRssiTimestamp());
				prepStmt.setBoolean(24, connection.getNeedsPairing());
				prepStmt.setString(25, connection.getPeripheralIdentifier());
				logQueryImportant(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				success = true;
			} catch (SQLException e) {
				addLog(connection.getCentralDeviceId(), e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		} else {
			addLog("addConnection", "connection was null", LogConstants.WARNING);
		}
		return success;
	}
}

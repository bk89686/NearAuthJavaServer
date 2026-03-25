package com.humansarehuman.blue2factor.dataAndAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.entities.CompanyGroupDevice;
import com.humansarehuman.blue2factor.entities.CompanyGroupsDevices;
import com.humansarehuman.blue2factor.entities.OtherUserData;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.enums.NonMemberStrategy;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.enums.UserType;
import com.humansarehuman.blue2factor.entities.tables.BrandingDbObj;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyUrlDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.EmailSentDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.LdapServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.SolicitationDbObj;
import com.humansarehuman.blue2factor.utilities.Converters;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class CompanyDataAccess extends GroupDataAccess {

	public CompanyGroupDevice getCompanyGroupAndDeviceByDeviceId(String deviceId) {
		String query = "SELECT " + DeviceFields.companyGroupDeviceFields
				+ " FROM B2F_DEVICE device JOIN B2F_GROUP grp on device.GROUP_ID = grp.GROUP_ID "
				+ "JOIN B2F_COMPANY company ON grp.COMPANY_ID = company.COMPANY_ID WHERE device.DEVICE_ID = ?";
		CompanyGroupDevice companyGroupDevice = null;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				companyGroupDevice = resultToCompanyGroupDevice(rs);
			}
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return companyGroupDevice;
	}

	public boolean userIsAdmin(GroupDbObj group) {
		boolean canView = false;
		if (group != null) {
			UserType userType = group.getUserType();
			canView = userType == UserType.ADMIN || userType == UserType.ADMIN_VIEWER
					|| userType == UserType.SUPER_ADMIN || userType == UserType.AUDITOR;

		}
		return canView;
	}

	private CompanyGroupDevice resultToCompanyGroupDevice(ResultSet rs) {
		CompanyGroupDevice companyGroupDevice = null;
		CompanyDbObj company = null;
		GroupDbObj group = null;
		DeviceDbObj device = null;
		try {
			String companyCompanyId = rs.getString("company_COMPANY_ID");
			String companyName = rs.getString("company_COMPANY_NAME");
			boolean companyActive = rs.getBoolean("company_ACTIVE");
			String companySecret = rs.getString("company_COMPANY_SECRET");
			int companyAcceptedTypes = rs.getInt("company_ACCEPTED_TYPES");
			Date companyCreateDate = rs.getTimestamp("company_CREATE_DATE");
			String companyLoginToken = rs.getString("company_COMPANY_LOGIN_TOKEN");
			String companyCompletionUrl = rs.getString("company_COMPANY_COMPLETION_URL");
			String apiKey = rs.getString("company_API_KEY");
			int licenseCount = rs.getInt("company_LICENSE_COUNT");
			int companyLicensesInUse = rs.getInt("company_LICENSES_IN_USE");
			String companyBaseUrl = rs.getString("company_COMPANY_BASE_URL");
			String companyLoginUrl = rs.getString("company_COMPANY_LOGIN_URL");
			String f1Method = rs.getString("company_F1_METHOD");
			String f2Method = rs.getString("company_F2_METHOD");
			String logoutUrl = rs.getString("company_LOGOUT_URL");
			String urlRegex = rs.getString("company_URL_REGEX");
			NonMemberStrategy nonMemberStrategy;
			try {
				nonMemberStrategy = NonMemberStrategy.valueOf(rs.getString("company_NON_MEMBER_STRATEGY"));
			} catch (Exception e) {
				nonMemberStrategy = NonMemberStrategy.ALLOW_AUTHENTICATED_ONLY;
			}
			boolean companyPushAllowed = rs.getBoolean("company_PUSH_ALLOWED");
			boolean companyTextAllowed = rs.getBoolean("company_TEXT_ALLOWED");
			int pushSeconds = rs.getInt("company_PUSH_TIMEOUT_SECONDS");
			int textSeconds = rs.getInt("company_TEXT_TIMEOUT_SECONDS");
			int passkeyTimeout = rs.getInt("company_PASSKEY_TIMEOUT_SECONDS");
			boolean allowAllFromIdp = rs.getBoolean("company_ALLOW_ALL_FROM_IDP");
			boolean moveB2fUsersToIdp = rs.getBoolean("company_MOVE_B2F_USERS_TO_IDP");
			String emailDomain = rs.getString("company_EMAIL_DOMAIN");
			int adminCodeTimeout = rs.getInt("company_ADMIN_CODE_TIMEOUT_SECONDS");
			String entityIdVal = rs.getString("company_ENTITY_ID_VAL");
			company = new CompanyDbObj(companyCompanyId, companyName, companySecret, companyAcceptedTypes,
					companyActive, companyCreateDate, companyLoginToken, companyCompletionUrl, apiKey, licenseCount,
					companyLicensesInUse, companyBaseUrl, companyLoginUrl, stringToAuthMethod(f1Method),
					stringToAuthMethod(f2Method), logoutUrl, urlRegex, nonMemberStrategy, companyPushAllowed,
					companyTextAllowed, pushSeconds, textSeconds, passkeyTimeout, allowAllFromIdp, moveB2fUsersToIdp,
					emailDomain, adminCodeTimeout, entityIdVal);
			addLog("company created: " + (company != null));
		} catch (SQLException e) {
			addLog(e);
		}
		try {
			String groupCompanyId = rs.getString("group_COMPANY_ID");
			String groupId = rs.getString("group_GROUP_ID");
			String groupName = rs.getString("group_GROUP_NAME");
			boolean groupActive = rs.getBoolean("group_ACTIVE");
			int acceptedTypes = rs.getInt("group_ACCEPTED_TYPES");
			Timestamp groupCreateDate = rs.getTimestamp("group_CREATE_DATE");
			int timeoutSecs = rs.getInt("group_TIMEOUT_SECS");
			String groupPw = rs.getString("group_GROUP_PW");
			String salt = rs.getString("group_SALT");
			int groupLicensesAllowed = rs.getInt("group_DEVICES_ALLOWED");
			int groupLicensesInUse = rs.getInt("group_DEVICES_IN_USE");
			int permissions = rs.getInt("group_PERMISSIONS");
			String username = rs.getString("group_USERNAME");
			Timestamp tokenDate = rs.getTimestamp("group_TOKEN_DATE");
			String uid = rs.getString("group_UID");
			String userTypeStr = rs.getString("group_USER_TYPE");
			boolean userExempt = rs.getBoolean("group_USER_EXEMPT");
			boolean groupPushAllowed = rs.getBoolean("group_PUSH_ALLOWED");
			boolean groupTextAllowed = rs.getBoolean("group_TEXT_ALLOWED");
			this.addLog("userRole: " + userTypeStr);
			group = new GroupDbObj(groupCompanyId, groupId, groupName, acceptedTypes, groupActive, groupCreateDate,
					timeoutSecs, groupPw, salt, groupLicensesAllowed, groupLicensesInUse, permissions, username,
					tokenDate, uid, Converters.stringToUserType(userTypeStr, "recordToGroup"), userExempt,
					groupPushAllowed, groupTextAllowed);
		} catch (SQLException e) {
			addLog(e);
		}
		try {
			String deviceGroupId = rs.getString("device_GROUP_ID");
			String userId = rs.getString("device_USER_ID");
			String deviceId = rs.getString("device_DEVICE_ID");
			int seed = rs.getInt("device_SEED");
			boolean active = rs.getBoolean("device_ACTIVE");
			String fcmId = rs.getString("device_FCM_ID");
			Date createDate = rs.getTimestamp("device_CREATE_DATE");
			Timestamp lastUpdate;
			try {
				lastUpdate = rs.getTimestamp("device_LAST_UPDATE");
			} catch (Exception e1) {
				lastUpdate = DateTimeUtilities.getCurrentTimestamp();
			}
			String deviceType = rs.getString("device_DEVICE_TYPE");
			OsClass operatingSystem;
			try {
				operatingSystem = OsClass.valueOf(rs.getString("device_OPERATING_SYSTEM").toUpperCase());
			} catch (Exception e) {
				operatingSystem = OsClass.UNKNOWN;
				this.addLog("recordToDevice", "illegal os class: '" + rs.getString("device_OPERATING_SYSTEM") + ";");
			}
			String osVersion = rs.getString("device_OS_VERSION");
			String loginToken = rs.getString("device_LOGIN_TOKEN");

			String btAddress = rs.getString("device_BT_ADDRESS");
			Date lastCompleteCheck = rs.getTimestamp("device_LAST_COMPLETE_CHECK");
			Integer lastGmtOffset = rs.getInt("device_LAST_GMT_OFFSET");
			String userLanguage = rs.getString("device_USER_LANGUAGE");
			String screenSize = rs.getString("device_SCREEN_SIZE");

			String rnd = rs.getString("device_RAND");
			boolean showIcon = rs.getBoolean("device_SHOW_ICON");
			Double devicePriority = rs.getDouble("device_DEVICE_PRIORITY");
			boolean triggerUpdate = rs.getBoolean("device_TRIGGER_UPDATE");
			int recentPushes = rs.getInt("device_RECENT_PUSHES");
			boolean unresponsive = rs.getBoolean("device_UNRESPONSIVE");
			Timestamp lastPush = rs.getTimestamp("device_LAST_PUSH");
			boolean pushNow = rs.getBoolean("device_PUSH_LOUD");
			boolean pushFailure = rs.getBoolean("device_PUSH_FAILURE");
			String command = rs.getString("device_COMMAND");
			String temp = rs.getString("device_TEMP");
			boolean central = rs.getBoolean("device_CENTRAL");
			addLog("recordToDevice", "central? " + central + " for deviceId: " + deviceId);
			Timestamp lastReset = rs.getTimestamp("device_LAST_RESET");
			boolean turnedOff = rs.getBoolean("device_TURNED_OFF");
			String devClass = rs.getString("device_DEVICE_CLASS");
			DeviceClass deviceClass = DeviceClass.UNKNOWN;
			if (devClass != null) {
				deviceClass = DeviceClass.valueOf(rs.getString("device_DEVICE_CLASS").toUpperCase());
			}
			if (lastReset == null) {
				lastReset = DateTimeUtilities.getBaseTimestamp();
			}
			boolean hasBle = rs.getBoolean("device_HAS_BLE");
			boolean screensaverOn = rs.getBoolean("device_SCREENSAVER_ON");
			Timestamp lastVariableRetrieval = rs.getTimestamp("device_LAST_VARIABLE_RETRIEVAL");
			boolean browserInstallComplete = rs.getBoolean("device_BROWSER_INSTALL_COMPLETE");
			boolean signedIn = rs.getBoolean("device_SIGNED_IN");
			Timestamp lastSilentPush = rs.getTimestamp("device_LAST_SILENT_PUSH");
			Timestamp lastSilentPushResponse = null;
			try {
				lastSilentPushResponse = rs.getTimestamp("device_LAST_SILENT_PUSH_RESPONSE");
			} catch (Exception e) {
				// wtf
			}
			boolean turnOffFromInstaller = rs.getBoolean("device_TURN_OFF_FROM_INSTALLER");
			String phoneNumber = rs.getString("device_PHONE_NUMBER");
			boolean multiUser = rs.getBoolean("device_MULTI_USER");
			boolean passkeyEnabled = rs.getBoolean("device_PASSKEY_ENABLED");
			Integer txPower = rs.getInt("device_PASSKEY_ENABLED");
			boolean terminate = rs.getBoolean("device_TERMINATE");
			addLog("recordToDevice", "HAS_BLE: " + hasBle);
			device = new DeviceDbObj(deviceGroupId, userId, deviceId, seed, active, fcmId, btAddress, createDate,
					lastUpdate, deviceType, operatingSystem, loginToken, lastCompleteCheck, lastGmtOffset, osVersion,
					userLanguage, screenSize, rnd, showIcon, devicePriority, triggerUpdate, recentPushes, unresponsive,
					lastPush, pushNow, pushFailure, command, temp, central, lastReset, screensaverOn,
					lastVariableRetrieval, turnedOff, deviceClass, browserInstallComplete, signedIn, lastSilentPush,
					lastSilentPushResponse, hasBle, turnOffFromInstaller, phoneNumber, multiUser, passkeyEnabled,
					txPower, terminate);
		} catch (SQLException e) {
			addLog(e);
		}
		companyGroupDevice = new CompanyGroupDevice(company, group, device);
		return companyGroupDevice;
	}

	public CompanyGroupsDevices getAllCompanyGroupsAndDevices(String companyId) {
		String query = "SELECT " + DeviceFields.companyGroupDeviceFields
				+ " FROM B2F_COMPANY company JOIN B2F_GROUP grp ON grp.COMPANY_ID = company.COMPANY_ID "
				+ "JOIN B2F_DEVICE device ON device.GROUP_ID = grp.GROUP_ID WHERE company.COMPANY_ID = ? AND "
				+ "grp.ACTIVE AND device.ACTIVE ORDER BY grp.GROUP_ID";
		CompanyGroupsDevices companyGroupsDevices = null;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				companyGroupsDevices = resultToCompanyGroupsDevices(rs);
			}
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return companyGroupsDevices;
	}

	public ArrayList<GroupDbObj> getAllActiveCompanyGroups(String companyId) {
		ArrayList<GroupDbObj> groups = new ArrayList<>();
		GroupDbObj group;
		String query = "SELECT * FROM B2F_GROUP WHERE COMPANY_ID = ? AND ACTIVE = ? AND GROUP_NAME != ? ORDER BY GROUP_NAME";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			prepStmt.setBoolean(2, true);
			prepStmt.setString(3, "Anonymous Group");
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				group = this.recordToGroup(rs);
				groups.add(group);
			}
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return groups;
	}

	public ArrayList<OtherUserData> getAllActiveCompanyGroupsWithDevices(String companyId, GroupDbObj group) {
		OtherUserData otherUserData;
		ArrayList<OtherUserData> otherUsersData = new ArrayList<>();
		String query = "SELECT bg.GROUP_ID, bg.USERNAME, bg.GROUP_NAME FROM B2F_GROUP bg "
				+ "LEFT JOIN B2F_DEVICE bd ON bg.GROUP_ID = bd.GROUP_ID WHERE DEVICE_ID is "
				+ "not NULL AND COMPANY_ID = ? AND bd.ACTIVE AND bg.ACTIVE AND GROUP_NAME != ? AND bg.GROUP_ID != ?"
				+ "GROUP BY bg.GROUP_ID, bg.GROUP_NAME, bg.USERNAME";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			prepStmt.setString(2, "Anonymous Group");
			prepStmt.setString(3, group.getGroupId());
			this.logQueryImportant("getAllActiveCompanyGroupsWithDevices", prepStmt);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				String groupId = rs.getString("GROUP_ID");
				String username = rs.getString("USERNAME");
				String groupName = rs.getString("GROUP_NAME");
				otherUserData = new OtherUserData(username, groupName, groupId);
				otherUsersData.add(otherUserData);
			}
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return otherUsersData;
	}

	private CompanyGroupsDevices resultToCompanyGroupsDevices(ResultSet rs) {
		CompanyGroupsDevices companyGroupsDevices = null;

		return companyGroupsDevices;
	}

	public boolean companyMatchesDevice(CompanyDbObj company, DeviceDbObj device) {
		boolean matches = false;
		if (company != null) {
			CompanyDbObj deviceCo = this.getCompanyByGroupId(device.getGroupId());
			if (deviceCo.getCompanyId().equals(company.getCompanyId())) {
				matches = true;
			} else {
				addLog("company Id from device: " + deviceCo.getCompanyId() + " != coId from Co: "
						+ company.getCompanyId());
			}
		}
		return matches;
	}

	public boolean areThereNewlySignedUpUsers(CompanyDbObj company) {
		ArrayList<GroupDbObj> groups = getGroupsCreatedWithinXDays(company, Constants.NUMBER_OF_DAYS_TO_SIGN_UP);
		boolean notSetup = false;
		for (GroupDbObj group : groups) {
			notSetup = !doesConnectionExistByEmail(group.getGroupName());
			if (notSetup) {
				break;
			}
		}
		return notSetup;

	}

	public boolean isNewlySignedUp(GroupDbObj group) {
		boolean notSetup = false;
		Timestamp twoDaysAgo = DateTimeUtilities
				.getCurrentTimestampMinusHours(Constants.NUMBER_OF_DAYS_TO_SIGN_UP * 24);
		if (group.getCreateDate().after(twoDaysAgo)) {
			notSetup = !doesConnectionExistByEmail(group.getGroupName());
		}
		return notSetup;
	}

	public ArrayList<GroupDbObj> getGroupsCreatedWithinXDays(CompanyDbObj company, int numberOfDays) {
		ArrayList<GroupDbObj> groups = new ArrayList<>();
		String query = "SELECT * FROM B2F_GROUP WHERE COMPANY_ID = ? AND CREATE_DATE > ?";
		Timestamp xDaysAgo = DateTimeUtilities.getCurrentTimestampMinusHours(numberOfDays * 24);
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, company.getCompanyId());
			prepStmt.setTimestamp(2, xDaysAgo);
			rs = executeQuery(prepStmt);
			GroupDbObj group;
			while (rs.next()) {
				group = recordToGroup(rs);
				groups.add(group);
			}
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return groups;
	}

	/*
	 * public ServerConnectionDbObj getServerConnectionByServerAndClientIds(String
	 * serverId, String clientId) { ServerConnectionDbObj serverConn = null; String
	 * query =
	 * "SELECT * FROM B2F_SERVER_CONNECTION WHERE SERVER_ID = ? AND CLIENT_ID = ? AND ACTIVE = ?"
	 * ; MySqlConn msc = new MySqlConn(); Connection conn = msc.getConnection();
	 * PreparedStatement prepStmt; try { prepStmt = conn.prepareStatement(query);
	 * prepStmt.setString(1, serverId); prepStmt.setString(2, clientId);
	 * prepStmt.setBoolean(3, true); ResultSet rs = executeQuery(prepStmt); if
	 * (rs.next()) { serverConn = this.recordToServerConnection(rs); } } catch
	 * (Exception e) { addLog(e); } finally { MySqlConn.close(rs, prepStmt, conn); }
	 * return serverConn; }
	 * 
	 * ServerConnectionDbObj recordToServerConnection(ResultSet rs) {
	 * ServerConnectionDbObj serverConn = null; try { Timestamp createDate =
	 * rs.getTimestamp("CREATE_DATE"); String serverId = rs.getString("SERVER_ID");
	 * String clientId = rs.getString("CLIENT_ID"); int permissions =
	 * rs.getInt("PERMISSIONS"); Boolean active = rs.getBoolean("ACTIVE");
	 * serverConn = new ServerConnectionDbObj(createDate, serverId, clientId,
	 * permissions, active); } catch (Exception e) { addLog(e); } return serverConn;
	 * }
	 */

	public ArrayList<SolicitationDbObj> getUnsolicitedCompanies() {
		ArrayList<SolicitationDbObj> solicitations = new ArrayList<>();
		String query = "SELECT * FROM B2F_SOLICITATION WHERE CONTACT_COUNT = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setInt(1, 0);
			rs = executeQuery(prepStmt);
			SolicitationDbObj solicitation;
			while (rs.next()) {
				solicitation = this.recordToSolicitation(rs);
				solicitations.add(solicitation);
			}
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return solicitations;
	}

	private SolicitationDbObj recordToSolicitation(ResultSet rs) {
		SolicitationDbObj solicitation = null;
		try {
			String recordId = rs.getString("RECORD_ID");
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			String userName = rs.getString("CONTACT_NAME");
			String emailAddress = rs.getString("EMAIL_ADDRESS");
			String coName = rs.getString("COMPANY_NAME");
			Timestamp lastContact = rs.getTimestamp("LAST_CONTACT");
			int contactCount = rs.getInt("CONTACT_COUNT");
			solicitation = new SolicitationDbObj(recordId, createDate, userName, emailAddress, coName, lastContact,
					contactCount);
		} catch (Exception e) {
			this.addLog(e);
		}
		return solicitation;
	}

	public void updateSolicitationCompany(SolicitationDbObj solicitation) {
		String query = "UPDATE B2F_SOLICITATION SET EMAIL_ADDRESS = ?, COMPANY_NAME = ?, LAST_CONTACT = ?, "
				+ "CONTACT_COUNT = ? WHERE RECORD_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);

			prepStmt.setString(1, solicitation.getEmailAddress());
			prepStmt.setString(2, solicitation.getCompanyName());
			prepStmt.setString(3, solicitation.getContactName());
			prepStmt.setTimestamp(3, solicitation.getLastContact());
			prepStmt.setInt(4, solicitation.getContactCount());
			prepStmt.setString(5, solicitation.getRecordId());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	public void addEmailSent(EmailSentDbObj emailSent) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_EMAIL_SENT (EMAIL_ID, SOLICITATION_ID, MAILING_LIST_ID, "
					+ "CREATE_DATE, EMAIL_ADDRESS, SUBJECT, MESSAGE) VALUES (" + "?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, emailSent.getEmailId());
				prepStmt.setString(2, emailSent.getSolicitationId());
				prepStmt.setString(3, emailSent.getMailingListId());
				prepStmt.setTimestamp(4, emailSent.getCreateDate());
				prepStmt.setString(5, emailSent.getEmailAddress());
				prepStmt.setString(6, emailSent.getSubject());
				prepStmt.setString(7, emailSent.getMessage());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("addBranding", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public LdapServerDbObj getLdapServerFromCompany(CompanyDbObj company) {
		return getLdapServerFromCompany(company.getCompanyId());
	}

	public LdapServerDbObj getLdapServerFromCompany(String companyId) {
		LdapServerDbObj ldapServer = null;
		String query = "SELECT * FROM B2F_LDAP_SERVER WHERE COMPANY_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				ldapServer = this.recordToLdapServer(rs);
			}
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return ldapServer;
	}

	public LdapServerDbObj recordToLdapServer(ResultSet rs) {
		LdapServerDbObj ldapServer = null;
		try {
			String tableId = rs.getString("TABLE_ID");
			String companyId = rs.getString("COMPANY_ID");
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			boolean active = rs.getBoolean("ACTIVE");
			String providerUrl = rs.getString("PROVIDER_URL");
			String searchBase = rs.getString("SEARCH_BASE");
			String jksFile = rs.getString("JKS_FILE");
			String jksPassword = rs.getString("JKS_PASSWORD");
			String serviceUsername = rs.getString("SERVICE_USERNAME");
			String servicePassword = rs.getString("SERVICE_PASSWORD");
			ldapServer = new LdapServerDbObj(tableId, companyId, createDate, active, providerUrl, searchBase, jksFile,
					jksPassword, serviceUsername, servicePassword);
		} catch (Exception e) {
			this.addLog(e);
		}
		return ldapServer;
	}

	public void updateLdapServer(LdapServerDbObj ldapServer) {
		new Thread(() -> {
			String query = "UPDATE B2F_LDAP_SERVER SET COMPANY_ID = ?, CREATE_DATE = ?, ACTIVE = ?, "
					+ "PROVIDER_URL = ?, SEARCH_BASE = ?, JKS_FILE = ?, JKS_PASSWORD = ?, "
					+ "SERVICE_USERNAME = ?, SERVICE_PASSWORD = ? WHERE TABLE_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, ldapServer.getCompanyId());
				prepStmt.setTimestamp(2, ldapServer.getCreateDate());
				prepStmt.setBoolean(3, ldapServer.isActive());
				prepStmt.setString(4, ldapServer.getProviderUrl());
				prepStmt.setString(5, ldapServer.getSearchBase());
				prepStmt.setString(6, ldapServer.getJksFile());
				prepStmt.setString(7, ldapServer.getJksPassword());
				prepStmt.setString(8, ldapServer.getServiceUsername());
				prepStmt.setString(9, ldapServer.getServicePassword());
				prepStmt.setString(10, ldapServer.getTableId());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("updateLdapServer", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public BrandingDbObj getBranding(CompanyDbObj company) {
		return getBranding(company.getCompanyId());
	}

	public BrandingDbObj getBranding(String companyId) {
		BrandingDbObj branding = null;
		String query = "SELECT * FROM B2F_BRANDING WHERE COMPANY_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				branding = this.recordToBranding(rs);
			}
		} catch (Exception e) {
			addLog("getBranding", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return branding;
	}

	public void addBranding(BrandingDbObj branding) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_BRANDING (" + brandingFields + ") VALUES (" + "?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, branding.getCompanyId());
				prepStmt.setString(2, branding.getIconPath());
				prepStmt.setString(3, branding.getBackgroundColor());
				prepStmt.setString(4, branding.getForegroundColor());
				prepStmt.setString(5, branding.getTitleImagePath());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("addBranding", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public BrandingDbObj recordToBranding(ResultSet rs) {
		BrandingDbObj branding = null;
		try {
			String companyId = rs.getString("COMPANY_ID");
			String iconPath = rs.getString("ICON_PATH");
			String backgroundColor = rs.getString("BACKGROUND_COLOR");
			addLog("bgColor: " + backgroundColor);
			String foregroundColor = rs.getString("FOREGROUND_COLOR");
			String titleImagePath = rs.getString("TITLE_IMAGE_PATH");
			branding = new BrandingDbObj(companyId, iconPath, backgroundColor, foregroundColor, titleImagePath);
		} catch (Exception e) {
			this.addLog(e);
		}
		return branding;
	}

	public void updateBranding(BrandingDbObj branding) {
		new Thread(() -> {
			String query = "UPDATE B2F_BRANDING SET ICON_PATH = ?, BACKGROUND_COLOR = ?, "
					+ "FOREGROUND_COLOR = ?, TITLE_IMAGE_PATH = ? WHERE COMPANY_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, branding.getIconPath());
				prepStmt.setString(2, branding.getBackgroundColor());
				prepStmt.setString(3, branding.getForegroundColor());
				prepStmt.setString(4, branding.getTitleImagePath());
				prepStmt.setString(5, branding.getCompanyId());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("updateBranding", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

//  String tableId;
//	String companyId;
//	Timestamp createDate;
//	boolean active;
//	String providerUrl;
//	String searchBase;
//	String jksFile;
//	String jksPassword;
	public void addLdapServer(LdapServerDbObj ldapServer) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_LDAP_SERVER (" + ldapServerFields + ") VALUES ("
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, ldapServer.getCompanyId());
				prepStmt.setString(2, ldapServer.getCompanyId());
				prepStmt.setTimestamp(3, ldapServer.getCreateDate());
				prepStmt.setBoolean(4, ldapServer.isActive());
				prepStmt.setString(5, ldapServer.getProviderUrl());
				prepStmt.setString(6, ldapServer.getSearchBase());
				prepStmt.setString(7, ldapServer.getJksFile());
				prepStmt.setString(8, ldapServer.getJksPassword());
				prepStmt.setString(9, ldapServer.getServiceUsername());
				prepStmt.setString(10, ldapServer.getServicePassword());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("addLdapServer", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public boolean urlMatchesCompany(CompanyDbObj company, String referrer) {
		boolean matches = false;
		ArrayList<CompanyUrlDbObj> urls = getAllUrlRegexForCompany(company);
		if (urls.size() == 0) {
			matches = true;
		} else {
			for (CompanyUrlDbObj url : urls) {
				matches = Pattern.matches(url.getUrlRegex(), referrer.toLowerCase());
				this.addLog("does " + referrer.toLowerCase() + " match " + url.getUrlRegex() + "? " + matches);
				if (matches) {
					break;
				} else {
					this.addLog("the string: " + referrer.toLowerCase() + "does not match the regex " + url.getUrlRegex(), LogConstants.IMPORTANT);
				}
			}
		}
		return matches;
	}

	public ArrayList<CompanyUrlDbObj> getAllUrlRegexForCompany(CompanyDbObj company) {
		return getAllUrlRegexForCompany(company.getCompanyId());
	}

	public ArrayList<CompanyUrlDbObj> getAllUrlRegexForCompany(String companyId) {
		ArrayList<CompanyUrlDbObj> urls = new ArrayList<>();
		String query = "SELECT * FROM B2F_COMPANY_URL WHERE COMPANY_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				CompanyUrlDbObj nextUrl = recordToCompanyUrl(rs);
				urls.add(nextUrl);
			}
		} catch (Exception e) {
			addLog("getAllUrlRegexForCompany", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return urls;
	}

	public boolean otherSuperAdminsExist(GroupDbObj group) {
		boolean othersExist = false;
		String query = "SELECT * FROM B2F_GROUP WHERE USER_TYPE = ? AND ACTIVE = ? AND GROUP_NAME != ?"
				+ " AND COMPANY_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, UserType.SUPER_ADMIN.toString());
			prepStmt.setBoolean(2, true);
			prepStmt.setString(3, group.getGroupName());
			prepStmt.setString(4, group.getCompanyId());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				othersExist = true;
			}
		} catch (Exception e) {
			addLog("otherSuperAdminsExist", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return othersExist;
	}

	public boolean rescindFingerprintsForBrowser(BrowserDbObj browser, String url) {
		boolean success = false;
		String query = "UPDATE B2F_AUTHENTICATOR SET EXPIRED = ? WHERE BROWSER_ID = ? AND "
				+ "EXPIRED = ? AND BASE_ URL = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setString(2, browser.getBrowserId());
			prepStmt.setBoolean(3, false);
			prepStmt.setString(4, url);
			logQueryImportant(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			addLog("rescindFingerprintsForBrowser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public void rescindTokensForBrowser(String browserId) {
		new Thread(() -> {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE BROWSER_ID = ? AND DESCRIPTION = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);

				prepStmt.setTimestamp(1, now);
				prepStmt.setString(2, browserId);
				prepStmt.setString(3, TokenDescription.BROWSER_SESSION.toString());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("rescindTokensForBrowser", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			expireBrowser(browserId);
		}).start();
	}

	public void expireBrowser(String browserId) {
		new Thread(() -> {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			String query = "UPDATE B2F_BROWSER SET EXPIRE_DATE = ? WHERE BROWSER_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, now);
				prepStmt.setString(2, browserId);
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("expireBrowser", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	/**
	 * 
	 * @param issuer
	 * @param companyId
	 * @return true if the issuer is found or there is no record for the company
	 */
	public boolean isEmailIssuerForCompany(String issuer, String companyId) {
		boolean emailIssuer = false;
		boolean found = false;
		int count = 0;
		String query = "SELECT * FROM B2F_EMAIL_ISSUER WHERE COMPANY_ID = ? AND ACTIVE = ? ";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			String savedIssuer;
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				count++;
				savedIssuer = rs.getString("ISSUER");
				if (savedIssuer.equalsIgnoreCase(issuer)) {
					found = true;
					break;
				}
			}
			if (found || count == 0) {
				emailIssuer = true;
			}
		} catch (SQLException e) {
			this.addLog("isEmailIssuerForCompany", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return emailIssuer;
	}

	public CompanyDbObj getCompanyById(String companyId) {
		CompanyDbObj company = null;
		String query = "SELECT * FROM B2F_COMPANY WHERE COMPANY_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				addLog("company found");
				company = recordToCompany(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getCompanyById", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return company;
	}

	public CompanyDbObj getCompanyByPrivateKey(String companyPrivateKey) {
		CompanyDbObj company = null;
		String query = "SELECT co.* FROM B2F_COMPANY co JOIN B2F_KEY key ON "
				+ "co.COMPANY_ID = key.COMPANY_ID WHERE key.KEY_TEXT = ? AND " + "KeyType = "
				+ KeyType.SP_PRIVATE.name();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyPrivateKey);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("companyPrivateKey", "company found", LogConstants.DEBUG);
				company = recordToCompany(rs);
			} else {
				addLog("companyPrivateKey", "company found", LogConstants.DEBUG);
			}
		} catch (SQLException e) {
			this.addLog("companyPrivateKey", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return company;
	}

	public CompanyDbObj getCompanyByPublicKey(String companyPublicKey) {
		CompanyDbObj company = null;
		String query = "SELECT * FROM B2F_COMPANY WHERE COMPANY_PUBLIC_KEY = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyPublicKey);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("getCompanyByKey", "company found", LogConstants.DEBUG);
				company = recordToCompany(rs);
			} else {
				addLog("getCompanyByKey", "company found", LogConstants.DEBUG);
			}
		} catch (SQLException e) {
			this.addLog("getCompanyByKey", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return company;
	}

	public CompanyDbObj getCompanyByApiKey(String apiKey) {
		CompanyDbObj company = null;
		String query = "SELECT * FROM B2F_COMPANY WHERE API_KEY = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, apiKey);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				company = recordToCompany(rs);
			} else {
				addLog("company not found using: " + prepStmt.toString());
			}
		} catch (SQLException e) {
			this.addLog("getCompanyByApiKey", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return company;
	}

	public CompanyDbObj getCompanyByEmailDomain(String emailDomain) {
		CompanyDbObj company = null;
		String query = "SELECT * FROM B2F_COMPANY WHERE EMAIL_DOMAIN = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, emailDomain);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				company = recordToCompany(rs);
			} else {
				addLog("company not found using: " + prepStmt.toString());
			}
		} catch (SQLException e) {
			this.addLog("getCompanyByEmailDomain", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return company;
	}

	public ArrayList<CompanyDbObj> getAllActiveCompanies() {
		ArrayList<CompanyDbObj> companies = new ArrayList<>();
		String query = "SELECT * FROM B2F_COMPANY WHERE ACTIVE = ?";
		CompanyDbObj company;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				company = recordToCompany(rs);
				companies.add(company);
			}
		} catch (SQLException e) {
			this.addLog("getCompanyByDevId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return companies;
	}

	public CompanyDbObj getCompanyByDevId(String deviceId) {
		CompanyDbObj company = null;
		String query = "SELECT company.* FROM B2F_COMPANY company "
				+ "JOIN B2F_GROUP bgroup ON company.COMPANY_ID = bgroup.COMPANY_ID "
				+ "JOIN B2F_DEVICE device ON bgroup.GROUP_ID = device.GROUP_ID " + "WHERE device.DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				company = recordToCompany(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getCompanyByDevId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return company;
	}

	public CompanyDbObj getCompanyByGroupId(String groupId) {
		CompanyDbObj company = null;
		String query = "SELECT company.* FROM B2F_COMPANY company "
				+ "JOIN B2F_GROUP bgroup ON company.COMPANY_ID = bgroup.COMPANY_ID " + "WHERE bgroup.GROUP_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, groupId);
			this.logQueryImportant("getCompanyId", prepStmt);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				company = recordToCompany(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getCompanyByGroupId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return company;
	}

	public boolean isLicenseAvailableForCompany(String companyId) {
		boolean success = false;
		CompanyDbObj company = this.getCompanyById(companyId);
		if (company != null) {
			int licensesInUse = getLicensesInUse(companyId);
			if (company.getLicenseCount() > licensesInUse) {
				success = true;
			}
		} else {
			addLog("isLicenseAvailable", "company was null", LogConstants.INFO);
		}
		return success;
	}

	public int getLicensesInUse(String companyId) {
		int licenseCount = 0;
		String query = "SELECT COUNT(GROUP_ID) AS TOTAL FROM B2F_GROUP bg WHERE bg.COMPANY_ID = ? AND bg.ACTIVE = ? AND "
				+ "(SELECT COUNT(DEVICE_ID) FROM B2F_DEVICE bd WHERE bg.GROUP_ID = bd.GROUP_ID AND bd.ACTIVE = ?) > 1";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			prepStmt.setBoolean(2, true);
			prepStmt.setBoolean(3, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				licenseCount = rs.getInt("TOTAL");
			}
		} catch (SQLException e) {
			addLog("getLicensesInUse", "company was null", LogConstants.INFO);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return licenseCount;
	}

	public CompanyUrlDbObj recordToCompanyUrl(ResultSet rs) {
		CompanyUrlDbObj url = null;
		try {
			String tableId = rs.getString("TABLE_ID");
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			boolean active = rs.getBoolean("ACTIVE");
			String companyId = rs.getString("COMPANY_ID");
			String urlRegex = rs.getString("URL_REGEX");
			url = new CompanyUrlDbObj(tableId, createDate, active, companyId, urlRegex);
		} catch (SQLException e) {
			addLog(e);
		}
		return url;
	}

	public CompanyDbObj getCompanyByToken(String token) {
		return getCompanyByToken(token, true);
	}

	public CompanyDbObj getCompanyByToken(String token, boolean notExpired) {
		CompanyDbObj company = null;
		String now = DateTimeUtilities.getLastTimestampString();
		String query = "SELECT co.* FROM B2F_TOKEN tk JOIN B2F_GROUP gp on tk.GROUP_ID = gp.GROUP_ID "
				+ "JOIN B2F_COMPANY co ON gp.COMPANY_ID = co.COMPANY_ID WHERE tk.TOKEN_ID = ?";
		if (notExpired) {
			query += " AND (tk.EXPIRE_TIME IS NULL OR tk.EXPIRE_TIME > ?)";
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, token);
			if (notExpired) {
				prepStmt.setString(2, now);
			}
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				company = this.recordToCompany(rs);
			}
			this.addLog(token, "found: " + (company != null));
		} catch (Exception e) {
			this.addLog("getCompanyByToken", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return company;
	}

	public CompanyDbObj getCompanyByTokenAndDescription(String token, TokenDescription tokenDescription,
			boolean ignoreExpiration) {
		CompanyDbObj company = null;
		String now = DateTimeUtilities.getLastTimestampString();
		String query = "SELECT co.* FROM B2F_TOKEN tk JOIN B2F_GROUP gp on tk.GROUP_ID = gp.GROUP_ID "
				+ "JOIN B2F_COMPANY co ON gp.COMPANY_ID = co.COMPANY_ID WHERE tk.TOKEN_ID = ? AND tk.DESCRIPTION = ?";
		if (!ignoreExpiration) {
			query += " AND (tk.EXPIRE_TIME IS NULL OR tk.EXPIRE_TIME > ?)";
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, token);
			prepStmt.setString(2, tokenDescription.toString());
			if (!ignoreExpiration) {
				prepStmt.setString(3, now);
			}
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				company = this.recordToCompany(rs);
			}
			this.addLog(token, "found: " + (company != null));
		} catch (Exception e) {
			this.addLog("getCompanyByToken", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return company;
	}

	public void addCompanyUrl(CompanyUrlDbObj url) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_COMPANY_URL (TABLE_ID, CREATE_DATE, ACTIVE, COMPANY_ID, URL_REGEX) VALUES (?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, url.getTableId());
				prepStmt.setTimestamp(2, url.getCreateDate());
				prepStmt.setBoolean(3, url.isActive());
				prepStmt.setString(4, url.getCompanyId());
				prepStmt.setString(5, url.getUrlRegex());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("addCompanyUrl", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void addCompany(CompanyDbObj company) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_COMPANY (" + companyFields + ") VALUES ("
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, company.getCompanyId());
				prepStmt.setString(2, company.getCompanyName());
				prepStmt.setBoolean(3, company.isActive());
				prepStmt.setString(4, company.getCompanySecret());
				prepStmt.setInt(5, company.getAcceptedTypes());
				prepStmt.setTimestamp(6, DateTimeUtilities.getCurrentTimestamp());
				prepStmt.setString(7, company.getCompanyLoginToken());
				prepStmt.setString(8, company.getCompanyCompletionUrl());
				prepStmt.setString(9, company.getApiKey());
				prepStmt.setInt(10, company.getLicenseCount());
				prepStmt.setInt(11, company.getLicensesInUse());
				prepStmt.setString(12, company.getCompanyBaseUrl());
				prepStmt.setString(13, company.getCompanyLoginUrl());
				prepStmt.setString(14, company.getF1Method().authMethodName());
				prepStmt.setString(15, company.getF2Method().authMethodName());
				prepStmt.setString(16, company.getLogoutUrl());
				prepStmt.setString(17, company.getUrlRegex());
				prepStmt.setString(18, company.getNonMemberStrategy().toString());
				prepStmt.setBoolean(19, company.isPushAllowed());
				prepStmt.setBoolean(20, company.isTextAllowed());
				prepStmt.setInt(21, company.getPushTimeoutSeconds());
				prepStmt.setInt(22, company.getTextTimeoutSeconds());
				prepStmt.setInt(23, company.getPasskeyTimeoutSeconds());
				prepStmt.setBoolean(24, company.isAllowAllFromIdp());
				prepStmt.setBoolean(25, company.isMoveB2fUsersToIdp());
				prepStmt.setString(26, company.getEmailDomain());
				prepStmt.setInt(27, company.getAdminCodeTimeoutSeconds());
				prepStmt.setString(28, company.getEntityIdVal());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("addCompany", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void updateCompanyUrl(CompanyUrlDbObj url) {
		new Thread(() -> {
			String query = "UPDATE B2F_COMPANY_URL SET ACTIVE = ?, COMPANY_ID = ?, REGEX_URL = ? WHERE TABLE_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, url.isActive());
				prepStmt.setString(2, url.getCompanyId());
				prepStmt.setString(3, url.getUrlRegex());
				prepStmt.setString(4, url.getTableId());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("updateCompanyUrl", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void updateCompany(CompanyDbObj company) {
		new Thread(() -> {
			String query = "UPDATE B2F_COMPANY SET COMPANY_NAME = ?, ACTIVE = ?, COMPANY_SECRET = ?, ACCEPTED_TYPES = ?, "
					+ "CREATE_DATE = ?, COMPANY_LOGIN_TOKEN = ?, COMPANY_COMPLETION_URL = ?, API_KEY = ?, LICENSE_COUNT = ?, "
					+ "LICENSES_IN_USE = ?, COMPANY_BASE_URL = ?, "
					+ "COMPANY_LOGIN_URL = ?, F1_METHOD = ?, F2_METHOD = ?, LOGOUT_URL = ?, URL_REGEX = ?,"
					+ "NON_MEMBER_STRATEGY = ?, PUSH_ALLOWED = ?, TEXT_ALLOWED = ?, "
					+ "PUSH_TIMEOUT_SECONDS = ?, TEXT_TIMEOUT_SECONDS = ?, PASSKEY_TIMEOUT_SECONDS = ?, "
					+ "ALLOW_ALL_FROM_IDP = ?, MOVE_B2F_USERS_TO_IDP = ?, EMAIL_DOMAIN = ?, ADMIN_CODE_TIMEOUT_SECONDS = ?, "
					+ "ENTITY_ID_VAL = ? WHERE COMPANY_ID = ?;";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);

				prepStmt.setString(1, company.getCompanyName());
				prepStmt.setBoolean(2, company.isActive());
				prepStmt.setString(3, company.getCompanySecret());
				prepStmt.setInt(4, company.getAcceptedTypes());
				prepStmt.setTimestamp(5, new java.sql.Timestamp(company.getCreateDate().getTime()));
				prepStmt.setString(6, company.getCompanyLoginToken());
				prepStmt.setString(7, company.getCompanyCompletionUrl());
				prepStmt.setString(8, company.getApiKey());
				prepStmt.setInt(9, company.getLicenseCount());
				prepStmt.setInt(10, company.getLicensesInUse());
				prepStmt.setString(11, company.getCompanyBaseUrl());
				prepStmt.setString(12, company.getCompanyLoginUrl());
				prepStmt.setString(13, company.getF1Method().authMethodName());
				prepStmt.setString(14, company.getF2Method().authMethodName());
				prepStmt.setString(15, company.getLogoutUrl());
				prepStmt.setString(16, company.getUrlRegex());
				prepStmt.setString(17, company.getNonMemberStrategy().toString());
				prepStmt.setBoolean(18, company.isPushAllowed());
				prepStmt.setBoolean(19, company.isTextAllowed());
				prepStmt.setInt(20, company.getPushTimeoutSeconds());
				prepStmt.setInt(21, company.getTextTimeoutSeconds());
				prepStmt.setInt(22, company.getPasskeyTimeoutSeconds());
				prepStmt.setBoolean(23, company.isAllowAllFromIdp());
				prepStmt.setBoolean(24, company.isMoveB2fUsersToIdp());
				prepStmt.setString(25, company.getEmailDomain());
				prepStmt.setInt(26, company.getAdminCodeTimeoutSeconds());
				prepStmt.setString(27, company.getEntityIdVal());
				prepStmt.setString(28, company.getCompanyId());
				logQueryImportant(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("updateCompany", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}
}

package com.humansarehuman.blue2factor.dataAndAccess;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.http.util.TextUtils;
import org.junit.Test;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.communication.PushNotifications;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.SecondContact;
import com.humansarehuman.blue2factor.entities.enums.AuthorizationMethod;
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.enums.CodeEnvironment;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.enums.NonMemberStrategy;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.AuthenticatorDbObj;
import com.humansarehuman.blue2factor.entities.tables.AuthorizationDbObj;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.ConnectionLogDbObj;
import com.humansarehuman.blue2factor.entities.tables.ConnectionRecordDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.MailingList;
import com.humansarehuman.blue2factor.entities.tables.ServerConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.ServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.SshConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.TestUserDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.Converters;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.jwt.JsonWebToken;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.AttestedCredentialDataConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.client.CollectedClientData;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

public class DataAccess extends DeviceFields {

	public boolean addSshConnection(SshConnectionDbObj ssh) {
		boolean added = false;
		String query = "INSERT INTO B2F_SSH_CONNECTION VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, ssh.getSshId());
			prepStmt.setString(2, ssh.getCompanyId());
			prepStmt.setString(3, ssh.getDeviceId());
			prepStmt.setString(4, ssh.getServerId());
			prepStmt.setString(5, ssh.getClientUser());
			prepStmt.setString(6, ssh.getServerUser());
			prepStmt.setTimestamp(7, ssh.getCreateDate());
			prepStmt.setTimestamp(8, ssh.getCompletionDate());
			prepStmt.setString(9, ssh.getLocalIpAddress());
			prepStmt.setString(10, ssh.getServerIpAddress());
			prepStmt.setInt(11, ssh.getClientOutcome());
			prepStmt.setInt(12, ssh.getServerOutcome());
			prepStmt.executeUpdate();
			added = true;
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return added;
	}

	public SshConnectionDbObj getMostRecentOpenSshConnection(String remoteUser, String remoteIp, String localUser,
			String localIp, String companyId) {
		SshConnectionDbObj ssh = null;
		String query = "SELECT * FROM B2F_SSH_CONNECTION WHERE COMPANY_ID = ? AND SERVER_USER = ? "
				+ "AND LOCAL_IP_ADDRESS = ? AND CLIENT_OUTCOME = ? AND SERVER_OUTCOME = ? ";
		if (!TextUtils.isBlank(remoteUser)) {
			query += "AND CLIENT_USER = ? ";
		}
		query += "ORDER BY CREATE_DATE DESC LIMIT 1";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			prepStmt.setString(2, localUser);
			prepStmt.setString(3, remoteIp);
			prepStmt.setInt(4, Outcomes.SUCCESS);
			prepStmt.setInt(5, Outcomes.UNKNOWN_STATUS);
			if (!TextUtils.isBlank(remoteUser)) {
				prepStmt.setString(6, remoteUser);
			}
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				ssh = recordToShhConnection(rs);
			}
		} catch (SQLException e) {
			this.addLog("getMostRecentOpenSshConnection", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return ssh;
	}

	public void updateSshConnection(SshConnectionDbObj ssh) {
		new Thread(() -> {
			String query = "UPDATE B2F_SSH_CONNECTION SET COMPANY_ID = ?, DEVICE_ID = ?, SERVER_ID = ?, "
					+ "CLIENT_USER = ?, SERVER_USER = ?, CREATE_DATE = ?, COMPLETION_DATE = ?, LOCAL_IP_ADDRESS = ?, "
					+ "SERVER_IP_ADDRESS= ?, CLIENT_OUTCOME = ?, " + "SERVER_OUTCOME = ? WHERE SSH_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();

				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, ssh.getCompanyId());
				prepStmt.setString(2, ssh.getDeviceId());
				prepStmt.setString(3, ssh.getServerId());
				prepStmt.setString(4, ssh.getClientUser());
				prepStmt.setString(5, ssh.getServerUser());
				prepStmt.setTimestamp(6, ssh.getCreateDate());
				prepStmt.setTimestamp(7, ssh.getCompletionDate());
				prepStmt.setString(8, ssh.getLocalIpAddress());
				prepStmt.setString(9, ssh.getServerIpAddress());
				prepStmt.setInt(10, ssh.getClientOutcome());
				prepStmt.setInt(11, ssh.getServerOutcome());
				prepStmt.setString(12, ssh.getSshId());
				logQuery("updateSshConnection", prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("updateSshConnection", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	private SshConnectionDbObj recordToShhConnection(ResultSet rs) throws SQLException {
		String sshId = rs.getString("SSH_ID");
		String companyId = rs.getString("COMPANY_ID");
		String deviceId = rs.getString("DEVICE_ID");
		String serverId = rs.getString("SERVER_ID");
		String clientUser = rs.getString("CLIENT_USER");
		String serverUser = rs.getString("SERVER_USER");
		Timestamp createDate = rs.getTimestamp("CREATE_DATE");
		Timestamp completionDate = null;
		try {
			completionDate = rs.getTimestamp("COMPLETION_DATE");
		} catch (Exception e) {
			this.addLog(e);
		}
		String localIpAddress = rs.getString("LOCAL_IP_ADDRESS");
		String serverIpAddress = rs.getString("SERVER_IP_ADDRESS");
		int clientOutcome = rs.getInt("CLIENT_OUTCOME");
		int serverOutcome = rs.getInt("SERVER_OUTCOME");
		return new SshConnectionDbObj(sshId, companyId, deviceId, serverId, clientUser, serverUser, createDate,
				completionDate, localIpAddress, serverIpAddress, clientOutcome, serverOutcome);
	}

	public boolean isTestUser(String email, boolean updateTestUser) {
		boolean tester = false;
		try {
			String emailSuffix = GeneralUtilities.getEmailDomain(email);
			String query = "SELECT * FROM B2F_TEST_USER WHERE (EMAIL = ? OR EMAIL = ?) "
					+ "AND ACTIVE= ? AND INSTALLS < INSTALL_LIMIT";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, email);
				prepStmt.setString(2, "*@" + emailSuffix);
				prepStmt.setBoolean(3, true);
				rs = executeImportantQuery(prepStmt);
				if (rs.next()) {
					TestUserDbObj testUser = recordToTestUser(rs);
					testUser.setLastCheck(DateTimeUtilities.getCurrentTimestamp());
					if (updateTestUser) {
						testUser.setInstalls(testUser.getInstalls() + 1);
					}
					updateTestUser(testUser);
					tester = true;
				}
			} catch (SQLException se) {
				this.addLog(se);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		} catch (Exception e) {
			addLog(e);
		}
		return tester;
	}

	public void updateTestUser(TestUserDbObj testUser) {
		String query = "UPDATE B2F_TEST_USER SET COMPANY_NAME = ?, ACTIVE = ?, EMAIL = ?, "
				+ "CREATE_DATE = ?, LAST_CHECK = ?, INSTALLS = ?, INSTALL_LIMIT = ? WHERE TEST_USER_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, testUser.getCompanyName());
			prepStmt.setBoolean(2, testUser.isActive());
			prepStmt.setString(3, testUser.getEmail());
			prepStmt.setTimestamp(4, testUser.getCreateDate());
			prepStmt.setTimestamp(5, testUser.getLastCheck());
			prepStmt.setInt(6, testUser.getInstalls());
			prepStmt.setInt(7, testUser.getInstallLimit());
			prepStmt.setString(8, testUser.getTestUserId());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	public TestUserDbObj recordToTestUser(ResultSet rs) {
		TestUserDbObj testUserDbObj = null;
		try {
			String testUserId = rs.getString("TEST_USER_ID");
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			boolean active = rs.getBoolean("ACTIVE");
			String email = rs.getString("EMAIL");
			String companyName = rs.getString("COMPANY_NAME");
			int installs = rs.getInt("INSTALLS");
			int installLimit = rs.getInt("INSTALL_LIMIT");
			Timestamp lastCheck = rs.getTimestamp("LAST_CHECK");
			testUserDbObj = new TestUserDbObj(testUserId, companyName, active, email, createDate, lastCheck, installs,
					installLimit);
		} catch (Exception e) {
			addLog("recordToMailingList", e);
		}
		return testUserDbObj;
	}

	public boolean addEmailToMailingList(String email, String name, boolean beta, String backend, String emailId) {
		boolean added = false;
		MailingList mailingList = getMailingListRecord(email);
		if (mailingList == null) {
			addLog("name: " + name, LogConstants.WARNING);
			String query = "INSERT INTO B2F_MAILING_LIST VALUES (?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, DateTimeUtilities.getCurrentTimestamp());
				prepStmt.setString(2, email);
				prepStmt.setString(3, name);
				prepStmt.setBoolean(4, true);
				prepStmt.setBoolean(5, beta);
				prepStmt.setString(6, backend);
				prepStmt.setString(7, emailId);
				logQueryImportant(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				added = true;
			} catch (SQLException e) {
				this.addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		} else {
			addLog(email + " is already in our table", LogConstants.WARNING);
			mailingList.setActive(true);
			mailingList.setEmailId(emailId);
			this.updateEmailListByEmail(mailingList);
		}
		return added;
	}

	public String getEmailById(String emailId) {
		String email = "";
		MailingList mailingList = getMailingListRecordById(emailId);
		if (mailingList != null) {
			email = mailingList.getEmail();
		}
		return email;
	}

	public void deactiveEmail(String emailId) {
		String query = "UPDATE B2F_MAILING_LIST SET ACTIVE = ? WHERE EMAIL_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, emailId);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	public MailingList getMailingListRecord(String email) {
		MailingList mailingList = null;
		String query = "SELECT * FROM B2F_MAILING_LIST WHERE EMAIL = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, email);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				mailingList = recordToMailingList(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getMailingListRecord", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return mailingList;
	}

	public MailingList getMailingListRecordById(String emailId) {
		MailingList mailingList = null;
		String query = "SELECT * FROM B2F_MAILING_LIST WHERE EMAIL_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, emailId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				mailingList = recordToMailingList(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getMailingListRecord", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return mailingList;
	}

	public ResultSet executeImportantQuery(PreparedStatement prepStmt) throws SQLException {
		logQueryImportant(getMethodNameInExecute(), prepStmt);
		return prepStmt.executeQuery();
	}

	public ResultSet executeQuery(PreparedStatement prepStmt) throws SQLException {
		logQuery(getMethodNameInExecute(), prepStmt);
		return prepStmt.executeQuery();
	}

	public boolean hasSessionBeenUsed(DeviceDbObj device, String session) {
		boolean usedBefore = true;
		TokenDbObj token = this.getToken(session);
		if (token == null) {
			// this is good
			this.addLog("hasSessionBeenUsed", "session has not been used");
			usedBefore = false;
		}
		return usedBefore;
	}

	public void addKey(KeyDbObj key) {
//		new Thread(() -> {
		String query = "INSERT INTO B2F_KEY VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, key.getKeyId());
			prepStmt.setString(2, key.getDeviceId());
			prepStmt.setString(3, key.getBrowserId());
			prepStmt.setString(4, key.getGroupId());
			prepStmt.setString(5, key.getCompanyId());
			prepStmt.setTimestamp(6, key.getCreateDate());
			prepStmt.setBoolean(7, key.isActive());
			prepStmt.setString(8, key.getKeyType().toString());
			prepStmt.setString(9, key.getKeyText());
			prepStmt.setBoolean(10, key.isPublicKey());
			prepStmt.setString(11, key.getAlgorithm());
			prepStmt.setString(12, key.getUrl());
			prepStmt.setTimestamp(13, key.getExpireDate());
			logQueryImportant(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog(key.getKeyId(), "addKey", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
//		}).start();
	}

	public ServerDbObj getServerByToken(String tokenId) {
		ServerDbObj server = null;
		if (!TextUtils.isEmpty(tokenId)) {
			String query = "SELECT server.* FROM B2F_SERVER server JOIN B2F_TOKEN token ON "
					+ "token.DEVICE_ID = server.SERVER_ID WHERE TOKEN_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, tokenId);
				rs = executeQuery(prepStmt);
				while (rs.next()) {
					server = recordToServer(rs, false);
					break;
				}
			} catch (SQLException e) {
				this.addLog("getServerByServerId", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (server == null) {
				addLog("getServerByServerId", "device is null", LogConstants.WARNING);
			}
		}
		return server;
	}

	public KeyDbObj getServerSshPublicKey(String serverId) {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY WHERE DEVICE_ID = ? AND KEY_TYPE = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, serverId);
			prepStmt.setString(2, KeyType.SERVER_SSH_PUBLIC_KEY.toString());
			prepStmt.setBoolean(3, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
			}
		} catch (SQLException e) {
			this.addLog("getServerSshPublicKey", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public BrowserDbObj getLastUsedBrowser(String deviceId) {
		BrowserDbObj browser = null;
		String query = "SELECT * FROM B2F_BROWSER WHERE DEVICE_ID = ? AND ACTIVE = ? ORDER BY "
				+ "LAST_UPDATE DESC LIMIT 1";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				browser = recordToBrowser(rs);
			}
		} catch (SQLException e) {
			this.addLog("getLastUsedBrowser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return browser;
	}

	public KeyDbObj getKeyByTypeAndBrowserTokenAndSite(KeyType keyType, String browserToken, String url) {
		KeyDbObj key = null;
		String query = "SELECT bk.* FROM B2F_KEY bk JOIN B2F_TOKEN token ON bk.BROWSER_ID = token.BROWSER_ID "
				+ " WHERE bk.KEY_TYPE = ? AND token.TOKEN_ID = ? AND bk.ACTIVE = ?  AND bk.URL = ?";
		// TODO: restore the url and fi

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, keyType.toString());
			prepStmt.setString(2, browserToken);
			prepStmt.setBoolean(3, true);
			prepStmt.setString(4, url);
			this.logQueryImportant("getKeyByTypeAndBrowserTokenAndSite", prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
			} else {
				this.addLog("getKeyByTypeAndBrowserTokenAndSite", "key not found");
			}
		} catch (SQLException e) {
			this.addLog("getKeyByTypeAndBrowserTokenAndSite", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public KeyDbObj getJwsSigningKey() {
		KeyDbObj key = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "SELECT * FROM B2F_KEY WHERE KEY_TYPE = ? AND ACTIVE = ? AND "
				+ "EXPIRE_DATE > ? AND PUBLIC_KEY = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, KeyType.JWT.toString());
			prepStmt.setBoolean(2, true);
			prepStmt.setTimestamp(3, now);
			prepStmt.setBoolean(4, false);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
				this.addLog("getJwsSigningKey", "keyFound");
			}
		} catch (SQLException e) {
			this.addLog("getJwsSigningKey", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public KeyDbObj getJustExpiredJwsPublicKey() {
		KeyDbObj key = null;
		Timestamp yesterday = DateTimeUtilities.getCurrentTimestampMinusHours(24);
		String query = "SELECT * FROM B2F_KEY WHERE KEY_TYPE = ? AND ACTIVE = ? AND EXPIRE_DATE > ? AND PUBLIC_KEY = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, KeyType.JWT.toString());
			prepStmt.setBoolean(2, false);
			prepStmt.setTimestamp(3, yesterday);
			prepStmt.setBoolean(4, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
			}
		} catch (SQLException e) {
			this.addLog("getKeyByTypeAndBrowser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public KeyDbObj getCurrentJwtPublicKey() {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY WHERE KEY_TYPE = ? AND ACTIVE = ? AND PUBLIC_KEY = ? ORDER BY EXPIRE_DATE DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, KeyType.JWT.toString());
			prepStmt.setBoolean(2, true);
			prepStmt.setBoolean(3, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
			}
			if (key.getExpireDate().before(DateTimeUtilities.getCurrentTimestamp())) {
				JsonWebToken jwt = new JsonWebToken();
				jwt.updateJwtKey();
				key = getCurrentJwtPublicKey();
			}
		} catch (Exception e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public KeyDbObj getKeyById(String keyId) {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY WHERE KEY_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, keyId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
			}
		} catch (Exception e) {
			this.addLog("getKeyById", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public KeyDbObj getKeyByTypeAndBrowser(KeyType keyType, BrowserDbObj browser) {
		return getKeyByTypeAndBrowser(keyType, browser, null);
	}

	public KeyDbObj getKeyByTypeAndBrowser(KeyType keyType, BrowserDbObj browser, String url) {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY WHERE BROWSER_ID = ? AND KEY_TYPE = ? AND ACTIVE = ?";
		if (url != null) {
			query += " AND URL = ?";
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, browser.getBrowserId());
			prepStmt.setString(2, keyType.toString());
			prepStmt.setBoolean(3, true);
			if (url != null) {
				prepStmt.setString(4, url);
			}
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				this.addLog("key Found");
				key = recordToKey(rs);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public KeyDbObj getKeyByTypeAndBrowserAndNotOurUrl(KeyType keyType, BrowserDbObj browser) {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY WHERE BROWSER_ID = ? AND KEY_TYPE = ? AND ACTIVE = ?";
		query += " AND URL != ?";

		this.addLog("SELECT * FROM B2F_KEY WHERE BROWSER_ID = '" + browser.getBrowserId() + "' AND KEY_TYPE = '"
				+ keyType + "' AND ACTIVE = true AND URL != '" + Urls.getUrlWithoutProtocol() + "';");
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, browser.getBrowserId());
			prepStmt.setString(2, keyType.toString());
			prepStmt.setBoolean(3, true);
			prepStmt.setString(4, Urls.getUrlWithoutProtocol());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public void deactivateOldJwts(String key1, String key2) {
//		new Thread(() -> {
		String query = "UPDATE B2F_KEY SET EXPIRE_DATE = ?, ACTIVE = ? WHERE KEY_TYPE = ? "
				+ "AND ACTIVE = ? AND KEY_ID NOT IN (?, ?)";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setTimestamp(1, now);
			prepStmt.setBoolean(2, false);
			prepStmt.setString(3, KeyType.JWT.toString());
			prepStmt.setBoolean(4, true);
			prepStmt.setString(5, key1);
			prepStmt.setString(6, key2);
			logQueryImportant("deactivateOldJwts", prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog("deactivateOldJwts", e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
//		}).start();
	}

	public void deactivateAllJwtsForDevice(String deviceId) {
		new Thread(() -> {
			String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE DEVICE_ID = ? AND DESCRIPTION = ?";
			Timestamp tenMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(10);
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, tenMinutesAgo);
				prepStmt.setString(2, deviceId);
				prepStmt.setString(3, TokenDescription.JWT.toString());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("deactivateAllJwtsForDevice", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	// should not be used for browser keys
	public void deactivateOldKeysByTypeAndDevice(KeyType keyType, String deviceId) {
//		new Thread(() -> {
		String query = "UPDATE B2F_KEY SET ACTIVE = ? WHERE KEY_TYPE = ? AND DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, keyType.toString());
			prepStmt.setString(3, deviceId);
			logQueryImportant("deactivateOldKeysByTypeAndDevice", prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog("deactivateOldKeysByTypeAndDevice", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
//		}).start();
	}

	public void deactivateKey(KeyDbObj key) {
//		new Thread(() -> {
		String query = "UPDATE B2F_KEY SET ACTIVE = ? WHERE KEY_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, key.getKeyId());
			logQueryImportant("deactivateKey", prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog("deactivateKey", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
//		}).start();
	}

	public void deactivateKeysByBrowserAndUrl(String browserId, String url, String notKey) {
//		new Thread(() -> {
		String query = "UPDATE B2F_KEY SET ACTIVE = ? WHERE BROWSER_ID = ? AND URL = ? AND KEY_ID != ?";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, browserId);
			prepStmt.setString(3, url);
			prepStmt.setString(4, notKey);
			logQueryImportant("deactivateKeysByBrowserAndUrl", prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog("deactivateKeysByBrowserAndUrl", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
//		}).start();
	}

	public KeyDbObj getClientSshSignatureKeyFromDeviceId(String deviceId) {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY bk JOIN B2F_GROUP bg on bk.COMPANY_ID = bg.COMPANY_ID "
				+ "JOIN B2F_DEVICE bd ON bg.GROUP_ID = bd.GROUP_ID WHERE bk.KEY_TYPE = ? AND "
				+ "bk.ACTIVE = ? AND bd.DEVICE_ID = ?";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			String keyType = KeyType.SERVER_SSH_PRIVATE_KEY.toString();
			prepStmt.setString(1, keyType);
			prepStmt.setBoolean(2, true);
			prepStmt.setString(3, deviceId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("getClientSshSignatureKey", "key found");
				key = recordToKey(rs);
			} else {
				addLog("getClientSshSignatureKey", "key not found");
			}
		} catch (SQLException e) {
			this.addLog("getClientSshSignatureKey", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public KeyDbObj getKeyByTypeAndServerId(KeyType keyType, String serverId) {
		return getKeyByTypeAndDeviceId(keyType, serverId, null);
	}

	public KeyDbObj createNewPrivateSshKey(ServerDbObj server) {
		KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();
		String privateKey = Encoders.BASE64.encode(keyPair.getPrivate().getEncoded());
		String algorithm = Jwts.SIG.RS256.getId();
		expireKeysByTypeAndDeviceId(KeyType.SERVER_SSH_PRIVATE_KEY, server.getServerId());
		KeyDbObj key = new KeyDbObj(GeneralUtilities.randomString(), server.getServerId(), null, null,
				server.getCompanyId(), KeyType.SERVER_SSH_PRIVATE_KEY, privateKey, false, algorithm, null,
				DateTimeUtilities.getCurrentTimestampPlusDays(3652));
		addKey(key);
		return key;
	}

	public KeyDbObj getCompanyServerPrivateSshKey(String companyId) {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY WHERE KEY_TYPE = ? AND COMPANY_ID = ? " + "AND ACTIVE = ?";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, KeyType.SERVER_SSH_PRIVATE_KEY.toString());
			prepStmt.setString(2, companyId);
			prepStmt.setBoolean(3, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public static String getMethodName() {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		return ste[2].getMethodName();
	}

	public static String getMethodNameInLogFn() {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		return ste[3].getMethodName();
	}

	public static String getMethodNameInExecute() {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		return ste[3].getMethodName();
	}

	public static String getCurrentStackTrace(int skip) {
		String trace = "stack trace unavailable";
		try {
			final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			trace = GeneralUtilities.stackTraceToString(ste, skip).replace(")", ")\n");
		} catch (Exception e) {

		}
		return trace;
	}

	public void logCurrentStackTrace(String deviceId, String caller) {
		new DataAccess().addLog(deviceId, caller, DataAccess.getCurrentStackTrace(3), LogConstants.IMPORTANT);
	}

	public void logQuery(String fn, PreparedStatement prepStmt) {
		if (LogConstants.LOG_QUERY) {
			String[] prepArray = prepStmt.toString().split("com.mysql.cj.jdbc.ClientPreparedStatement: ");
			if (prepArray.length >= 2) {
				addLog("", fn, prepArray[1] + ";", LogConstants.TRACE);
			} else {
				addLog("", fn, prepArray[0] + ";", LogConstants.TRACE);
			}
		}
	}

	public void logQueryImportant(String fn, PreparedStatement prepStmt) {
		if (LogConstants.LOG_QUERY) {
			String[] prepArray = prepStmt.toString().split("com.mysql.cj.jdbc.ClientPreparedStatement: ");
			if (prepArray.length >= 2) {
				addLog("", fn, prepArray[1] + ";", LogConstants.IMPORTANT);
			} else {
				addLog("", fn, prepArray[0] + ";", LogConstants.IMPORTANT);
			}
		}
	}

	public void logQueryImportant(String deviceId, String fn, PreparedStatement prepStmt) {
		if (LogConstants.LOG_QUERY) {
			String[] prepArray = prepStmt.toString().split("com.mysql.cj.jdbc.ClientPreparedStatement: ");
			if (prepArray.length >= 2) {
				addLog(deviceId, fn, prepArray[1] + ";", LogConstants.IMPORTANT);
			} else {
				addLog(deviceId, fn, prepArray[0] + ";", LogConstants.IMPORTANT);
			}
		}
	}

	public void logQuery(PreparedStatement prepStmt) {
		if (LogConstants.LOG_QUERY) {
			String[] prepArray = prepStmt.toString().split("com.mysql.cj.jdbc.ClientPreparedStatement: ");
			if (prepArray.length == 2) {
				String fn = getMethodNameInLogFn();
				addLog(fn, prepArray[1] + ";");
			}
		}
	}

	public KeyDbObj getCompanySshPrivateKey(String companyId) {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY WHERE KEY_TYPE = ? AND COMPANY_ID = ? " + "AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, KeyType.SERVER_SSH_PRIVATE_KEY.toString());
			prepStmt.setString(2, companyId);
			prepStmt.setBoolean(3, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
				try {
					String publicKey = new Encryption().getPublicKeyStringFromPrivate(key);
					this.addLog("publicKey should be: " + publicKey);
				} catch (NoSuchAlgorithmException e) {
					this.addLog(e);
				}
			}
		} catch (SQLException | InvalidKeySpecException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public ArrayList<DeviceDbObj> getSshTokenForUser(String username, String hostname, String companyId) {
		ArrayList<DeviceDbObj> devices = new ArrayList<>();
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "SELECT dev.* FROM B2F_DEVICE dev JOIN B2F_TOKEN tk ON dev.DEVICE_ID = tk.DEVICE_ID"
				+ " JOIN B2F_GROUP grp ON dev.GROUP_ID = grp.GROUP_ID JOIN B2F_SERVER svr ON grp.COMPANY_ID = svr.COMPANY_ID"
				+ " WHERE tk.EXPIRE_TIME > ? AND tk.DESCRIPTION = ? AND dev.ACTIVE = ? AND grp.UID = ? "
				+ " AND grp.COMPANY_ID = ?";// AND svr.HOSTNAME = ?";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setTimestamp(1, now);
			prepStmt.setString(2, TokenDescription.SSH.toString());
			prepStmt.setBoolean(3, true);
			prepStmt.setString(4, username);
			prepStmt.setString(5, companyId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				devices.add(recordToDevice(rs));
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return devices;
	}

	public KeyDbObj getCompanySshPrivateKey(CompanyDbObj company) {
		return getCompanySshPrivateKey(company.getCompanyId());
	}

	public void expireKeysByTypeAndDeviceId(KeyType keyType, String deviceId) {
		String query = "UPDATE B2F_KEY SET ACTIVE = ? WHERE KEY_TYPE = ? AND DEVICE_ID = ? AND ACTIVE = ?";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, keyType.toString());
			prepStmt.setString(3, deviceId);
			prepStmt.setBoolean(4, true);
			this.addLog("expireKeysStackTrace" + getMethodName(), 0);
			logQueryImportant("expireKeysByTypeAndDeviceId", prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	public void expireKeysByTypeAndCompanyId(KeyType keyType, String companyId) {
//		new Thread(() -> {
		String query = "UPDATE B2F_KEY SET ACTIVE = ? WHERE KEY_TYPE = ? AND COMPANY_ID = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, keyType.toString());
			prepStmt.setString(3, companyId);
			prepStmt.setBoolean(4, true);
			logQueryImportant("expireKeysByTypeAndCompanyId", prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
//		}).start();
	}

	public KeyDbObj getKeyByTypeAndDeviceId(KeyType keyType, String deviceId) {
		return getKeyByTypeAndDeviceId(keyType, deviceId, null);
	}

	public KeyDbObj getKeyByTypeAndDeviceId(KeyType keyType, String deviceId, String url) {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY WHERE KEY_TYPE = ? AND DEVICE_ID = ? AND ACTIVE = ?";
		if (url != null) {
			query += " AND URL = ?";
		}
		query += " ORDER BY CREATE_DATE DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, keyType.toString());
			prepStmt.setString(2, deviceId);
			prepStmt.setBoolean(3, true);
			if (url != null) {
				prepStmt.setString(4, url);
			}
			rs = executeQuery(prepStmt);
			this.logQueryImportant(deviceId, "getKeyByTypeAndDeviceId", prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
			} else {
				addLog(deviceId, "key not found for deviceId: " + deviceId + " and type: " + keyType.toString(),
						LogConstants.ERROR);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public KeyDbObj getKeyByTypeAndGroupId(KeyType keyType, String groupId) {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY WHERE KEY_TYPE = ? AND GROUP_ID = ? AND ACTIVE = ? ORDER BY CREATE_DATE DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, keyType.toString());
			prepStmt.setString(2, groupId);
			prepStmt.setBoolean(3, true);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public KeyDbObj getActiveKeyByTypeAndCompanyId(KeyType keyType, String companyId) {
		KeyDbObj key = null;
		String query = "SELECT * FROM B2F_KEY WHERE KEY_TYPE = ? AND COMPANY_ID = ? AND ACTIVE = ? ORDER BY CREATE_DATE DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, keyType.toString());
			prepStmt.setString(2, companyId);
			prepStmt.setBoolean(3, true);
			rs = prepStmt.executeQuery();
			logQuery(getMethodName(), prepStmt);
			if (rs.next()) {
				key = recordToKey(rs);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return key;
	}

	public KeyDbObj getB2fBrowserPublicKeyByBrowser(BrowserDbObj browser, String url) {
		return getKeyByTypeAndBrowser(KeyType.BROWSER_PUBLIC_KEY, browser, url);
	}

	public KeyDbObj getB2fBrowserPublicKeyByDevice(DeviceDbObj device, String url) {
		return getKeyByTypeAndDeviceId(KeyType.BROWSER_PUBLIC_KEY, device.getDeviceId(), url);
	}

	private KeyDbObj recordToKey(ResultSet rs) throws SQLException {
		String keyId = rs.getString("KEY_ID");
		String deviceId = rs.getString("DEVICE_ID");
		String browserId = rs.getString("BROWSER_ID");
		String groupId = rs.getString("GROUP_ID");
		String companyId = rs.getString("COMPANY_ID");
		Timestamp createDate = rs.getTimestamp("CREATE_DATE");
		boolean active = rs.getBoolean("ACTIVE");
		KeyType keyType = KeyType.valueOf(rs.getString("KEY_TYPE"));
		String keyText = rs.getString("KEY_TEXT");
		Boolean publicKey = rs.getBoolean("PUBLIC_KEY");
		String algorithm = rs.getString("ALGORITHM");
		String url = rs.getString("URL");
		Timestamp expireDate;
		try {
			expireDate = rs.getTimestamp("EXPIRE_DATE");
		} catch (Exception e) {
			this.addLog(e);
			expireDate = DateTimeUtilities.getCurrentTimestamp();
		}
		return new KeyDbObj(keyId, deviceId, browserId, groupId, companyId, createDate, active, keyType, keyText,
				publicKey, algorithm, url, expireDate);
	}

	// SELECT functions

//	public BasicResponse isServerConnectionAllowed(String token, String deviceId, String serverId) {
//		DeviceDbObj device = new DeviceDataAccess().getDeviceByDeviceId(deviceId);
//		ServerDbObj server = this.getServerByServerId(serverId);
//		BasicResponse basicResponse;
//		if (device.getLoginToken() != null && device.getTemp().equals("theendoftheworldasweknowit")) {
//			basicResponse = new BasicResponse(Outcomes.SUCCESS, "Blue2Factor bypassed.");
//		} else {
//			if (!server.isEnabled()) {
//				basicResponse = new BasicResponse(Outcomes.SUCCESS, "Blue2Factor has been disabled on this server.");
//			} else {
//				basicResponse = isAccessCodeValid(token, "isServerConnectionAllowed");
//				if (basicResponse.getOutcome() == Outcomes.SUCCESS) {
//					this.addLog(deviceId, "isServerConnectionAllowed", "access code was valid", LogConstants.DEBUG);
//					basicResponse = isServerConnectionSetup(device, server);
//				}
//			}
//		}
//		return basicResponse;
//	}

	/**
	 * This adds the server to all users of the company. This should be modified if
	 * we decide to look at different permissions for different users
	 * 
	 * @param server
	 */
	public void addServerForUsers(ServerDbObj server) {
		ArrayList<GroupDbObj> groups = new GroupDataAccess().getGroupsByCompanyId(server.getCompanyId());
		for (GroupDbObj group : groups) {
			this.addServerConnection(group, server);
		}
	}

	public boolean isServerConnectionSetupBool(DeviceDbObj device, ServerDbObj server) {
		return isServerConnectionSetup(device, server).getOutcome() == Outcomes.SUCCESS;
	}

	public boolean isServerConnectionSetupByIds(String deviceId, String serverId) {
		return isServerConnectionSetupById(deviceId, serverId).getOutcome() == Outcomes.SUCCESS;
	}

	public BasicResponse isServerConnectionSetupById(String deviceId, String serverId) {
		int outcome = Outcomes.FAILURE;
		String result = "";
		// if (device.getGroupId().equals(server.get))
		if (!TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(serverId)) {
			ServerConnectionDbObj serverConnection = this.getServerConnectionByDeviceAndServerIds(deviceId, serverId);
			if (serverConnection != null) {
				if (serverConnection.isActive()) {
					outcome = Outcomes.SUCCESS;
				} else {
					result = "You are no longer allowed to connect to this server.";
				}
			} else {
				result = "You do not have permission to connect to this server.";
			}
		} else {
			result = "the device or server was null";
		}
		return new BasicResponse(outcome, result);
	}

	public KeyDbObj getSshClientPublicKey(String deviceId) {
		return this.getKeyByTypeAndDeviceId(KeyType.TERMINAL_SSH_PUBLIC_KEY, deviceId);
	}

	public KeyDbObj getSshClientPrivateKey(String deviceId) {
		return this.getKeyByTypeAndDeviceId(KeyType.TERMINAL_SSH_PRIVATE_KEY, deviceId);
	}

	public void deactivateTerminalSshKeys(String deviceId) {
//		new Thread(() -> {
		String query = "UPDATE B2F_KEY SET EXPIRE_DATE = ?, ACTIVE = ? WHERE KEY_TYPE IN ( ?, ? ) " + "AND ACTIVE = ?";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setTimestamp(1, now);
			prepStmt.setBoolean(2, false);
			prepStmt.setString(3, KeyType.TERMINAL_SSH_PRIVATE_KEY.toString());
			prepStmt.setString(3, KeyType.TERMINAL_SSH_PUBLIC_KEY.toString());
			prepStmt.setBoolean(4, true);
			logQueryImportant("deactivateTerminalSshKeys", prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog("deactivateTerminalSshKeys", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
//		}).start();
	}

	public BasicResponse isServerConnectionSetup(DeviceDbObj device, ServerDbObj server) {
		int outcome = Outcomes.FAILURE;
		String result = "";
		// if (device.getGroupId().equals(server.get))
		if (device != null && server != null) {
			ServerConnectionDbObj serverConnection = this.getServerConnectionByDeviceAndServer(device, server);
			if (serverConnection != null) {
				if (serverConnection.isActive()) {
					outcome = Outcomes.SUCCESS;
				} else {
					result = "You are no longer allowed to connect to this server.";
				}
			} else {
				result = "You do not have permission to connect to this server.";
			}
		} else {
			result = "the device or server was null";
		}
		return new BasicResponse(outcome, result);
	}

	public ServerConnectionDbObj getServerConnectionByDeviceAndServer(DeviceDbObj device, ServerDbObj server) {
		ServerConnectionDbObj serverConnection = null;

		String query = "SELECT " + serverConnectionFields + " FROM B2F_SERVER_CONNECTION WHERE "
				+ "CLIENT_ID = ? AND SERVER_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, device.getDeviceId());
			prepStmt.setString(2, server.getServerId());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				serverConnection = recordToServerConnection(rs);
			}
		} catch (SQLException e) {
			this.addLog(device.getDeviceId(), "getServerConnectionByDeviceAndServer", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return serverConnection;
	}

	public ServerConnectionDbObj getServerConnectionByDeviceAndServerIds(String deviceId, String serverId) {
		ServerConnectionDbObj serverConnection = null;

		String query = "SELECT " + serverConnectionFields + " FROM B2F_SERVER_CONNECTION WHERE "
				+ "CLIENT_ID = ? AND SERVER_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setString(2, serverId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				serverConnection = recordToServerConnection(rs);
			}
		} catch (SQLException e) {
			this.addLog(deviceId, "getServerConnectionByDeviceAndServerIds", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return serverConnection;
	}

	public void addServer(ServerDbObj server) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_SERVER VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, server.getCreateDate());
				prepStmt.setString(2, server.getServerId());
				prepStmt.setString(3, server.getB2fId());
				prepStmt.setInt(4, server.getPermissions());
				prepStmt.setBoolean(5, server.isActive());
				prepStmt.setBoolean(6, server.isEnabled());
				prepStmt.setString(7, server.getServerName());
				prepStmt.setString(8, server.getCompanyId());
				prepStmt.setString(9, server.getHostname());
				prepStmt.setString(10, server.getDescription());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog(server.getServerId(), "addServer", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	private void addServerConnection(GroupDbObj group, ServerDbObj server) {
		ServerConnectionDbObj serverConn = new ServerConnectionDbObj(DateTimeUtilities.getCurrentTimestamp(),
				server.getServerId(), group.getGroupId(), 0, true);
		addServerConnection(serverConn);
	}

	private void addServerConnection(ServerConnectionDbObj serverConnection) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_SERVER_CONNECTION VALUES (?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, serverConnection.getCreateDate());
				prepStmt.setString(2, serverConnection.getServerId());
				prepStmt.setString(3, serverConnection.getClientId());
				prepStmt.setInt(4, serverConnection.getPermissions());
				prepStmt.setBoolean(5, serverConnection.isActive());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("addServerConnection", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

//	private BasicResponse isAccessCodeValid(String accessCodeStr, String caller) {
//		int outcome = Outcomes.FAILURE;
//		String reason = "";
//		AccessCodeDbObj accessCode = this.getAccessCodeFromAccessString(accessCodeStr);
//		if (accessCode != null) {
//			if (accessCode.isActive()) {
//				Timestamp thirtySecondsAgo = DateTimeUtilities.getCurrentTimestampMinusSeconds(30);
//				if (accessCode.getCreateDate().after(thirtySecondsAgo)) {
//					outcome = Outcomes.SUCCESS;
//					accessCode.setActive(false, caller);
//					this.updateAccessCode(accessCode);
//				} else {
//					reason = "The access code is no longer active, please rerun your command in Blue2Factor.";
//				}
//			} else {
//				reason = "The access code is expired, please rerun your command in Blue2Factor.";
//			}
//		} else {
//			reason = "This server must be accessed by rerunning the Blue2Factor command.";
//		}
//		return new BasicResponse(outcome, reason);
//	}

	public AccessCodeDbObj getAccessCodeFromAccessString(String accessCodeString) {
		AccessCodeDbObj accessCode = null;
		String query = "SELECT * FROM B2F_ACCESS_STRINGS WHERE ACCESS_CODE = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, accessCodeString);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				accessCode = this.recordToAccessCode(rs);
				addLog("getAccessCodeFromAccessString", "access code was found");
			} else {
				addLog("getAccessCodeFromAccessString", "access code not found");
			}
		} catch (SQLException e) {
			addLog("getAccessCodeFromAccessString", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return accessCode;
	}

	public CheckDbObj getExistingCheck(DeviceConnectionDbObj connection) {
		CheckDbObj check = null;
		String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_DEVICE_ID = ? "
				+ "AND PERIPHERAL_DEVICE_ID = ? AND EXPIRED = ? AND COMPLETED = ? AND CREATE_DATE > ? "
				+ "ORDER BY CREATE_DATE DESC";
		Timestamp ts3MinutesAgo = DateTimeUtilities.getCurrentTimestampMinusSeconds(3 * 60);
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connection.getCentralDeviceId());
			prepStmt.setString(2, connection.getPeripheralDeviceId());
			prepStmt.setBoolean(3, false);
			prepStmt.setBoolean(4, false);
			prepStmt.setTimestamp(5, ts3MinutesAgo);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				check = recordToCheck(rs);
			}
		} catch (SQLException e) {
			this.addLog(connection.getCentralDeviceId(), "getExistingCheck", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public CheckDbObj getServerCheckForSsh(String clientId, String clientString, String serverStr) {
		CheckDbObj check = null;
		String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_INSTANCE_ID = ? "
				+ "AND PERIPHERAL_INSTANCE_ID = ? AND PERIPHERAL_DEVICE_ID = ? AND CHECK_TYPE = ? "
				+ "AND COMPLETED = ? AND EXPIRED = ? ORDER BY CREATE_DATE DESC LIMIT 1";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, serverStr);
			prepStmt.setString(2, clientString);
			prepStmt.setString(3, clientId);
			prepStmt.setString(4, CheckType.SSH_CONN.checkTypeName());
			prepStmt.setBoolean(5, false);
			prepStmt.setBoolean(6, false);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				check = recordToCheck(rs);
			}
		} catch (SQLException e) {
			this.addLog(clientId, "getServerCheckForSsh", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public CheckDbObj getCheckByDeviceInstanceIds(String centralInstanceId, String peripheralInstanceId) {
		CheckDbObj check = null;
		String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_INSTANCE_ID = ? " + "AND PERIPHERAL_INSTANCE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, centralInstanceId);
			prepStmt.setString(2, peripheralInstanceId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				check = recordToCheck(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public class CheckInstance {

		private String encryptedCentralVal;
		private String encryptedPeripheralVal;
		private String instanceId;

		public CheckInstance(String encryptedCentralVal, String encryptedPeripheralVal, String instanceId) {
			super();
			this.encryptedCentralVal = encryptedCentralVal;
			this.encryptedPeripheralVal = encryptedPeripheralVal;
			this.instanceId = instanceId;
		}

		public String getEncryptedCentralVal() {
			return encryptedCentralVal;
		}

		public void setEncryptedCentralVal(String encryptedCentralVal) {
			this.encryptedCentralVal = encryptedCentralVal;
		}

		public String getEncryptedPeripheralVal() {
			return encryptedPeripheralVal;
		}

		public void setEncryptedPeripheralVal(String encryptedPeripheralVal) {
			this.encryptedPeripheralVal = encryptedPeripheralVal;
		}

		public String getInstanceId() {
			return instanceId;
		}

		public void setInstanceId(String instanceId) {
			this.instanceId = instanceId;
		}

	}

	public CheckInstance getMostRecentCheckInstanceByDevices(DeviceDbObj centralDevice, DeviceDbObj peripheralDevice,
			DeviceDataAccess dataAccess) {
		CheckInstance checkInstance = null;
		CheckDbObj check = null;
		String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_DEVICE_ID = ? AND PERIPHERAL_DEVICE_ID = ? "
				+ "AND EXPIRED = ? AND COMPLETED + ? ORDER BY CREATE_DATE DESC LIMIT 1";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, centralDevice.getDeviceId());
			prepStmt.setString(2, peripheralDevice.getDeviceId());
			prepStmt.setBoolean(3, false);
			prepStmt.setInt(4, Outcomes.INCOMPLETE);
			rs = executeQuery(prepStmt);
			logQuery("getMostRecentCheckInstanceByDevices", prepStmt);
			if (rs.next()) {
				check = recordToCheck(rs);
				if (!check.isCompleted() && !check.isExpired()) {
					long secondsAgo = DateTimeUtilities.timestampSecondAgo(check.getCreateDate());
					if (secondsAgo < 60) {
						dataAccess.addLog(peripheralDevice.getDeviceId(),
								"We will NOT create a new check because last one was created " + secondsAgo
										+ " seconds ago at "
										+ DateTimeUtilities.timestampToReadableTime(check.getCreateDate()),
								LogConstants.TRACE);
						Encryption encryption = new Encryption();
						String encryptedCentral = encryption.encryptStringWithPublicKey(centralDevice,
								check.getCentralInstanceId());
						String encryptedPeripheral = encryption.encryptStringWithPublicKey(peripheralDevice,
								check.getPeripheralInstanceId());
						checkInstance = new CheckInstance(encryptedCentral, encryptedPeripheral, check.getInstanceId());
					} else {
						dataAccess.addLog(peripheralDevice.getDeviceId(),
								"We will create a new check because last one was created " + secondsAgo
										+ " seconds ago at "
										+ DateTimeUtilities.timestampToReadableTime(check.getCreateDate()),
								LogConstants.TRACE);
					}

				} else {
					if (check.isCompleted()) {
						dataAccess.addLog(peripheralDevice.getDeviceId(),
								"most recent check was completed at " + check.getCompletionDate(),
								LogConstants.IMPORTANT);
					} else {
						dataAccess.addLog(peripheralDevice.getDeviceId(), "most recent check was expired",
								LogConstants.IMPORTANT);
					}
				}
			} else {
				dataAccess.addLog(peripheralDevice.getDeviceId(), "This is the first check for this device",
						LogConstants.WARNING);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return checkInstance;
	}

	public boolean shouldReuseCheck(CheckDbObj check) {
		boolean reuse = false;
		if (check != null) {
			if (!check.isCompleted() && !check.isExpired()) {
				if (DateTimeUtilities.getCurrentTimestampMinusSeconds(60).before(check.getCreateDate())) {
					reuse = true;
				}
			}
		}
		return reuse;
	}

	public CheckDbObj getCheckByCentralInstanceAndPeripheralDeviceIds(String centralInstanceId, String peripheralId) {
		CheckDbObj check = null;
		String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_INSTANCE_ID = ? " + "AND PERIPHERAL_DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, centralInstanceId);
			prepStmt.setString(2, peripheralId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				check = recordToCheck(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public CheckDbObj getLastPushCheckForCentral(String centralDeviceId) {
		CheckDbObj check = null;
		String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_DEVICE_ID = ? AND "
				+ "CHECK_TYPE = ? ORDER BY CREATE_DATE DESC LIMIT 1";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, centralDeviceId);
			prepStmt.setString(2, CheckType.PUSH.checkTypeName());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				check = recordToCheck(rs);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public CheckDbObj getActiveCheckByDeviceIds(String centralId, String peripheralId) {
		CheckDbObj check = null;
		String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_INSTANCE_ID = ? "
				+ "AND PERIPHERAL_INSTANCE_ID = ? AND EXPIRED = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, centralId);
			prepStmt.setString(2, peripheralId);
			prepStmt.setBoolean(3, false);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				check = recordToCheck(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(centralId, "getActiveCheckByDeviceIds", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public ArrayList<CheckDbObj> getAllChecksByDeviceIds(String centralId, String peripheralId) {
		ArrayList<CheckDbObj> checks = new ArrayList<>();
		CheckDbObj check = null;
		String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_INSTANCE_ID = ? " + "AND PERIPHERAL_INSTANCE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, centralId);
			prepStmt.setString(2, peripheralId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				check = recordToCheck(rs);
				checks.add(check);
			}
		} catch (SQLException e) {
			this.addLog(centralId, "getAllCheckByDeviceIds", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return checks;
	}

//    public String getServiceUuid(DeviceDbObj device) {
//        String serviceUuid = null;
//        ArrayList<DeviceConnectionDbObj> connections = new DeviceConnectionDataAccess()
//                .getAllConnectionsForDevice(device);
//        if (connections != null && connections.size() > 0) {
//            serviceUuid = connections.get(0).getServiceUuid();
//        }
//        return serviceUuid;
//    }

	public ServerDbObj getServerByB2fId(String b2fId) {
		ServerDbObj server = null;
		if (!TextUtils.isEmpty(b2fId)) {
			String query = "SELECT * FROM B2F_SERVER WHERE B2F_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, b2fId);
				rs = executeQuery(prepStmt);
				while (rs.next()) {
					server = recordToServer(rs, false);
					break;
				}
			} catch (SQLException e) {
				addLog("getServerByB2fId", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (server == null) {
				addLog(b2fId, "getServerByB2fId", "device is null", LogConstants.WARNING);
			}
		}
		return server;
	}

	// the code is the first 8 chars in the serverId
	public ServerDbObj getServerByCode(String serverCode) {
		ServerDbObj server = null;
		if (!TextUtils.isEmpty(serverCode)) {
			String query = "SELECT " + serverFields + " FROM B2F_SERVER WHERE SUBSTRING(SERVER_ID,1,8) = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, serverCode);
				rs = executeQuery(prepStmt);
				while (rs.next()) {
					server = recordToServer(rs, false);
					break;
				}
			} catch (SQLException e) {
				addLog("getServerByCode", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (server == null) {
				addLog(serverCode, "getServerByCode", "device is null", LogConstants.WARNING);
			}
		}
		return server;
	}

	public ServerDbObj getActiveServerByName(String serverName, String companyId) {
		ServerDbObj server = null;
		String query = "SELECT * FROM B2F_SERVER WHERE SERVER_NAME = ? AND COMPANY_ID = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, serverName);
			prepStmt.setString(2, companyId);
			prepStmt.setBoolean(3, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				server = recordToServer(rs, false);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getActiveServerByName", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		if (server == null) {
			addLog("getActiveServerByName", "server is null (which is probably good)");
		}
		return server;
	}

	public ServerDbObj getServerByServerId(String serverId) {
		ServerDbObj server = null;
		if (!TextUtils.isEmpty(serverId)) {
			String query = "SELECT " + serverFields + " FROM B2F_SERVER WHERE SERVER_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, serverId);
				rs = executeQuery(prepStmt);
				while (rs.next()) {
					server = recordToServer(rs, false);
					break;
				}
			} catch (SQLException e) {
				this.addLog(serverId, "getServerByServerId", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (server == null) {
				addLog(serverId, "getServerByServerId", "device is null", LogConstants.WARNING);
			}
		}
		return server;
	}

	public ServerDbObj getActiveServerByServerId(String serverId) {
		ServerDbObj server = null;
		if (!TextUtils.isEmpty(serverId)) {
			String query = "SELECT " + serverFields + " FROM B2F_SERVER WHERE SERVER_ID = ? AND ACTIVE = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, serverId);
				prepStmt.setBoolean(2, true);
				rs = executeQuery(prepStmt);
				while (rs.next()) {
					server = recordToServer(rs, false);
					break;
				}
			} catch (SQLException e) {
				this.addLog(serverId, "getActiveServerByServerId", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (server == null) {
				addLog(serverId, "getActiveServerByServerId", "server is null", LogConstants.WARNING);
			}
		}
		return server;
	}

	public ArrayList<ServerDbObj> getActiveServersByCompanyId(String companyId) {
		return this.getActiveServersByCompanyId(companyId, false);
	}

	public ArrayList<ServerDbObj> getActiveServersByCompanyId(String companyId, boolean truncateIds) {
		ArrayList<ServerDbObj> servers = new ArrayList<>();

		if (!TextUtils.isEmpty(companyId)) {
			String query = "SELECT " + serverFields + " FROM B2F_SERVER WHERE COMPANY_ID = ? AND ACTIVE = ? "
					+ "AND ENABLED = ?";
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
				while (rs.next()) {
					servers.add(recordToServer(rs, truncateIds));
				}
			} catch (SQLException e) {
				addLog(companyId, "getActiveServersByCompanyId", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
			if (servers.size() == 0) {
				addLog(companyId, "getActiveServersByCompanyId", "servers are null", LogConstants.WARNING);
			}
		}
		return servers;
	}

	public DeviceDbObj getActiveDeviceByMachineId(String machineId) {
		DeviceDbObj device = null;
		if (!machineId.equals("")) {
			String query = "SELECT * FROM B2F_DEVICE WHERE LOGIN_TOKEN = ? AND ACTIVE = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, machineId);
				prepStmt.setBoolean(2, true);
				rs = executeImportantQuery(prepStmt);
				while (rs.next()) {
					device = recordToDevice(rs);
					break;
				}
			} catch (SQLException e) {
				this.addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}
		return device;
	}

	public DeviceDbObj getDeviceByMachineId(String machineId) {
		DeviceDbObj device = null;
		if (!machineId.equals("")) {
			String query = "SELECT * FROM B2F_DEVICE WHERE LOGIN_TOKEN = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, machineId);
				rs = executeQuery(prepStmt);
				while (rs.next()) {
					device = recordToDevice(rs);
					break;
				}
			} catch (SQLException e) {
				addLog("getDeviceByMachineId", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}
		return device;
	}

	public void rescindAuth(DeviceDbObj device) {
		new Thread(() -> {
			String query = "UPDATE B2F_AUTHORIZATION SET AUTHORIZATION_COMPLETED = ? WHERE " + "REQUESTING_DEVICE = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, true);
				prepStmt.setString(2, device.getDeviceId());
				logQuery(getMethodName(), prepStmt);
				if (prepStmt.executeUpdate() > 0) {
					this.addLog(device.getDeviceId(), "authorization recinded");
				}
			} catch (Exception e) {
				this.addLog(device.getDeviceId(), "rescindAuth", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	/**
	 * Now minus the time range (maybe 30 minutes)
	 * 
	 * @return
	 */
	protected Timestamp getAuthTimeLimit() {
		int MINUTES_AGO = 3;
		return new Timestamp(System.currentTimeMillis() - MINUTES_AGO * 60 * 1000);
	}

	public boolean isInCurrentCheckWithoutIpAddress(SecondContact secondContact, boolean fromCentral) {
		return isInCurrentCheckWithoutIpAddress(secondContact.getCentralDevice(), secondContact.getPeripheralDevice(),
				secondContact.getInstanceId(), secondContact.getSsid(), secondContact.getIpAddress(),
				secondContact.getGmtOffset(), fromCentral);
	}

	public boolean updateLatestCheckAsSuccessful(DeviceConnectionDbObj connection) {
		boolean success = false;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_DEVICE_ID = ? "
				+ "AND PERIPHERAL_DEVICE_ID = ? AND EXPIRED = ? ORDER BY CREATE_DATE DESC LIMIT 1";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			CheckDbObj checkRow = null;
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, connection.getCentralDeviceId());
			prepStmt.setString(2, connection.getPeripheralDeviceId());
			prepStmt.setBoolean(3, false);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				checkRow = this.recordToCheck(rs);
				checkRow.setCompleted(true);
				checkRow.setCompletionDate(DateTimeUtilities.getCurrentTimestampMinusSeconds(10));
				checkRow.setVerfiedReceipt(true);
				checkRow.setOutcome(Outcomes.SUCCESS);
				this.updateCheck(checkRow);
				dataAccess.addConnectionLogIfNeeded(checkRow, true, checkRow.getCentralDeviceId(),
						"updateLatestCheckAsSuccessful", ConnectionType.PROX);
				success = true;
			}

		} catch (SQLException e) {
			addLog(connection.getCentralDeviceId(), "updateLatestCheck", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		addLog(connection.getCentralDeviceId(), "success: " + success);
		return success;
	}

	private boolean isInCurrentCheckWithoutIpAddress(DeviceDbObj centralDevice, DeviceDbObj peripheralDevice,
			String instanceId, String ssid, String bssId, int gmtOffset, boolean fromCentral) {
		boolean success = false;

		String query = ("SELECT * FROM B2F_CHECK WHERE INSTANCE_ID = ? AND CENTRAL_DEVICE_ID = ?"
				+ " AND PERIPHERAL_DEVICE_ID = ? AND EXPIRED = ?");

		CheckDbObj checkRow = null;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, instanceId);
				prepStmt.setString(2, centralDevice.getDeviceId());
				prepStmt.setString(3, peripheralDevice.getDeviceId());
				prepStmt.setBoolean(4, false);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					checkRow = this.recordToCheck(rs);
				}
			} catch (SQLException e) {
				addLog(centralDevice.getDeviceId(), e);
			}
			if (checkRow != null) {
				addLog(centralDevice.getDeviceId(), "check exists");
				if (fromCentral) {
					success = this.updateSuccessfulCheckFromCentral(checkRow, centralDevice, peripheralDevice, ssid,
							bssId, gmtOffset);
				} else {
					success = this.updateSuccessfulCheckFromPeripheral(checkRow, centralDevice, peripheralDevice, ssid,
							bssId, gmtOffset);
				}
			} else {
				addLog(centralDevice.getDeviceId(), "check row was null for instanceId: " + instanceId + ", senderId: "
						+ centralDevice.getDeviceId() + ", recipientId: " + peripheralDevice.getDeviceId());
			}
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public void resetRecentPushes() {
		new Thread(() -> {
			String query = "UPDATE B2F_DEVICE SET RECENT_PUSHES = 0";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				this.addLog("We just performed the daily push count reset", LogConstants.IMPORTANT);
			} catch (SQLException e) {
				addLog("resetRecentPushes", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	/*
	 * "CREATE_DATE, CONNECTION_ID, PERIPHERAL_DEVICE_ID, CENTRAL_DEVICE_ID, " +
	 * "SERVICE_UUID, CHARACTER_UUID, GROUP_ID, ACTIVE, LAST_CHECK, LAST_SUCCESS, "
	 * +
	 * "ADVERTISING_PERIPHERAL, CONNECTING_CENTRAL, SUBSCRIBED, INSTALL_COMPLETE, "
	 * + "PERIPHERAL_CONNECTED, LAST_PERIPHERAL_CONNECTION_SUCCESS," +
	 * "CENTRAL_CONNECTED, LAST_CENTRAL_CONNECTION_SUCCESS";
	 */
	protected DeviceConnectionDbObj recordToConnection(ResultSet rs) {
		DeviceConnectionDbObj connection = null;
		String connectionId = "";
		try {
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			connectionId = rs.getString("CONNECTION_ID");
			String peripheralDeviceId = rs.getString("PERIPHERAL_DEVICE_ID");
			String centralDeviceId = rs.getString("CENTRAL_DEVICE_ID");
			String serviceUuid = rs.getString("SERVICE_UUID");
			String characterUuid = rs.getString("CHARACTER_UUID");
			String groupId = rs.getString("GROUP_ID");
			boolean active = rs.getBoolean("ACTIVE");
			this.addLog("recordToConnection", "active: " + active);
			Timestamp lastCheck = rs.getTimestamp("LAST_CHECK");
			Timestamp lastSuccess = rs.getTimestamp("LAST_SUCCESS");
			Timestamp advertisingPeripheral = rs.getTimestamp("ADVERTISING_PERIPHERAL");
			Timestamp connectingCentral = rs.getTimestamp("CONNECTING_CENTRAL");
			boolean subscribed = rs.getBoolean("SUBSCRIBED");
			boolean installComplete = rs.getBoolean("INSTALL_COMPLETE");
			boolean peripheralConnected = rs.getBoolean("PERIPHERAL_CONNECTED");
			Timestamp lastPeripheralConnectionSuccess = rs.getTimestamp("LAST_PERIPHERAL_CONNECTION_SUCCESS");
			Boolean centralConnected = rs.getBoolean("CENTRAL_CONNECTED");
			Timestamp lastCentralConnectionSuccess = rs.getTimestamp("LAST_CENTRAL_CONNECTION_SUCCESS");
			Boolean needsPairing = rs.getBoolean("NEEDS_PAIRING");
			Timestamp lastSubscribed;
			try {
				lastSubscribed = rs.getTimestamp("LAST_SUBSCRIBED");
			} catch (Exception e) {
				lastSubscribed = DateTimeUtilities.getBaseTimestamp();
			}
			String centralOsId = rs.getString("CENTRAL_OS_ID");
			Integer peripheralRssi = rs.getInt("PERIPHERAL_RSSI");
			Integer centralRssi = rs.getInt("CENTRAL_RSSI");
			Timestamp peripheralRssiTimestamp = rs.getTimestamp("PERIPHERAL_RSSI_TIMESTAMP");
			Timestamp centralRssiTimestamp = rs.getTimestamp("CENTRAL_RSSI_TIMESTAMP");
			String peripheralIdentifier = rs.getString("PERIPHERAL_IDENTIFIER");
			connection = new DeviceConnectionDbObj(createDate, connectionId, peripheralDeviceId, centralDeviceId,
					serviceUuid, characterUuid, groupId, active, lastCheck, lastSuccess, peripheralConnected,
					lastPeripheralConnectionSuccess, centralConnected, lastCentralConnectionSuccess,
					advertisingPeripheral, connectingCentral, subscribed, lastSubscribed, installComplete, centralOsId,
					peripheralRssi, centralRssi, peripheralRssiTimestamp, centralRssiTimestamp, needsPairing,
					peripheralIdentifier);
		} catch (SQLException e) {
			this.addLog(connectionId, "recordToConnection", e);
		}
		return connection;
	}

	protected ConnectionRecordDbObj recordToConnectionRecord(ResultSet rs) {
		ConnectionRecordDbObj connRecord = null;
		try {
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			String connectionId = rs.getString("CONNECTION_ID");
			boolean connected = rs.getBoolean("CONNECTED");
			String src = rs.getString("SRC");
			connRecord = new ConnectionRecordDbObj(createDate, connectionId, connected, src);
		} catch (Exception e) {
			addLog("recordToConnectionRecord", e);
		}
		return connRecord;
	}

	protected DeviceDbObj recordToDevice(ResultSet rs) {
		return recordToDevice(rs, "");
	}

	protected DeviceDbObj recordToDevice(ResultSet rs, String src) {
		DeviceDbObj device = null;
		try {
			String groupId = rs.getString("GROUP_ID");
			String userId = rs.getString("USER_ID");
			String deviceId = rs.getString("DEVICE_ID");
			int seed = rs.getInt("SEED");
			boolean active = rs.getBoolean("ACTIVE");
			String fcmId = rs.getString("FCM_ID");
			Date createDate = rs.getTimestamp("CREATE_DATE");
			Timestamp lastUpdate;
			try {
				lastUpdate = rs.getTimestamp("LAST_UPDATE");
			} catch (Exception e1) {
				lastUpdate = DateTimeUtilities.getCurrentTimestamp();
			}
			String deviceType = rs.getString("DEVICE_TYPE");
			OsClass operatingSystem;
			try {
				operatingSystem = OsClass.valueOf(rs.getString("OPERATING_SYSTEM").toUpperCase());
			} catch (Exception e) {
				operatingSystem = OsClass.UNKNOWN;
				this.addLog("recordToDevice", "illegal os class: '" + rs.getString("OPERATING_SYSTEM") + ";");
			}
			String osVersion = rs.getString("OS_VERSION");
			String loginToken = rs.getString("LOGIN_TOKEN");

			String btAddress = rs.getString("BT_ADDRESS");
			Date lastCompleteCheck = rs.getTimestamp("LAST_COMPLETE_CHECK");
			Integer lastGmtOffset = rs.getInt("LAST_GMT_OFFSET");
			String userLanguage = rs.getString("USER_LANGUAGE");
			String screenSize = rs.getString("SCREEN_SIZE");

			String rnd = rs.getString("RAND");
			boolean showIcon = rs.getBoolean("SHOW_ICON");
			Double devicePriority = rs.getDouble("DEVICE_PRIORITY");
			boolean triggerUpdate = rs.getBoolean("TRIGGER_UPDATE");
			int recentPushes = rs.getInt("RECENT_PUSHES");
			boolean unresponsive = rs.getBoolean("UNRESPONSIVE");
			Timestamp lastPush = rs.getTimestamp("LAST_PUSH");
			boolean pushNow = rs.getBoolean("PUSH_LOUD");
			boolean pushFailure = rs.getBoolean("PUSH_FAILURE");
			String command = rs.getString("COMMAND");
			String temp = rs.getString("TEMP");
			boolean central = rs.getBoolean("CENTRAL");
			addLog("recordToDevice", "central? " + central + " for deviceId: " + deviceId + " from " + src);
			Timestamp lastReset = rs.getTimestamp("LAST_RESET");
			boolean turnedOff = rs.getBoolean("TURNED_OFF");
			String devClass = rs.getString("DEVICE_CLASS");
			DeviceClass deviceClass = DeviceClass.UNKNOWN;
			if (devClass != null) {
				deviceClass = DeviceClass.valueOf(rs.getString("DEVICE_CLASS").toUpperCase());
			}
			if (lastReset == null) {
				lastReset = DateTimeUtilities.getBaseTimestamp();
			}
			boolean hasBle = rs.getBoolean("HAS_BLE");
			boolean screensaverOn = rs.getBoolean("SCREENSAVER_ON");
			Timestamp lastVariableRetrieval = rs.getTimestamp("LAST_VARIABLE_RETRIEVAL");
			boolean browserInstallComplete = rs.getBoolean("BROWSER_INSTALL_COMPLETE");
			boolean signedIn = rs.getBoolean("SIGNED_IN");
			Timestamp lastSilentPush = rs.getTimestamp("LAST_SILENT_PUSH");
			Timestamp lastSilentPushResponse = null;
			try {
				lastSilentPushResponse = rs.getTimestamp("LAST_SILENT_PUSH_RESPONSE");
			} catch (Exception e) {
				// wtf
			}
			boolean turnOffFromInstaller = rs.getBoolean("TURN_OFF_FROM_INSTALLER");
			String phoneNumber = rs.getString("PHONE_NUMBER");
			boolean multiUser = rs.getBoolean("MULTI_USER");
			boolean passkeyEnabled = rs.getBoolean("PASSKEY_ENABLED");
			Integer txPower = rs.getInt("TX_POWER");
			boolean terminate = rs.getBoolean("TERMINATE");
			addLog("recordToDevice", "HAS_BLE: " + hasBle);
			device = new DeviceDbObj(groupId, userId, deviceId, seed, active, fcmId, btAddress, createDate, lastUpdate,
					deviceType, operatingSystem, loginToken, lastCompleteCheck, lastGmtOffset, osVersion, userLanguage,
					screenSize, rnd, showIcon, devicePriority, triggerUpdate, recentPushes, unresponsive, lastPush,
					pushNow, pushFailure, command, temp, central, lastReset, screensaverOn, lastVariableRetrieval,
					turnedOff, deviceClass, browserInstallComplete, signedIn, lastSilentPush, lastSilentPushResponse,
					hasBle, turnOffFromInstaller, phoneNumber, multiUser, passkeyEnabled, txPower, terminate);
		} catch (SQLException e) {
			addLog("recordToDevice", e);
		}
		return device;
	}

	public KeyDbObj createServerPrivateSshKeyForCompanyIfNeeded(String companyId) {
		KeyDbObj serverKey = getCompanyServerPrivateSshKey(companyId);
		if (serverKey == null) {
			serverKey = createServerPrivateSshKeyForCompany(companyId);
		}
		return serverKey;
	}

	public KeyDbObj createServerPrivateSshKeyForCompany(String companyId) {
		KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();
		String privateKey = Encoders.BASE64.encode(keyPair.getPrivate().getEncoded());
		String algorithm = Jwts.SIG.RS256.getId();
		KeyDbObj key = new KeyDbObj(GeneralUtilities.randomString(), null, null, null, companyId,
				KeyType.SERVER_SSH_PRIVATE_KEY, privateKey, false, algorithm, null,
				DateTimeUtilities.getCurrentTimestampPlusDays(3652));
		return key;
	}

	public boolean updateServer(ServerDbObj server) {
		boolean success = false;
		String query = "UPDATE B2F_SERVER SET B2F_ID = ?, PERMISSIONS = ?, ACTIVE = ?, ENABLED = ?, SERVER_NAME = ?, "
				+ "COMPANY_ID = ?, HOSTNAME = ?, DESCRIPTION = ?";
		query += " WHERE SERVER_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, server.getB2fId());
			prepStmt.setInt(2, server.getPermissions());
			prepStmt.setBoolean(3, server.isActive());
			prepStmt.setBoolean(4, server.isEnabled());

			prepStmt.setString(5, server.getServerName());
			prepStmt.setString(6, server.getCompanyId());
			prepStmt.setString(7, server.getHostname());
			prepStmt.setString(8, server.getDescription());
			prepStmt.setString(9, server.getServerId());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			this.addLog(server.getServerId(), "updateServer", "Server updated", LogConstants.DEBUG);
			success = true;
		} catch (SQLException e) {
			this.addLog(server.getServerId(), "updateServer", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public void deleteOldRecords() {
		int oneDay = 60 * 60 * 24;
		int sevenDays = 7 * oneDay;
		deleteLogsOlderThan(sevenDays);
		deleteCheckRecordsOlderThan(sevenDays);
		deleteTokenRecordsOlderThan(oneDay);
	}

	private void deleteCheckRecordsOlderThan(int secondsAgo) {
		String query = "DELETE FROM B2F_CHECK WHERE EXPIRATION_DATE < NOW() - INTERVAL ? SECOND";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setInt(1, secondsAgo);
			logQuery(getMethodName(), prepStmt);
			int rows = prepStmt.executeUpdate();
			this.addLog(rows + " rows were deleted from B2F_CHECK", LogConstants.IMPORTANT);
		} catch (SQLException e) {
			this.addLog("deleteCheckRecordsOlderThan", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	private void deleteTokenRecordsOlderThan(int secondsAgo) {
		String query = "DELETE FROM B2F_TOKEN WHERE EXPIRE_TIME < NOW() - INTERVAL ? SECOND";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setInt(1, secondsAgo);
			logQuery(getMethodName(), prepStmt);
			int rows = prepStmt.executeUpdate();
			this.addLog(rows + " rows were deleted from B2F_TOKEN", LogConstants.IMPORTANT);
		} catch (SQLException e) {
			this.addLog("deleteTokenRecordsOlderThan", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	private void deleteLogsOlderThan(int secondsAgo) {
		String query = "DELETE FROM B2F_LOG WHERE CREATE_DATE < NOW() - INTERVAL ? SECOND";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setInt(1, secondsAgo);
			logQuery(getMethodName(), prepStmt);
			int rows = prepStmt.executeUpdate();
			this.addLog(rows + " rows were deleted from B2F_LOG", LogConstants.IMPORTANT);
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	public AccessCodeDbObj addAccessCode(String deviceId, String caller) {
		return addAccessCode(deviceId, GeneralUtilities.randomString(), caller);
	}

	public AccessCodeDbObj addAccessCodeWithBrowserId(String deviceId, String browserId, String caller) {
		return addAccessCodeWithBrowserId(deviceId, GeneralUtilities.randomString(), browserId, caller);
	}

	public AccessCodeDbObj addAccessCode(String deviceId, String accessCodeStr, String caller) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
		String coId = "";
		if (company != null) {
			coId = company.getCompanyId();
		}
		addLog("accessCode: " + accessCodeStr);
		AccessCodeDbObj accessCode = new AccessCodeDbObj(DateTimeUtilities.getCurrentTimestamp(), accessCodeStr, coId,
				"", deviceId, 0, true, false);
		this.addAccessCode(accessCode, caller);
		return accessCode;
	}

	public AccessCodeDbObj addAccessCodeWithBrowserId(String deviceId, String accessCodeStr, String browserId,
			String caller) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
		String coId = "";
		if (company != null) {
			coId = company.getCompanyId();
		}
		AccessCodeDbObj accessCode = new AccessCodeDbObj(accessCodeStr, coId, "", deviceId, 0, true, browserId, false);
		this.addAccessCode(accessCode, caller);
		return accessCode;
	}

	public boolean updateAccessCode(AccessCodeDbObj accessCode) {
		boolean success = false;
		String query = "UPDATE B2F_ACCESS_STRINGS SET SERVER_ID = ?, DEVICE_ID = ?, PERMISSIONS = ?, ACTIVE = ?, "
				+ "BROWSER_ID = ?, COMPANY_ID = ?, ONE_TIME_ACCESS = ?";
		query += " WHERE ACCESS_CODE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, accessCode.getServerId());
			prepStmt.setString(2, accessCode.getDeviceId());
			prepStmt.setInt(3, accessCode.getPermissions());
			prepStmt.setBoolean(4, accessCode.isActive());
			prepStmt.setString(5, accessCode.getBrowserId());
			prepStmt.setString(6, accessCode.getCompanyId());
			prepStmt.setBoolean(7, accessCode.isOneTimeAccess());
			prepStmt.setString(8, accessCode.getAccessCode());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			this.addLog(accessCode.getDeviceId(), prepStmt.toString());
			success = true;
		} catch (SQLException e) {
			this.addLog(accessCode.getDeviceId(), e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public boolean updateEmailList(MailingList mailing) {
		boolean success = false;
		String query = "UPDATE B2F_MAILING LIST SET EMAIL = ?, NAME = ?, ACTIVE = ?, "
				+ "BETA = ?, BACKEND = ? WHERE EMAIL_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, mailing.getEmail());
			prepStmt.setString(2, mailing.getName());
			prepStmt.setBoolean(3, mailing.getActive());
			prepStmt.setBoolean(4, mailing.getBeta());
			prepStmt.setString(5, mailing.getBackend());
			prepStmt.setString(5, mailing.getEmailId());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public boolean updateEmailListByEmail(MailingList mailing) {
		boolean success = false;
		String query = "UPDATE B2F_MAILING_LIST SET EMAIL_ID = ?, NAME = ?, ACTIVE = ?, "
				+ "BETA = ?, BACKEND = ? WHERE EMAIL = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, mailing.getEmailId());
			prepStmt.setString(2, mailing.getName());
			prepStmt.setBoolean(3, mailing.getActive());
			prepStmt.setBoolean(4, mailing.getBeta());
			prepStmt.setString(5, mailing.getBackend());
			prepStmt.setString(6, mailing.getEmail());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	private AccessCodeDbObj recordToAccessCode(ResultSet rs) {
		AccessCodeDbObj accessCode = null;
		try {

			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			String accessCodeString = rs.getString("ACCESS_CODE");
			String companyId = rs.getString("COMPANY_ID");
			String serverId = rs.getString("SERVER_ID");
			String deviceId = rs.getString("DEVICE_ID");
			int permissions = rs.getInt("PERMISSIONS");
			boolean active = rs.getBoolean("ACTIVE");
			String browserId = rs.getString("BROWSER_ID");
			addLog("active: " + active);
			accessCode = new AccessCodeDbObj(createDate, accessCodeString, companyId, serverId, deviceId, permissions,
					active, browserId, false);
		} catch (Exception e) {
			addLog("recordToAccessCode", e);
		}
		return accessCode;
	}

	protected GroupDbObj recordToGroup(ResultSet rs) {
		GroupDbObj group = null;
		try {
			String companyId = rs.getString("COMPANY_ID");
			String groupId = rs.getString("GROUP_ID");
			String groupName = rs.getString("GROUP_NAME");
			boolean active = rs.getBoolean("ACTIVE");
			int acceptedTypes = rs.getInt("ACCEPTED_TYPES");
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			int timeoutSecs = rs.getInt("TIMEOUT_SECS");
			String groupPw = rs.getString("GROUP_PW");
			String salt = rs.getString("SALT");
			int licensesAllowed = rs.getInt("DEVICES_ALLOWED");
			int licensesInUse = rs.getInt("DEVICES_IN_USE");
			int permissions = rs.getInt("PERMISSIONS");
			String username = rs.getString("USERNAME");
			Timestamp tokenDate = rs.getTimestamp("TOKEN_DATE");
			String uid = rs.getString("UID");
			String userTypeStr = rs.getString("USER_TYPE");
			boolean userExempt = rs.getBoolean("USER_EXEMPT");
			boolean pushAllowed = rs.getBoolean("PUSH_ALLOWED");
			boolean textAllowed = rs.getBoolean("TEXT_ALLOWED");
			this.addLog("userRole: " + userTypeStr);
			group = new GroupDbObj(companyId, groupId, groupName, acceptedTypes, active, createDate, timeoutSecs,
					groupPw, salt, licensesAllowed, licensesInUse, permissions, username, tokenDate, uid,
					Converters.stringToUserType(userTypeStr, "recordToGroup"), userExempt, pushAllowed, textAllowed);
		} catch (SQLException e) {
			addLog(e);
		}
		return group;
	}

	protected CompanyDbObj recordToCompany(ResultSet rs) {
		CompanyDbObj company = null;
		try {
			String companyId = rs.getString("COMPANY_ID");
			String companyName = rs.getString("COMPANY_NAME");
			boolean active = rs.getBoolean("ACTIVE");
			String companySecret = rs.getString("COMPANY_SECRET");
			int acceptedTypes = rs.getInt("ACCEPTED_TYPES");
			Date createDate = rs.getTimestamp("CREATE_DATE");
			String companyLoginToken = rs.getString("COMPANY_LOGIN_TOKEN");
			String companyCompletionUrl = rs.getString("COMPANY_COMPLETION_URL");
			String apiKey = rs.getString("API_KEY");
			int licenseCount = rs.getInt("LICENSE_COUNT");
			int licensesInUse = rs.getInt("LICENSES_IN_USE");
			String companyBaseUrl = rs.getString("COMPANY_BASE_URL");
			String companyLoginUrl = rs.getString("COMPANY_LOGIN_URL");
			String f1Method = rs.getString("F1_METHOD");
			String f2Method = rs.getString("F2_METHOD");
			String logoutUrl = rs.getString("LOGOUT_URL");
			String urlRegex = rs.getString("URL_REGEX");
			NonMemberStrategy nonMemberStrategy;
			try {
				nonMemberStrategy = NonMemberStrategy.valueOf(rs.getString("NON_MEMBER_STRATEGY"));
			} catch (Exception e) {
				nonMemberStrategy = NonMemberStrategy.ALLOW_AUTHENTICATED_ONLY;
			}
			boolean pushAllowed = rs.getBoolean("PUSH_ALLOWED");
			boolean textAllowed = rs.getBoolean("TEXT_ALLOWED");
			int pushSeconds = rs.getInt("PUSH_TIMEOUT_SECONDS");
			int textSeconds = rs.getInt("TEXT_TIMEOUT_SECONDS");
			int passkeyTimeout = rs.getInt("PASSKEY_TIMEOUT_SECONDS");
			boolean allowAllFromIdp = rs.getBoolean("ALLOW_ALL_FROM_IDP");
			boolean moveB2fUsersToIdp = rs.getBoolean("MOVE_B2F_USERS_TO_IDP");
			String emailDomain = rs.getString("EMAIL_DOMAIN");
			int adminCodeTimeout = rs.getInt("ADMIN_CODE_TIMEOUT_SECONDS");
			String entityIdVal = rs.getString("ENTITY_ID_VAL");
			company = new CompanyDbObj(companyId, companyName, companySecret, acceptedTypes, active, createDate,
					companyLoginToken, companyCompletionUrl, apiKey, licenseCount, licensesInUse, companyBaseUrl,
					companyLoginUrl, stringToAuthMethod(f1Method), stringToAuthMethod(f2Method), logoutUrl, urlRegex,
					nonMemberStrategy, pushAllowed, textAllowed, pushSeconds, textSeconds, passkeyTimeout,
					allowAllFromIdp, moveB2fUsersToIdp, emailDomain, adminCodeTimeout, entityIdVal);
			addLog("recordToCompany", "company created: " + (company != null));
		} catch (SQLException e) {
			addLog("recordToCompany", e);
		}
		return company;
	}

	public AuthorizationMethod stringToAuthMethod(String f1MethodStr) {
		AuthorizationMethod f1Method = AuthorizationMethod.NONE;
		switch (f1MethodStr) {
		case "saml":
			f1Method = AuthorizationMethod.SAML;
			break;
		case "api":
			f1Method = AuthorizationMethod.API;
			break;
		case "openid":
			f1Method = AuthorizationMethod.OPEN_ID_CONNECT;
			break;
		case "iframe":
			f1Method = AuthorizationMethod.I_FRAME;
			break;
		case "ldap":
			f1Method = AuthorizationMethod.LDAP;
		}
		return f1Method;
	}

	private ServerConnectionDbObj recordToServerConnection(ResultSet rs) {
		ServerConnectionDbObj serverConnection = null;
		try {
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			String serverId = rs.getString("SERVER_ID");
			String clientId = rs.getString("CLIENT_ID");
			int permissions = rs.getInt("PERMISSIONS");
			boolean active = rs.getBoolean("ACTIVE");
			serverConnection = new ServerConnectionDbObj(createDate, serverId, clientId, permissions, active);
		} catch (Exception e) {
			addLog("recordToServerConnection", e);
		}
		return serverConnection;
	}

	protected MailingList recordToMailingList(ResultSet rs) {
		MailingList mailingList = null;
		try {
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			String email = rs.getString("EMAIL");
			String name = rs.getString("NAME");
			boolean active = rs.getBoolean("ACTIVE");
			boolean beta = rs.getBoolean("BETA");
			String backend = rs.getString("BACKEND");
			String emailId = rs.getString("EMAIL_ID");
			mailingList = new MailingList(createDate, email, name, active, beta, backend, emailId);
		} catch (Exception e) {
			addLog("recordToMailingList", e);
		}
		return mailingList;

	}

	public String truncateId(String serverId) {
		String newId = serverId.replaceAll("-", "");
		return newId.substring(0, 8);
	}

	protected ServerDbObj recordToServer(ResultSet rs, boolean truncateIds) {
		ServerDbObj server = null;
		try {
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			String serverId = rs.getString("SERVER_ID");
			if (truncateIds) {
				serverId = truncateId(serverId);
			}
			String deviceId = rs.getString("B2F_ID");
			int permissions = rs.getInt("PERMISSIONS");
			boolean active = rs.getBoolean("ACTIVE");
			boolean enabled = rs.getBoolean("ENABLED");
			String serverName = rs.getString("SERVER_NAME");
			String companyId = rs.getString("COMPANY_ID");
			String hostname = rs.getString("HOSTNAME");
			String description = rs.getString("DESCRIPTION");
			server = new ServerDbObj(createDate, serverId, deviceId, permissions, active, enabled, serverName,
					companyId, hostname, description);
		} catch (Exception e) {
			addLog("recordToServer", e);
		}
		return server;
	}

//    public void pushIfStale(DeviceConnectionDbObj connection) {
//        if (DateTimeUtilities.timeDifferenceInSecondsFromNow(connection.getLastSuccess()) > 60
//                * 10) {
//            PushNotifications push = new PushNotifications();
//            push.sendPushByDeviceId(connection.getCentralDeviceId(), false, "pushStaleConnections");
//        }
//    }

	public int pushStalePeripherals() {
		int pushCount = 0;
		String query = "SELECT dev.* FROM B2F_DEVICE dev JOIN B2F_DEVICE_CONNECTION con "
				+ "ON dev.DEVICE_ID = con.PERIPHERAL_DEVICE_ID WHERE DEVICE_CLASS = ? AND dev.ACTIVE = ? AND "
				+ "(con.CURRENTLY_CONNECTED = ? OR con.BLUETOOTH_CONNECTED = ?) AND "
				+ "(con.LAST_SUCCESS < ? OR con.LAST_BLUETOOTH_SUCCESS < ?) AND "
				+ "(con.LAST_SUCCESS > ? OR con.LAST_BLUETOOTH_SUCCESS > ?)";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			Timestamp mostRecentTime = DateTimeUtilities.getCurrentTimestampMinusSeconds(18 * 60);
			Timestamp oldestTime = DateTimeUtilities.getCurrentTimestampMinusSeconds(20 * 60);
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, DeviceClass.TABLET.toString());
			prepStmt.setBoolean(2, true);
			prepStmt.setBoolean(3, true);
			prepStmt.setBoolean(4, true);
			prepStmt.setTimestamp(5, mostRecentTime);
			prepStmt.setTimestamp(6, mostRecentTime);
			prepStmt.setTimestamp(7, oldestTime);
			prepStmt.setTimestamp(8, oldestTime);
			rs = executeQuery(prepStmt);
			DeviceDbObj device;
			PushNotifications push = new PushNotifications();
			while (rs.next()) {
				device = this.recordToDevice(rs);

				if (!TextUtils.isEmpty(device.getFcmId())) {
					push.sendPushToDevice(device);
				}
				pushCount++;
			}
		} catch (Exception e) {
			this.addLog("pushStalePeripherals", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return pushCount;
	}

	public int pushStaleCentrals() {
		String query = "SELECT * FROM B2F_DEVICE_CONNECTION WHERE (CURRENTLY_CONNECTED = ? OR "
				+ "BLUETOOTH_CONNECTED = ?) AND ACTIVE = ? AND (LAST_SUCCESS < ? OR LAST_BLUETOOTH_SUCCESS < ?) AND "
				+ "(LAST_SUCCESS > ? OR LAST_BLUETOOTH_SUCCESS > ?)";
		int pushCount = 0;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			this.addLog("pushStaleCentrals", "called");
			Timestamp mostRecentTime = DateTimeUtilities.getCurrentTimestampMinusSeconds(18 * 60);
			Timestamp oldestTime = DateTimeUtilities.getCurrentTimestampMinusSeconds(20 * 60);
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setBoolean(2, true);
			prepStmt.setBoolean(3, true);
			prepStmt.setTimestamp(4, mostRecentTime);
			prepStmt.setTimestamp(5, mostRecentTime);
			prepStmt.setTimestamp(6, oldestTime);
			prepStmt.setTimestamp(7, oldestTime);
			rs = executeQuery(prepStmt);
			DeviceConnectionDbObj connection;
			PushNotifications push = new PushNotifications();
			while (rs.next()) {
				connection = this.recordToConnection(rs);
				push.sendPushByDeviceId(connection.getCentralDeviceId(), false, "pushStaleCentrals", true, false);
				pushCount++;
			}
		} catch (Exception e) {
			this.addLog("pushStaleCentrals", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return pushCount;
	}

	protected CheckDbObj recordToCheck(ResultSet rs) {
		CheckDbObj check = null;
		String centralDeviceId = "";
		try {
			String checkId = rs.getString("CHECK_ID");
			String instanceId = rs.getString("INSTANCE_ID");
			centralDeviceId = rs.getString("CENTRAL_DEVICE_ID");
			String peripheralDeviceId = rs.getString("PERIPHERAL_DEVICE_ID");
			String charUuid = rs.getString("SERVICE_UUID");
			String userId = rs.getString("USER_ID");
			String senderBssId = rs.getString("CENTRAL_BSSID");
			String senderSsid = rs.getString("CENTRAL_SSID");
			String receiverBssid = rs.getString("PERIPHERAL_BSSID");
			String receiverSsid = rs.getString("PERIPHERAL_SSID");
			boolean expired = rs.getBoolean("EXPIRED");
			boolean completed = rs.getBoolean("COMPLETED");
			int outcome = rs.getInt("OUTCOME");
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			Timestamp completionDate = rs.getTimestamp("COMPLETION_DATE");
			boolean verifiedReceipt = rs.getBoolean("VERIFIED_RECEIPT");
			String checkStr = rs.getString("CHECK_TYPE");
			if (checkStr.equalsIgnoreCase("polling")) {
				checkStr = CheckType.PROX.checkTypeName().toLowerCase();
			}
			addLog(centralDeviceId, "recordToCheck: " + checkStr);
			CheckType checkType;
			if (checkStr.equals(CheckType.SSHPUSH.checkTypeName().toLowerCase())) {
				checkType = CheckType.SSHPUSH;
			} else if (checkStr.equals(CheckType.CONNECTION_FROM_PERIPHERAL.checkTypeName().toLowerCase())) {
				checkType = CheckType.CONNECTION_FROM_PERIPHERAL;
			} else if (checkStr.equalsIgnoreCase(CheckType.ADMIN_CODE.checkTypeName())) {
				checkType = CheckType.ADMIN_CODE;
			} else {
				checkType = CheckType.valueOf(checkStr.toUpperCase());
			}
			String centralInstanceId = rs.getString("CENTRAL_INSTANCE_ID");
			String peripheralInstanceId = rs.getString("PERIPHERAL_INSTANCE_ID");
			Timestamp expirationDate = rs.getTimestamp("EXPIRATION_DATE");

			check = new CheckDbObj(checkId, instanceId, centralDeviceId, peripheralDeviceId, charUuid, userId,
					senderBssId, senderSsid, receiverBssid, receiverSsid, expired, completed, outcome, createDate,
					completionDate, verifiedReceipt, checkType, centralInstanceId, peripheralInstanceId,
					expirationDate);
		} catch (SQLException e) {
			this.addLog(centralDeviceId, "recordToCheck", e);
		}
		return check;
	}

	public CheckDbObj getCheckByInstance(String instanceId) {
		CheckDbObj check = null;
		String query = ("SELECT * FROM B2F_CHECK WHERE INSTANCE_ID=?");
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, instanceId);
			rs = prepStmt.executeQuery();
			if (rs.next()) {
				check = recordToCheck(rs);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public CheckDbObj getCheckByCentralInstance(String instanceId) {
		CheckDbObj check = null;
		String query = ("SELECT * FROM B2F_CHECK WHERE CENTRAL_INSTANCE_ID=?");
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, instanceId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				check = recordToCheck(rs);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public CheckDbObj getCheckByPeripheralAndCode(String perfId, String code) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		CheckDbObj check = null;
		String query = ("SELECT * FROM B2F_CHECK WHERE PERIPHERAL_DEVICE_ID = ? "
				+ "AND CENTRAL_INSTANCE_ID = ? AND CHECK_TYPE = ? AND COMPLETED = ? AND CREATE_DATE > ? "
				+ "AND EXPIRED = ? AND EXPIRATION_DATE > ?");
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, perfId);
			prepStmt.setString(2, code);
			prepStmt.setString(3, CheckType.TXT.toString());
			prepStmt.setBoolean(4, false);
			Timestamp threeMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(3);
			prepStmt.setTimestamp(5, threeMinutesAgo);
			prepStmt.setBoolean(6, false);
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			prepStmt.setTimestamp(7, now);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				check = recordToCheck(rs);
				check.setCompleted(true);
				check.setCompletionDate(DateTimeUtilities.getCurrentTimestamp());
				check.setOutcome(Outcomes.SUCCESS);
				this.updateCheck(check);
				dataAccess.addConnectionLogIfNeeded(check, true, check.getCentralDeviceId(),
						"getCheckByPeripheralAndCode", ConnectionType.TXT);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public CheckDbObj getCheckByDeviceIdAndAdminCode(DeviceDbObj device, String code) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		CheckDbObj check = null;
		String query = ("SELECT * FROM B2F_CHECK WHERE CENTRAL_INSTANCE_ID = ? AND CHECK_TYPE = ? AND EXPIRATION_DATE > ? AND COMPLETED = ?");
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, code);
			prepStmt.setString(2, CheckType.ADMIN_CODE.checkTypeName());
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			prepStmt.setTimestamp(3, now);
			prepStmt.setBoolean(4, false);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				check = recordToCheck(rs);
				check.setCompleted(true);
				if (device.isCentral()) {
					check.setCentralDeviceId(device.getDeviceId());
				} else {
					check.setPeripheralDeviceId(device.getDeviceId());
				}
				check.setCompletionDate(now);
				check.setExpirationDate(DateTimeUtilities
						.getCurrentTimestampMinusSeconds(Constants.ADMIN_CODE_DURATION_AFTER_SUBMITION_SECONDS));
				check.setOutcome(Outcomes.SUCCESS);
				this.updateCheck(check);
				dataAccess.addConnectionLogIfNeeded(check, true, check.getCentralDeviceId(),
						"getCheckByDeviceIdAndAdminCode", ConnectionType.ADMIN_CODE);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public CheckDbObj getActivePushCheckByCentralInstance(String instanceId) {
		CheckDbObj check = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "SELECT * FROM B2F_CHECK WHERE CENTRAL_INSTANCE_ID = ? AND EXPIRATION_DATE >= ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, instanceId);
			prepStmt.setTimestamp(2, now);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				check = recordToCheck(rs);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	public void expirePushSuccessesByPeripheral(String peripheralId) {
		new Thread(() -> {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			String query = ("UPDATE B2F_CHECK SET EXPIRED = ? WHERE PERIPHERAL_DEVICE_ID = ? AND "
					+ "CHECK_TYPE IN (?, ?) AND OUTCOME = ? AND EXPIRATION_DATE > ?");
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, true);
				prepStmt.setString(2, peripheralId);
				prepStmt.setString(3, CheckType.PUSH.name());
				prepStmt.setString(4, CheckType.SSHPUSH.checkTypeName().toLowerCase());
				prepStmt.setInt(5, 0);
				prepStmt.setTimestamp(6, now);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public CheckDbObj getPushCheckByCentralInstance(String centralInstanceId) {
		CheckDbObj check = null;
		String query = ("SELECT * FROM B2F_CHECK WHERE CENTRAL_INSTANCE_ID = ? "
				+ "AND CREATE_DATE > ? AND EXPIRED = ? AND COMPLETED = ? AND CHECK_TYPE IN (?, ?)");
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, centralInstanceId);
			Timestamp createDate = DateTimeUtilities.getCurrentTimestampMinusSeconds(601);
			prepStmt.setTimestamp(2, createDate);
			prepStmt.setBoolean(3, false);
			prepStmt.setBoolean(4, false);
			prepStmt.setString(5, CheckType.PUSH.checkTypeName().toLowerCase());
			prepStmt.setString(6, CheckType.SSHPUSH.checkTypeName().toLowerCase());
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("check exist for " + centralInstanceId);
				check = recordToCheck(rs);
			} else {
				addLog("check not found for " + centralInstanceId);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return check;
	}

	// INSERT AND UPDATE FUNCTIONS

	private boolean updateSuccessfulCheckFromPeripheral(CheckDbObj checkRow, DeviceDbObj centralDevice,
			DeviceDbObj peripheralDevice, String ssid, String bssId, int gmtOffset) {
		boolean success = false;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			if (checkRow != null) {
				this.addLog(peripheralDevice.getDeviceId(), "check row was NOT null");
				checkRow.setCompleted(true);
				checkRow.setCompletionDate(DateTimeUtilities.getCurrentTimestamp());
				checkRow.setOutcome(Outcomes.SUCCESS);
				checkRow.setPeripheralSsid(ssid);
				checkRow.setPeripheralBssid(bssId);
				checkRow.setVerfiedReceipt(true);
				this.updateCheck(checkRow);
				dataAccess.updateDeviceSuccesses(peripheralDevice, centralDevice, false);
				dataAccess.addConnectionLogIfNeeded(checkRow, true, checkRow.getPeripheralDeviceId(),
						"updateSuccessfulCheckFromPeripheral", ConnectionType.PROX);
				success = true;
			} else {
				this.addLog(peripheralDevice.getDeviceId(), "check row WAS null");
			}
		} catch (Exception e) {
			this.addLog(centralDevice.getDeviceId(), "updateSuccessfulCheckFromPeripheral", e);
		}
		return success;
	}

	private boolean updateSuccessfulCheckFromCentral(CheckDbObj checkRow, DeviceDbObj centralDevice,
			DeviceDbObj peripheralDevice, String ssid, String bssId, int gmtOffset) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		boolean success = false;
		try {
			if (checkRow != null) {
				this.addLog(centralDevice.getDeviceId(), "check row was NOT null");
				checkRow.setCompleted(true);
				checkRow.setCompletionDate(DateTimeUtilities.getCurrentTimestamp());
				checkRow.setOutcome(Outcomes.SUCCESS);
				checkRow.setCentralSsid(removeQuotes(ssid));
				checkRow.setCentralBssid(bssId);
				checkRow.setVerfiedReceipt(true);
				this.updateCheck(checkRow);
				dataAccess.updateDeviceSuccesses(centralDevice, peripheralDevice, true);
				dataAccess.addConnectionLogIfNeeded(checkRow, true, checkRow.getCentralDeviceId(),
						"updateSuccessfulCheckFromCentral", ConnectionType.PROX);
				success = true;
			} else {
				this.addLog(centralDevice.getDeviceId(), "check row WAS null");
			}
		} catch (Exception e) {
			this.addLog(centralDevice.getDeviceId(), e);
		}
		return success;
	}

	private String removeQuotes(String str) {
		return str.replace("&quot;", "").replace("'", "");
	}

	public boolean didReconnectInTime(Timestamp expire, DeviceConnectionDbObj conn) {
		boolean reconnected = false;
		if (conn.getLastPeripheralConnectionSuccess().after(expire) || conn.getLastSubscribed().after(expire)
				|| conn.getLastCentralConnectionSuccess().after(expire) || conn.getLastSuccess().after(expire)) {
			reconnected = true;
		}
		addLog("didReconnectInTime", "reconnected: " + reconnected);
		addLog("didReconnectInTime", expire + " vs " + conn.getLastPeripheralConnectionSuccess() + ", "
				+ conn.getLastSubscribed() + ", and " + conn.getLastSuccess());
		return reconnected;
	}

	public boolean isDisconnectStillActive(String disconnectString) {
		boolean stillActive = false;
		String query = "SELECT * FROM B2F_DISCONNECT WHERE DISCONNECT_STRING = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, disconnectString);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				stillActive = true;
			}
		} catch (SQLException e) {
			addLog("isDisconnectStillActive", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		this.addLog("isDisconnectStillActive", "disconnect still active for '" + disconnectString + "'? " + stillActive,
				LogConstants.DEBUG);
		return stillActive;
	}

	public boolean cancelDisconnect(String disconnectString) {
		String query = "UPDATE B2F_DISCONNECT SET ACTIVE = ? WHERE DISCONNECT_STRING = ?";
		int outcome = 0;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, disconnectString);
			logQuery(getMethodName(), prepStmt);
			outcome = prepStmt.executeUpdate();
		} catch (SQLException e) {
			addLog("cancelDisconnect", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return outcome != 0;
	}

	public boolean cancelAllRecentDisconnects(DeviceConnectionDbObj connection) {
		String query = "UPDATE B2F_DISCONNECT SET ACTIVE = ? WHERE CONNECTION_ID = ? AND CREATE_DATE >= ?";
		java.sql.Date oneMinuteAgo = new java.sql.Date(DateTimeUtilities.nowPlusSeconds(-60).getTime());
		int outcome = 0;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, connection.getConnectionId());
			prepStmt.setDate(3, oneMinuteAgo);
			logQuery(getMethodName(), prepStmt);
			outcome = prepStmt.executeUpdate();
		} catch (SQLException e) {
			addLog(connection.getCentralDeviceId(), "cancelAllRecentDisconnects", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return outcome != 0;
	}

	public void addDisconnectString(DeviceConnectionDbObj connection, String disconnectString) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_DISCONNECT (CREATE_DATE, DISCONNECT_STRING, ACTIVE, CONNECTION_ID) "
					+ "VALUES (?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, DateTimeUtilities.getCurrentTimestamp());
				prepStmt.setString(2, disconnectString);
				prepStmt.setBoolean(3, true);
				prepStmt.setString(4, connection.getConnectionId());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog(connection.getCentralDeviceId(), "addDisconnectString", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void expireStaleServiceChecks(String serviceUuid, Timestamp ts) {
		new Thread(() -> {
			String query = "UPDATE B2F_CHECK SET EXPIRED = ? WHERE SERVICE_UUID = ? "
					+ "AND CREATE_DATE < ?  AND (COMPLETION_DATE < ? OR COMPLETION_DATE IS NULL) AND CHECK_TYPE = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, 1);
				prepStmt.setString(2, serviceUuid);
				prepStmt.setTimestamp(3, ts);
				prepStmt.setTimestamp(4, ts);
				prepStmt.setString(5, CheckType.PROX.toString());
				this.logQueryImportant("expireStaleServiceChecks", prepStmt);
				prepStmt.executeUpdate();
			} catch (Exception e) {
				addLog("expireStaleServiceChecks", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void expireStaleChecks(DeviceConnectionDbObj connection, Timestamp ts) {
		new Thread(() -> {
			String query = "UPDATE B2F_CHECK SET EXPIRED = ? WHERE CENTRAL_DEVICE_ID = ? AND PERIPHERAL_DEVICE_ID = ? "
					+ " AND CREATE_DATE < ?  AND (COMPLETION_DATE < ? OR COMPLETION_DATE IS NULL)";
			this.addLog(connection.getCentralDeviceId(), "expiring @ " + ts.toString());
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, 1);
				prepStmt.setString(2, connection.getCentralDeviceId());
				prepStmt.setString(3, connection.getPeripheralDeviceId());
				prepStmt.setTimestamp(4, ts);
				prepStmt.setTimestamp(5, ts);
				this.logQueryImportant("expireStaleChecks", prepStmt);
				prepStmt.executeUpdate();
			} catch (Exception e) {
				addLog(connection.getPeripheralDeviceId(), e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void expireDeviceNow(DeviceDbObj centralDevice) {
		new Thread(() -> {
			String query = "UPDATE B2F_CHECK SET EXPIRED = ? WHERE CENTRAL_DEVICE_ID = ? ";
			this.addLog(centralDevice.getDeviceId(), "expiring all for " + centralDevice.getDeviceId());
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, 1);
				prepStmt.setString(2, centralDevice.getDeviceId());
				logQueryImportant("expireDeviceNow", prepStmt);
				prepStmt.executeUpdate();
			} catch (Exception e) {
				addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void expirePeripheralDeviceChecksNow(DeviceDbObj peripheralDevice) {
		new Thread(() -> {
			String query = "UPDATE B2F_CHECK SET EXPIRED = ? WHERE PERIPHERAL_DEVICE_ID = ? ";
			this.addLog("expiring all for " + peripheralDevice.getDeviceId());
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, 1);
				prepStmt.setString(2, peripheralDevice.getDeviceId());
				prepStmt.executeUpdate();
			} catch (Exception e) {
				addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void updateCheck(CheckDbObj check) {
		new Thread(() -> {
			String query = "UPDATE B2F_CHECK SET INSTANCE_ID = ?, CENTRAL_DEVICE_ID = ?, "
					+ "PERIPHERAL_DEVICE_ID = ?, SERVICE_UUID = ?, USER_ID = ?, "
					+ "CENTRAL_BSSID = ?, CENTRAL_SSID = ?, PERIPHERAL_BSSID = ?, PERIPHERAL_SSID = ?, "
					+ "EXPIRED = ?, COMPLETED = ?, OUTCOME = ?, COMPLETION_DATE = ?, "
					+ "VERIFIED_RECEIPT = ? WHERE CHECK_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, check.getInstanceId());
				prepStmt.setString(2, check.getCentralDeviceId());
				prepStmt.setString(3, check.getPeripheralDeviceId());
				prepStmt.setString(4, check.getServiceUuid());
				prepStmt.setString(5, check.getUserId());
				prepStmt.setString(6, check.getCentralBssid());
				prepStmt.setString(7, check.getCentralSsid());
				prepStmt.setString(8, check.getPeripheralBssid());
				prepStmt.setString(9, check.getPeripheralSsid());
				prepStmt.setBoolean(10, check.isExpired());
				prepStmt.setBoolean(11, check.isCompleted());
				prepStmt.setInt(12, check.getOutcome());
				prepStmt.setString(13, DateTimeUtilities.utilDateToSqlStringDt(check.getCompletionDate()));
				prepStmt.setBoolean(14, check.isVerfiedReceipt());
				prepStmt.setString(15, check.getCheckId());
				deactivateFingerprints(check.getCentralDeviceId(), check.getPeripheralDeviceId());
				this.logQueryImportant("updateCheck", prepStmt);
				prepStmt.executeUpdate();
				this.addLog(check.getCentralDeviceId(), "Check row updated for checkId " + check.getCheckId(),
						LogConstants.TRACE);
			} catch (SQLException e) {
				this.addLog(check.getCentralDeviceId(), "updateCheck", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void deactivateFingerprints(String deviceId1, String deviceId2) {
		new Thread(() -> {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			String query = "UPDATE B2F_CHECK SET EXPIRATION_DATE = ?, EXPIRED = ? WHERE EXPIRED = ? AND "
					+ "CHECK_TYPE = ? AND (CENTRAL_DEVICE_ID = ? || PERIPHERAL_DEVICE_ID = ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, now);
				prepStmt.setBoolean(2, true);
				prepStmt.setBoolean(3, false);
				prepStmt.setString(4, CheckType.PASSKEY.name());
				prepStmt.setString(5, deviceId1);
				prepStmt.setString(6, deviceId2);
				prepStmt.executeUpdate();
			} catch (Exception e) {
				this.addLog(deviceId1, e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}

		}).start();
	}

	public void addLog(String text) {
		addLog("", getMethodNameInLogFn(), text, LogConstants.TRACE);
	}

	public void addLogSynchronous(String text) {
		addLogSynchronous("", getMethodNameInLogFn(), text, LogConstants.TRACE);
	}

	public void addLog(String text, int logLevel) {
		addLog("", getMethodNameInLogFn(), text, logLevel);
	}

	public void addLogSynchronous(String text, int logLevel) {
		addLogSynchronous("", getMethodNameInLogFn(), text, logLevel);
	}

	public void addLog(String deviceId, String text) {
		addLog(deviceId, getMethodNameInLogFn(), text, LogConstants.TRACE);
	}

	public void addLogSynchronous(String deviceId, String text) {
		addLogSynchronous(deviceId, getMethodNameInLogFn(), text, LogConstants.TRACE);
	}

	public void addLog(String deviceId, String text, int logLevel) {
		addLog(deviceId, getMethodNameInLogFn(), text, logLevel);
	}

	public void addLogSynchronous(String deviceId, String text, int logLevel) {
		addLogSynchronous(deviceId, getMethodNameInLogFn(), text, logLevel);
	}

	boolean dontLog = false;
	boolean print = true;

	public void addLog(final String devId, final String source, final String text, final int logLevel) {
		try {
			CodeEnvironment env = Constants.ENVIRONMENT;
			this.addLog(devId, source, text, logLevel, env);
		} catch (Exception e) {
			// pass
		}
	}

	public void addLogSynchronous(final String devId, final String source, final String text, final int logLevel) {
		try {
			CodeEnvironment env = Constants.ENVIRONMENT;
			this.addLogSynchronous(devId, source, text, logLevel, env);
		} catch (Exception e) {
			// pass
		}
	}

	private void addLog(final String devId, final String source, final String text, final int logLevel,
			CodeEnvironment env) {
		if (!dontLog && logLevel >= LogConstants.LOG_LEVEL) {
			new Thread(() -> {
				addLogSynchronous(devId, source, text, logLevel, env);
			}).start();
		}
	}

	private void addLogSynchronous(final String devId, final String source, final String text, final int logLevel,
			CodeEnvironment env) {
		if (!dontLog && logLevel >= LogConstants.LOG_LEVEL) {
			String src = source;
			if (src.length() > 2000) {
				String new_src = src.substring(0, 2000);
				addLog(devId, source, src.substring(2001), logLevel, env);
				src = new_src;
			}
			if (source == null) {
				src = "no source";
			}
			String query = "INSERT INTO B2F_LOG (" + logFields + ") VALUES (?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, DateTimeUtilities.getLastTimestampString());
				prepStmt.setString(2, devId);
				prepStmt.setString(3, src);
				prepStmt.setString(4, text);
				prepStmt.setInt(5, logLevel);
				try {

					prepStmt.setString(6, env.toString());
				} catch (Exception e2) {
					prepStmt.setString(6, "prod?");
					// e2.printStackTrace();
				}
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				logger.error(source, e);
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}
		if (print) {
			System.out.println(text);
		}
	}

	public void addLog(Exception e) {
		addLog("", getMethodNameInLogFn(), e);
	}

	public void addLog(String deviceId, Exception e) {
		addLog(deviceId, getMethodNameInLogFn(), e);
	}

	public void addLog(String devId, String source, Exception e) {
		logger.error(source, e);
		if (!(e instanceof SQLTransientConnectionException)) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = e.getLocalizedMessage() + "\r\n" + sw.toString();
			addLog(devId, source, sStackTrace, LogConstants.ERROR);
		}
	}

	public TokenDbObj addBrowserAndTokenWithId(DeviceDbObj device, String tokenId, TokenDescription description,
			String url) {
		BrowserDbObj browser = this.addBrowser(device, description.toString());
		return this.addTokenWithId(device, browser.getBrowserId(), tokenId, description, 0, url);
	}

	public BrowserDbObj addBrowserWithoutDevice(String description) {
		String browserId = GeneralUtilities.randomString();
		return addDevicelessBrowser(browserId, description);
	}

	public BrowserDbObj addBrowser(DeviceDbObj device, String description) {
		String browserId = GeneralUtilities.randomString(40);
		return addBrowser(device, browserId, description);
	}

	public void updateAuthTokenWithBrowser(String deviceId, String browserId) {
		new Thread(() -> {
			String query = "UPDATE B2F_TOKEN SET BROWSER_ID = ? WHERE DEVICE_ID = ? AND DESCRIPTION = ? "
					+ "AND BROWSER_ID IS NULL";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, browserId);
				prepStmt.setString(2, deviceId);
				prepStmt.setString(3, TokenDescription.AUTHENTICATION.toString());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (Exception e) {
				this.addLog("updateAuthTokenWithBrowser", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	/**
	 * To use oneTimeAccess machines
	 * 
	 * @param browserId
	 * @param description
	 * @return
	 */
	public BrowserDbObj addDevicelessBrowser(String browserId, String description) {
		BrowserDbObj browser = null;
		String query = "INSERT INTO B2F_BROWSER (" + browserFields + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Timestamp tenYearsFromNow = DateTimeUtilities.getCurrentTimestampPlusDays(3652);
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, null);
			prepStmt.setString(2, browserId);
			prepStmt.setTimestamp(3, now);
			prepStmt.setTimestamp(4, tenYearsFromNow);
			prepStmt.setTimestamp(5, now);
			prepStmt.setString(6, description);
			prepStmt.setBoolean(7, false);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			browser = new BrowserDbObj(null, browserId, now, tenYearsFromNow, now, description, false);
		} catch (Exception e) {
			this.addLog("addBrowser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return browser;
	}

	public BrowserDbObj addBrowser(DeviceDbObj device, String browserId, String description) {
		Timestamp expireTime;
		if (device.isMultiUser()) {
			expireTime = DateTimeUtilities.getCurrentTimestampPlusSeconds(60 * 60 * 2);
		} else {
			expireTime = DateTimeUtilities.getCurrentTimestampPlusDays(3652);
		}
		return addBrowser(device, browserId, description, expireTime);
	}

	// returns browserObject
	public BrowserDbObj addBrowser(DeviceDbObj device, String browserId, String description, Timestamp expirationDate) {
		BrowserDbObj browser = null;
		String query = "INSERT INTO B2F_BROWSER (" + this.browserFields + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, device.getDeviceId());
			prepStmt.setString(2, browserId);
			prepStmt.setTimestamp(3, now);
			prepStmt.setTimestamp(4, expirationDate);
			prepStmt.setTimestamp(5, now);
			prepStmt.setString(6, description);
			prepStmt.setBoolean(7, false);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			browser = new BrowserDbObj(device.getDeviceId(), browserId, now, expirationDate, now, description, false);
		} catch (Exception e) {
			this.addLog(device.getDeviceId(), "addBrowser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return browser;
	}

	public TokenDbObj addTokenWithId(String groupId, String deviceId, String browserId, String tokenId,
			TokenDescription description, int permission, String baseUrl) {
		Timestamp expire = DateTimeUtilities.getCurrentTimestampPlusDays(90);
		return addTokenWithId(groupId, deviceId, browserId, tokenId, description, permission, baseUrl, expire);
	}

	public TokenDbObj addTokenWithId(String groupId, String deviceId, String browserId, String tokenId,
			TokenDescription description, int permission, String baseUrl, Timestamp expireTime) {
		TokenDbObj token = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "INSERT INTO B2F_TOKEN (" + tokenFields + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, groupId);
			prepStmt.setString(2, deviceId);
			prepStmt.setString(3, tokenId);
			prepStmt.setTimestamp(4, now);
			prepStmt.setTimestamp(5, expireTime);
			prepStmt.setTimestamp(6, now);
			prepStmt.setTimestamp(7, now);
			prepStmt.setString(8, description.toString());
			prepStmt.setBoolean(9, false);
			prepStmt.setString(10, browserId);
			prepStmt.setInt(11, permission);
			prepStmt.setString(12, baseUrl);

			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			token = new TokenDbObj(groupId, deviceId, tokenId, now, expireTime, null, null, description.toString(),
					false, browserId, permission, baseUrl);
			this.addLog(deviceId, "token added");
		} catch (Exception e) {
			this.addLog(deviceId, e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public TokenDbObj addTokenWithId(DeviceDbObj device, String browserId, String tokenId, TokenDescription description,
			int permission, String url) {
		return this.addTokenWithId(device.getGroupId(), device.getDeviceId(), browserId, tokenId, description,
				permission, url);
	}

	public TokenDbObj addTempTokenWithId(DeviceDbObj device, String browserId, String tokenId,
			TokenDescription description, int permission, String url) {
		Timestamp expire = DateTimeUtilities.getCurrentTimestampPlusSeconds(Constants.TEMP_BROWSER_EXPIRATION_SECS);
		return addTokenWithId(device.getGroupId(), device.getDeviceId(), browserId, tokenId, description, permission,
				url, expire);
	}

	public TokenDbObj addToken(DeviceDbObj device, TokenDescription description, String url) {
		String tokenId = GeneralUtilities.randomString(32) + "-" + GeneralUtilities.randomString(16) + "-"
				+ GeneralUtilities.randomString(4);
		return this.addTokenWithId(device, "", tokenId, description, 0, url);
	}

	public TokenDbObj addGroupToken(GroupDbObj group, int permission) {
		String tokenId = GeneralUtilities.randomString(32) + "-" + GeneralUtilities.randomString(16) + "-"
				+ GeneralUtilities.randomString(4);
		TokenDbObj token = null;
		String query = "INSERT INTO B2F_TOKEN (" + tokenFields + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, group.getGroupId());
			prepStmt.setString(2, "");
			prepStmt.setString(3, tokenId);
			prepStmt.setTimestamp(4, now);

			Timestamp timestamp = DateTimeUtilities.getCurrentTimestampPlusSeconds(60 * 60);
			prepStmt.setTimestamp(5, timestamp);
			prepStmt.setTimestamp(6, now);
			prepStmt.setTimestamp(7, now);
			prepStmt.setString(8, TokenDescription.ADMIN.toString());
			prepStmt.setBoolean(9, false);
			prepStmt.setString(10, "");
			prepStmt.setInt(11, permission);
			prepStmt.setString(12, "");
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			token = new TokenDbObj(group.getGroupId(), null, tokenId, now, null, null, null,
					TokenDescription.ADMIN.toString(), false, "", permission, "");
		} catch (Exception e) {
			this.addLog("addTokenWithId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public TokenDbObj addTempToken(DeviceDbObj device, String browserId, TokenDescription description, String url) {
		String tokenId;
		if (device.isCentral()) {
			tokenId = getCentralToken();
		} else {
			tokenId = getPeripheralToken();
		}
		this.addLog("addTempToken", "tokenVal: " + tokenId);
		Timestamp expire = DateTimeUtilities.getCurrentTimestampPlusSeconds(Constants.TEMP_BROWSER_EXPIRATION_SECS);
		return addTokenWithId(device.getGroupId(), device.getDeviceId(), browserId, tokenId, description, 0, url,
				expire);
	}

	public TokenDbObj addToken(DeviceDbObj device, String browserId, TokenDescription description, String url) {
		TokenDbObj token;
		if (device.isCentral()) {
			token = this.addTokenCentral(device, browserId, description, url);
		} else {
			token = this.addTokenPeripheral(device, browserId, description, url);
		}
		this.addLog("addToken", "tokenVal: " + token.getTokenId());
		return token;
	}

	@Test
	public void testToken() {
		int thou = 0;
		for (int i = 0; i < 10000000; i++) {
			String token0 = GeneralUtilities.randomString(32) + "-" + GeneralUtilities.randomString(16) + "-"
					+ GeneralUtilities.randomString(4);
			String token1 = getCentralToken();
			String token2 = getPeripheralToken();
			boolean randCent = isCentralToken(token0);
			boolean cent = isCentralToken(token1);
			boolean notCent = isCentralToken(token2);
			boolean randPerf = isPeripheralToken(token0);
			boolean perf = isPeripheralToken(token2);
			boolean notPerf = isPeripheralToken(token1);
			if (randCent || !cent || notCent || randPerf || !perf || notPerf) {
				System.out.println("failure on " + i);
			}
			if (i % 1000000 == 0) {
				System.out.println(Integer.toString(thou) + "00000");
				thou++;
			}
		}
		System.out.println("done");
	}

	public TokenDbObj addTokenCentral(DeviceDbObj device, String browserId, TokenDescription description, String url) {
		String tokenId;
		try {
			if (device.getDeviceClass().toString().endsWith(DeviceClass.TEMP.toString())) {
				tokenId = getTempCentralToken();
			} else {
				tokenId = getCentralToken();
			}
		} catch (Exception e) {
			tokenId = getCentralToken();
		}
		return this.addTokenWithId(device, browserId, tokenId, description, 0, url);
	}

	String[] zeroList = { "e", "r", "n" };
	String[] firstList = { "c", "B", "0", "2", "p", "L", "i", "N" };
	String[] secondList = { "g", "J", "d", "8", "9", "Y", "R", "x", "j", "p" };
	String[] thirdList = { "l", "6" };
	String[] fourthList = { "k" };
	String[] fifthList = { "j", "n", "a", "b", "h", "m", "z", "O", "H", "3", "4", "b", "m", "v", "G" };

	public String getCentralToken() {
		String zeroeth = GeneralUtilities.getRandomListLetter(zeroList);
		String first = GeneralUtilities.getRandomListLetter(firstList);
		String second = GeneralUtilities.getRandomListLetter(secondList);
		String third = GeneralUtilities.getRandomListLetter(thirdList);
		String fourth = GeneralUtilities.getRandomListLetter(fourthList);
		String fifth = GeneralUtilities.getRandomListLetter(fifthList);
		return GeneralUtilities.randomString(7) + zeroeth + GeneralUtilities.randomString(2) + first
				+ GeneralUtilities.randomString(8) + second + GeneralUtilities.randomString(12) + "-"
				+ GeneralUtilities.randomString(4) + third + GeneralUtilities.randomString(4) + fourth
				+ GeneralUtilities.randomString(6) + "-" + GeneralUtilities.randomString(2) + fifth
				+ GeneralUtilities.randomString(1);
	}

	public String getTempCentralToken() {
		return getCentralToken() + "b2ft";
	}

	public TokenDbObj addTokenPeripheral(DeviceDbObj device, String browserId, TokenDescription description,
			String url) {
		String tokenId;
		try {
			if (device.getDeviceClass().toString().endsWith(DeviceClass.TEMP.toString())) {
				tokenId = getTempPeripheralToken();
			} else {
				tokenId = getPeripheralToken();
			}
		} catch (Exception e) {
			tokenId = getPeripheralToken();
		}
		return this.addTokenWithId(device, browserId, tokenId, description, 0, url);
	}

	public String getPeripheralToken() {
		String zeroeth = GeneralUtilities.getRandomListLetter(zeroList);
		String first = GeneralUtilities.getRandomListLetter(firstList);
		String second = GeneralUtilities.getRandomListLetter(secondList);
		String third = GeneralUtilities.getRandomListLetter(fifthList);
		String fourth = GeneralUtilities.getRandomListLetter(fourthList);
		String fifth = GeneralUtilities.getRandomListLetter(thirdList);
		return GeneralUtilities.randomString(7) + zeroeth + first + GeneralUtilities.randomString(12) + second
				+ GeneralUtilities.randomString(10) + "-" + GeneralUtilities.randomString(7) + third
				+ GeneralUtilities.randomString(3) + fourth + GeneralUtilities.randomString(4) + "-"
				+ GeneralUtilities.randomString(2) + fifth + GeneralUtilities.randomString(1);
	}

	public String getTempPeripheralToken() {
		return getPeripheralToken() + "b2ft";
	}

	// PGu4z47jXP2k8Z7F5vSjm2ufOfiysIua-XbUvl15PKLmQZWhq-wlzj
	public boolean isCentralToken(String token) {
		boolean central = false;
		DataAccess dataAccess = new DataAccess();
		if (contains(zeroList, token.substring(7, 8))) {
			if (contains(firstList, token.substring(10, 11))) {
				if (contains(secondList, token.substring(19, 20))) {
					if (contains(thirdList, token.substring(37, 38))) {
						if (contains(fourthList, token.substring(42, 43))) {
							if (contains(fifthList, token.substring(52, 53))) {
								central = true;
							} else {
								dataAccess.addLog("isCentralToken", "5: " + token.substring(52, 53));
							}
						} else {
							dataAccess.addLog("isCentralToken", "4: " + token.substring(42, 43));
						}
					} else {
						dataAccess.addLog("isCentralToken", "3: " + token.substring(37, 38));
					}
				} else {
					dataAccess.addLog("isCentralToken", "2: " + token.substring(19, 20));
				}
			} else {
				dataAccess.addLog("isCentralToken", "1: " + token.substring(10, 11));
			}
		} else {
//            dataAccess.addLog("isCentralToken", "0: " + token.substring(7, 8));
		}
		new DataAccess().addLog(token, "" + central);
		return central;

	}

	// bZkcn9qBamP3ZUBBrhpLaJPiyFHd4xut-uWKNtFQjHrUL6tkJ-euld
	public boolean isPeripheralToken(String token) {
		boolean perf = false;
		if (contains(zeroList, token.substring(7, 8))) {
			if (contains(firstList, token.substring(8, 9))) {
				if (contains(secondList, token.substring(21, 22))) {
					if (contains(fifthList, token.substring(40, 41))) {
						if (contains(fourthList, token.substring(44, 45))) {
							if (contains(thirdList, token.substring(52, 53))) {
								perf = true;
							}
						}
					}
				}
			}
		}
		return perf;
	}

	public static <T> boolean contains(final T[] array, final T v) {
		if (v == null) {
			for (final T e : array)
				if (e == null)
					return true;
		} else {
			for (final T e : array)
				if (e == v || v.equals(e))
					return true;
		}

		return false;
	}

	public TokenDbObj addToken(String groupId, String deviceId, String browserId, TokenDescription description,
			String url) {
		String tokenId = GeneralUtilities.randomString(32) + "-" + GeneralUtilities.randomString(16) + "-"
				+ GeneralUtilities.randomString(4);
		return this.addTokenWithId(groupId, deviceId, browserId, tokenId, description, 0, url);
	}

	public boolean updateAuthenticatorByCredId(AuthenticatorDbObj authenticator) {
		boolean success = false;
		// We don't update expired
		String query = "UPDATE B2F_AUTHENTICATOR SET BROWSER_ID = ?, TYPE = ?, SIGN_COUNT = ?, "
				+ "ATTESTED_CREDENTIAL_DATA = ?, ATTESTATION_OBJECT = ?, COLLECTED_CLIENT_DATA = ?, FORMAT = ?, "
				+ "CREATE_DATE = ?, CHALLENGE = ?, BASE_URL = ? WHERE CRED_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, authenticator.getBrowserId());
			prepStmt.setString(2, authenticator.getType());
			prepStmt.setLong(3, authenticator.getSignCount());

			ObjectConverter objectConverter = new ObjectConverter();
			addLog("updateAuthenticatorByCredId", "objectConverter created");
			AttestedCredentialDataConverter acdc = new AttestedCredentialDataConverter(objectConverter);
			byte[] attestedCredBytes = acdc.convert(authenticator.getAttestedCredentialData());
			addLog("updateAuthenticatorByCredId", "acdc converted");
			prepStmt.setBytes(4, attestedCredBytes);
			AttestationObjectConverter aoj = new AttestationObjectConverter(objectConverter);
			byte[] ao = aoj.convertToBytes(authenticator.getAttestationObject());
			addLog("updateAuthenticatorByCredId", "aoj converted");
			prepStmt.setBytes(5, ao);

			CollectedClientData ccd = authenticator.getCollectedClientData();
			CollectedClientDataConverter ccdc = new CollectedClientDataConverter(objectConverter);
			prepStmt.setBytes(6, ccdc.convertToBytes(ccd));
			prepStmt.setString(7, authenticator.getFormat());
			prepStmt.setTimestamp(8, DateTimeUtilities.getCurrentTimestamp());
			prepStmt.setString(9, authenticator.getChallenge());
			prepStmt.setString(10, authenticator.getBaseUrl());
			prepStmt.setString(11, authenticator.getCredId());
			logQueryImportant(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			addLog("updateAuthenticatorByCredId", "auth row updated");
			success = true;
		} catch (SQLException e) {
			addLog("updateAuthenticatorByCredId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public boolean isFingerprintAvailable(BrowserDbObj browser) {
		boolean available = false;
		if (browser != null) {
			String query = "SELECT * FROM B2F_AUTHENTICATOR WHERE BROWSER_ID = ? AND EXPIRED = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, browser.getBrowserId());
				prepStmt.setBoolean(2, false);
				rs = executeQuery(prepStmt);
				if (rs.next()) {
					available = true;
				} else {
					this.addLog("isFingerprintAvailable", "notFound");
				}
			} catch (Exception e) {
				this.addLog("isFingerprintAvailable", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}
		return available;
	}

	public void updateAuthenticatorByBrowserAndUrl(AuthenticatorDbObj authenticator) {
		new Thread(() -> {
			// dont update expired
			String query = "UPDATE B2F_AUTHENTICATOR SET CRED_ID = ?, TYPE = ?, SIGN_COUNT = ?, "
					+ "ATTESTED_CREDENTIAL_DATA = ?, ATTESTATION_OBJECT = ?, COLLECTED_CLIENT_DATA = ?, FORMAT = ?, "
					+ "CREATE_DATE = ?, CHALLENGE = ? WHERE BROWSER_ID = ? AND BASE_URL = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, authenticator.getCredId());
				prepStmt.setString(2, authenticator.getType());
				prepStmt.setLong(3, authenticator.getSignCount());

//            CborConverter cborConverter = new CborConverter();
				ObjectConverter objectConverter = new ObjectConverter();
				AttestedCredentialDataConverter acdc = new AttestedCredentialDataConverter(objectConverter);
				addLog("updateAuthenticatorByBrowserAndUrl", "acdc converted");
				byte[] acd = acdc.convert(authenticator.getAttestedCredentialData());
				prepStmt.setBytes(4, acd);
				AttestationObjectConverter aoj = new AttestationObjectConverter(objectConverter);
				addLog("updateAuthenticatorByBrowserAndUrl", "aoj converted");
				byte[] ao = aoj.convertToBytes(authenticator.getAttestationObject());
				prepStmt.setBytes(5, ao);
//            JsonConverter jsonConverter = new JsonConverter();
				CollectedClientData ccd = authenticator.getCollectedClientData();
				CollectedClientDataConverter ccdc = new CollectedClientDataConverter(objectConverter);
				prepStmt.setBytes(6, ccdc.convertToBytes(ccd));
				prepStmt.setString(7, authenticator.getFormat());
				prepStmt.setTimestamp(8, DateTimeUtilities.getCurrentTimestamp());
				prepStmt.setString(9, authenticator.getChallenge());
				prepStmt.setString(10, authenticator.getBrowserId());
				prepStmt.setString(11, authenticator.getBaseUrl());
				logQueryImportant(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				addLog("updateAuthenticatorByBrowserAndUrl", "auth row updated");
			} catch (SQLException e) {
				addLog("updateAuthenticatorByBrowserAndUrl", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public AuthenticatorDbObj getActiveAuthenticatorByCredentialId(String credId) {
		AuthenticatorDbObj authenticator = null;
		String query = "SELECT * FROM B2F_AUTHENTICATOR WHERE CRED_ID = ? AND EXPIRED = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, credId);
			prepStmt.setBoolean(2, false);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				authenticator = recordToAuthenticator(rs);
			} else {
				addLog("getActiveAuthenticatorByCredentialId", "authenticator is null");
			}
		} catch (SQLException e) {
			addLog("getActiveAuthenticatorByCredentialId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return authenticator;
	}

	public void expireOtherAutheticatorsForBrowser(String browserId, String challenge) {
		new Thread(() -> {
			String query = "UPDATE B2F_AUTHENTICATOR SET EXPIRED = ? WHERE BROWSER_ID = ? " + "AND CHALLENGE != ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, true);
				prepStmt.setString(2, browserId);
				prepStmt.setString(3, challenge);
				logQueryImportant(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog("expireOtherAutheticatorsForBrowser", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void removeOtherAuthenticator(String browserId, String url) {
		new Thread(() -> {
			String query = "UPDATE B2F_AUTHENTICATOR SET EXPIRED = ? WHERE BROWSER_ID = ? AND BASE_URL = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, true);
				prepStmt.setString(2, browserId);
				prepStmt.setString(3, url);
				logQueryImportant(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog("removeOtherAuthenticator", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public AuthenticatorDbObj getActiveAuthenticatorByTokenIdAndUrl(String tokenId, String url) {
		AuthenticatorDbObj authenticator = null;
		String query = "SELECT auth.* FROM B2F_AUTHENTICATOR auth JOIN B2F_TOKEN tok ON "
				+ "auth.BROWSER_ID = tok.BROWSER_ID WHERE tok.TOKEN_ID = ? "
//				+ "AND auth.EXPIRED = ? "
				+ "AND auth.BASE_URL = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tokenId);
//			prepStmt.setBoolean(2, false);
			prepStmt.setString(2, url);
			logQueryImportant("getActiveAuthenticatorByTokenIdAndUrl", prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				authenticator = recordToAuthenticator(rs);
			}
		} catch (SQLException e) {
			addLog("getActiveAuthenticatorByBrowserId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return authenticator;
	}

	public boolean checkForAuthenticatorWithBrowserId(String browserId) {
		boolean authenticatorExist = false;
		String query = "SELECT * FROM B2F_AUTHENTICATOR WHERE BROWSER_ID = ? AND "
				+ "COLLECTED_CLIENT_DATA is not null AND EXPIRED = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, browserId);
			prepStmt.setBoolean(2, false);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				authenticatorExist = true;
			}
		} catch (SQLException e) {
			addLog("getActiveAuthenticatorByBrowserId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return authenticatorExist;
	}

	public boolean areBiometricsAvailableForDevice(DeviceDbObj device) {
		return this.areBiometricsAvailableForDevice(device.getDeviceId());
	}

	public boolean areBiometricsAvailableForDevice(String deviceId) {
		return this.getAuthenticatorByDevice(deviceId) != null;
	}

	public AuthenticatorDbObj getAuthenticatorByDevice(String deviceId) {
		AuthenticatorDbObj authenticator = null;
		String query = "SELECT * FROM B2F_AUTHENTICATOR WHERE DEVICE_ID = ? AND "
				+ "COLLECTED_CLIENT_DATA is not null AND EXPIRED = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setBoolean(2, false);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				authenticator = recordToAuthenticator(rs);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return authenticator;
	}

	public AuthenticatorDbObj getAuthenticatorByBrowserIdAndUrl(String browserId, String baseUrl) {
		AuthenticatorDbObj authenticator = null;
		String query = "SELECT * FROM B2F_AUTHENTICATOR WHERE BROWSER_ID = ? AND EXPIRED = ? AND BASE_URL = ? AND CRED_ID IS NOT NULL";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, browserId);
			prepStmt.setBoolean(2, false);
			prepStmt.setString(3, baseUrl);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				authenticator = recordToAuthenticator(rs);
			}
		} catch (SQLException e) {
			addLog("getActiveAuthenticatorByBrowserId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return authenticator;
	}

	public boolean addAuthenticator(AuthenticatorDbObj authenticator) {
		boolean success = false;
		String query = "INSERT INTO B2F_AUTHENTICATOR (CRED_ID, BROWSER_ID, TYPE, SIGN_COUNT, "
				+ "ATTESTED_CREDENTIAL_DATA, ATTESTATION_OBJECT, COLLECTED_CLIENT_DATA, "
				+ "FORMAT, CREATE_DATE, EXPIRED, CHALLENGE, BASE_URL) VALUES (?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, authenticator.getCredId());
			prepStmt.setString(2, authenticator.getBrowserId());
			prepStmt.setString(3, authenticator.getType());
			prepStmt.setLong(4, authenticator.getSignCount());
//            CborConverter cborConverter = new CborConverter();
			ObjectConverter objectConverter = new ObjectConverter();
			byte[] acd;
			if (authenticator.getAttestedCredentialData() != null) {
				AttestedCredentialDataConverter acdc = new AttestedCredentialDataConverter(objectConverter);
				acd = acdc.convert(authenticator.getAttestedCredentialData());
				addLog("addAuthenticator", "acdc converted");
			} else {
				acd = null;
			}
			prepStmt.setBytes(5, acd);
			byte[] attestationObject;
			if (authenticator.getAttestationObject() != null) {
				AttestationObjectConverter aoj = new AttestationObjectConverter(objectConverter);
				attestationObject = aoj.convertToBytes(authenticator.getAttestationObject());
				addLog("addAuthenticator", "aoj converted");
			} else {
				attestationObject = null;
			}
			prepStmt.setBytes(6, attestationObject);
			byte[] ccdBytes;
			if (authenticator.getCollectedClientData() != null) {
//                JsonConverter jsonConverter = new JsonConverter();
				CollectedClientData ccd = authenticator.getCollectedClientData();
				CollectedClientDataConverter ccdc = new CollectedClientDataConverter(objectConverter);
				ccdBytes = ccdc.convertToBytes(ccd);
				addLog("addAuthenticator", "ccd converted");
			} else {
				ccdBytes = null;
			}
			prepStmt.setBytes(7, ccdBytes);
			prepStmt.setString(8, authenticator.getFormat());
			prepStmt.setTimestamp(9, authenticator.getCreateDate());
			prepStmt.setBoolean(10, authenticator.isExpired());
			prepStmt.setString(11, authenticator.getChallenge());
			prepStmt.setString(12, authenticator.getBaseUrl());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			addLog("addAuthenticator", "auth row added");
			success = true;
		} catch (SQLException e) {
			addLog("addAuthenticator", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	private AuthenticatorDbObj recordToAuthenticator(ResultSet rs) {
		AuthenticatorDbObj authenticator = null;
		try {
			String credId = rs.getString("CRED_ID");
			String browserId = rs.getString("BROWSER_ID");
			String type = rs.getString("TYPE");
			Long signCount = rs.getLong("SIGN_COUNT");
			byte[] attestedCredentialDataBytes = rs.getBytes("ATTESTED_CREDENTIAL_DATA");
//            CborConverter cborConverter = new CborConverter();
			ObjectConverter objectConverter = new ObjectConverter();
			AttestedCredentialDataConverter target = new AttestedCredentialDataConverter(objectConverter);
			AttestedCredentialData attestedCredentialData = null;
			if (attestedCredentialDataBytes != null) {
				attestedCredentialData = target.convert(attestedCredentialDataBytes);
				this.addLog("recordToAuthenticator", "acd converted");
			}

			byte[] attestationStatementBytes = rs.getBytes("ATTESTATION_OBJECT");
			byte[] ccd = rs.getBytes("COLLECTED_CLIENT_DATA");
			String format = rs.getString("FORMAT");
			String challenge = rs.getString("CHALLENGE");
			String baseUrl = rs.getString("BASE_URL");

			AttestationObject attestationObject = null;
			CollectedClientData collectedClientData = null;
			if (format != null) {
				AttestationObjectConverter aoc = new AttestationObjectConverter(objectConverter);
				attestationObject = aoc.convert(attestationStatementBytes);
				this.addLog("recordToAuthenticator", "aoc converted");
//                JsonConverter jsonConverter = new JsonConverter();
				CollectedClientDataConverter converter = new CollectedClientDataConverter(objectConverter);
				collectedClientData = converter.convert(Base64.getEncoder().encodeToString(ccd));
				this.addLog("recordToAuthenticator", "ccd converted");
			} else {
				if (attestationStatementBytes != null) {
					this.addLog("recordToAuthenticator", "len: " + attestationStatementBytes.length);
				}
			}
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			boolean expired = rs.getBoolean("EXPIRED");
			authenticator = new AuthenticatorDbObj(credId, browserId, type, attestedCredentialData, attestationObject,
					collectedClientData, signCount, createDate, expired, challenge, baseUrl);
			this.addLog("recordToAuthenticator", "it worked");
		} catch (SQLException e) {
			addLog("recordToAuthenticator", e);
		}
		return authenticator;
	}

	public String addCompletedCheckForFingerprint(BaseController controller, String instanceId,
			HttpServletRequest request) {
		String session = null;
		Timestamp expiration = DateTimeUtilities.getCurrentTimestampPlusSeconds(Constants.PASSKEY_TIMEOUT_SECS);
		if (!TextUtils.isBlank(instanceId)) {
			if (addCompletedCheckForFingerprint(instanceId, expiration)) {
				session = instanceId;
			}
		} else {
			IdentityObjectFromServer idObj = controller.getIdObjWithoutCompany(request);
			if (addCompletedCheckForFingerprint(idObj, expiration)) {
				session = instanceId;
			}
		}
		return session;
	}

	public boolean addCompletedCheckForFingerprint(String instanceId, Timestamp expiration) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		addLog("addCompletedCheckForFingerprint", "adding record", LogConstants.TRACE);
		DeviceDbObj device = dataAccess.getDeviceByToken(instanceId, "addCompletedCheckForFingerprint");
		return addCompletedCheckForFingerprint(device, instanceId, expiration, dataAccess);
	}

	public boolean addCompletedCheckForFingerprint(IdentityObjectFromServer idObj, Timestamp expiration) {
		boolean success = false;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		addLog("addCompletedCheckForFingerprint", "adding record");

		DeviceDbObj device = idObj.getDevice();
		if (device != null) {
			TokenDbObj token = dataAccess.addToken(device, TokenDescription.BROWSER_SESSION,
					Urls.getUrlWithoutProtocol());
			success = addCompletedCheckForFingerprint(device, token.getDeviceId(), expiration, dataAccess);
		}
		return success;
	}

	public boolean addCompletedCheckForFingerprint(DeviceDbObj device, String instanceId, Timestamp expiration,
			DeviceDataAccess dataAccess) {
		boolean success = false;
		dataAccess.addLog("adding record", LogConstants.TRACE);
		String checkId = GeneralUtilities.randomString();
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		if (device != null) {
			dataAccess.addLog("addCompletedCheckForFingerprint", "device found", LogConstants.TRACE);
			try {
				String centralId = null;
				String perfId = null;
				if (device.isCentral()) {
					centralId = device.getDeviceId();
				} else {
					perfId = device.getDeviceId();
				}
				CheckDbObj check = new CheckDbObj(checkId, instanceId, centralId, perfId, null, device.getUserId(),
						null, null, null, null, false, true, Outcomes.SUCCESS, now, now, true, CheckType.PASSKEY,
						instanceId, null, expiration);
				dataAccess.addCheck(check);
				ConnectionLogDbObj connLog = new ConnectionLogDbObj(null, device.getDeviceId(), true, now,
						device.getDeviceId(), "addCompletedCheckForFingerprint", ConnectionType.PASSKEY);
				dataAccess.addConnectionLog(connLog);
				success = true;
				dataAccess.addLog(device.getDeviceId(), "adding record complete", LogConstants.TRACE);
			} catch (Exception e) {
				dataAccess.addLog(e);
			}
		} else {
			dataAccess.addLog(instanceId, "device not found", LogConstants.WARNING);
		}
		return success;
	}

	public void updateBrowser(BrowserDbObj browser) {
		new Thread(() -> {
			String query = "UPDATE B2F_BROWSER SET DEVICE_ID = ?, CREATE_DATE = ?, EXPIRE_DATE = ?, "
					+ "LAST_UPDATE = ?, DESCRIPTION = ?, HAS_FAILED = ? WHERE BROWSER_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, browser.getDeviceId());
				prepStmt.setTimestamp(2, browser.getCreateDate());
				prepStmt.setTimestamp(3, browser.getExpireDate());
				prepStmt.setTimestamp(4, browser.getLastUpdate());
				prepStmt.setString(5, browser.getDescription());
				prepStmt.setBoolean(6, browser.getHasFailed());
				prepStmt.setString(7, browser.getBrowserId());

				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				addLog("updateBrowser", "browser row updated");
			} catch (SQLException e) {
				addLog("updateBrowser", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public boolean expireOtherTokens(TokenDbObj token, TokenDescription description, String url) {
		boolean completed = false;
		String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ?, LAST_UPDATE = ?"
				+ "WHERE DEVICE_ID = ? AND EXPIRE_TIME > ? AND "
				+ "TOKEN_ID != ? AND DESCRIPTION = ? AND AUTHORIZATION_TIME < ? AND BASE_URL = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setTimestamp(1, now);
			prepStmt.setTimestamp(2, now);
			prepStmt.setString(3, token.getDeviceId());
			prepStmt.setTimestamp(4, now);
			prepStmt.setString(5, token.getTokenId());
			prepStmt.setString(6, description.toString());
			prepStmt.setTimestamp(7, DateTimeUtilities.getCurrentTimestampMinusSeconds(10));
			prepStmt.setString(8, url);
			logQuery(getMethodName(), prepStmt);
			completed = prepStmt.executeUpdate() > 0;
			this.addLog(token.getDeviceId(), "finished");
			completed = true;
		} catch (Exception e) {
			this.addLog("expireOtherTokens", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return completed;
	}

	public void expireOtherBrowserTokens(TokenDbObj token, String url) {
		new Thread(() -> {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			Timestamp twentySecondsAgo = DateTimeUtilities.getCurrentTimestampMinusSeconds(20);
			String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ?, LAST_UPDATE = ?"
					+ "WHERE DEVICE_ID = ? AND EXPIRE_TIME > ? AND AUTHORIZATION_TIME < ? AND "
					+ "TOKEN_ID != ? AND DESCRIPTION = ? AND BASE_URL = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();

				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, now);
				prepStmt.setTimestamp(2, now);
				prepStmt.setString(3, token.getDeviceId());
				prepStmt.setTimestamp(4, now);
				prepStmt.setTimestamp(5, twentySecondsAgo);
				prepStmt.setString(6, token.getTokenId());
				prepStmt.setString(7, TokenDescription.BROWSER_SESSION.toString());
				prepStmt.setString(8, url);
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				this.addLog(token.getDeviceId(), "finished");
			} catch (Exception e) {
				this.addLog("expireOtherBrowserTokens", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void expireSshTokensForUser(String uid, String coId) {
		new Thread(() -> {
			Timestamp pastTime = DateTimeUtilities.getCurrentTimestampMinusMinutes(10);
			String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE GROUP_ID = (SELECT GROUP_ID "
					+ "FROM B2F_GROUP WHERE UID = ? AND COMPANY_ID = ?) AND DESCRIPTION = ? AND EXPIRE_TIME > "
					+ "CURRENT_TIMESTAMP";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, pastTime);
				prepStmt.setString(2, uid);
				prepStmt.setString(3, coId);
				prepStmt.setString(4, TokenDescription.SSH.toString());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				this.addLog(prepStmt.toString());
			} catch (Exception e) {
				this.addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public TokenDbObj getActiveSshTokenForUser(String uid) {
		TokenDbObj token = null;
		String query = "SELECT * FROM B2F_TOKEN tok JOIN B2F_GROUP grp ON tok.GROUP_ID = grp.GROUP_ID"
				+ " WHERE grp.UID = ? AND tok.DESCRIPTION = ? AND tok.EXPIRE_TIME >= CURRENT_TIMESTAMP";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, uid);
			prepStmt.setString(2, TokenDescription.SSH.toString());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				token = this.recordToToken(rs);
			}
		} catch (Exception e) {
			this.addLog("getActiveSshTokenForUser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public List<TokenDbObj> getActiveTokensByDevice(DeviceDbObj device) {
		ArrayList<TokenDbObj> tokens = new ArrayList<>();
		TokenDbObj token = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "SELECT * FROM B2F_TOKEN WHERE DEVICE_ID = ? AND EXPIRE_TIME >= ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, device.getDeviceId());
			prepStmt.setTimestamp(2, now);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				token = this.recordToToken(rs);
				this.addLog(device.getDeviceId(), "tokenFound. Expire time: " + token.getExpireTime());
				tokens.add(token);
			}
		} catch (Exception e) {
			this.addLog(device.getDeviceId(), "getActiveTokensByDevice", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return tokens;
	}

	public TokenDbObj getActiveTokenUsingDescription(String tokenId, TokenDescription desc) {
		TokenDbObj token = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "SELECT * FROM B2F_TOKEN WHERE TOKEN_ID = ? AND DESCRIPTION = ? AND " + "EXPIRE_TIME > ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tokenId);
			prepStmt.setString(2, desc.toString());
			prepStmt.setTimestamp(3, now);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				token = this.recordToToken(rs);
			}
		} catch (Exception e) {
			addLog("getActiveTokenUsingDescription", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public TokenDbObj getToken(String tokenId) {
		TokenDbObj token = null;
		String query = "SELECT * FROM B2F_TOKEN WHERE TOKEN_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tokenId);
			rs = executeQuery(prepStmt);
			logQueryImportant("getToken", prepStmt);
			if (rs.next()) {
				token = this.recordToToken(rs);
			}
		} catch (Exception e) {
			addLog("getToken", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public TokenDbObj getActiveTokenByOrDescriptionAndTokenId(TokenDescription desc, TokenDescription desc2,
			String tokenId) {
		TokenDbObj token = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "SELECT * FROM B2F_TOKEN WHERE (DESCRIPTION = ? OR DESCRIPTION = ?) AND "
				+ "TOKEN_ID = ? AND EXPIRE_TIME > ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, desc.toString());
			prepStmt.setString(2, desc2.toString());
			prepStmt.setString(3, tokenId);
			prepStmt.setTimestamp(4, now);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				token = this.recordToToken(rs);
			}
		} catch (Exception e) {
			addLog("getActiveTokenByOrDescriptionAndTokenId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public TokenDbObj getTokenByOrDescriptionAndTokenId(TokenDescription desc, TokenDescription desc2, String tokenId) {
		TokenDbObj token = null;
		String query = "SELECT * FROM B2F_TOKEN WHERE (DESCRIPTION = ? OR DESCRIPTION = ?) AND TOKEN_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, desc.toString());
			prepStmt.setString(2, desc2.toString());
			prepStmt.setString(3, tokenId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				token = this.recordToToken(rs);
			}
		} catch (Exception e) {
			addLog("getTokenByDeviceAndDescription", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public TokenDbObj getTokenByDescriptionAndTokenId(TokenDescription desc, String tokenId) {
		TokenDbObj token = null;
		String query = "SELECT * FROM B2F_TOKEN WHERE DESCRIPTION = ? AND TOKEN_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, desc.toString());
			prepStmt.setString(2, tokenId);
			logQuery(prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				token = this.recordToToken(rs);
			}
		} catch (Exception e) {
			addLog("getTokenByDeviceAndDescription", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public TokenDbObj getActiveTokenByDescriptionAndTokenId(TokenDescription desc, String tokenId) {
		TokenDbObj token = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "SELECT * FROM B2F_TOKEN WHERE DESCRIPTION = ? AND EXPIRE_TIME > ? AND TOKEN_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, desc.toString());
			prepStmt.setTimestamp(2, now);
			prepStmt.setString(3, tokenId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				token = this.recordToToken(rs);
			}
		} catch (Exception e) {
			addLog("getActiveTokenByDescriptionAndTokenId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public boolean rescindKeysForBrowser(BrowserDbObj browser, String url) {
		boolean success = false;
		String query = "UPDATE B2F_KEY SET ACTIVE = ? WHERE BROWSER_ID = ? AND KEY_TYPE IN (?, ?) AND "
				+ "ACTIVE = ? AND URL = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, false);
			prepStmt.setString(2, browser.getBrowserId());
			prepStmt.setString(3, KeyType.B2F_SERVER_PRIVATE_KEY_FOR_BROWSER.toString());
			prepStmt.setString(4, KeyType.BROWSER_PUBLIC_KEY.toString());
			prepStmt.setBoolean(5, true);
			prepStmt.setString(6, url);
			logQueryImportant("rescindKeysForBrowser", prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			addLog("rescindKeysForBrowser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public TokenDbObj getTokenByDeviceDescriptionAndTokenId(String deviceId, TokenDescription desc, String tokenId) {
		TokenDbObj token = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "SELECT * FROM B2F_TOKEN WHERE DEVICE_ID = ? AND DESCRIPTION = ? AND "
				+ "EXPIRE_TIME > ? AND TOKEN_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setString(2, desc.toString());
			prepStmt.setTimestamp(3, now);
			prepStmt.setString(4, tokenId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				token = this.recordToToken(rs);
			}
		} catch (Exception e) {
			addLog("getTokenByDeviceAndDescription", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public TokenDbObj getTokenByDeviceAndDescription(String deviceId, TokenDescription desc) {
		TokenDbObj token = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "SELECT * FROM B2F_TOKEN WHERE DEVICE_ID = ? AND DESCRIPTION = ? AND " + "EXPIRE_TIME > ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setString(2, desc.toString());
			prepStmt.setTimestamp(3, now);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				token = this.recordToToken(rs);
			}
		} catch (Exception e) {
			addLog("getTokenByDeviceAndDescription", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return token;
	}

	public BrowserDbObj getBrowserById(String browserId) {
		BrowserDbObj browser = null;
		String query = "SELECT * FROM B2F_BROWSER WHERE BROWSER_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, browserId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				browser = this.recordToBrowser(rs);
			} else {
				this.addLog("getBrowserById", "record not found", LogConstants.INFO);
			}
		} catch (Exception e) {
			addLog("getBrowserById", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return browser;
	}

	public BrowserDbObj getBrowserByAccessCode(AccessCodeDbObj accessCode) {
		return getBrowserByAccessCode(accessCode.getAccessCode());
	}

	public BrowserDbObj getBrowserByAccessCode(String accessCode) {
		// I changed this on 2/25/2024. I do not understand why we were using ten
		// minutes ago
		BrowserDbObj browser = null;
//		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
//		Timestamp tenMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(10);
		String query = "SELECT browser.* FROM B2F_ACCESS_STRINGS access join B2F_BROWSER browser ON "
				+ " access.BROWSER_ID = browser.BROWSER_ID WHERE access.ACCESS_CODE = ? AND access.ACTIVE = ?";
//				+ "access.CREATE_DATE > ?";
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
				browser = this.recordToBrowser(rs);
			} else {
				this.addLog("getBrowserByAccessCode", "record not found", LogConstants.INFO);
			}
		} catch (Exception e) {
			addLog("getBrowserByAccessCode", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return browser;
	}

	public BrowserDbObj getBrowserByActiveToken(String tokenId, TokenDescription tokenDescription) {
		BrowserDbObj browser = null;
		String query = "SELECT browser.* FROM B2F_TOKEN token join B2F_BROWSER browser ON "
				+ "token.BROWSER_ID = browser.BROWSER_ID WHERE token.TOKEN_ID = ? AND token.DESCRIPTION = ? "
				+ "AND token.EXPIRE_TIME > ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tokenId);
			prepStmt.setString(2, tokenDescription.toString());
			prepStmt.setTimestamp(3, DateTimeUtilities.getCurrentTimestamp());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				browser = this.recordToBrowser(rs);
			} else {
				this.addLog("record not found");
			}
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return browser;
	}

	public BrowserDbObj getBrowserByToken(String tokenId, TokenDescription tokenDescription) {
		BrowserDbObj browser = null;
		String query = "SELECT browser.* FROM B2F_TOKEN token join B2F_BROWSER browser ON "
				+ "token.BROWSER_ID = browser.BROWSER_ID WHERE token.TOKEN_ID = ? AND token.DESCRIPTION = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tokenId);
			prepStmt.setString(2, tokenDescription.toString());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				browser = this.recordToBrowser(rs);
			} else {
				this.addLog("getBrowserByToken", "record not found", LogConstants.WARNING);
			}
		} catch (Exception e) {
			addLog("getBrowserByToken", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return browser;
	}

	private TokenDbObj recordToToken(ResultSet rs) {
		TokenDbObj token = null;
		try {
			String groupId = rs.getString("GROUP_ID");
			String deviceId = rs.getString("DEVICE_ID");
			String tokenId = rs.getString("TOKEN_ID");
			Timestamp authorizationTime = rs.getTimestamp("AUTHORIZATION_TIME");
			Timestamp expireTime = rs.getTimestamp("EXPIRE_TIME");
			Timestamp lastCheck = rs.getTimestamp("LAST_CHECK");
			Timestamp lastUpdate = rs.getTimestamp("LAST_UPDATE");
			String description = rs.getString("DESCRIPTION");
			boolean needsUpdate = rs.getBoolean("NEEDS_UPDATE");
			String browserId = rs.getString("BROWSER_ID");
			int permission = rs.getInt("PERMISSION");
			String baseUrl = rs.getString("BASE_URL");
			this.addLog(deviceId, "setting exp to: " + expireTime);
			token = new TokenDbObj(groupId, deviceId, tokenId, authorizationTime, expireTime, lastCheck, lastUpdate,
					description, needsUpdate, browserId, permission, baseUrl);
		} catch (Exception e) {
			this.addLog("recordToToken", e);
		}
		return token;
	}

	protected BrowserDbObj recordToBrowser(ResultSet rs) {
		BrowserDbObj browser = null;
		try {
			String deviceId = rs.getString("DEVICE_ID");
			String browserId = rs.getString("BROWSER_ID");
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			Timestamp expireDate = rs.getTimestamp("EXPIRE_DATE");
			Timestamp lastUpdate = rs.getTimestamp("LAST_UPDATE");
			String description = rs.getString("DESCRIPTION");
			Boolean hasFailed = rs.getBoolean("HAS_FAILED");
			browser = new BrowserDbObj(deviceId, browserId, createDate, expireDate, lastUpdate, description, hasFailed);
			this.addLog("browserId: " + browserId);
		} catch (Exception e) {
			this.addLog("recordToBrowser", e);
		}
		return browser;
	}

	private boolean expireTokensForDevice(String deviceId, String src) {
		addLog(deviceId, "called from " + src);
		boolean success = false;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE (EXPIRE_TIME > ? OR EXPIRE_TIME "
				+ "IS NULL) AND DEVICE_ID = ? ";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, DateTimeUtilities.utilDateToSqlStringDt(now));
			prepStmt.setTimestamp(2, now);
			prepStmt.setString(3, deviceId);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			addLog("expireTokensForDevice", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public boolean expireTokensForDevice(DeviceDbObj device, String src) {
		return this.expireTokensForDevice(device.getDeviceId(), src);
	}

	public boolean expireTokensForSiteUser(BrowserDbObj browser, String url) {
		return expireTokensExcept(browser, "", "expireTokensForSiteUser", url);
	}

	private boolean expireTokensExcept(BrowserDbObj browser, String exceptToken, String src, String url) {
		addLog("expireTokensExcept", "called from " + src);
		boolean success = false;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Timestamp oneSecondAgo = DateTimeUtilities.addSeconds(new Timestamp(System.currentTimeMillis()), -1);
		String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE (EXPIRE_TIME IS NULL OR EXPIRE_TIME > ?) "
				+ "AND BROWSER_ID = ? AND TOKEN_ID != ? AND AUTHORIZATION_TIME < ? AND BASE_URL = ?";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setTimestamp(1, now);
			prepStmt.setTimestamp(2, now);
			prepStmt.setString(3, browser.getBrowserId());
			prepStmt.setString(4, exceptToken);
			prepStmt.setTimestamp(5, oneSecondAgo);
			prepStmt.setString(6, url);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public boolean expireTokensExcept(String exceptToken, TokenDescription tokenDescription, String src, String url) {
		BrowserDbObj browser = this.getBrowserByToken(exceptToken, tokenDescription);
		return this.expireTokensExcept(browser, exceptToken, src, url);
	}

	public boolean expireLambdaTokensExcept(String exceptToken, String deviceId) {
		boolean success = false;
		String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE (EXPIRE_TIME IS NULL OR EXPIRE_TIME > ?) "
				+ "AND DEVICE_ID = ? AND TOKEN_ID != ?  AND AUTHORIZATION_TIME < ? AND DESCRIPTION = ?";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Timestamp oneSecondAgo = DateTimeUtilities.addSeconds(new Timestamp(System.currentTimeMillis()), -1);
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setTimestamp(1, now);
			prepStmt.setTimestamp(2, now);
			prepStmt.setString(3, deviceId);
			prepStmt.setString(4, exceptToken);
			prepStmt.setTimestamp(5, oneSecondAgo);
			prepStmt.setString(6, TokenDescription.LAMBDA.toString());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public boolean expireLambdaToken(String token) {
		boolean success = false;
		String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE TOKEN_ID = ? AND DESCRIPTION = ?";
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setTimestamp(1, now);
			prepStmt.setString(2, token);
			prepStmt.setString(3, TokenDescription.LAMBDA.toString());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (Exception e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public void expireResync(String deviceId) {
		new Thread(() -> {
			Timestamp tenMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(10);
			String query = "UPDATE B2F_TOKEN SET EXPIRE_TIME = ? WHERE DEVICE_ID = ? AND DESCRIPTION = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, tenMinutesAgo);
				prepStmt.setString(2, deviceId);
				prepStmt.setString(3, TokenDescription.RESYNC.toString());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				addLog("expireResync", "token row updated");
			} catch (SQLException e) {
				addLog("expireResync", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void updateToken(TokenDbObj token) {
		new Thread(() -> {
			String query = "UPDATE B2F_TOKEN SET GROUP_ID = ?, DEVICE_ID = ?, AUTHORIZATION_TIME = ?, EXPIRE_TIME = ?, "
					+ "LAST_CHECK = ?, LAST_UPDATE = ?, DESCRIPTION = ?, "
					+ "NEEDS_UPDATE = ?, BROWSER_ID = ?, PERMISSION = ? WHERE TOKEN_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, token.getGroupId());
				prepStmt.setString(2, token.getDeviceId());
				prepStmt.setTimestamp(3, token.getAuthorizationTime());
				prepStmt.setString(4, DateTimeUtilities.utilDateToSqlStringDt(token.getExpireTime()));
				prepStmt.setTimestamp(5, token.getLastCheck());
				prepStmt.setTimestamp(6, token.getLastUpdate());
				prepStmt.setString(7, token.getDescription());
				prepStmt.setBoolean(8, token.getNeedsUpdate());
				prepStmt.setString(9, token.getBrowserId());
				prepStmt.setInt(10, token.getPermission());
				prepStmt.setString(11, token.getTokenId());
				logQuery(prepStmt);
				prepStmt.executeUpdate();
				addLog("updateToken", "token row updated");
			} catch (SQLException e) {
				addLog("updateToken", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void recordPush(DeviceDbObj centralDevice, DeviceDbObj peripheral) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		String firstLetter = GeneralUtilities.randomLetters(1);
		String[] centralInstanceIdPair = new Encryption().createEncryptedInstanceId(centralDevice, firstLetter);
		if (peripheral != null) {
			String[] peripheralInstanceIdPair = new Encryption().createEncryptedInstanceId(peripheral, firstLetter);
			dataAccess.addLog("addNewCheck", "encryptedCentralId: " + centralInstanceIdPair[1]);
			dataAccess.addLog("addNewCheck", "encryptedPeripheralId: " + peripheralInstanceIdPair[1]);
			String serviceUuid = null;
			if (peripheral.getDeviceId().equals(centralDevice.getDeviceId())) {
				DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(peripheral, true);
				serviceUuid = connection.getServiceUuid();
			}
			Timestamp timestamp = DateTimeUtilities.getCurrentTimestampPlusSeconds(90);
			CheckDbObj check = new CheckDbObj(GeneralUtilities.randomString(), GeneralUtilities.randomString(20),
					centralDevice.getDeviceId(), peripheral.getDeviceId(), serviceUuid, centralDevice.getUserId(), "",
					"", null, null, false, false, Outcomes.INCOMPLETE, DateTimeUtilities.getCurrentTimestamp(), null,
					false, CheckType.PUSH, "", "", timestamp);
			dataAccess.addLog("addNewCheck", "added Check");
			dataAccess.addCheck(check);
		} else {
			dataAccess.addLog("addNewCheck", "peripheral was null for id");
		}
	}

	public void addCheck(CheckDbObj check) {
		String query = "INSERT INTO B2F_CHECK (" + checkFields
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		new Thread(() -> {
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, check.getCheckId());
				prepStmt.setString(2, check.getInstanceId());
				prepStmt.setString(3, check.getCentralDeviceId());
				prepStmt.setString(4, check.getPeripheralDeviceId());
				if (check.getServiceUuid() != null) {
					prepStmt.setString(5, check.getServiceUuid().toLowerCase());
				} else {
					prepStmt.setString(5, null);
				}
				prepStmt.setString(6, check.getUserId());
				prepStmt.setString(7, check.getCentralBssid());
				prepStmt.setString(8, check.getCentralSsid());
				prepStmt.setString(9, check.getPeripheralBssid());
				prepStmt.setString(10, check.getPeripheralSsid());
				prepStmt.setBoolean(11, check.isExpired());
				prepStmt.setBoolean(12, check.isCompleted());
				prepStmt.setInt(13, check.getOutcome());
				prepStmt.setTimestamp(14, check.getCreateDate());
				prepStmt.setTimestamp(15, check.getCompletionDate());
				prepStmt.setBoolean(16, check.isVerfiedReceipt());
				if (check.getCheckType() != null) {
					prepStmt.setString(17, check.getCheckType().checkTypeName().toLowerCase());
				} else {
					prepStmt.setString(17, CheckType.PROX.checkTypeName().toLowerCase());
				}
				prepStmt.setString(18, check.getCentralInstanceId());
				prepStmt.setString(19, check.getPeripheralInstanceId());
				prepStmt.setTimestamp(20, check.getExpirationDate());
				logQueryImportant("addCheck", prepStmt);

				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog(check.getCentralDeviceId(), "addCheck", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	public void addAuthorization(AuthorizationDbObj auth) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_AUTHORIZATION (" + authFields + ") VALUES (" + "?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, auth.getUserId());
				prepStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				addLog("authorization time: " + auth.getAuthorizationTime());
				prepStmt.setBoolean(3, auth.isAuthorizationComplete());
				prepStmt.setString(4, auth.getRequestingDevice());
				prepStmt.setString(5, auth.getCentralDevice());
				logQueryImportant("addAuthorization", prepStmt);
				prepStmt.executeUpdate();
				addLog("authorization added to db");
			} catch (SQLException e) {
				addLog(e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

	// DELETE FUNCTIONS
	public boolean deleteGroupByUser(String user) {
		boolean success = false;
		String query = "DELETE FROM B2F_GROUP WHERE GROUP_NAME = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, user);
			logQuery("deleteGroupByUser", prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (SQLException e) {
			this.addLog("deleteGroupByUser", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public boolean deleteConnectionByPeripheralDevice(DeviceDbObj device) {
		return deleteConnectionByPeripheralDevId(device.getDeviceId());
	}

	private boolean deleteConnectionByPeripheralDevId(String deviceId) {
		boolean success = false;
		String query = "DELETE FROM B2F_DEVICE_CONNECTION WHERE PERIPHERAL_DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
			success = true;
		} catch (SQLException e) {
			addLog("deleteConnectionByPeripheralDevId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public void addAccessCode(AccessCodeDbObj accessCode, String caller) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_ACCESS_STRINGS VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			ResultSet rs = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setTimestamp(1, accessCode.getCreateDate());
				prepStmt.setString(2, accessCode.getAccessCode());
				prepStmt.setString(3, accessCode.getCompanyId());
				prepStmt.setString(4, accessCode.getServerId());
				prepStmt.setString(5, accessCode.getDeviceId());
				prepStmt.setInt(6, accessCode.getPermissions());
				prepStmt.setBoolean(7, accessCode.isActive());
				prepStmt.setString(8, accessCode.getBrowserId());
				prepStmt.setBoolean(9, accessCode.isOneTimeAccess());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("addAccessCode", e);
			} finally {
				MySqlConn.close(rs, prepStmt, conn);
			}
		}).start();
	}

}

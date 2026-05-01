package com.humansarehuman.blue2factor.dataAndAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.http.util.TextUtils;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;

public class GroupDataAccess extends DeviceDataAccess {
	public GroupDbObj getGroupById(String groupId) {
		GroupDbObj group = null;
		String query = "SELECT * FROM B2F_GROUP WHERE GROUP_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, groupId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("group found");
				group = recordToGroup(rs);
				addLog("group != null ? :" + (group != null));
			} else {
				addLog("no group found for group ID: " + groupId);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	public GroupDbObj getActiveGroupById(String groupId) {
		GroupDbObj group = null;
		String query = "SELECT * FROM B2F_GROUP WHERE GROUP_ID = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, groupId);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("group found");
				group = recordToGroup(rs);
				addLog("group != null ? :" + (group != null));
			} else {
				addLog("no group found for group ID: " + groupId);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	public GroupDbObj getActiveGroupFromToken(TokenDbObj token) {
		GroupDbObj group = null;
		if (!TextUtils.isEmpty(token.getGroupId())) {
			group = getActiveGroupById(token.getGroupId());
		} else {
			group = getActiveGroupByDeviceId(token.getDeviceId());
		}
		return group;
	}
	
	public TokenDbObj getDefaultBrowserTokenForChris() {
		TokenDbObj token = null;
		String query = "SELECT * FROM B2F_TOKEN WHERE GROUP_ID = ? AND DESCRIPTION = ? AND "
				+ "EXPIRE_TIME > ? ORDER BY EXPIRE_TIME DESC LIMIT 1";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, Constants.CHRIS_GROUP_ID);
			prepStmt.setString(2, TokenDescription.BROWSER_TOKEN.toString());
			prepStmt.setTimestamp(3, DateTimeUtilities.getCurrentTimestamp());
			this.logQuery(query, prepStmt);
			rs = executeQuery(prepStmt);
			
			if (rs.next()) {
				token = this.recordToToken(rs);
			}
		} catch (SQLException e) {
			this.addLog("getGroupById", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		
		return token;
	}

	public GroupDbObj getGroupByDeviceId(String deviceId) {
		GroupDbObj group = null;
		String query = "SELECT gp.* FROM B2F_GROUP gp JOIN B2F_DEVICE dev ON gp.GROUP_ID = dev.GROUP_ID WHERE dev.DEVICE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				group = recordToGroup(rs);
			}
		} catch (SQLException e) {
			this.addLog("getGroupById", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	public GroupDbObj getActiveGroupByDeviceId(String deviceId) {
		GroupDbObj group = null;
		String query = "SELECT gp.* FROM B2F_GROUP gp JOIN B2F_DEVICE dev ON gp.GROUP_ID = dev.GROUP_ID WHERE "
				+ "dev.DEVICE_ID = ? AND gp.ACTIVE = ?";
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
				group = recordToGroup(rs);
			}
		} catch (SQLException e) {
			this.addLog("getGroupById", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	public GroupDbObj getGroupByUid(String uid, String companyId) {
		GroupDbObj group = null;
		String query = "SELECT * FROM B2F_GROUP WHERE UID = ? AND COMPANY_ID = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, uid);
			prepStmt.setString(2, companyId);
			prepStmt.setBoolean(3, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				group = recordToGroup(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getGroupByUid", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	public int getDeviceCountForGroup(String groupId, boolean activeOnly) {
		int licenseCount = 0;
		String query = "SELECT COUNT(DEVICE_ID) AS TOTAL FROM B2F_DEVICE WHERE GROUP_ID = ? AND DEVICE_CLASS != ?";
		if (activeOnly) {
			query += " AND ACTIVE = ?";
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
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				licenseCount = rs.getInt("TOTAL");
			}
		} catch (SQLException e) {
			this.addLog("getDeviceCountForGroup", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return licenseCount;
	}

	public int getDeviceCountForCompany(String companyId, boolean activeOnly) {
		int licenseCount = 0;
		String query = "SELECT COUNT(DEVICE_ID) AS TOTAL FROM B2F_DEVICE WHERE COMPANY_ID = ? AND DEVICE_CLASS != ?";
		if (activeOnly) {
			query += " AND ACTIVE = ?";
		}
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			prepStmt.setString(2, DeviceClass.TEMP.toString());
			if (activeOnly) {
				prepStmt.setBoolean(3, true);
			}
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				licenseCount = rs.getInt("TOTAL");
			}
		} catch (SQLException e) {
			this.addLog("getDeviceCountForGroup", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return licenseCount;
	}

	public GroupDbObj getGroupByUserPw(String username, String pw) {
		GroupDbObj group = null;
		String query = "SELECT * FROM B2F_GROUP WHERE GROUP_NAME = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, username);
			rs = executeQuery(prepStmt);
			Encryption encryption = new Encryption();
			while (rs.next() && rs.getString("SALT") != null) {
				this.addLog("getGroupByUserPw", "group found", LogConstants.DEBUG);
				String enteredPw = encryption.encryptPw(pw, rs.getString("SALT"));
				if (enteredPw.equals(rs.getString("GROUP_PW"))) {
					this.addLog("getGroupByUserPw", "pw correct", LogConstants.DEBUG);
					group = this.recordToGroup(rs);
					break;
				} else {
					this.addLog("getGroupByUserPw", "pw incorrect: " + enteredPw, LogConstants.WARNING);
					group = null;
				}
			}
		} catch (SQLException e) {
			this.addLog("getGroupByUserPw", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	public GroupDbObj getGroupByEmail(String email) {
		GroupDbObj group = null;
//		email = getEquivalentEmail(email);
		String query = "SELECT * FROM B2F_GROUP WHERE GROUP_NAME = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, email);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				group = this.recordToGroup(rs);
			}
		} catch (SQLException e) {
			this.addLog("getGroupByEmail", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	@SuppressWarnings("unused")
	private String getEquivalentEmail(String email) {
		if (email.equalsIgnoreCase("chris@blue2factor.com")) {
			email = "chris@humansarehuman.com";
		}
		return email;
	}

	public GroupDbObj getGroupByEmailAndCompanyId(String email, String companyId) {
		GroupDbObj group = null;
		String query = "SELECT * FROM B2F_GROUP WHERE GROUP_NAME = ? AND COMPANY_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, email);
			prepStmt.setString(2, companyId);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				group = this.recordToGroup(rs);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	public GroupDbObj getGroupByAccessCode(String accessCode) {
		GroupDbObj group = null;
		String query = "SELECT gr.* FROM B2F_GROUP gr JOIN B2F_DEVICE dev ON gr.GROUP_ID = dev.GROUP_ID "
				+ "JOIN B2F_ACCESS_STRINGS ac ON dev.DEVICE_ID = ac.DEVICE_ID WHERE ac.ACCESS_CODE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, accessCode);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				group = this.recordToGroup(rs);
			}
		} catch (SQLException e) {
			this.addLog("getGroupByAccessCode", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	public GroupDbObj getGroupByAdminToken(String token) {
		GroupDbObj group = null;
		String query = "SELECT gr.* FROM B2F_GROUP gr JOIN B2F_TOKEN tk ON gr.GROUP_ID = tk.GROUP_ID "
				+ "WHERE tk.TOKEN_ID = ? AND tk.TOKEN_DESCRIPTION = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, token);
			prepStmt.setString(2, TokenDescription.ADMIN.toString());
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				group = this.recordToGroup(rs);
			}
		} catch (SQLException e) {
			this.addLog("getGroupByToken", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	public GroupDbObj getGroupByToken(String token) {
		GroupDbObj group = null;
		String query = "SELECT gr.* FROM B2F_GROUP gr JOIN B2F_TOKEN tk ON gr.GROUP_ID = tk.GROUP_ID "
				+ "WHERE tk.TOKEN_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, token);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				group = this.recordToGroup(rs);
			}
		} catch (SQLException e) {
			this.addLog("getGroupByToken", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return group;
	}

	public ArrayList<GroupDbObj> getGroupsFromCompany(CompanyDbObj company) {
		return getGroupsByCompanyId(company.getCompanyId());
	}

	public ArrayList<GroupDbObj> getGroupsByCompanyId(String companyId) {
		ArrayList<GroupDbObj> groups = new ArrayList<>();
		String query = "SELECT * FROM B2F_GROUP WHERE COMPANY_ID = ? AND " + "ACTIVE = ? ORDER BY GROUP_NAME";
		GroupDbObj group;
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				group = this.recordToGroup(rs);
				groups.add(group);
			}
		} catch (SQLException e) {
			this.addLog("getGroupsByCompanyId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return groups;
	}

	public ArrayList<GroupDbObj> getGroupsByCompanyWithoutAnonymous(CompanyDbObj company) {
		return getGroupsByCompanyIdWithoutAnonymous(company.getCompanyId());
	}

	public ArrayList<GroupDbObj> getGroupsByCompanyIdWithoutAnonymous(String companyId) {
		ArrayList<GroupDbObj> groups = new ArrayList<>();
		String query = "SELECT * FROM B2F_GROUP WHERE COMPANY_ID = ? AND ACTIVE = ? AND GROUP_NAME != ? ORDER BY GROUP_NAME";
		GroupDbObj group;
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
		} catch (SQLException e) {
			this.addLog("getGroupsByCompanyId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return groups;
	}

	public boolean isLicenseAvailableForGroup(String groupId) {
		boolean success = false;
		CompanyDataAccess companyDataAccess = new CompanyDataAccess();
		CompanyDbObj company = new CompanyDataAccess().getCompanyByGroupId(groupId);
		if (company != null) {
			int licensesInUse = companyDataAccess.getLicensesInUse(company.getCompanyId());
			if (company.getLicenseCount() > licensesInUse) {
				success = true;
			}
		} else {
			addLog("isLicenseAvailable", "company was null", LogConstants.INFO);
		}
		return success;
	}

	public void addGroup(GroupDbObj group) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_GROUP (" + groupFields + ") VALUES ("
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, group.getCompanyId());
				prepStmt.setString(2, group.getGroupId());
				prepStmt.setString(3, group.getGroupName());
				prepStmt.setBoolean(4, group.isActive());
				prepStmt.setInt(5, group.getAcceptedTypes());
				prepStmt.setTimestamp(6, DateTimeUtilities.getCurrentTimestamp());
				prepStmt.setInt(7, group.getTimeoutSecs());
				prepStmt.setString(8, group.getGroupPw());
				prepStmt.setString(9, group.getSalt());
				prepStmt.setInt(10, group.getDevicesAllowed());
				prepStmt.setInt(11, group.getDevicesInUse());
				prepStmt.setInt(12, group.getPermissions());
				prepStmt.setString(13, group.getUsername());
				prepStmt.setTimestamp(14, DateTimeUtilities.getCurrentTimestamp());
				prepStmt.setString(15, group.getUid());
				prepStmt.setString(16, group.getUserType().toString());
				prepStmt.setBoolean(17, group.isUserExempt());
				prepStmt.setBoolean(18, group.isPushAllowed());
				prepStmt.setBoolean(19, group.isTextAllowed());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				addLog("addGroup", "group added to db");
			} catch (SQLException e) {
				addLog("addGroup", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	public void updateGroup(GroupDbObj group) {
		new Thread(() -> {
			String query = "UPDATE B2F_GROUP SET COMPANY_ID = ?, GROUP_NAME = ?, ACTIVE = ?, ACCEPTED_TYPES = ?, "
					+ "CREATE_DATE = ?, TIMEOUT_SECS = ?, GROUP_PW = ?, SALT = ?, DEVICES_ALLOWED = ?, "
					+ "DEVICES_IN_USE = ?, PERMISSIONS = ?, TOKEN_DATE = ?, UID = ?, USER_TYPE = ?, USERNAME = ?, "
					+ "USER_EXEMPT = ?, PUSH_ALLOWED = ?, TEXT_ALLOWED = ? WHERE GROUP_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, group.getCompanyId());
				prepStmt.setString(2, group.getGroupName());
				prepStmt.setBoolean(3, group.isActive());
				prepStmt.setInt(4, group.getAcceptedTypes());
				prepStmt.setTimestamp(5, new Timestamp(group.getCreateDate().getTime()));
				prepStmt.setInt(6, group.getTimeoutSecs());
				prepStmt.setString(7, group.getGroupPw());
				prepStmt.setString(8, group.getSalt());
				prepStmt.setInt(9, group.getDevicesAllowed());
				prepStmt.setInt(10, group.getDevicesInUse());
				prepStmt.setInt(11, group.getPermissions());
				Timestamp tokenDate = new Timestamp(System.currentTimeMillis());
				if (group.getTokenDate() != null) {
					tokenDate = group.getTokenDate();
				}
				prepStmt.setTimestamp(12, tokenDate);
				prepStmt.setString(13, group.getUid());
				prepStmt.setString(14, group.getUserType().toString());
				prepStmt.setString(15, group.getUsername());
				prepStmt.setBoolean(16, group.isUserExempt());
				prepStmt.setBoolean(17, group.isPushAllowed());
				prepStmt.setBoolean(18, group.isTextAllowed());
				prepStmt.setString(19, group.getGroupId());

				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
				addLog("updateGroup", "group update in db");
			} catch (SQLException e) {
				addLog("updateGroup", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

}

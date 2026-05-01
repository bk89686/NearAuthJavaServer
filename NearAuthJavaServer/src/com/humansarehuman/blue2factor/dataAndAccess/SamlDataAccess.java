package com.humansarehuman.blue2factor.dataAndAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.entities.enums.SignatureStatus;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlAuthnRequestDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlIdentityProviderDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlServiceProviderDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class SamlDataAccess extends CompanyDataAccess {

	public boolean isSuccessfulAuthnByGroup(GroupDbObj group) {
		boolean success = false;
		Timestamp fiveMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(5);
		String query = "SELECT * FROM B2F_SAML_AUTHN_REQUEST WHERE GROUP_ID = ? AND "
				+ "OUTCOME = ? AND EXPIRED = ? AND CREATE_DATE >= ? ORDER BY CREATE_DATE DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, group.getGroupId());
			prepStmt.setInt(2, Outcomes.SUCCESS);
			prepStmt.setBoolean(3, false);
			prepStmt.setTimestamp(4, fiveMinutesAgo);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("record found", LogConstants.IMPORTANT);
				SamlAuthnRequestDbObj authnRequest = recordToSamlAuthRequest(rs);
				authnRequest.setExpired(true);
				this.updateSamlAuthRequestByRelayState(authnRequest);
				success = true;
			} else {
				addLog("no records matched", LogConstants.IMPORTANT);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public boolean getSuccessfulAuthnByGroup(GroupDbObj group) {
		boolean success = false;
		Timestamp fiveMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(5);
		String query = "SELECT * FROM B2F_SAML_AUTHN_REQUEST WHERE GROUP_ID = ? AND "
				+ "OUTCOME = ? AND EXPIRED = ? AND CREATE_DATE >= ? ORDER BY CREATE_DATE DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, group.getGroupId());
			prepStmt.setInt(2, Outcomes.SUCCESS);
			prepStmt.setBoolean(3, false);
			prepStmt.setTimestamp(4, fiveMinutesAgo);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("record found", LogConstants.IMPORTANT);
				SamlAuthnRequestDbObj authnRequest = recordToSamlAuthRequest(rs);
				authnRequest.setExpired(true);
				this.updateSamlAuthRequestByRelayState(authnRequest);
				success = true;
			} else {
				addLog("no records matched", LogConstants.IMPORTANT);
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public boolean getSuccessfulAuthnByDevice(DeviceDbObj device) {
		boolean success = false;
		Timestamp fiveMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(5);
		String query = "SELECT * FROM B2F_SAML_AUTHN_REQUEST WHERE DEVICE_ID = ? AND "
				+ "OUTCOME = ? AND EXPIRED = ? AND CREATE_DATE >= ? ORDER BY CREATE_DATE DESC";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, device.getDeviceId());
			prepStmt.setInt(2, Outcomes.SUCCESS);
			prepStmt.setBoolean(3, false);
			prepStmt.setTimestamp(4, fiveMinutesAgo);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("getSuccessfulAuthnByGroup", "record found", LogConstants.IMPORTANT);
				SamlAuthnRequestDbObj authnRequest = recordToSamlAuthRequest(rs);
				authnRequest.setExpired(true);
				this.updateSamlAuthRequestByRelayState(authnRequest);
				success = true;
			} else {
				addLog("getSuccessfulAuthnByGroup", "no records matched", LogConstants.IMPORTANT);
			}
		} catch (SQLException e) {
			addLog("getSuccessfulAuthnByGroup", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return success;
	}

	public void expireAllRequestForGroup(String groupId) {
		String query = "UPDATE B2F_SAML_AUTHN_REQUEST SET EXPIRED = ? WHERE GROUP_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setBoolean(1, true);
			prepStmt.setString(2, groupId);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
	}

	public void deactivateSamlIdentityProvider(String companyId) {
		SamlIdentityProviderDbObj samlIdp = getSamlIdpFromCompanyId(companyId);
		if (samlIdp != null) {
			samlIdp.setActive(false);
			this.updateSamlIdentityProvider(samlIdp);
		}
	}

	public SamlServiceProviderDbObj getServiceProviderByAcsUrl(String acsUrl, String companyId) {
		SamlServiceProviderDbObj serviceProvider = null;
		String query = "SELECT * FROM B2F_SERVICE_PROVIDER WHERE ACS_URL = ? AND COMPANY_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, acsUrl);
			prepStmt.setString(2, companyId);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				serviceProvider = this.recordToSamlServiceProvider(rs, "getServiceProviderByAcsUrl");
			}
		} catch (SQLException e) {
			addLog("updateSamlAuthRequestByRelayState", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return serviceProvider;
	}

	public void deleteServiceProvider(String tableId) {
		String query = "DELETE FROM B2F_SAML_SERVICE_PROVIDER WHERE TABLE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tableId);
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog("deleteServiceProvider", e);
		} finally {
			MySqlConn.close(prepStmt, conn);
		}
	}

	public SamlServiceProviderDbObj getServiceProviderByTableId(String tableId) {
		SamlServiceProviderDbObj serviceProvider = null;
		String query = "SELECT * FROM B2F_SAML_SERVICE_PROVIDER WHERE TABLE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tableId);
			logQuery(getMethodName(), prepStmt);
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				serviceProvider = this.recordToSamlServiceProvider(rs, "getServiceProviderByTableId");
			}
		} catch (SQLException e) {
			addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return serviceProvider;
	}

	public void updateSamlAuthRequestByRelayState(SamlAuthnRequestDbObj authn) {
		new Thread(() -> {
			String query = "UPDATE B2F_SAML_AUTHN_REQUEST SET OUTGOING_REQUEST_ID = ?, INCOMING_REQUEST_ID = ?, "
					+ "ISSUE_INSTANCE = ?, ISSUER = ?, OUTGOING_ACS_URL = ?, INCOMING_ACS_URL = ?, ENCRYPTED_AUTHN_REQUEST = ?, "
					+ "IP_ADDRESS = ?, EXPIRED = ?, SIGNATURE_STATUS = ?, SAML_IDENTITY_PROVIDER_ID = ?, "
					+ "COMPANY_ID = ?, IDENTITY_PROVIDER_NAME = ?, GROUP_ID = ?, DEVICE_ID = ?, TOKEN = ?, "
					+ "OUTCOME = ?, SENDER = ? WHERE OUTGOING_RELAY_STATE = ?";
			addLog("updateSamlAuthRequestByRelayState", "relayState: " + authn.getOutgoingRelayState());
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, authn.getOutgoingRequestId());
				prepStmt.setString(2, authn.getIncomingRequestId());
				prepStmt.setTimestamp(3, authn.getIssueInstance());
				prepStmt.setString(4, authn.getIssuer());
				prepStmt.setString(5, authn.getOutgoingAcsUrl());
				prepStmt.setString(6, authn.getIncomingAcsUrl());
				prepStmt.setString(7, authn.getEncryptedAuthnRequest());
				prepStmt.setString(8, authn.getIpAddress());
				prepStmt.setBoolean(9, authn.isExpired());
				prepStmt.setInt(10, authn.getSignatureStatus().getValue());
				prepStmt.setString(11, authn.getSamlIdentityProviderId());
				prepStmt.setString(12, authn.getCompanyId());
				prepStmt.setString(13, authn.getIdentityProviderName());
				prepStmt.setString(14, authn.getGroupId());
				prepStmt.setString(15, authn.getDeviceId());
				prepStmt.setString(16, authn.getToken());
				prepStmt.setInt(17, authn.getOutcome());
				prepStmt.setString(18, authn.getSender());
				prepStmt.setString(19, authn.getOutgoingRelayState());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog("updateSamlAuthRequestByRelayState", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	public void updateSamlAuthRequestByTableId(SamlAuthnRequestDbObj authn) {
		new Thread(() -> {
			String query = "UPDATE B2F_SAML_AUTHN_REQUEST SET OUTGOING_REQUEST_ID = ?, INCOMING_REQUEST_ID = ?, "
					+ "ISSUE_INSTANCE = ?, ISSUER = ?, OUTGOING_ACS_URL = ?, INCOMING_ACS_URL = ?, ENCRYPTED_AUTHN_REQUEST = ?, IP_ADDRESS = ?, "
					+ "EXPIRED = ?, SIGNATURE_STATUS = ?, SAML_IDENTITY_PROVIDER_ID = ?, "
					+ "COMPANY_ID = ?, IDENTITY_PROVIDER_NAME = ?, GROUP_ID = ?, DEVICE_ID = ?, TOKEN = ?, "
					+ "OUTCOME = ?, SENDER = ?, OUTGOING_RELAY_STATE = ? WHERE TABLE_ID = ?";
			addLog("updateSamlAuthRequestByRelayState", "relayState: " + authn.getOutgoingRelayState());
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, authn.getOutgoingRequestId());
				prepStmt.setString(2, authn.getIncomingRequestId());
				prepStmt.setTimestamp(3, authn.getIssueInstance());
				prepStmt.setString(4, authn.getIssuer());
				prepStmt.setString(5, authn.getOutgoingAcsUrl());
				prepStmt.setString(6, authn.getIncomingAcsUrl());
				prepStmt.setString(7, authn.getEncryptedAuthnRequest());
				prepStmt.setString(8, authn.getIpAddress());
				prepStmt.setBoolean(9, authn.isExpired());
				prepStmt.setInt(10, authn.getSignatureStatus().getValue());
				prepStmt.setString(11, authn.getSamlIdentityProviderId());
				prepStmt.setString(12, authn.getCompanyId());
				prepStmt.setString(13, authn.getIdentityProviderName());
				prepStmt.setString(14, authn.getGroupId());
				prepStmt.setString(15, authn.getDeviceId());
				prepStmt.setString(16, authn.getToken());
				prepStmt.setInt(17, authn.getOutcome());
				prepStmt.setString(18, authn.getSender());
				prepStmt.setString(19, authn.getOutgoingRelayState());
				prepStmt.setString(20, authn.getTableId());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog("updateSamlAuthRequestByRelayState", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	private void updateSamlIdentityProvider(SamlIdentityProviderDbObj samlIdp) {
		new Thread(() -> {
			String query = "UPDATE B2F_SAML_IDENTITY_PROVIDER SET ENTITY_ID = ?, IDENTITY_PROVIDER_NAME = ?, "
					+ "SIGNING_CERTIFICATE = ?, ENCRYPTING_CERTIFICATE = ?, NAME_ID_FORMAT = ?, "
					+ "REDIRECT_URL = ?, POST_URL = ?, ARTIFACT_URL = ?, ACTIVE = ?, REFERENCING_COMPANY_ID = ?, ENCRYPT_REQUEST = ?, "
					+ "SIGN_REQUEST = ?, ACS_URL = ?, DESTINATION_URL = ?, REDIRECT_BINDING = ?, POST_BINDING = ?, ARTIFACT_BINDING = ?, "
					+ "VALID_UNTIL = ? WHERE TABLE_ID = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, samlIdp.getEntityId());
				prepStmt.setString(2, samlIdp.getIdentityProviderName());
				prepStmt.setString(3, samlIdp.getSigningCertificate());
				prepStmt.setString(4, samlIdp.getEncryptingCertificate());
				prepStmt.setString(5, samlIdp.getNameIdFormat());
				prepStmt.setString(6, samlIdp.getRedirectUrl());
				prepStmt.setString(7, samlIdp.getPostBinding());
				prepStmt.setString(8, samlIdp.getArtifactUrl());
				prepStmt.setBoolean(9, samlIdp.isActive());
				prepStmt.setString(10, samlIdp.getReferencingCompany());
				prepStmt.setBoolean(11, samlIdp.isEncryptRequest());
				prepStmt.setBoolean(12, samlIdp.isSignRequest());
				prepStmt.setString(13, samlIdp.getAcsUrl());
				prepStmt.setString(14, samlIdp.getDestinationUrl());
				prepStmt.setString(15, samlIdp.getRedirectBinding());
				prepStmt.setString(16, samlIdp.getPostBinding());
				prepStmt.setString(17, samlIdp.getArtifactBinding());
				prepStmt.setTimestamp(18, samlIdp.getValidUntil());
				prepStmt.setString(19, samlIdp.getTableId());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog("updateSamlIdentityProvider", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	public void addSamlIdentityProvider(SamlIdentityProviderDbObj samlIdentityProvider) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_SAML_IDENTITY_PROVIDER (TABLE_ID, CREATE_DATE, ENTITY_ID, "
					+ "IDENTITY_PROVIDER_NAME, SIGNING_CERTIFICATE, ENCRYPTING_CERTIFICATE, NAME_ID_FORMAT, "
					+ "REDIRECT_URL, POST_URL, ARTIFACT_URL, ACTIVE, REFERENCING_COMPANY_ID, ENCRYPT_REQUEST, "
					+ "SIGN_REQUEST, ACS_URL, DESTINATION_URL, REDIRECT_BINDING, POST_BINDING, ARTIFACT_BINDING, "
					+ "VALID_UNTIL, LOGOUT_URL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, samlIdentityProvider.getTableId());
				prepStmt.setTimestamp(2, DateTimeUtilities.getCurrentTimestamp());
				prepStmt.setString(3, samlIdentityProvider.getEntityId());
				prepStmt.setString(4, samlIdentityProvider.getIdentityProviderName());
				prepStmt.setString(5, samlIdentityProvider.getSigningCertificate());
				prepStmt.setString(6, samlIdentityProvider.getEncryptingCertificate());
				prepStmt.setString(7, samlIdentityProvider.getNameIdFormat());
				prepStmt.setString(8, samlIdentityProvider.getRedirectUrl());
				prepStmt.setString(9, samlIdentityProvider.getPostUrl());
				prepStmt.setString(10, samlIdentityProvider.getArtifactUrl());
				prepStmt.setBoolean(11, samlIdentityProvider.isActive());
				prepStmt.setString(12, samlIdentityProvider.getReferencingCompany());
				prepStmt.setBoolean(13, samlIdentityProvider.isEncryptRequest());
				prepStmt.setBoolean(14, samlIdentityProvider.isSignRequest());
				prepStmt.setString(15, samlIdentityProvider.getAcsUrl());
				prepStmt.setString(16, samlIdentityProvider.getDestinationUrl());
				prepStmt.setString(17, samlIdentityProvider.getRedirectBinding());
				prepStmt.setString(18, samlIdentityProvider.getPostBinding());
				prepStmt.setString(19, samlIdentityProvider.getArtifactBinding());
				prepStmt.setTimestamp(20, samlIdentityProvider.getValidUntil());
				prepStmt.setString(21, samlIdentityProvider.getLogoutUrl());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog("addSamlIdentityProvider", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	public void addSamlServiceProvider(SamlServiceProviderDbObj samlServiceProvider) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_SAML_SERVICE_PROVIDER (TABLE_ID, CREATE_DATE, SERVICE_PROVIDER_ENTITY_ID, "
					+ "SERVICE_PROVIDER_ISSUER, SERVICE_PROVIDER_NAME, SERVICE_PROVIDER_URL, "
					+ "SERVICE_PROVIDER_METADATA_URL, ACTIVE, REFERENCING_COMPANY_ID, ACS_URL, "
					+ "LOGOUT_URL, SERVICE_PROVIDER_METADATA, SP_ENCRYPTION_CERT) VALUES "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, samlServiceProvider.getTableId());
				prepStmt.setTimestamp(2, DateTimeUtilities.getCurrentTimestamp());
				prepStmt.setString(3, samlServiceProvider.getServiceProviderEntityId());
				prepStmt.setString(4, samlServiceProvider.getServiceProviderIssuer());
				prepStmt.setString(5, samlServiceProvider.getServiceProviderName());
				prepStmt.setString(6, samlServiceProvider.getServiceProviderUrl());
				prepStmt.setString(7, samlServiceProvider.getServiceProviderMetadataUrl());
				prepStmt.setBoolean(8, samlServiceProvider.isActive());
				prepStmt.setString(9, samlServiceProvider.getReferencingCompanyId());
				prepStmt.setString(10, samlServiceProvider.getAcsUrl());
				prepStmt.setString(11, samlServiceProvider.getLogoutUrl());
				prepStmt.setString(12, samlServiceProvider.getServiceProviderMetadata());
				prepStmt.setString(13, samlServiceProvider.getSpEncryptionCert());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog("addSamlServiceProvider", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	public void addSamlAuthnRequest(SamlAuthnRequestDbObj samlAuthnRequest) {
		new Thread(() -> {
			String query = "INSERT INTO B2F_SAML_AUTHN_REQUEST (IDENTITY_PROVIDER_NAME, CREATE_DATE, "
					+ "SAML_IDENTITY_PROVIDER_ID, OUTGOING_REQUEST_ID, INCOMING_REQUEST_ID, "
					+ "OUTGOING_RELAY_STATE, INCOMING_RELAY_STATE, ISSUE_INSTANCE, ISSUER, OUTGOING_ACS_URL, INCOMING_ACS_URL, SIGNATURE_STATUS, "
					+ "ENCRYPTED_AUTHN_REQUEST, IP_ADDRESS, EXPIRED, COMPANY_ID, GROUP_ID, DEVICE_ID, "
					+ "TOKEN, OUTCOME, SENDER, TABLE_ID) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, samlAuthnRequest.getIdentityProviderName());
				prepStmt.setTimestamp(2, DateTimeUtilities.getCurrentTimestamp());
				prepStmt.setString(3, samlAuthnRequest.getSamlIdentityProviderId());
				prepStmt.setString(4, samlAuthnRequest.getOutgoingRequestId());
				prepStmt.setString(5, samlAuthnRequest.getIncomingRequestId());
				prepStmt.setString(6, samlAuthnRequest.getOutgoingRelayState());
				prepStmt.setString(7, samlAuthnRequest.getIncomingRelayState());
				prepStmt.setTimestamp(8, samlAuthnRequest.getIssueInstance());
				prepStmt.setString(9, samlAuthnRequest.getIssuer());
				prepStmt.setString(10, samlAuthnRequest.getOutgoingAcsUrl());
				prepStmt.setString(11, samlAuthnRequest.getIncomingAcsUrl());
				prepStmt.setInt(12, samlAuthnRequest.getSignatureStatus().getValue());
				prepStmt.setString(13, samlAuthnRequest.getEncryptedAuthnRequest());
				prepStmt.setString(14, samlAuthnRequest.getIpAddress());
				prepStmt.setBoolean(15, samlAuthnRequest.isExpired());
				prepStmt.setString(16, samlAuthnRequest.getCompanyId());
				prepStmt.setString(17, samlAuthnRequest.getGroupId());
				prepStmt.setString(18, samlAuthnRequest.getDeviceId());
				prepStmt.setString(19, samlAuthnRequest.getToken());
				prepStmt.setInt(20, Outcomes.UNKNOWN_STATUS);
				prepStmt.setString(21, samlAuthnRequest.getSender());
				prepStmt.setString(22, samlAuthnRequest.getTableId());
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				addLog(e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	public SamlIdentityProviderDbObj getSamlIdentityProviderFromTableId(String tableId) {
		SamlIdentityProviderDbObj samlIdp = null;
		String query = "SELECT * FROM B2F_SAML_IDENTITY_PROVIDER WHERE TABLE_ID = ? AND ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tableId);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				samlIdp = recordToSamlIdentityProvider(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getSamlIdentityProviderFromTableId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return samlIdp;
	}

	public SamlIdentityProviderDbObj getSamlIdentityProviderFromDeviceId(String deviceId) {
		SamlIdentityProviderDbObj samlIdp = null;
		String query = "SELECT idp.* FROM B2F_SAML_IDENTITY_PROVIDER idp JOIN B2F_GROUP grp"
				+ " ON idp.REFERENCING_COMPANY_ID = grp.COMPANY_ID JOIN B2F_DEVICE device "
				+ "ON grp.GROUP_ID = device.GROUP_ID WHERE DEVICE_ID = ? AND idp.ACTIVE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setBoolean(2, true);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				samlIdp = recordToSamlIdentityProvider(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getSamlIdentityProviderFromDeviceId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return samlIdp;
	}

	public SamlAuthnRequestDbObj updateSamlAuthnRequestOutcome(SamlAuthnRequestDbObj samlAuthnRequest, int outcome,
			String ipAddress) { // , String issuer) {
		samlAuthnRequest.setOutcome(outcome);
		samlAuthnRequest.setIpAddress(ipAddress);
		// samlAuthnRequest.setIssuer(issuer);
		this.updateSamlAuthRequestByRelayState(samlAuthnRequest);
		return samlAuthnRequest;
	}

	public SamlAuthnRequestDbObj updateSamlAuthnRequestOutcome(SamlAuthnRequestDbObj samlAuthnRequest, int outcome) {
		addLog("updating response");
		samlAuthnRequest.setOutcome(outcome);
		this.updateSamlAuthRequestByRelayState(samlAuthnRequest);
		return samlAuthnRequest;
	}

	public SamlIdentityProviderDbObj getSamlIdpFromCompany(CompanyDbObj company) {
		return getSamlIdpFromCompanyId(company.getCompanyId());
	}

	public SamlAuthnRequestDbObj recentlyAuthenticatedOnInstall(DeviceDbObj device) {
		return this.recentlyAuthenticatedOnInstall(device.getDeviceId());
	}

	/**
	 * returns the recent authentication record if there is one, null if there isn't
	 * 
	 * @param deviceId
	 * @return
	 */
	public SamlAuthnRequestDbObj recentlyAuthenticatedOnInstall(String deviceId) {
		SamlAuthnRequestDbObj samlRequest = null;
		String query = "SELECT * FROM B2F_SAML_AUTHN_REQUEST WHERE DEVICE_ID = ? AND SIGNATURE_STATUS = ? AND CREATE_DATE > ? "
				+ "AND (SENDER = ? || SENDER = ?) ";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			Timestamp fiveMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(15);
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			prepStmt.setInt(2, Outcomes.SUCCESS);
			prepStmt.setTimestamp(3, fiveMinutesAgo);
			prepStmt.setString(4, "app");
			prepStmt.setString(5, "appDone");
			rs = executeQuery(prepStmt);
			if (rs.next()) {
				addLog("recentlyAuthenticatedOnInstall", "record found", LogConstants.IMPORTANT);
				samlRequest = this.recordToSamlAuthRequest(rs);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return samlRequest;
	}

	public SamlIdentityProviderDbObj getSamlIdpFromCompanyId(String companyId) {
		SamlIdentityProviderDbObj samlIdp = null;
		String query = "SELECT * FROM B2F_SAML_IDENTITY_PROVIDER WHERE REFERENCING_COMPANY_ID = ? AND ACTIVE = ?";
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
				samlIdp = recordToSamlIdentityProvider(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getSamlIdpFromCompany", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return samlIdp;
	}

	public SamlAuthnRequestDbObj getAuthRequestByOutgoingRelayState(String relayState) {
		SamlAuthnRequestDbObj samlAuthReq = null;
		String query = "SELECT * FROM B2F_SAML_AUTHN_REQUEST WHERE OUTGOING_RELAY_STATE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, relayState);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				addLog("getAuthRequestByOutgoingRelayState", "record found", LogConstants.INFO);
				samlAuthReq = recordToSamlAuthRequest(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return samlAuthReq;
	}

	public SamlAuthnRequestDbObj getAuthRequestByOutgoingRequestId(String requestId) {
		SamlAuthnRequestDbObj samlAuthReq = null;
		String query = "SELECT * FROM B2F_SAML_AUTHN_REQUEST WHERE OUTGOING_REQUEST_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, requestId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				addLog("getAuthRequestByOutgoingRequestId", "record found", LogConstants.IMPORTANT);
				samlAuthReq = recordToSamlAuthRequest(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return samlAuthReq;
	}

	public SamlAuthnRequestDbObj getAuthRequestByIncomingRelayState(String relayState) {
		SamlAuthnRequestDbObj samlAuthReq = null;
		String query = "SELECT * FROM B2F_SAML_AUTHN_REQUEST WHERE INCOMING_RELAY_STATE = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, relayState);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				addLog("getAuthRequestByIncomingRelayState", "record found", LogConstants.INFO);
				samlAuthReq = recordToSamlAuthRequest(rs);
				addLog("tableId: " + samlAuthReq.getTableId());
				break;
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return samlAuthReq;
	}

	public SamlAuthnRequestDbObj getSamlAuthnRequestByTableId(String tableId) {
		SamlAuthnRequestDbObj samlAuthReq = null;
		String query = "SELECT * FROM B2F_SAML_AUTHN_REQUEST WHERE TABLE_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, tableId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				addLog("getSamlAuthnRequestByTableId", "record found", LogConstants.IMPORTANT);
				samlAuthReq = recordToSamlAuthRequest(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getSamlAuthnRequestByTableId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return samlAuthReq;
	}

	public SamlAuthnRequestDbObj getSamlAuthnRequestByOutgoingId(String reqId) {
		SamlAuthnRequestDbObj samlAuthReq = null;
		String query = "SELECT * FROM B2F_SAML_AUTHN_REQUEST WHERE OUTGOING_REQUEST_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, reqId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				addLog("getSamlAuthnRequestByOutgoingId", "record found", LogConstants.IMPORTANT);
				samlAuthReq = recordToSamlAuthRequest(rs);
				break;
			}
		} catch (SQLException e) {
			this.addLog("getSamlAuthnRequestById", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return samlAuthReq;
	}

	public ArrayList<SamlServiceProviderDbObj> getSamlServiceProvidersbyCompanyId(String companyId) {
		ArrayList<SamlServiceProviderDbObj> serviceProviders = new ArrayList<>();
		String query = "SELECT * FROM B2F_SAML_SERVICE_PROVIDER WHERE REFERENCING_COMPANY_ID = ? "
				+ "AND SERVICE_PROVIDER_NAME IS NOT NULL AND ACTIVE = ? ORDER BY SERVICE_PROVIDER_NAME";
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
				SamlServiceProviderDbObj samlService = recordToSamlServiceProvider(rs,
						"getSamlServiceProvidersbyCompanyId");
				serviceProviders.add(samlService);
			}
		} catch (SQLException e) {
			this.addLog(e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		this.addLog("sp count: " + serviceProviders.size());
		return serviceProviders;
	}

	/**
	 * this makes no sense
	 * 
	 * @param companyId
	 * @return
	 */
	public SamlServiceProviderDbObj getSamlServiceProviderbyCompanyId(String companyId) {
		SamlServiceProviderDbObj samlService = null;
		String query = "SELECT * FROM B2F_SAML_SERVICE_PROVIDER WHERE REFERENCING_COMPANY_ID = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, companyId);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				samlService = recordToSamlServiceProvider(rs, "getSamlServiceProviderbyCompanyId");
				break;
			}
		} catch (SQLException e) {
			this.addLog("getSamlServiceProviderbyCompanyId", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		this.addLog("getSamlServiceProviderbyCompanyId", "sp exists? " + (samlService != null));
		return samlService;
	}

	public SamlServiceProviderDbObj getSamlServiceProviderFromIssuer(String issuer) {
		SamlServiceProviderDbObj samlService = null;
		String query = "SELECT * FROM B2F_SAML_SERVICE_PROVIDER WHERE SERVICE_PROVIDER_ISSUER = ?";
		Connection conn = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, issuer);
			rs = executeQuery(prepStmt);
			while (rs.next()) {
				samlService = recordToSamlServiceProvider(rs, "getSamlServiceProviderFromIssuer");
				break;
			}
		} catch (SQLException e) {
			this.addLog("getSamlServiceProviderFromIssuer", e);
		} finally {
			MySqlConn.close(rs, prepStmt, conn);
		}
		return samlService;
	}

	public void expireDefaultServiceProviderByCompanyId(String companyId) {
		new Thread(() -> {
			String query = "UPDATE B2F_SAML_SERVICE_PROVIDER SET ACTIVE = ? WHERE "
					+ "SERVICE_PROVIDER_ENTITY_ID = ? AND COMPANY_ID = ? AND ACTIVE = ?";
			Connection conn = null;
			PreparedStatement prepStmt = null;
			try {
				conn = MySqlConn.getConnection();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setBoolean(1, false);
				prepStmt.setString(2, "");
				prepStmt.setString(3, companyId);
				prepStmt.setBoolean(4, true);
				logQuery(getMethodName(), prepStmt);
				prepStmt.executeUpdate();
			} catch (SQLException e) {
				this.addLog("expireDefaultServiceProviderByCompanyId", e);
			} finally {
				MySqlConn.close(prepStmt, conn);
			}
		}).start();
	}

	public void updateServiceProvider(SamlServiceProviderDbObj serviceProvider) {
		String query = "UPDATE B2F_SAML_SERVICE_PROVIDER SET SERVICE_PROVIDER_ENTITY_ID = ?, "
				+ "SERVICE_PROVIDER_ISSUER = ?, SERVICE_PROVIDER_NAME = ?, SERVICE_PROVIDER_URL = ?, "
				+ "SERVICE_PROVIDER_METADATA_URL = ?, ACTIVE = ?, REFERENCING_COMPANY_ID = ?, "
				+ "ACS_URL = ?, SERVICE_PROVIDER_METADATA = ? WHERE TABLE_ID = ?";

		Connection conn = null;
		PreparedStatement prepStmt = null;
		try {
			conn = MySqlConn.getConnection();
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, serviceProvider.getServiceProviderEntityId());
			prepStmt.setString(2, serviceProvider.getServiceProviderIssuer());
			prepStmt.setString(3, serviceProvider.getServiceProviderName());
			prepStmt.setString(4, serviceProvider.getServiceProviderUrl());
			prepStmt.setString(5, serviceProvider.getServiceProviderMetadataUrl());
			prepStmt.setBoolean(6, serviceProvider.isActive());
			prepStmt.setString(7, serviceProvider.getReferencingCompanyId());
			prepStmt.setString(8, serviceProvider.getAcsUrl());
			prepStmt.setString(9, serviceProvider.getServiceProviderMetadata());
			prepStmt.setString(10, serviceProvider.getTableId());
			logQuery(getMethodName(), prepStmt);
			prepStmt.executeUpdate();
		} catch (SQLException e) {
			this.addLog("expireDefaultServiceProviderByCompanyId", e);
		} finally {
			MySqlConn.close(prepStmt, conn);

		}
	}

	private SamlAuthnRequestDbObj recordToSamlAuthRequest(ResultSet rs) {
		SamlAuthnRequestDbObj samlAuthnReq = null;
		try {
			String tableId = rs.getString("TABLE_ID");
			String identityProviderName = rs.getString("IDENTITY_PROVIDER_NAME");
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			String identityProviderId = rs.getString("SAML_IDENTITY_PROVIDER_ID");
			String outgoingRequestId = rs.getString("OUTGOING_REQUEST_ID");
			String incomingRequestId = rs.getString("INCOMING_REQUEST_ID");
			String outgoingRelayState = rs.getString("OUTGOING_RELAY_STATE");
			String incomingRelayState = rs.getString("INCOMING_RELAY_STATE");
			addLog("recordToSamlAuthRequest", "relayState: " + outgoingRelayState);
			Timestamp issueInstance = rs.getTimestamp("ISSUE_INSTANCE");
			String issuer = rs.getString("ISSUER");
			String outgoingAcsUrl = rs.getString("OUTGOING_ACS_URL");
			String incomingAcsUrl = rs.getString("INCOMING_ACS_URL");
			int sigStatusInt = rs.getInt("SIGNATURE_STATUS");
			String companyId = rs.getString("COMPANY_ID");
			String groupId = rs.getString("GROUP_ID");
			String deviceId = rs.getString("DEVICE_ID");
			String token = rs.getString("TOKEN");
			int outcome = rs.getInt("OUTCOME");
			String sender = rs.getString("SENDER");
			this.addLog("recordToSamlAuthRequest", "sender: " + sender);
			SignatureStatus signatureStatus;
			switch (sigStatusInt) {
			case 1:
				signatureStatus = SignatureStatus.SUCCESS;
				break;
			case 2:
				signatureStatus = SignatureStatus.FAILURE;
				break;
			default:
				signatureStatus = SignatureStatus.UNVALIDATE;
			}
			String encryptedAuthnRequest = rs.getString("ENCRYPTED_AUTHN_REQUEST");
			String ipAddress = rs.getString("IP_ADDRESS");
			Boolean expired = rs.getBoolean("EXPIRED");

			samlAuthnReq = new SamlAuthnRequestDbObj(tableId, identityProviderName, createDate, identityProviderId,
					outgoingRequestId, incomingRequestId, outgoingRelayState, incomingRelayState, issueInstance, issuer,
					outgoingAcsUrl, incomingAcsUrl, signatureStatus, encryptedAuthnRequest, ipAddress, expired,
					companyId, groupId, deviceId, token, outcome, sender);
			addLog("recordToSamlAuthRequest", "SamlAuthnRequestDbObj built");
		} catch (Exception e) {
			addLog("recordToSamlAuthRequest", e);
		}
		return samlAuthnReq;
	}

	private SamlIdentityProviderDbObj recordToSamlIdentityProvider(ResultSet rs) {
		SamlIdentityProviderDbObj samlIdp = null;
		try {
			String tableId = rs.getString("TABLE_ID");
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			String entityId = rs.getString("ENTITY_ID");
			String identityProviderName = rs.getString("IDENTITY_PROVIDER_NAME");
			String signingCertificate = rs.getString("SIGNING_CERTIFICATE");
			String encryptingCertificate = rs.getString("ENCRYPTING_CERTIFICATE");
			String nameIdFormat = rs.getString("NAME_ID_FORMAT");
			String redirectUrl = rs.getString("REDIRECT_URL");
			String postUrl = rs.getString("POST_URL");
			String artifactUrl = rs.getString("ARTIFACT_URL");
			boolean active = rs.getBoolean("ACTIVE");
			String companyId = rs.getString("REFERENCING_COMPANY_ID");
			boolean encryptRequest = rs.getBoolean("ENCRYPT_REQUEST");
			boolean signRequest = rs.getBoolean("SIGN_REQUEST");
			String acsUrl = rs.getString("ACS_URL");
			String destinationUrl = rs.getString("DESTINATION_URL");
			String redirectBinding = rs.getString("REDIRECT_BINDING");
			String postBinding = rs.getString("POST_BINDING");
			String artifactBinding = rs.getString("ARTIFACT_BINDING");
			Timestamp validUntil = rs.getTimestamp("VALID_UNTIL");
			String logoutUrl = rs.getString("LOGOUT_URL");
			samlIdp = new SamlIdentityProviderDbObj(tableId, createDate, entityId, identityProviderName,
					signingCertificate, encryptingCertificate, nameIdFormat, redirectUrl, postUrl, artifactUrl, active,
					companyId, encryptRequest, signRequest, acsUrl, destinationUrl, redirectBinding, postBinding,
					artifactBinding, validUntil, logoutUrl);
		} catch (Exception e) {
			addLog("recordToSamlIdentityProvider", e);
		}
		return samlIdp;
	}

	private SamlServiceProviderDbObj recordToSamlServiceProvider(ResultSet rs, String src) {
		SamlServiceProviderDbObj samlService = null;
		try {
			addLog("from " + src);
			String tableId = rs.getString("TABLE_ID");
			Timestamp createDate = rs.getTimestamp("CREATE_DATE");
			String serviceProviderId = rs.getString("SERVICE_PROVIDER_ENTITY_ID");
			String serviceProviderIssuer = rs.getString("SERVICE_PROVIDER_ISSUER");
			String serviceProviderName = rs.getString("SERVICE_PROVIDER_NAME");
			String serviceProviderUrl = rs.getString("SERVICE_PROVIDER_URL");
			String serviceProviderMetadataUrl = rs.getString("SERVICE_PROVIDER_METADATA_URL");
			boolean active = rs.getBoolean("ACTIVE");
			String referencingCompanyId = rs.getString("REFERENCING_COMPANY_ID");
			String acsUrl = rs.getString("ACS_URL");
			String logoutUrl = rs.getString("LOGOUT_URL");
			String serviceProviderMetadata = rs.getString("SERVICE_PROVIDER_METADATA");
			String spEncryptionCert = rs.getString("SP_ENCRYPTION_CERT");
			addLog("prior to create");
			samlService = new SamlServiceProviderDbObj(tableId, createDate, serviceProviderId, serviceProviderIssuer,
					serviceProviderName, serviceProviderUrl, serviceProviderMetadataUrl, active, referencingCompanyId,
					acsUrl, logoutUrl, serviceProviderMetadata, spEncryptionCert);
			addLog("samlServiceObj created? " + (samlService != null));
		} catch (Exception e) {
			addLog("error found: " + e.getMessage());
			addLog(e);
		}
		return samlService;
	}
}

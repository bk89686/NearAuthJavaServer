package com.humansarehuman.blue2factor.dataAndAccess;

//@Controller
//@RequestMapping("/createTables")
@SuppressWarnings("all")
public class B2fModel { // NO_UCD (unused code)
	/*
	 * Connect to GCC gcloud compute --project "blue2factor-backend" ssh --zone
	 * "us-central1-f" "b2f-be-v3-vm"
	 * 
	 * 
	 * Delete old logs DELETE FROM B2F_LOG WHERE DATE(CREATE_DATE) < '2021-11-21';
	 * 
	 * Delete user and related records DELETE dev, ky, token, brw FROM B2F_DEVICE
	 * dev JOIN B2F_KEY ky ON dev.DEVICE_ID = ky.DEVICE_ID JOIN B2F_TOKEN token ON
	 * dev.DEVICE_ID = token.DEVICE_ID JOIN B2F_BROWSER brw ON
	 * dev.DEVICE_ID=brw.DEVICE_ID JOIN B2F_GROUP grp WHERE grp.GROUP_NAME =
	 * 'chris@blue2factor.com';
	 * 
	 * Select Logs before ... SELECT * FROM B2F_LOG WHERE CREATE_DATE < '2019-10-02
	 * 17:07:40.832' ORDER BY CREATE_DATE DESC LIMIT 20;
	 * 
	 * Get logs from devices SELECT * from B2F_LOG WHERE SRC = 'webLog' ORDER BY
	 * CREATE_DATE DESC LIMIT 100;
	 * 
	 * Get active Device info SELECT DEVICE_ID, DEVICE_TYPE, OPERATING_SYSTEM,
	 * RECENT_PUSHES, LAST_PUSH, CENTRAL, TURNED_OFF, PUBLIC_KEY FROM B2F_DEVICE
	 * WHERE ACTIVE = 1;
	 * 
	 * Get active device connection info SELECT PERIPHERAL_DEVICE_ID,
	 * CENTRAL_DEVICE_ID, SERVICE_UUID, LAST_CHECK, LAST_SUCCESS,
	 * LAST_PERIPHERAL_CONNECTION, BLUETOOTH_CONNECTED, LAST_BLUETOOTH_SUCCESS,
	 * CURRENTLY_CONNECTED, ADVERTISING_PERIPHERAL, CONNECTING_CENTRAL, SUBSCRIBED,
	 * LAST_SUBSCRIBED FROM B2F_DEVICE_CONNECTION;
	 * 
	 * 
	 * Get all device info but rand and public key SELECT GROUP_ID, USER_ID,
	 * DEVICE_ID, SEED, ACTIVE, FCM_ID, CREATE_DATE, BT_ADDRESS, DEVICE_TYPE,
	 * OPERATING_SYSTEM, OS_VERSION, LOGIN_TOKEN, LAST_COMPLETE_CHECK,
	 * LAST_GMT_OFFSET, USER_LANGUAGE, SCREEN_SIZE, SHOW_ICON, DEVICE_PRIORITY,
	 * TRIGGER_UPDATE, RECENT_PUSHES, UNRESPONSIVE, PUSH_LOUD, PUSH_FAILURE,
	 * COMMAND, TEMP, CENTRAL FROM B2F_DEVICE;
	 * 
	 * 
	 * Unzip logs in macOs cd
	 * /Users/cjm10/Library/Developer/Xcode/DerivedData/Blue2Factor-
	 * aycszoqkxosmyvgkupkpuybrohfq/ Logs/Debug gunzip -c -S .xcactivitylog
	 * C41B0739-20BB-43C2-9248-A985933A9F34.xcactivitylog >
	 * C41B0739-20BB-43C2-9248-A985933A9F34.log
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

//	@RequestMapping(method = RequestMethod.GET)
//	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
//		int outcome = Constants.FAILURE;
//		if (createAllTables()) {
//			outcome = Constants.SUCCESS;
//		}
//		BasicResponse resp = new BasicResponse(outcome, "");
//		model = this.addBasicResponse(model, resp);
//		return "result";
//	}
//	
//	private boolean createAllTables() {
//		boolean success = false;
//		MySqlConn msc = new MySqlConn();
//		Connection conn = msc.getConnection();
//		PreparedStatement prepStmt1;
//		PreparedStatement prepStmt2;
//		PreparedStatement prepStmt3;
//		PreparedStatement prepStmt4;
//		try {
//			try {
//				prepStmt1 = conn.prepareStatement(coQuery);
//				prepStmt1.executeQuery();
//				logger.info("coQuery done");
//				prepStmt2 = conn.prepareStatement(grQuery);
//				prepStmt2.executeQuery();
//				logger.info("grQuery done");
//				prepStmt3 = conn.prepareStatement(chQuery);
//				prepStmt3.executeQuery();
//				logger.info("chQuery done");
//				prepStmt4 = conn.prepareStatement(deQuery);
//				prepStmt4.executeQuery();
//				logger.info("deQuery done");
//				success = true;
//			} catch (SQLException e) {
//				logger.error(e.getMessage(), e);
//			} 
//		} finally {
//			msc.closeConnection("isInCurrentCheck");
//		}
//		return success;
//	}
//	
	/*
	 * CREATE TABLE IF NOT EXISTS B2F_KEY (KEY_ID varchar(255), DEVICE_ID
	 * varchar(255), BROWSER_ID varchar(255), CREATE_DATE timestamp(3), ACTIVE
	 * tinyint(1) NOT NULL DEFAULT 1, KEY_TYPE varchar(255), KEY_TEXT varchar(8193),
	 * PUBLIC_KEY tinyint(1) NOT NULL DEFAULT 0, ALGORITHM
	 * varchar(255))ENGINE=InnoDB DEFAULT CHARSET=latin1
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_BRANDING (COMPANY_ID varchar(255), ICON_PATH
	 * varchar(4095), BACKGROUND_COLOR varchar(255), FOREGROUND_COLOR varchar(255),
	 * TITLE_IMAGE_PATH varchar(4095)) ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_SAML_IDENTITY_PROVIDER (CREATE_DATE
	 * timestamp(3), ENTITY_ID varchar(255), IDENTITY_PROVIDER_NAME varchar(4095),
	 * SIGNING_CERTIFICATE varchar(8193), ENCRYPTING_CERTIFICATE varchar(8193),
	 * NAME_ID_FORMAT varchar(1023), REDIRECT_URL varchar(4095), POST_URL
	 * varchar(4095) ACTIVE tinyint(1) NOT NULL DEFAULT 0) ENGINE=InnoDB DEFAULT
	 * CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_DEVICE_CONNECTION_RECORDS (CREATE_DATE timestamp(3),
	 * CONNECTION_ID varchar(255), CONNECTED tinyint(1), SRC varchar(4096))
	 * ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * 
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_SAML_SERVICE_PROVIDER (CREATE_DATE
	 * timestamp(3), SERVICE_PROVIDER_ID varchar(255), SERVICE_PROVIDER_ISSUER
	 * varchar(4096), SERVICE_PROVIDER_NAME varchar(4096), SERVICE_PROVIDER_URL
	 * varchar(4096), SERVICE_PROVIDER_509_CERT varchar(8193),
	 * SERVICE_PROVIDER_PRIVATE_KEY varchar(8193), SERVICE_PROVIDER_METADATA_URL
	 * varchar(4096), ACTIVE tinyint(1) NOT NULL DEFAULT 0) ENGINE=InnoDB DEFAULT
	 * CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_LOG (CREATE_DATE timestamp(3), DEVICE_ID
	 * varchar(255), SRC varchar(2047), TXT text, LEVEL int) ENGINE=InnoDB DEFAULT
	 * CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_AUTHORIZATION (USER_ID varchar(255),
	 * AUTHORIZATION_TIME timestamp(3), AUTHORIZATION_COMPLETED tinyint(1) NOT NULL
	 * DEFAULT 0, REQUESTING_DEVICE varchar(255), CENTRAL_DEVICE varchar(255))
	 * ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_DEVICE_CONNECTION (CREATE_DATE timestamp(3),
	 * CONNECTION_ID varchar(255), PERIPHERAL_DEVICE_ID varchar(255),
	 * CENTRAL_DEVICE_ID varchar(255), SERVICE_UUID varchar(255), CHARACTER_UUID
	 * varchar(255), GROUP_ID varchar(255), ACTIVE tinyint(1) NOT NULL DEFAULT 0,
	 * LAST_CHECK timestamp(3), LAST_SUCCESS timestamp(3),
	 * LAST_PERIPHERAL_CONNECTION timestamp(3), BLUETOOTH_CONNECTED tinyint(1) NOT
	 * NULL DEFAULT 0, LAST_BLUETOOTH_SUCCESS timestamp(3), CURRENTLY_CONNECTED
	 * tinyint(1) NOT NULL DEFAULT 0, ADVERTISING_PERIPHERAL timestamp(3),
	 * CONNECTING_CENTRAL timestamp(3), SUBSCRIBED tinyint(1) NOT NULL DEFAULT 0)
	 * ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * 
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_MAILING_LIST (CREATE_DATE timestamp(3), EMAIL
	 * varchar(1023), ACTIVE tinyint(1) NOT NULL DEFAULT 1) ENGINE=InnoDB DEFAULT
	 * CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_TOKEN (GROUP_ID varchar(255),DEVICE_ID
	 * varchar(255),TOKEN_ID varchar(255),AUTHORIZATION_TIME
	 * timestamp(3),EXPIRE_TIME timestamp(3),LAST_CHECK timestamp(3),LAST_UPDATE
	 * timestamp(3),DESCRIPTION text,NEEDS_UPDATE tinyint(1) NOT NULL DEFAULT
	 * 0,BROWSER_ID varchar(255)) ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_BROWSER (DEVICE_ID varchar(255), BROWSER_ID
	 * varchar(255), CREATE_DATE timestamp(3), EXPIRE_DATE timestamp(3), LAST_UPDATE
	 * timestamp(3), DESCRIPTION varchar(2047)) ENGINE=InnoDB DEFAULT
	 * CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_COMPANY (COMPANY_ID varchar(255) DEFAULT NULL,
	 * COMPANY_NAME varchar(255) DEFAULT NULL, ACTIVE tinyint(1) NOT NULL DEFAULT 1,
	 * COMPANY_SECRET varchar(255) DEFAULT NULL, ACCEPTED_TYPES int(11) DEFAULT
	 * NULL, CREATE_DATE timestamp(3), COMPANY_LOGIN_TOKEN varchar(255) DEFAULT
	 * NULL, COMPANY_COMPLETION_URL text, API_KEY varchar(255), LICENSE_COUNT
	 * int(11) NOT NULL DEFAULT 0, LICENSES_IN_USE int(11) NOT NULL DEFAULT 0,
	 * COMPANY_PUBLIC_KEY varchar(255), COMPANY_PRIVATE_KEY varchar(255),
	 * COMPANY_BASE_URL text ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_GROUP (COMPANY_ID varchar(255) DEFAULT NULL,
	 * GROUP_ID varchar(255) DEFAULT NULL, GROUP_NAME varchar(255) DEFAULT NULL,
	 * ACTIVE tinyint(1) NOT NULL DEFAULT 1, ACCEPTED_TYPES int(11) DEFAULT NULL,
	 * CREATE_DATE timestamp(3), TIMEOUT_SECS int(11) DEFAULT NULL, GROUP_PW
	 * varchar(2047), SALT varchar(255), DEVICES_ALLOWED int(11) NOT NULL DEFAULT 0,
	 * DEVICES_IN_USE int(11) NOT NULL DEFAULT 0, PERMISSIONS int(11) NOT NULL
	 * DEFAULT 0, USERNAME varchar(255), TOKEN_DATE timestamp(3)) ENGINE=InnoDB
	 * DEFAULT CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_CHECK (CHECK_ID varchar(255) DEFAULT
	 * NULL,INSTANCE_ID varchar(255) DEFAULT NULL, CENTRAL_DEVICE_ID varchar(255)
	 * DEFAULT NULL,PERIPHERAL_DEVICE_ID varchar(255) DEFAULT NULL,SERVICE_UUID
	 * varchar(127) DEFAULT NULL,USER_ID varchar(255) DEFAULT NULL,CENTRAL_BSSID
	 * varchar(255) DEFAULT NULL,CENTRAL_SSID varchar(255) DEFAULT
	 * NULL,PERIPHERAL_BSSID varchar(255) DEFAULT NULL,PERIPHERAL_SSID varchar(255)
	 * DEFAULT NULL,CENTRAL_B2FID varchar(255) DEFAULT NULL,PERIPHERAL_B2FID
	 * varchar(255) DEFAULT NULL,EXPIRED tinyint(1) NOT NULL DEFAULT 0,COMPLETED
	 * tinyint(1) NOT NULL DEFAULT 0,OUTCOME tinyint(4) DEFAULT NULL,CREATE_DATE
	 * timestamp(3),COMPLETION_DATE timestamp(3), VERIFIED_RECEIPT tinyint(1) NOT
	 * NULL DEFAULT 0,CHECK_TYPE varchar(255)) ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_DEVICE (GROUP_ID varchar(255) DEFAULT
	 * NULL,USER_ID varchar(255) DEFAULT NULL, DEVICE_ID varchar(255) DEFAULT
	 * NULL,SEED int(11) DEFAULT NULL,ACTIVE tinyint(1) NOT NULL DEFAULT 1, FCM_ID
	 * varchar(255) DEFAULT NULL, BT_ADDRESS varchar(255) DEFAULT NULL, CREATE_DATE
	 * timestamp, DEVICE_TYPE varchar(255) DEFAULT NULL, OPERATING_SYSTEM
	 * varchar(255) DEFAULT NULL,LOGIN_TOKEN varchar(255) DEFAULT
	 * NULL,LAST_COMPLETE_CHECK datetime DEFAULT NULL,OS_VERSION varchar(255)
	 * DEFAULT NULL,USER_LANGUAGE varchar(255) DEFAULT NULL,LAST_GMT_OFFSET int(11)
	 * DEFAULT NULL, SCREEN_SIZE varchar(255) DEFAULT NULL,RAND text,SHOW_ICON
	 * tinyint(1) NOT NULL DEFAULT 0, DEVICE_PRIORITY int(11) NOT NULL DEFAULT 0,
	 * TRIGGER_UPDATE tinyint(1) NOT NULL DEFAULT 0, RECENT_PUSHES int(11) NOT NULL
	 * DEFAULT 0, UNRESPONSIVE tinyint(1) NOT NULL DEFAULT 0, LAST_PUSH
	 * timestamp(3), PUSH_LOUD tinyint(1) NOT NULL DEFAULT 1, PUSH_FAILURE
	 * tinyint(1) NOT NULL DEFAULT 0, COMMAND varchar(2047),TEMP varchar(2047),
	 * CENTRAL tinyint(1) NOT NULL DEFAULT 0) ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * CREATE TABLE IF NOT EXISTS B2F_DISCONNECT (CREATE_DATE timestamp,
	 * DISCONNECT_STRING varchar(255) DEFAULT NULL, ACTIVE tinyint(1) NOT NULL
	 * DEFAULT 1,CONNECTION_ID varchar(255)) ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * CREATE TABLE IF NOT EXISTS B2F_SERVER (CREATE_DATE timestamp(3),SERVER_ID
	 * varchar(255) DEFAULT NULL, B2F_ID varchar(255) DEFAULT NULL,PERMISSIONS int
	 * DEFAULT 0,ACTIVE tinyint(1) NOT NULL DEFAULT 1,ENABLED tinyint(1) NOT NULL
	 * DEFAULT 1) ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_SERVER_CONNECTION (CREATE_DATE
	 * timestamp(3),SERVER_ID varchar(255) DEFAULT NULL, GROUP_ID varchar(255)
	 * DEFAULT NULL,PERMISSIONS int DEFAULT 0,ACTIVE tinyint(1) NOT NULL DEFAULT 1)
	 * ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_ACCESS_STRINGS (CREATE_DATE
	 * timestamp(3),ACCESS_CODE varchar(255) DEFAULT NULL, SERVER_ID varchar(255)
	 * DEFAULT NULL, DEVICE_ID varchar(255) DEFAULT NULL,PERMISSIONS int DEFAULT
	 * 0,ACTIVE tinyint(1) NOT NULL DEFAULT 1) ENGINE=InnoDB DEFAULT CHARSET=latin1;
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_AUTHENTICATOR (CRED_ID varchar(511),
	 * BROWSER_ID varchar(255), TYPE varchar(255), SIGN_COUNT BIGINT,
	 * ATTESTED_CREDENTIAL_DATA BLOB, ATTESTATION_OBJECT BLOB, FORMAT varchar(255),
	 * CREATE_DATE timestamp(3), EXPIRED tinyint(1));
	 * 
	 * CREATE TABLE IF NOT EXISTS B2F_QUICK_CHECK (LAST_UPDATE timestamp(3), 
	 * DEVICE_ID varchar(255), CONNECTED tinyint(1));
	 * 
	 * 
	 */
}

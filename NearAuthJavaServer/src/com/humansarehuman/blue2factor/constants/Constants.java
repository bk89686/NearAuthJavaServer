package com.humansarehuman.blue2factor.constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.enums.CodeEnvironment;

import io.jsonwebtoken.io.IOException;

@SuppressWarnings("ucd")
public class Constants {
	public static int MAX_CONNECTIONS = 800;
	public static int minLogRecordLength = 45;

	public static CodeEnvironment ENVIRONMENT = getCurrentEnv();
	public static CodeEnvironment currEnv = null;
	public final static String APP_NAME = "NearAuth.ai";
	public final static int RAND_STRING_LENGTH = 11;
	public final static String RESET = "reset";
	public final static int CURRENT_VERSION = 14;
	public final static String ANONYMOUS_GROUP = "Anonymous Group";
	public final static String TURN_OFF_FROM_INSTALLER = "turnOffFromInstaller";
	public final static String TERMINATE = "terminate";
	public final static String CHRIS_GROUP_ID = "b2f978b4-7d0c-43bc-82e6-cb3e7dc065e5";

	// commands from api
	public final static String FACTOR_2_CHECK = "factor2Check";
	public final static String FACTOR_2_CHECK_FROM_SERVER = "factor2CheckServer";
	public final static String CONFIRM_TOKEN = "confirm";
	public final static String DEALLOCATE = "deallocate";
	public final static String SEND_LOUD_PUSH = "sendPush";
	public final static String LOG_IT = "log";
	public final static String SETUP = "setup";
	public final static String NEEDS_SAML_VERIFICATION = "needs SAML verification";
	public final static String NEEDS_RESCAN = "needs rescan";
	public final static String NOT_SETUP = "NearAuth.ai is not setup";
	public final static String HANDLE_FAILURE = "handleFailure";
	public final static String SIGNUP_CHECK = "signupCheck";
	public final static String PUSH_CHECK = "pushCheck";
	public final static String BROWSER_CHECK = "browserCheck";
	public final static String FINGERPRINT_NOT_AVAILABLE = "fpNotAvailable";
	public final static String JS_FACTOR1_VALIDATE = "jsF1Validate";
	public final static String CREATE_BROWSER_KEY = "createBrowserKey";
	public final static String SAVE_SERVER_KEY = "createServerKey";
	public final static String GET_SERVER_KEY = "getServerPublicKey";
	public final static String GET_BROWSER_KEY = "getBrowserPrivateKey";
	public final static String IDENTITY_PROVIDER_SUCCESS = "identityProviderSuccess";
	public final static String GET_SAML_DATA = "getSamlData";

	// api responses
	public final static String TOKEN_NOT_FOUND = "token not found";
	public final static String SESSION_NOT_FOUND = "session not found";
	public final static String TOKEN_EXPIRED = "token expired";
	public final static String INVALID_AUTH_TOKEN = "invalidAuthToken";
	public final static String INVALID_JWT = "invalidJwt";
	public final static String INVALID_USER = "invalidUser";
	public final static String TOKEN_ERROR = "token error";
	public final static String BROWSER_NOT_FOUND = "browser was not found";
	public final static String BROWSER_EXPIRED = "browser was expired";
	public final static String CHECK_FIRST_FACTOR = "check first factor";
	public final static String CHECK_NOT_FOUND = "check not found";
	public final static String DEVICE_ASLEEP = "device asleep";
	public final static String SSL_CONNECTION_NOT_SETUP = "ssl connection not setup";
	public final static String CO_NOT_FOUND = "company was not found";

	public final static String DEV_NOT_FOUND = "the device you are looking for could not be identified";
	public final static String DEVICE_NOT_LOCAL = "Your second factor could not be authenticated";
	public final static String DEVICE_NOT_LOCAL_SSH = "notLocal";
	public final static String GET_BIOMETRICS = "getBiometrics";
	public final static String BIOMETRICS_NOT_FOUND_ON_DEVICE = "biometricsNotFoundOnDevice";
	public final static String DIFFERENT_EMAILS = "differentEmails";
	public final static String EMAIL_SIGNED_UP = "that user has already been signed up";
	public final static String LOUD_PUSH_SENT_MSG = "push sent";
	public final static String NO_LICENSES_ARE_AVAILABLE = "No more licenses are available for this account";
	public final static String NOT_PROX = "another device could not be found nearby";
	public final static String COMPANY_USER_MISMATCH = "company user mismatch";
	public final static String COMPANY_URL_MISMATCH = "company URL mismatch";
	public final static String UNKNOWN_USER = "the user could not be found";
	public final static String INACTIVE_USER = "you must re-sign in to do that";
	public final static String DEVICE_ID_MISMATCH = "deviceId mismatch";
	public final static String TIMED_OUT = "the request timed out";
	public final static String SSH_RECORD_NOT_FOUND = "ssh record not found";
	public final static String METHOD_NOT_ALLOWED = "method not allowed";
	public final static String BAD_COMMAND = "bad command";
	public final static String NOT_PERMITTED = "not permitted";
	public final static String SIGNED_OUT = "device is signed out";
	public final static String EXPIRED_TOKEN = "expired token";
	public final static String EXPIRED_CHECK = "check was expired";
	public final static String USER_NOT_FOUND = "the user was not found";
	public final static String SERVICE_UUID_WAS_NULL = "service uuid was null";
	public final static String ERROR_FOUND = "error encountered";
	public final static String INCORRECT_CREDS = "We could not find a person with that username and password.";
	public final static String FINGERPRINT_CREDENTIALS_NOT_FOUND = "fingerprint credentials not found";
	public final static String F2_TOKEN_NOT_VALIDATED = "F2 token not validated";
	public final static String NO_RECENT_COMMUNICATION = "no recent communication";
	public final static String PROCESSING = "processing";
	public final static String GROUP_NOT_FOUND = "group not found";
	public final static String USERNAME_WAS_EMPTY = "username was empty";
	public final static String ALREADY_ACTIVATED = "already activate";
	public final static String BASE_URL_NOT_SET = "base url not set";
	public final static String SECOND_FACTOR_FAILED = "second factor failed";
	public final static String PUSH_RECENTLY_SENT = "a push was recently sent";
	public final static String PUSH_NOT_ALLOWED = "pushNotAllowed";
	public final static String STALE_PUSH = "stalePush";
	public final static String DEV_FOUND_BY_MACHINE_ID = "devFoundByMachineId";
	public final static String CENTRAL_NOT_FOUND = "central not found";
	public final static String PERIPHERAL_NOT_FOUND = "peripheral not found";
	public final static String PHONE_NUMBER_BLANK = "phone number not found";
	public final static String SCREENSAVER_ON = "screensaver on";
	public final static String TEXTS_NOT_ALLOWED = "texts not allowed";
	public final static String TEXTS_FAILED = "text failed";
	public final static String TEST_USER = "testAccount";

	// public final static int API_ERROR = -1;

	// Statuses
	public final static int STILL_SEARCHING = 0;
	public final static String SERVER_CONNECTING = "serverConnecting";
	public final static String SERVER_CONNECTED = "serverConnected";
	public final static String SERVER_BLUETOOTH_CONNECTED = "serverBluetoothConnected";
	public final static String SERVER_DISCONNECTED = "serverDisconnected";
	public final static String PERIPHERAL_CONNECTED = "peripheralConnected";
	public final static String PERIPHERAL_SUBSCRIBED = "peripheralSubscribed";
	public final static String PERIPHERAL_DISCONNECTED = "peripheralDisconnected";
	public final static String NO_PERIPHERALS_ONLINE = "noPeripheralsOnline";

	// times
	public final static int MAX_SECONDS = 50;
	public final static int MAX_FULL_SYNCH_SECONDS = 120;
	public final static int RECEIPT_SECONDS = 10;
	public final static int DEFAULT_VALIDATION_SECONDS = 65;
	public final static int COOKIE_MAX_SECONDS = 60 * 60 * 24 * 30; // 30 days
	public final static int SECONDS_BETWEEN_FULL_CHECKS = 60 * 60 * 2; // 2 hours
	public final static int AUTHORIZATION_MINUTES = 30;
	public final static int PASSKEY_TIMEOUT_SECS = 60 * 60 * 5; // 5 HOURS
	public final static int VERY_RECENT_FINGERPRINT_OR_PUSH_MOBILE_TIMEOUT_SECS = 60 * 5; // 5
																							// minutes
	public final static int PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS = 60 * 60 * 24 * 7; // 1 week
	public final static int PREVIOUS_CONNECTION_LIMIT_IF_BLUETOOTH_CONNECTED_SECONDS_MOBILE_PERIPHERAL = 60 * 60 * 24
			* 14; // 2 weeks

	public final static int BLUETOOTH_CONNECTION_TIME_PERIOD = 60 * 20; // 20 minutes
	public final static int TEMP_BROWSER_EXPIRATION_SECS = 60 * 60 * 3; // 3 hours
	public final static int SSH_TIMEOUT = 12;
	public final static int SSH_NOT_THIS_TIME = 22;
	public final static int ADMIN_CODE_DURATION_SECONDS = 60 * 60 * 24 * 7; // a week
	public final static int ADMIN_CODE_DURATION_AFTER_SUBMITION_SECONDS = 60 * 60 * 24; // 24 hours
	public final static int NUMBER_OF_DAYS_TO_SIGN_UP = 3;
	public final static int SECONDS_TO_NOT_PUSH = 10;
	public final static int SECONDS_TO_NOT_PUSH_IF_UNRESPONSIVE = 600;

	// accepted services
	public final static int BLE = 1;
	public final static int BSSID = 2;
	public final static int WIFI_SSID = 4;
	public final static int NFC = 8;
	public final static int BLUETOOTH_CLASSIC = 16;

	// push notification
	public final static String SERVER_API = "AAAAZd9i9bo:APA91bHSK_jwRbOx14HXyq9I6AClCjxzntRQ9G3xOXP12gyB7uKifSAJIRIScZ8Y_2fBY_udl38cv7BYUt006CluHt6kVPv8iovdhuH20tMuC0QeuHxMy5iYv36eV7nYj2214_CE9H8d";
	public final static String FCM_URL = "https://fcm.googleapis.com/v1/projects/blue2factor/messages:send";
	public final static String API_URL = Urls.getSecureUrl() + "/b2fUserApi";

	// app types
	public final static String IOS = "iOs";
	public final static String ANDROID = "android";
	public final static String OSX = "osx";
	public final static String WINDOWS = "windows";

	// app links (these are incorrect - for point2.me to test)
	public final static String GENERIC_FAILURE = Urls.getSecureUrl() + "/failure";
	public final static String APP_STORE_LINK = "https://goo.gl/obNBKo";
	public final static String GOOGLE_PLAY_LINK = "https://goo.gl/tkRCd6";

	public final static String APPENDED_STRING = "TheresAlwaysMoneyInTheBananaStand";

	public final static String NO_COOKIE = "noCookie";
	public final static String DEVICE_NOT_FOUND = "deviceNotFound";
	public final static String DEVICE_INACTIVE = "device is inactive";
	public final static String SERVER_NOT_FOUND = "serverNotFound";
	public final static String COMPANY_NOT_FOUND = "companyNotFound";
	public final static String CONNECTION_NOT_FOUND = "connectionNotFound";
	public final static String DEVICE_NOT_CONNECTED = "deviceNotConnected";
	public final static String SAML_SUCCESS_NOT_FOUND = "samlSuccessNotFound";
	public final static String DEVICE_ID_WAS_BLANK = "deviceIdWasBlank";
	public final static String KEY_NOT_FOUND = "keyNotFound";
	public final static String KEY_ERROR = "keyError";
	public final static String DECRYPTION_ERROR = "decryptionError";
	public final static String ENCRYPTION_ERROR = "encryptionError";
	public final static String REQUIRES_ENCRYPTION = "requiresEncryption";
	public final static String SIGNATURE_VALIDATION_FAILED = "signatureValidationFailed";

	public static CodeEnvironment getCurrentEnv() {
		DataAccess dataAccess = new DataAccess();
		if (currEnv == null) {
			try {
				String system = System.getProperty("os.name");
				String filePath;
				if (system.contains("Windows")) {
					filePath = "C:/tmp/b2fenv.txt";
				} else {
					filePath = "/etc/blue2factor-backend/b2fenv.txt";
				}
				String envType = getFileContents(filePath);
				dataAccess.addLog("envType: '" + envType + "'", LogConstants.WARNING);
				if (envType.equals("dev")) {
					currEnv = CodeEnvironment.DEV;
				} else if (envType.equals("test")) {
					currEnv = CodeEnvironment.TEST;
				} else if (envType.equals("local")) {
					currEnv = CodeEnvironment.LOCAL;
				} else {
					currEnv = CodeEnvironment.PROD;
				}
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String sStackTrace = e.getLocalizedMessage() + "\r\n" + sw.toString();
				dataAccess.addLog(sStackTrace, LogConstants.WARNING);
				System.out.println("b2fenv.txt not found");
				currEnv = CodeEnvironment.PROD;
			}
		}
		dataAccess.addLog("returning: " + currEnv);
		return currEnv;
	}

	private static String getFileContents(String filePath) {
		String lines = "";
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines += line;
			}
		} catch (IOException | java.io.IOException e) {
			new DataAccess().addLog(e);
		}
		return lines.strip();
	}
}

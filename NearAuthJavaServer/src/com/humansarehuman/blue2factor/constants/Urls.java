package com.humansarehuman.blue2factor.constants;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.enums.CodeEnvironment;

public class Urls {
	// parameters

	// internal URLs
	public final static String ADD_BROWSER_REQUEST = "/addBrowserRequest";
	public final static String ADD_BROWSER_AUTH = "/TTnbeuuat";
	public final static String ADD_COMMAND = "/8guceahdcradhtdhj";
	public final static String ADD_COMPANY = "/saubo89uht2nht4h";
	public final static String ADD_NEW_DEVICE = "/auntbnagkbakeugckdau787fek";
	public final static String ADD_USERS_DOC = "/addUsersDoc";
	public final static String APPLE_ASSOCIATION = "/apple-app-site-association";
	public final static String AUTHENTICATE = "/authenticate";
	public final static String AUTHORIZE_NEW_DEVICE = "/igtngnraeing843qfsd";
	public final static String AUTHORIZE_SSH_CLIENT = "/nhtuasenubsb4cpgr";
	public final static String B2F_LOG = "/ajfac6u6";
	public final static String BLUETOOTH_CONNECTED = "/bbn7haxrwv";
	public final static String BLUETOOTH_CONNECTED_BY_DEVICE_REFERENCE = "/bbn7haxrwvbdr";
	public final static String BROWSER_INSTALL = "/brsthbntjsbasne";
	public final static String CANCEL_DISCONNECTION = "/chwqhausaehmtheu";
	public final static String CHECK_FOR_QR_COMPLETION = "/qrComplete";
	public final static String CHECK_FOR_RESYNC_COMPLETION = "/isResyncComplete";
	public final static String CLIENT_SETUP = "/b2f-client-setup";
	public final static String COMPLETE_RESYNC_ON_CENTRAL = "/nashsnechmnstbm";
	public final static String CONFIRM_TOKEN = "/confirmToken";
	public final static String CONNECTION_CHECK = "/huasckbstuoeiaeuhtbneug";
	public final static String CONNECTION_CHECK_BY_UUID = "/tsaneuhnthsmbns";
	public final static String SSH_PRECHECK = "/snaehubtnshbaueae";
	public final static String SSH_PRECHECK_NEW = "/baueaeubtnshsnaeh";
	public final static String SSH_PRECHECK_CONFIRM = "/bauerhbtbho";
	public final static String DEACTIVATE_SSH_MACHINE = "/deactiveSshMachine";
	public final static String DELETE_OLD_RECORDS = "/natsuhisnontbmksmcrssahusenahuensshuchbkesanthe";
	public final static String DELETE_USER_DATA = "/deleteMyData";
	public final static String DEVICE_DATA = "/deviceData";
	public final static String DEVICE_DATA_ONE_DEVICE = "/shaneshuenhthubisa";
	public final static String DEVICE_DISCONNECTED = "/uaesnuh";
	public final static String DEVICE_SIGNOUT = "/deviceSignout";
	public final static String DOCUMENTATION = "/documentation";
	public final static String EXPIRE_TOKEN_FOR_CENTRAL = "/sbkgbuahkthnb";
	public final static String FAILSAFE = "/failsafe";
	public final static String FAILURE_AND_SETUP = "/SAML2/SSO/{apiKey}/b2f-res";
	public final static String FCM_UPDATE = "/fcmUpdate";
	public final static String GET_ADMIN_CODES = "/getAdminCodes";
	public final static String GET_NOTIFICATION = "/sauhatmuaseh";
	public final static String GET_SERVER_VARS = "/nu7q0HHk";
	public final static String GET_PUBLIC_KEY = "/key/{keyId}/public";
	public final static String GET_UNFOUND_PERIPHERAL_FROM_SERVICE = "/rgcrhh3e";
	public final static String INSTALL_COMPLETE = "/installComplete";
	public final static String IS_INSTALL_COMPLETE = "/jafa899";
	public final static String IS_INSTALL_COMPLETE_FROM_JS = "/isInstallComplete";
	public final static String IS_TEXT_ALLOWED = "/ssbaeull";
	public final static String FINGERPRINT_STATUS = "/fpStats";
	public final static String LOGOUT = "/logout";
	public final static String LOOKUP_BLUETOOTH_TYPE = "/shssnt2";
	public final static String LOOKUP_DEV_ID_BY_MACHINE_ID = "/jf8p1";
	public final static String MINUTELY_HACK_CRON = "/sau3eaneuhanasehuarrrhuh";
	public final static String ONE_TIME_ACCESS = "/oneTimeAccess";
	public final static String OPTOUT_EMAIL = "/optout";
	public final static String PAM_CLIENT_CHECK = "/pamClientCheck";
	public final static String PAM_SERVER_CHECK = "/pamServerCheck";
	public final static String PAM_SERVER_CHECK2 = "/psc2";
	public final static String PAM_SERVER_CHECK_BASIC = "/bpsh";
	public final static String PAM_SERVER_CLIENT_KEY_LOOKUP = "/pamPublicKey";
	public final static String PLEASE_WAIT = "/naheucshstnhaSl8";
	public final static String Q_AND_D = "/qandd";
	public final static String QR_REVALIDATE = "/QrRevalidate";
	public final static String QR_VALIDATE = "/fkljoqn88";
	public final static String REPORT_CONNECTING = "/ehuasnmtrr22ep";
	public final static String REPORT_QUITING = "/snhnsbbbb";
	public final static String REPORT_SLEEPING = "/rlhabknnoe";
	public final static String REQUIRES_ENCRYPTION = "/jpflycrb";
	public final static String RESET_CONNECTED_CENTRAL = "/kbognutbkuaeuj3p";
	public final static String RESET_DEVICES = "/ioaf65u8";
	public final static String RESET_ONE_DEVICE = "/shsbsncthm";
	public final static String RESYNC_CODE = "/rchrbrbtmtm";
	public final static String RSSI_UPDATE = "/rssiUpd";
	public final static String SAVE_PERIPHERAL_IDENTIFIER = "/shbb8o4";
	public final static String SCREEN_SAVER_STATUS_CHANGE = "/btuhkngcapnkx";
	public final static String SEND_EMAIL = "/afj9ifar";
	public final static String SEND_LOUD_PUSH = "/ffaj9ifarASTntshoo";
	public final static String SERVER_SETUP = "/b2f-server-setup";
	public final static String SERVICE_PROVIDERS = "/serviceProviders";
	public final static String SET_ACTIVE = "/thsbwmbae";
	public final static String SET_NEEDS_PAIRING = "/asnuhaeuae";
	public final static String SET_SETTINGS = "/asimcqau";
	public final static String SETTINGS = "/settings";
	public final static String SETUP_ADDED_DEVICE = "/naesbuscphubmasenbum";
	public final static String SETUP_PERIPHERAL_CONNECTION = "/uPohb77s2e";
	public final static String SETUP_CENTRAL_CONNECTION = "/olelU3sz";
	public final static String SIGN_IN = "/SAML2/SSO/{apiKey}/SignIn";
	public final static String SIGN_IN_GENERIC = "/signIn";
	public final static String STATUS = "/statuscjm";
	public final static String STOP_BACKGROUND = "/rch89aiii";
	public final static String SUBMIT_DECRYPTED_IDS = "/bkntbuabrskutbstku";
	public final static String SUBMIT_ENCRYPTED_PERIPHERAL_CONNECTION = "/snaehusaeuhnsaeuhsneuphbyb";
	public final static String SUBMIT_KEY = "/esauhnseoubase";
	public final static String SUBMIT_PUSH_RESPONSE = "/snnbsnbcbnstm";
	public final static String SUBSCRIBED = "/subchrs";
	public final static String TEST_CONNECTION = "/test";
	public final static String TO_LANDING_PAGE = "/finalizeB2f";
	public final static String UNINSTALL_DEVICE = "/uninstallDev";
	public final static String UPDATE_B2FID = "/aesnuhcxkasunhcrhx864nchnsbaengn";
	public final static String UPDATE_BLE_ENABLED = "/hbkjasss";
	public final static String UPDATE_JWT = "/snaheubrcehubesahuenoeouaeuaseuheastnuhmbasbkcuakuegsphmethe";
	public final static String UPDATE_NON_MEMBER_STRATEGY = "/aeu9aeu2sxchnit3";
	public final static String UPDATE_USER_FLOW_SETTINGS = "/nnanamjirmdui98nmouooiuu2";
	public final static String VALIDATE_ADMIN_CODE = "/adminstratorCode";
	public final static String VALIDATE_CONNECTION = "/o84b7";
	public final static String VALIDATE_DEV_ID = "/phulloehussab";
	public final static String VALIDATE_DEVICE_ID = "/oajpe89";
	public final static String VALIDATE_FROM_CONNECTED_CENTRAL = "/igcpndbiaxjoj1";
	public final static String VALIDATE_PERIPHERAL = "/naheucshstnhauuu";
	public final static String VALIDATE_JWT_DOC = "/validatingJwtDoc";
	public final static String VALIDATE_TEXT = "/snehuabbbb332";
	public final static String VALIDATE_TEXT_URL = "/snehua747b332";
	public final static String FACTOR_2_CHECK = "/b2fBrowser";
//	public final static String SECOND_FACTOR_VALIDATION = "/b2f-f2";
	public final static String VALIDATE_USER = "/nabccgs0";
	public final static String VERIFY_ACCESS = "/verifyAccess";
	public final static String USER_API_ENDPOINT = "/b2fUserApi";
	public final static String COMPANY = "/company";
	public final static String COMPANY_SIGNOUT = "/signout";
	public final static String CREATE_ENCRYPTION_KEY_COMPANY = "/b2f-newKeys";
	public final static String CLIENT_FAILURE = "/failure/{CompanyID}/reset";
	public final static String DOWNLOAD_CERT = "/cert/{companyID}/download";
	public final static String RESET_IDP_KEYS = "/keys/{companyID}/hsuaetuaseubacbuase";
	public final static String RESET_PASSWORD = "/pw/{companyId}/reset";
	public final static String JS_FAILURE = "/jsFailure";
	public final static String JOIN_MAILING_LIST = "/shsnamusssseuao";
	public final static String PASSKEY_AUTHENTICATION = "/fpAuth";
	public final static String PASSKEY_AUTHENTICATION_COMPLETION = "/fpAuthComplete";
	public final static String PASSKEY_REGISTRATION = "/fpReg";
	public final static String PASSKEY_REGISTRATION_COMPLETION = "/b2fRegSub";
	public final static String PASSKEY_WEBPAGE = "/regsb2f";
	public final static String NOT_SIGNED_UP = "/notSignedUp";
	public final static String CHECK_FOR_SAML_SUCCESS = "/snthckhnshae";
	public final static String LAMBDA_CHECK_TOKEN = "/Lambda/SSO/{apiKey}/checkToken";
	public final static String LAMBDA_CREATE_TOKEN = "/Lambda/SSO/{apiKey}/createToken";
	public final static String LAMBDA_UPDATE_TOKEN = "/Lambda/SSO/{apiKey}/updateToken";
	public final static String LDAP_SUBMIT = "/SAML2/SSO/{apiKey}/ldapSubmit";
	public final static String SAML_ENTITY_ID = "/SAML2/SSO/{apiKey}/EntityId";
	public final static String SAML_CLIENT_ENTITY_ID = "/SAML2/SSO/{apiKey}/ClientEntityId";
	public final static String SAML_IDP_METADATA = "/SAML2/SSO/{apiKey}/nearAuthIdpMetadata";
	public final static String SAML_SIGN_IN = "/SAML2/SSO/{apiKey}/samlSignin";
	public final static String COMPANY_VALIDATE = "/SAML2/SSO/{apiKey}/validate";
	public final static String SAML_RESPONSE_FROM_IDENTITY_PROVIDER = "/SAML2/SSO/{apiKey}/fromIdp";
	public final static String SAML_SIGNOUT = "/SAML2/SSO/{apiKey}/Signout";
	public final static String SAML_SP_METADATA = "/SAML2/SSO/{apiKey}/nearAuthSpMetadata";
	public final static String SAML_NEW_JWT = "/SAML2/SSO/newToken";
	public final static String SAML_REDIRECT = "/SAML2/SSO/{apiKey}/Redirect";
	public final static String SIGN_OUT = "/f1Signout";
	public final static String VALIDATE = "/SAML2/SSO/{apiKey}/ValidateOld";
	public final static String SETUP_FAILURE = "/setupFailure";
	public final static String TIME_SEARCH = "/timeSearch";
	public final static String TOKEN_VALIDATE = "/SAML2/SSO/{apiKey}/Token";
	public final static String FIRST_FACTOR_SETUP = "/b2f-f1-setup";
	public final static String QUICK_ACCESS_CRON = "/quickAccessCron";
	public final static String REPORT_GATT_STATUS = "/schhor";
	public final static String SERVER_FACTOR_VALIDATION = "/b2f-f1";
	public final static String SERVER_FAILURE = "/serverFailure";
	public final static String SILENT_PUSH_RESPONSE = "/sahlracheua";
	public final static String SUBMIT_PHONE_NUMBER = "/ssahuschrujO";
	public final static String SUCCESS_MESSAGE = "/success";
	public final static String TRIGGER_OPEN_APP = "/shbncco24";
	public final static String TRIGGER_PUSH = "/crbsaubatuhbka89384ghnmm";
	public final static String TRIGGER_PUSH_CRON = "/mnjtoxjnotd";
	public final static String TRIGGER_PUSH_RESPONSE = "/haeicrhasur";
	public final static String MAIN_URL = "https://www.nearauth.ai";
	public static String URL_WITHOUT_PROTOCOL = getUrlWithoutProtocol();
	public final static String SECURE_URL = getSecureUrl();
	public final static String ICON_PATH = SECURE_URL + "/imgFiles/favicon.ico";
	public final static String SERVER = "/server";
	public final static String SERVERS = "/servers";

	public final static String WRONG_USER = "/wrongUser";

	private static String urlWithoutProtocal = null;
	private static String secureUrl = null;

	public static String getSecureUrl() {

		if (Urls.secureUrl == null) {
			if (Constants.ENVIRONMENT == CodeEnvironment.LOCAL) {
				Urls.secureUrl = "http://" + getUrlWithoutProtocol();
			} else {
				Urls.secureUrl = "https://" + getUrlWithoutProtocol();
			}
		}
		return Urls.secureUrl;
	}

	public static String getUrlWithoutProtocol() {
		// if (Urls.urlWithoutProtocal == null) {
		Constants.ENVIRONMENT = Constants.getCurrentEnv();
		if (Constants.ENVIRONMENT == CodeEnvironment.PROD) {
			Urls.urlWithoutProtocal = "secure.nearauth.ai";
		} else if (Constants.ENVIRONMENT == CodeEnvironment.TEST) {
			Urls.urlWithoutProtocal = "test.nearauth.ai";
		} else if (Constants.ENVIRONMENT == CodeEnvironment.DEV) {
			Urls.urlWithoutProtocal = "dev.nearauth.ai";
		} else if (Constants.ENVIRONMENT == CodeEnvironment.LOCAL) {
			Urls.urlWithoutProtocal = "localhost:8080";
		}
		new DataAccess().addLog("env: " + Urls.urlWithoutProtocal);
		// }
		return Urls.urlWithoutProtocal;
	}

	public static String getTest() {
		String test = "";
		if (Constants.ENVIRONMENT == CodeEnvironment.TEST) {
			test = "_test";
		} else if (Constants.ENVIRONMENT == CodeEnvironment.DEV) {
			test = "_dev";
		}
		return test;
	}

}

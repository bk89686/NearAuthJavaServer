package com.humansarehuman.blue2factor.authentication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.util.TextUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.ui.ModelMap;

import com.humansarehuman.blue2factor.communication.PushNotifications;
import com.humansarehuman.blue2factor.communication.twilio.TextMessage;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess.CheckInstance;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceConnectionDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.AdminSignin;
import com.humansarehuman.blue2factor.entities.BasicDeviceParameters;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.BasicResponsePlusExtraBoolean;
import com.humansarehuman.blue2factor.entities.CompanyResponseHelper;
import com.humansarehuman.blue2factor.entities.ConnectionAttrs;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.RequestAndResponse;
import com.humansarehuman.blue2factor.entities.User;
import com.humansarehuman.blue2factor.entities.enums.AuthorizationMethod;
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.enums.UserType;
import com.humansarehuman.blue2factor.entities.jsonConversion.ApiRequestWithJsonKey;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.AuthorizationDbObj;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlIdentityProviderDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlServiceProviderDbObj;
import com.humansarehuman.blue2factor.entities.tables.ServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.saml.PublicKeyUtil;
import com.humansarehuman.blue2factor.utilities.saml.Saml;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public abstract class BaseController {
	@SuppressWarnings("ucd")
	boolean DEBUG = true;
	public final int SUCCESS = 0;
	@SuppressWarnings("ucd")
	public final int FAILURE = 1;
	@SuppressWarnings("ucd")
	public final int ERROR = -1;
	public String instanceId = "";
	public String baseUrl = "";

	protected final String myCompanyId = "MXJ9469AA88";
	protected final PrivateKey pk = getClientPrivateKey();


	private PrivateKey getClientPrivateKey() {
		PrivateKey pk = null;
		try {
			InputStream propFile = getClass().getResourceAsStream("/application.properties");
			if (propFile != null) {
				Properties prop = new Properties();
				prop.load(propFile);
				String pkStr = prop.getProperty("client.private.key");
				if (pkStr != null) {
					Encryption encryption = new Encryption();
					pk = encryption.stringToPrivateKey(pkStr);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pk;
	}

	protected String getSession(HttpServletRequest request, String name) {
		String session = null;
		try {
			session = (String) request.getSession().getAttribute(name);
		} catch (Exception e) {
			// no session?
		}
		return session;
	}

	public String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String sStackTrace = e.getLocalizedMessage() + "\r\n" + sw.toString();
		return sStackTrace;
	}

	public void addServiceProvider(String companyId) {
		SamlDataAccess dataAccess = new SamlDataAccess();

		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		Encryption encryption = new Encryption();
		try {
			dataAccess.addLog("adding service provider");
			String serviceProviderId = "";
			KeyPair keyPair = encryption.generateNewRsaKey();
			PrivateKey privateKey = keyPair.getPrivate();
			String privateString = encryption.privateKeyToString(privateKey);
			String x509 = new PublicKeyUtil().getX509CertAsString(privateString);
			dataAccess.expireKeysByTypeAndCompanyId(KeyType.SP_PRIVATE, companyId);
			dataAccess.expireKeysByTypeAndCompanyId(KeyType.SP_CERT, companyId);
			KeyDbObj privateKeyDbObj = new KeyDbObj(null, null, null, companyId, KeyType.SP_PRIVATE, privateString,
					false, null, null);
			dataAccess.addKey(privateKeyDbObj);
			KeyDbObj certKeyDbObj = new KeyDbObj(null, null, null, companyId, KeyType.SP_CERT, x509, true, null, null);
			dataAccess.addKey(certKeyDbObj);
			// TODO: Allow uploading of service provider encryption cert
			SamlServiceProviderDbObj sp = new SamlServiceProviderDbObj(GeneralUtilities.randomString(), now,
					serviceProviderId, null, null, null, null, true, companyId, "", null, null, null);
			dataAccess.addSamlServiceProvider(sp);
			dataAccess.addLog("done adding service provider");
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
	}

	protected boolean addCompanyIdpKeysIfNeeded(String companyId) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		Encryption encryption = new Encryption();
		boolean success = false;

		try {
			KeyDbObj coIdpPrivate = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_PRIVATE, companyId);
			KeyDbObj coIdpCert = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_CERT, companyId);
			if (coIdpPrivate == null || coIdpCert == null) {
				dataAccess.addLog("adding keys");
				KeyPair keyPair = encryption.generateNewRsaKey();
				PrivateKey privateKey = keyPair.getPrivate();
				String privateString = encryption.privateKeyToString(privateKey);
				String x509 = new PublicKeyUtil().getX509CertAsString(privateString);
				dataAccess.expireKeysByTypeAndCompanyId(KeyType.IDP_PRIVATE, companyId);
				dataAccess.expireKeysByTypeAndCompanyId(KeyType.IDP_CERT, companyId);
				KeyDbObj privateKeyDbObj = new KeyDbObj(null, null, null, companyId, KeyType.IDP_PRIVATE, privateString,
						false, null, null);
				dataAccess.addKey(privateKeyDbObj);
				KeyDbObj certKeyDbObj = new KeyDbObj(null, null, null, companyId, KeyType.IDP_CERT, x509, true, null,
						null);
				dataAccess.addKey(certKeyDbObj);
				success = true;
			} else {
				success = true;
			}

		} catch (

		Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}

	protected boolean addCompanyIdpKeys(String companyId) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		Encryption encryption = new Encryption();
		boolean success = false;
		try {
			KeyPair keyPair = encryption.generateNewRsaKey();
			PrivateKey privateKey = keyPair.getPrivate();
			String privateString = encryption.privateKeyToString(privateKey);
			String x509 = new PublicKeyUtil().getX509CertAsString(privateString);
			dataAccess.expireKeysByTypeAndCompanyId(KeyType.IDP_PRIVATE, companyId);
			dataAccess.expireKeysByTypeAndCompanyId(KeyType.IDP_CERT, companyId);
			KeyDbObj privateKeyDbObj = new KeyDbObj(null, null, null, companyId, KeyType.IDP_PRIVATE, privateString,
					false, null, null);
			dataAccess.addKey(privateKeyDbObj);
			KeyDbObj certKeyDbObj = new KeyDbObj(null, null, null, companyId, KeyType.IDP_CERT, x509, true, null, null);
			dataAccess.addKey(certKeyDbObj);
			success = true;
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}

	protected int handleNotProximate(DeviceDbObj device) {
		GroupDbObj group = new CompanyDataAccess().getGroupByDeviceId(device.getDeviceId());
		return handleNotProximate(group, device);
	}

	protected int handleNotProximate(IdentityObjectFromServer idObj) {
		return handleNotProximate(idObj.getGroup(), idObj.getDevice());
	}

	protected int handleNotProximate(GroupDbObj group, DeviceDbObj device) {
		int outcome = Outcomes.FAILURE;
		SamlDataAccess dataAccess = new SamlDataAccess();
		if (dataAccess.areBiometricsAvailableForDevice(device)) {
			outcome = Outcomes.GET_BIOMETRICS;
		} else {
			if (!device.isCentral()) {
				DeviceDbObj centralDevice = dataAccess.getCentralForPeripheral(device);
				if (dataAccess.isCentralDumbphone(centralDevice)) {
					if (group.isTextAllowed()) {
						TextMessage text = new TextMessage();
						text.textCodeToCentral(device, dataAccess);
						outcome = Outcomes.TEXT_SENT;
					}
				} else {
					if (group != null) {
						if (group.isPushAllowed()) {
							PushNotifications pushNotification = new PushNotifications();
							ApiResponse apiResponse = pushNotification.sendLoudPushByDevice(device, false);
							if (apiResponse != null) {
								if (apiResponse.outcome == Outcomes.SUCCESS) {
									outcome = Outcomes.LOUD_PUSH_SENT;
								}
							}
						}
					}
				}
			}
		}
		return outcome;
	}

	protected String getPersistentToken(HttpServletRequest request) {
		String token = this.getCookie(request, "B2F_AUTH");
		DataAccess dataAccess = new DataAccess();
		if (TextUtils.isEmpty(token) && !token.equals("null")) {
			dataAccess.addLog("cookie not found");
			token = this.getSession(request, "B2F_AUTH");
			if (TextUtils.isEmpty(token)) {
				dataAccess.addLog("session not found");
			}
		} else {
			dataAccess.addLog("cookie found");
		}
		dataAccess.addLog("token='" + token + "'");
		return token;
	}

	public IdentityObjectFromServer getIdentityObjectFromCookie(HttpServletRequest request, String apiKey) {
		String token = this.getPersistentToken(request);
		IdentityObjectFromServer idObj = null;
		if (!TextUtils.isEmpty(token)) {
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			idObj = getIdObjWithApiKey(token, apiKey, dataAccess);
		} else {
			CompanyDataAccess da = new CompanyDataAccess();
			da.addLog("token was null");
//			GroupDbObj group = da.getGroupByEmail("chris@humansarehuman.com");
//			ArrayList<DeviceDbObj> devices = da.getDevicesByGroupId(group.getGroupId(), true);
//			for (DeviceDbObj device : devices) {
//				if (device.getDeviceType().equals("arm64")) {
//					device.setSignedIn(false);
//					da.updateDevice(device, "getIdentityObjectFromCookie");
//					break;
//				}
//			}
		}
		return idObj;
	}

	protected TokenDbObj getPersistentTokenObj(HttpServletRequest request) {
		TokenDbObj token = null;
		String cookie = getPersistentToken(request);
		if (!TextUtils.isEmpty(cookie)) {
			DataAccess dataAccess = new DataAccess();
			token = dataAccess.getToken(cookie);
		}
		return token;
	}

	public IdentityObjectFromServer getIdObjWithoutCompany(HttpServletRequest request) {
		IdentityObjectFromServer idObj = null;
		TokenDbObj authToken = getPersistentTokenObj(request);
		String authTokenStr = null;
		if (authToken != null) {
			authTokenStr = authToken.getTokenId();
		}
		if (!TextUtils.isEmpty(authTokenStr)) {
			idObj = getIdObjWithoutCompany(authTokenStr);
		}
		return idObj;
	}

	public IdentityObjectFromServer getIdObjWithoutCompany(String authToken) {
		String companyId = null;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		return getIdObj(authToken, companyId, dataAccess);
	}

	public IdentityObjectFromServer getIdObjWithoutCompany(String authToken, CompanyDataAccess dataAccess) {
		String companyId = null;
		return getIdObj(authToken, companyId, dataAccess);
	}

	protected IdentityObjectFromServer getIdObjWithApiKey(String authToken, String apiKey,
			CompanyDataAccess dataAccess) {
		IdentityObjectFromServer idObj = null;
		DeviceDbObj device = null;
		CompanyDbObj company = null;
		TokenDbObj token = null;
		BrowserDbObj browser = null;
		dataAccess.addLog("start");
		try {
			if (!TextUtils.isEmpty(authToken)) {
				token = dataAccess.getActiveTokenByOrDescriptionAndTokenId(TokenDescription.AUTHENTICATION,
						TokenDescription.ADMIN, authToken);
				if (token != null) {
					dataAccess.addLog("tokenFound");
					device = dataAccess.getDeviceByDeviceId(token.getDeviceId());
					if (device != null) {
						dataAccess.addLog("deviceFound");

					}
					browser = dataAccess.getBrowserById(token.getBrowserId());
					if (browser != null && device == null) {
						device = dataAccess.getDeviceByBrowserId(browser.getBrowserId());
					}
					if (browser == null && device != null) {
						dataAccess.getBrowserForDevice(device);
					}
				} else {
					dataAccess.addLog("token was null");
				}
			} else {
				dataAccess.addLog("auth token not found");
			}
			company = dataAccess.getCompanyByApiKey(apiKey);
			if (company != null && company.isActive()) {
				dataAccess.addLog("company found by ApiKey");
			}
			idObj = new IdentityObjectFromServer(company, device, browser, token, false);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return idObj;
	}

	protected IdentityObjectFromServer getIdObj(String authToken, String apiKey, CompanyDataAccess dataAccess) {
		IdentityObjectFromServer idObj = null;
		DeviceDbObj device = null;
		CompanyDbObj company = null;
		CompanyDbObj apiCompany = null;
		TokenDbObj token = null;
		BrowserDbObj browser = null;
		try {
			if (!TextUtils.isEmpty(authToken)) {
				token = dataAccess.getActiveTokenByOrDescriptionAndTokenId(TokenDescription.AUTHENTICATION,
						TokenDescription.ADMIN, authToken);
				if (token != null) {
					dataAccess.addLog("tokenFound: " + token.getTokenId());
					device = dataAccess.getDeviceByDeviceId(token.getDeviceId());
					if (device != null) {
						dataAccess.addLog("deviceFound");

					}
					browser = dataAccess.getBrowserById(token.getBrowserId());
					if (browser != null) {
						dataAccess.addLog("browserFound");
						if (device == null) {
							device = dataAccess.getDeviceByBrowserId(browser.getBrowserId());
						}
					} else {
						dataAccess.addLog("browser not initially Found");
						if (device != null) {
							browser = dataAccess.getBrowserForDevice(device);
						}
					}
				} else {
					dataAccess.addLog("token was null");
				}
			} else {
				dataAccess.addLog("auth token not found");
			}
			apiCompany = dataAccess.getCompanyByApiKey(apiKey);
			if (apiCompany != null && apiCompany.isActive()) {
				if (device != null) {
					company = dataAccess.getCompanyByDevId(device.getDeviceId());
					dataAccess.addLog("company found by ApiKey");
					if (company == null || !company.getCompanyId().equals(apiCompany.getCompanyId())) {
						dataAccess.addLog("company mismatch");
						company = null;
					}
				} else {
					if (browser != null) {
						device = dataAccess.getDeviceByDeviceId(browser.getDeviceId());
						if (device != null) {
							dataAccess.addLog("device found via browser");
						}
					} else {
						dataAccess.addLog("device still null");
					}
				}
			} else {
				dataAccess.addLog("company not found by api");
				if (browser != null) {
					device = dataAccess.getDeviceByDeviceId(browser.getDeviceId());
					if (device != null) {
						company = dataAccess.getCompanyByDevId(device.getDeviceId());
						dataAccess.addLog("device and company found via browser");
					} else {
						dataAccess.addLog("device not found by browser");
					}
				} else {
					dataAccess.addLog("browser was null, couldn't find company", LogConstants.WARNING);
					if (device != null) {
						browser = dataAccess.getBrowserForDevice(device);
						dataAccess.addLog("we have no data on this device", LogConstants.WARNING);
					}
				}
			}
			idObj = new IdentityObjectFromServer(company, device, browser, false);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return idObj;
	}

	protected IdentityObjectFromServer getIdObj(String authToken, CompanyDbObj company, CompanyDataAccess dataAccess) {
		IdentityObjectFromServer idObj = null;
		DeviceDbObj device = null;
		TokenDbObj token = null;
		BrowserDbObj browser = null;
		try {
			if (!TextUtils.isEmpty(authToken)) {
				token = dataAccess.getActiveTokenByOrDescriptionAndTokenId(TokenDescription.AUTHENTICATION,
						TokenDescription.ADMIN, authToken);
				if (token != null) {
					dataAccess.addLog("tokenFound: " + token.getTokenId());
					device = dataAccess.getDeviceByDeviceId(token.getDeviceId());
					if (device != null) {
						dataAccess.addLog("deviceFound");

					}
					browser = dataAccess.getBrowserById(token.getBrowserId());
					if (browser != null) {
						dataAccess.addLog("browserFound");
						if (device == null) {
							device = dataAccess.getDeviceByBrowserId(browser.getBrowserId());
							if (device != null) {
								token.setDeviceId(device.getDeviceId());
								dataAccess.updateToken(token);
								dataAccess.addLog("deviceId added to token");
							}
						}
					} else {
						if (browser == null && device != null) {
							browser = dataAccess.getBrowserForDevice(device);
						}
					}
				} else {
					dataAccess.addLog("token was null");
				}
			} else {
				dataAccess.addLog("auth token not found");
			}
			if (company != null && company.isActive()) {
				dataAccess.addLog("company found by ApiKey");
			} else {
				dataAccess.addLog("company not found by ApiKey");
				if (company == null && device != null) {
					company = dataAccess.getCompanyByDevId(device.getDeviceId());
					if (company != null) {
						dataAccess.addLog("company found by device");
					} else {
						dataAccess.addLog("company not found by device");
					}
				} else {
					dataAccess.addLog("company not found by device b/c device was null");
				}
			}
			idObj = new IdentityObjectFromServer(company, device, browser, false);
		} catch (Exception e) {
			dataAccess.addLog("getIdObj", e);
		}
		return idObj;
	}

	protected IdentityObjectFromServer getIdObj(DeviceDataAccess dataAccess, CompanyDbObj company, DeviceDbObj device,
			TokenDbObj token, boolean fromJs) {
		BrowserDbObj browser = null;
		if (!TextUtils.isEmpty(token.getBrowserId())) {
			browser = dataAccess.getBrowserById(token.getBrowserId());
			if (browser == null && device != null) {
				browser = dataAccess.getBrowserForDevice(device);
			}
		}
		IdentityObjectFromServer idObj = new IdentityObjectFromServer(company, device, browser, fromJs);
		return idObj;
	}

	protected String addDevicePublicKeyAndReturnPublicKey(String companyId, DeviceDbObj device, String groupId,
			String publicKey, String url, CompanyDataAccess dataAccess)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IOException {
		String outgoingKey = null;
		if (addDevicePublicKey(device, groupId, publicKey, url)) {
			outgoingKey = new Encryption().createAndSaveKeyForDevice(companyId, groupId, device.getDeviceId(),
					dataAccess);
		}
		return outgoingKey;
	}

	protected boolean addDevicePublicKey(DeviceDbObj device, String groupId, String publicKey, String url) {
		return addDevicePublicKey(device, publicKey, url, KeyType.DEVICE_PUBLIC_KEY);
	}

	protected void addDeviceForegroundPublicKey(DeviceDbObj device, String groupId, String publicKey, String url) {
		addDevicePublicKey(device, publicKey, url, KeyType.DEVICE_PUBLIC_KEY_FOREGROUND);
	}

	protected boolean addDevicePublicKey(DeviceDbObj device, String publicKey, String url, KeyType keyType) {
		boolean keyCreated = false;
		String deviceId = device.getDeviceId();
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		// dataAccess.deactivateOldKeysByTypeAndDevice(keyType, deviceId);
		dataAccess.addLog(deviceId, "public key text: " + publicKey, LogConstants.TEMPORARILY_IMPORTANT);
		CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
		if (company != null) {
			KeyDbObj key = new KeyDbObj(deviceId, null, device.getGroupId(), company.getCompanyId(), keyType, publicKey,
					true, "RSA", url);
			dataAccess.deactivateKeyTypeForDeviceAndUrlExcept(keyType, deviceId, url, key.getKeyId());
			new DataAccess().addKey(key);
			keyCreated = true;
		} else {
			dataAccess.addLog("company was null when adding key. I think this is bad.", LogConstants.ERROR);
		}
		return keyCreated;
	}

//	protected boolean addDevicePublicKey(String deviceId, String publicKey, String url, KeyType keyType) {
//		boolean keyCreated = false;
//		CompanyDataAccess dataAccess = new CompanyDataAccess();
//		// dataAccess.deactivateOldKeysByTypeAndDevice(keyType, deviceId);
//		dataAccess.addLog(deviceId, "public key text: " + publicKey);
//		CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
//		if (company != null) {
//			KeyDbObj key = new KeyDbObj(deviceId, null, null, company.getCompanyId(), keyType, publicKey, true, "RSA",
//					url);
//			dataAccess.deactivateKeyTypeForDeviceAndUrlExcept(keyType, deviceId, url, key.getKeyId());
//			new DataAccess().addKey(key);
//			keyCreated = true;
//		} else {
//			dataAccess.addLog("company was null when adding key. I think this is bad.", LogConstants.ERROR);
//		}
//		return keyCreated;
//	}

	protected void showAllRequestParameters(HttpServletRequest request) {
		DataAccess dataAccess = new DataAccess();
		Enumeration<String> enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String parameterName = enumeration.nextElement().toString();
			dataAccess.addLog("showAllRequestParameters", parameterName + ": " + request.getParameter(parameterName));
		}
		dataAccess.addLog("showAllRequestParameters", "end parameters");
	}

	public HttpServletResponse addSecureHeaders(HttpServletResponse httpResponse) {
		httpResponse.addHeader("content-security-policy",
				"default-src 'none'; frame-ancestors 'none'; img-src 'self' https: data:; script-src 'self'; "
						+ "style-src 'self' https://fonts.googleapis.com/css; base-uri 'self'; form-action 'self'; "
						+ "object-src 'none'; manifest-src 'self'; font-src 'self' https://fonts.gstatic.com; "
						+ "connect-src 'self';");
		httpResponse.addHeader("Referrer-Policy", "strict-origin-when-cross-origin");
		httpResponse.addHeader("x-frame-options", "DENY");
		httpResponse.addHeader("x-xss-protection", "1; mode=block");
		httpResponse.addHeader("strict-transport-security", "max-age=31536000;includeSubDomains");
		httpResponse.addHeader("x-content-type-options", "nosniff");
		return httpResponse;
	}

	protected boolean setupPeripheralDevice(DeviceDbObj peripheralDevice, String groupId) {
		return new DeviceDataAccess().setGroupIdAndActive(peripheralDevice, groupId, true);
	}

	protected void setupConnection(DeviceDbObj centralDevice, DeviceDbObj peripheralDevice) {
		DeviceConnectionDataAccess dataAccess = new DeviceConnectionDataAccess();
		dataAccess.addLog(centralDevice.getDeviceId(), "completing connection setup", LogConstants.DEBUG);
		DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(peripheralDevice, false);
		dataAccess.setCentralIdGroupIdAndActive(centralDevice.getDeviceId(), centralDevice.getGroupId(), connection);
	}

	@SuppressWarnings("unchecked")
	public byte[] convertClientDataJson(String clientDataJson64) {
		byte[] newClientDataJsonBytes = null;
		// byte[] clientDataJsonBytes = Base64.decodeBase64(clientDataJson64);
		// String clientDataString =
		// java.util.Base64.getEncoder().encodeToString(clientDataJsonBytes);
		Base64 decoder = Base64.builder().setUrlSafe(true).get();
		byte[] decodedBytes = decoder.decode(clientDataJson64);
		String result = new String(decodedBytes);
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("start: " + clientDataJson64 + " - end: " + result);
		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(result);
			json.remove("androidPackageName");
			JSONObject newNode = new JSONObject();
			newNode.put("status", "supported");
			newNode.put("id", java.util.Base64.getEncoder().encodeToString(new byte[] { 0x01, 0x23, 0x45 }));
			json.put("tokenBinding", newNode);
			dataAccess.addLog("newData: " + json.toJSONString());
			newClientDataJsonBytes = json.toJSONString().getBytes("utf-8");

		} catch (Exception e) {
			dataAccess.addLog("convertClientDataJson", e);
		}
		return newClientDataJsonBytes;
	}

	public byte[] convertAuthenticatorData(String authData64) {
		char[] authDataArray = authData64.toCharArray();
		int arrayLen = (int) (authData64.length() * 0.75);
		byte[] fullArray = new byte[arrayLen];
		int newArrayIndex = 0;
		for (int i = 0; i < authDataArray.length; i += 4) {
			Character ch2 = null;
			Character ch3 = null;
			Character ch4 = null;
			if (i + 1 < authDataArray.length) {
				ch2 = authDataArray[i + 1];
				if (i + 2 < authDataArray.length) {
					ch3 = authDataArray[i + 2];
					if (i + 3 < authDataArray.length) {
						ch4 = authDataArray[i + 3];
					}
				}
			}
			byte[] bytes = getBytesFromCode(authDataArray[i], ch2, ch3, ch4);
			fullArray[newArrayIndex] = bytes[0];
			if (newArrayIndex + 1 < arrayLen) {
				fullArray[newArrayIndex + 1] = bytes[1];
				if (newArrayIndex + 2 < arrayLen) {
					fullArray[newArrayIndex + 2] = bytes[2];
				}
			}
			newArrayIndex += 3;
		}
		return fullArray;
	}

	private byte[] getBytesFromCode(Character ch1, Character ch2, Character ch3, Character ch4) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
		char[] charsArray = chars.toCharArray();
		byte indx1 = (byte) GeneralUtilities.getIndexOfArrayElement(charsArray, ch1);
		byte indx2 = 0;
		byte indx3 = 0;
		byte indx4 = 0;
		if (ch2 != null) {
			indx2 = (byte) GeneralUtilities.getIndexOfArrayElement(charsArray, ch2);
			if (ch3 != null) {
				indx3 = (byte) GeneralUtilities.getIndexOfArrayElement(charsArray, ch3);
				if (ch4 != null) {
					indx4 = (byte) GeneralUtilities.getIndexOfArrayElement(charsArray, ch4);
				}
			}
		}
		byte[] bytes = new byte[3];
		bytes[0] = (byte) ((indx1 << 2) | (indx2 >> 4));
		bytes[1] = (byte) (((indx2 & 15) << 4) | (indx3 >> 2));
		bytes[2] = (byte) (((indx3 & 3) << 6) | (indx4 & 63));
		return bytes;
	}

	protected BasicResponse validateFromJs(DeviceDbObj incomingDevice, DeviceDbObj device, String bssId, String token,
			String rnd, String baseUrl) {
		String reason = "";
		String newTokenId = "";
		int outcome = Outcomes.FAILURE;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("entry", LogConstants.INFO);
		if (device != null && device.isActive()) {
			dataAccess.addLog(device.getDeviceId(), "validating from browser with ipAddress: " + bssId,
					LogConstants.INFO);
			if (dataAccess.isAccessAllowed(device, "validateFromJs")) {
				outcome = Outcomes.SUCCESS;
				reason = "Verified.";
				dataAccess.setLastCompleteCheck(device, new Date());
			} else {
				reason = "check not found or too old or wrong bssId: " + bssId;
			}
			newTokenId = getNewToken(device, bssId, token, TokenDescription.BROWSER_SESSION, baseUrl);
		} else {
			if (device == null) {
				dataAccess.addLog("validateFromJs", "validating from browser: Device is null - ip: " + bssId,
						LogConstants.INFO);
				reason += "device not found -";
			} else {
				reason += "device inactive -";
				dataAccess.addLog(device.getDeviceId(), "validating from browser: Device inactive - ip: " + bssId,
						LogConstants.INFO);
			}
			reason += " for " + token;
		}
		dataAccess.addLog("validateFromJs", "validateFromJs - exit", LogConstants.DEBUG);
		BasicResponse response = new BasicResponse(outcome, reason, newTokenId);
		return response;
	}

	private String getNewToken(DeviceDbObj device, String bssId, String token, TokenDescription tokenDescription,
			String baseUrl) {
		DataAccess dataAccess = new DataAccess();
		BrowserDbObj browser = dataAccess.getBrowserByToken(token, tokenDescription);
		TokenDbObj newToken = dataAccess.addToken(device, browser.getBrowserId(), TokenDescription.BROWSER_SESSION,
				baseUrl);
		String newTokenId = newToken.getTokenId();
		return newTokenId;
	}

//	protected AdminSignin validateCompanyAdminById(GroupDbObj group, String b2fId, String ipAddress) {
//		DataAccess dataAccess = new DataAccess();
//		DeviceDbObj device = dataAccess.getDeviceByToken(b2fId);
//		return this.validateCompanyAdminByDevice(group, device, ipAddress);
//	}

	protected AdminSignin validateAdminByCookie(HttpServletRequest request, HttpServletResponse response) {
		String gid = getMainGroupCookie(request);
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("token= '" + gid + "'", LogConstants.TEMPORARILY_IMPORTANT);

		TokenDbObj token = dataAccess.getToken(gid);
		String reason = "";
		AdminSignin adminSignin = null;

		if (token != null) {
			dataAccess.addLog("token= found", LogConstants.TEMPORARILY_IMPORTANT);
			if (token.getPermission() % 2 == 1) {
				if (token.getExpireTime().after(DateTimeUtilities.getCurrentTimestamp())) {
					GroupDbObj group = dataAccess.getGroupByToken(gid);
					if (group != null) {
						dataAccess.addLog(group.toString());
						adminSignin = isGroupAllowed(group, response);
					} else {
						dataAccess.addLog("group not found");
						reason = Constants.USER_NOT_FOUND;
					}
				} else {
					reason = Constants.EXPIRED_TOKEN;
				}
			} else {
				reason = Constants.NOT_PERMITTED;
			}
		} else {
			reason = Constants.INACTIVE_USER;
		}
		if (adminSignin == null) {
			response = deleteAdminToken(response);
			adminSignin = new AdminSignin(null, null, false, false, false, reason, false, response);
		}
		return adminSignin;
	}

	private AdminSignin validateWithUserName(HttpServletRequest request, HttpServletResponse response,
			String username) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String pw = this.getRequestValue(request, "pw");
		GroupDbObj group = dataAccess.getGroupByUserPw(username, pw);
		AdminSignin adminSignin = null;
		if (group != null) {

			adminSignin = isGroupAllowed(group, response);
			dataAccess.addLog("group was Found", LogConstants.TEMPORARILY_IMPORTANT);
		} else {
			dataAccess.addLog("group was null", LogConstants.WARNING);
			adminSignin = new AdminSignin(null, group, false, false, false, Constants.INCORRECT_CREDS, false, response);
		}
		return adminSignin;
	}

	protected HttpServletResponse updateCompanyPageCookie(HttpServletResponse response, GroupDbObj group) {
		TokenDbObj token = new DataAccess().addGroupToken(group, 1);
		if (token != null) {
			response = setMainGroupCookie(response, token.getTokenId());
		}
		return response;
	}

	protected AdminSignin checkPermission(HttpServletRequest request, HttpServletResponse response) {
		String username = getRequestValue(request, "username");
		AdminSignin adminSignin;
		if (!TextUtils.isBlank(username)) {
			adminSignin = validateWithUserName(request, response, username);
		} else {
			adminSignin = validateAdminByCookie(request, response);
		}
		return adminSignin;
	}

	private HttpServletResponse deleteAdminToken(HttpServletResponse response) {
		response = setMainGroupCookie(response, "");
		return response;
	}

	@SuppressWarnings("unused")
	private boolean isAdmin(DeviceDbObj device) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		GroupDbObj group = dataAccess.getGroupById(device.getGroupId());
		return isAdmin(group);
	}

	private boolean isAdmin(GroupDbObj group) {
		boolean success = false;
		if (group != null) {
			new DataAccess().addLog("userType: " + group.getUserType());
			success = group.getUserType() == UserType.ADMIN || group.getUserType() == UserType.SUPER_ADMIN
					|| group.getUserType() == UserType.AUDITOR || group.getUserType() == UserType.UPDATE_SERVERS
					|| group.getUserType() == UserType.UPDATE_USERS
					|| group.getUserType() == UserType.UPDATE_USERS_AND_SERVERS
					|| group.getUserType() == UserType.ADMIN_VIEWER;
		}
		return success;
	}

	public int getVersion(HttpServletRequest request) {
		int version = 1;
		DataAccess dataAccess = new DataAccess();
		try {
			version = Integer.parseInt(getRequestValue(request, "v"));
		} catch (NumberFormatException e) {
			dataAccess.addLog("version not found");
		}
		if (version < Constants.CURRENT_VERSION) {
			dataAccess.addLog("this is an old version of our software", LogConstants.INFO);
		} else {
			dataAccess.addLog("current version");
		}
		return version;
	}

	protected Double getInitialDevicePriority(OsClass os) {
		Double priority;
		switch (os) {
		case IOS:
			priority = 150.0;
			break;
		case ANDROID:
			priority = 250.0;
			break;
		case WATCHOS:
			priority = 350.0;
			break;
		case WINDOWS:
			priority = 450.0;
			break;
		case OSX:
			priority = 650.0;
			break;
		default:
			priority = -1.0;
		}
		return priority;
	}

	public void printAllRequestParams(HttpServletRequest request) {
		DataAccess dataAccess = new DataAccess();
		Map<String, String[]> params = request.getParameterMap();
		params.forEach((k, v) -> dataAccess.addLog("Key = " + k + ", Value = " + getRequestValue(request, k)));
		Enumeration<String> headers = request.getHeaderNames();
		while (headers.hasMoreElements()) {
			String key = headers.nextElement();
			dataAccess.addLog("key: " + key + " = " + request.getHeader(key));
		}
		Enumeration<String> attributes = request.getAttributeNames();
		String requestValue;
		while (attributes.hasMoreElements()) {
			try {
				String attribute = attributes.nextElement();
				requestValue = (String) request.getAttribute(attribute);
				dataAccess.addLog("key: " + attribute + " => " + requestValue);
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public String getRequestValue(HttpServletRequest request, String value) {
		String requestValue = "";
		DataAccess dataAccess = new DataAccess();
		if (request.getParameter(value) == null) {
			requestValue = (String) request.getAttribute(value);
		} else {
			requestValue = request.getParameter(value).trim();
		}
		if (requestValue == null) {
			requestValue = "";
		}
		requestValue = requestValue.replace("%2B", "+");
		dataAccess.addLog(value + ": " + requestValue);
		return requestValue;
	}

	public boolean getRequestValueBoolean(HttpServletRequest request, String value, boolean defaultVal) {
		boolean newVal = defaultVal;
		String val = getRequestValue(request, value);
		if (val != null) {
			if (val.equals("true")) {
				newVal = true;
			} else if (val.equals("false")) {
				newVal = false;
			}
		}
		return newVal;
	}

	public String getKey(HttpServletRequest request) {
		String keyEnc = this.getRequestValue(request, "easu4heiv4");
		Encryption encryption = new Encryption();
		String decrypted = encryption.decryptKey(keyEnc);
		return decrypted;
	}

	public String getInitVector(HttpServletRequest request) {
		String keyEnc = this.getRequestValue(request, "jnpadmi85");
		Encryption encryption = new Encryption();
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("before Decrypt");
		String decrypted = encryption.decryptKey(keyEnc);
		dataAccess.addLog("decrypted: " + decrypted);
		return decrypted;
	}

	public boolean getEncryptedRequestBoolean(HttpServletRequest request, String value, String key, String initVector,
			boolean defaultVal) {
		boolean newVal = defaultVal;
		DataAccess dataAccess = new DataAccess();
		try {
			String val = getEncryptedRequestValue(request, value, key, initVector);
			dataAccess.addLog("val for " + value + " = " + val);
			if (val != null) {
				if (val.equals("true")) {
					newVal = true;
				} else if (val.equals("false")) {
					newVal = false;
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return newVal;
	}

	public String getEncryptedRequestValue(HttpServletRequest request, String value, String key, String iv) {
		String decrypted = null;
		try {
			String requestValue = getRequestValue(request, value);
			if (!requestValue.equals("")) {
				Encryption encryption = new Encryption();
				decrypted = encryption.decrypt(requestValue, key, iv);
				decrypted = decrypted.replace("#", "");
			}
		} catch (Exception e) {
			new DataAccess().addLog(e);
		}
		return decrypted;
	}

	public Integer getGmtOffset(HttpServletRequest request, String value) {
		int gmtOffset = -1;
		String gmtOffsetStr = getRequestValue(request, value);
		try {
			gmtOffset = Integer.parseInt(gmtOffsetStr);
		} catch (Exception e) {
			new DataAccess().addLog("Get GmtOffset Error for " + gmtOffsetStr, e);
		}
		return gmtOffset;
	}

	protected Integer getEncryptedGmtOffset(HttpServletRequest request, String value, String key, String iv) {
		int gmtOffset = -1;
		String gmtOffsetStr = getEncryptedRequestValue(request, value, key, iv);
		try {
			gmtOffset = Integer.parseInt(gmtOffsetStr);
		} catch (Exception e) {
			new DataAccess().addLog("Get GmtOffset Error for " + gmtOffsetStr, e);
		}
		return gmtOffset;
	}

	protected void logNonNullReason(BasicResponse response) {
		if (!TextUtils.isEmpty(response.getReason())) {
			new DataAccess()
					.addLog("non-null reason: " + response.getReason() + " with outcome: " + response.getOutcome());
		}
	}

	private void logNonNullReason(String reason) {
		if (!TextUtils.isEmpty(reason)) {
			new DataAccess().addLog("non-null reason: " + reason);
		}
	}

	/**
	 * deflate, base64 encoded, and url encode in that order
	 * 
	 * @param str to condense
	 * @return condensed string
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public String condense(String samlXml) throws UnsupportedEncodingException, IOException {

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(os, deflater);
		deflaterOutputStream.write(samlXml.getBytes("UTF-8"));
		deflaterOutputStream.close();
		os.close();
		String base64 = Base64.encodeBase64String(os.toByteArray());
		return base64; // URLEncoder.encode(base64, "UTF-8");
	}

	public ModelMap addSamlResponse(ModelMap model, Saml saml, String token) {
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog(saml.getSamlResponseAsString());
		try {
			String encoded = Base64.encodeBase64String(saml.getSamlResponseAsString().getBytes());
			dataAccess.addLog("condensed: " + encoded);
			model.addAttribute("action", saml.getAction());
			dataAccess.addLog("sending to " + saml.getAction());
			model.addAttribute("samlText", encoded);
			dataAccess.addLog("relayState " + token);
			model.addAttribute("relayState", token);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return model;
	}

	ModelMap addDeviceResponse(ModelMap model, BasicResponse response, BasicDeviceParameters params) {
		model.addAttribute("reason", response.getReason());
		model.addAttribute("outcome", response.getOutcome());
		model.addAttribute("token", response.getToken());
		model.addAttribute("instanceId", response.getInstanceId());
		model.addAttribute("serviceUuid", params.getServiceUuid());
		model.addAttribute("deviceId", params.getDeviceId());
		model.addAttribute("isCentral", Boolean.toString(params.isCentral()));
		model.addAttribute("centralType", params.getCentralType().osClassName());
		model.addAttribute("characteristic", params.getCharacteristic());
		model.addAttribute("previouslyConnected", params.isPreviouslyConnected());
		model.addAttribute("multiuser", params.isMultiuser());
		logNonNullReason(response);
		return model;
	}

	public ModelMap addBasicResponse(ModelMap model, BasicResponse response) {
		model.addAttribute("reason", response.getReason());
		model.addAttribute("outcome", response.getOutcome());
		model.addAttribute("token", response.getToken());
		model.addAttribute("instanceId", response.getInstanceId());
		model.addAttribute("connected", false);
		model.addAttribute("expireMillis", response.getExpireMillis());
		logNonNullReason(response);
		return model;
	}

	public ModelMap addBasicResponsePlusExtraBoolean(ModelMap model, BasicResponsePlusExtraBoolean response) {
		model.addAttribute("reason", response.getReason());
		model.addAttribute("outcome", response.getOutcome());
		model.addAttribute("token", response.getToken());
		model.addAttribute("extraBoolean", response.isExtraBoolan());
		logNonNullReason(response);
		return model;
	}

	protected ModelMap addReturnVars(ModelMap model, int outcome, String reason, ArrayList<ConnectionAttrs> devices,
			String instanceId, boolean connected, boolean subscribed, boolean connectedToAll, boolean showIcon,
			boolean fromFcm, boolean hasBle, boolean connecting) {
		return this.addReturnVars(model, outcome, reason, devices, instanceId, connected, subscribed, connectedToAll,
				showIcon, fromFcm, hasBle, connecting, 0);
	}

	protected ModelMap addReturnVars(ModelMap model, int outcome, String reason, ArrayList<ConnectionAttrs> devices,
			String instanceId, boolean connected, boolean subscribed, boolean connectedToAll, boolean showIcon,
			boolean fromFcm, boolean hasBle, boolean connecting, int pendingRequests) {
		model.addAttribute("outcome", outcome);
		model.addAttribute("reason", reason);
		model.addAttribute("instanceId", instanceId);
		model.addAttribute("connected", connected);
		model.addAttribute("subscribed", subscribed);
		model.addAttribute("connectedToAll", connectedToAll);
		model.addAttribute("showIcon", showIcon);
		model.addAttribute("fromFcm", fromFcm);
		model.addAttribute("hasBle", hasBle);
		model.addAttribute("deviceCount", devices.size());
		model.addAttribute("devices", devices);
		model.addAttribute("connecting", connecting);
		model.addAttribute("pendingRequests", pendingRequests);
		logNonNullReason(reason);
		return model;
	}

	protected ConnectionAttrs addNewCheckFromCentral(DeviceConnectionDbObj connection, DeviceDbObj centralDevice,
			DeviceDbObj peripheral, String instanceId, String clientIpAddress, String ssid, int iteration,
			CheckType checkType) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		boolean deviceConnected = dataAccess.isAccessAllowed(connection, centralDevice);
		boolean hasBle = centralDevice.getHasBle() && peripheral.getHasBle();
		ConnectionAttrs attrs = getExistingConnection(connection, centralDevice, peripheral, iteration, deviceConnected,
				hasBle, dataAccess);
		if (attrs == null) {
			String firstLetter = GeneralUtilities.randomLetters(1);
			String[] centralInstanceIdPair = new Encryption().createEncryptedInstanceId(centralDevice, firstLetter);
			if (centralInstanceIdPair != null) {
				dataAccess.addLog(peripheral.getDeviceId(), "new instance pair was created for our central",
						LogConstants.IMPORTANT);
				dataAccess.addLog(peripheral.getDeviceId(), "it is: " + centralInstanceIdPair[0] + " = " + centralInstanceIdPair[1],
						LogConstants.IMPORTANT);
				KeyDbObj key = Encryption.getDevicePublicKey(peripheral);
				if (key != null) {
					String[] peripheralInstanceIdPair = new Encryption().createEncryptedInstanceId(key, peripheral,
							firstLetter);
					if (peripheralInstanceIdPair != null) {
						attrs = new ConnectionAttrs(connection.getServiceUuid(), connection.getCharacteristicUuid(),
								"dev" + iteration, deviceConnected, centralInstanceIdPair[1],
								peripheralInstanceIdPair[1], instanceId, DateTimeUtilities.getCurrentTimestamp(),
								hasBle, connection.getPeripheralConnected(), connection.getCentralConnected(),
								connection.isSubscribed(), connection.getLastSuccess(), peripheral.getCommand(),
								peripheral.getDeviceClass().deviceClassName(), connection.getPeripheralIdentifier(),
								centralDevice.isPushFailure());
						if (hasBle || checkType != CheckType.PROX) {
							CheckDbObj check = new CheckDbObj(GeneralUtilities.randomString(),
									GeneralUtilities.randomString(20), connection.getCentralDeviceId(),
									connection.getPeripheralDeviceId(), connection.getServiceUuid(),
									centralDevice.getUserId(), clientIpAddress, ssid, null, null, false, false,
									Outcomes.INCOMPLETE, DateTimeUtilities.getCurrentTimestamp(), null, false,
									checkType, centralInstanceIdPair[0], peripheralInstanceIdPair[0]);
							dataAccess.addLog(centralDevice.getDeviceId(), "added Check", LogConstants.TEMPORARILY_IMPORTANT);
							dataAccess.addCheck(check);
						} else {
							dataAccess.addLog("check not added because Ble was not available for connection",
									LogConstants.WARNING);
						}
					} else {
						dataAccess.addLog(centralDevice.getDeviceId(), "wtf - no perf pair", LogConstants.ERROR);
					}
				} else {
					if (!peripheral.getHasBle()) {
						attrs = addConnectionForNoBleWithNullPublicKey(centralDevice, peripheral, connection,
								deviceConnected, checkType, clientIpAddress, ssid, iteration, dataAccess);
					}
				}
			} else {
				dataAccess.addLog("centralPair was null", LogConstants.WARNING);
			}
		}
		return attrs;
	}

	public ConnectionAttrs getExistingConnection(DeviceConnectionDbObj connection, DeviceDbObj central,
			DeviceDbObj peripheral, int iteration, boolean deviceConnected, boolean hasBle,
			DeviceDataAccess dataAccess) {
		ConnectionAttrs attrs = null;
		CheckInstance checkInstance = new DataAccess().getMostRecentCheckInstanceByDevices(central, peripheral,
				dataAccess);
		if (checkInstance != null) {
			attrs = new ConnectionAttrs(connection.getServiceUuid(), connection.getCharacteristicUuid(),
					"dev" + iteration, deviceConnected, checkInstance.getEncryptedCentralVal(),
					checkInstance.getEncryptedPeripheralVal(), checkInstance.getInstanceId(),
					DateTimeUtilities.getCurrentTimestamp(), hasBle, connection.getPeripheralConnected(),
					connection.getCentralConnected(), connection.isSubscribed(), connection.getLastSuccess(),
					peripheral.getCommand(), peripheral.getDeviceClass().deviceClassName(),
					connection.getPeripheralIdentifier(), central.isPushFailure());
		}
		return attrs;
	}

	public ConnectionAttrs addConnectionForNoBleWithNullPublicKey(DeviceDbObj centralDevice, DeviceDbObj peripheral,
			DeviceConnectionDbObj connection, boolean deviceConnected, CheckType checkType, String clientIpAddress,
			String ssid, int iteration, DataAccess dataAccess) {
		ConnectionAttrs attrs = new ConnectionAttrs(connection.getServiceUuid(), connection.getCharacteristicUuid(),
				"dev" + iteration, deviceConnected, "", "", instanceId, DateTimeUtilities.getCurrentTimestamp(), false,
				connection.getPeripheralConnected(), connection.getCentralConnected(), connection.isSubscribed(),
				connection.getLastSuccess(), peripheral.getCommand(), peripheral.getDeviceClass().deviceClassName(),
				connection.getPeripheralIdentifier(), centralDevice.isPushFailure());
		if (checkType != CheckType.PROX) {
			CheckDbObj check = new CheckDbObj(GeneralUtilities.randomString(), GeneralUtilities.randomString(20),
					connection.getCentralDeviceId(), connection.getPeripheralDeviceId(), connection.getServiceUuid(),
					centralDevice.getUserId(), clientIpAddress, ssid, null, null, false, false, Outcomes.INCOMPLETE,
					DateTimeUtilities.getCurrentTimestamp(), null, false, checkType, "", "");
			dataAccess.addLog("added Check");
			dataAccess.addCheck(check);
		} else {
			dataAccess.addLog("check not added because Ble was not available for connection");
		}
		return attrs;
	}

//    public KeyDbObj getDevicePublicKeyForeground(String deviceId) {
//        return new DataAccess().getKeyByTypeAndDeviceId(KeyType.DEVICE_PUBLIC_KEY_FOREGROUND,
//                deviceId);
//    }

//	public ModelMap addReturnServerVars(ModelMap model, int outcome, DeviceDbObj device, String reason) {
//		String instanceId = "";
//		DeviceDataAccess dataAccess = new DeviceDataAccess();
//		dataAccess.addLog("entry");
//		Boolean showIcon = false;
//		Boolean connecting = false;
//		Boolean connected = false;
//		Boolean connectedToAll = false;
//		Boolean subscribed = false;
//		Boolean hasBle = false;
//		ArrayList<ConnectionAttrs> connDevices = new ArrayList<ConnectionAttrs>();
//		try {
//			if (device != null) {
//				String deviceId = device.getDeviceId();
//				showIcon = device.getShowIcon();
//				hasBle = device.getHasBle();
//				ArrayList<DeviceConnectionDbObj> connections = dataAccess.getAllConnectionsForDevice(device, true);
//
//				dataAccess.addLog(device.getDeviceId(), "connected devices found: " + connections.size());
//				if (connections.size() > 0) {
//					outcome = Outcomes.SUCCESS;
//					int iteration = 0;
//					instanceId = GeneralUtilities.randomString(20);
//					for (DeviceConnectionDbObj connection : connections) {
//						if (connection != null) {
//							dataAccess.addLog(deviceId, "connected not null");
//							DeviceDbObj peripheral = dataAccess.getDeviceByDeviceId(connection.getPeripheralDeviceId());
//							connDevices.add(addNewCheckFromCentral(connection, device, peripheral, instanceId, "", "",
//									iteration, CheckType.PROX));
//							iteration++;
//						}
//						if (dataAccess.isAccessAllowed(connection, device)) {
//							connected = true;
//						} else {
//							connectedToAll = false;
//						}
//						if (connection.isSubscribed()) {
//							subscribed = true;
//						}
//					}
//				} else {
//					reason += " & no connected devices";
//				}
//				device.setLastVariableRetrieval(DateTimeUtilities.getCurrentTimestamp());
//				device.setTurnedOff(false);
//				dataAccess.updateDevice(device, "addReturnServerVars");
//			} else {
//				dataAccess.addLog("device was null");
//			}
//		} catch (Exception e) {
//			dataAccess.addLog("addReturnServerVars", e);
//		}
//		return this.addReturnVars(model, outcome, reason, connDevices, instanceId, connected, subscribed, showIcon,
//				connectedToAll, false, hasBle, connecting);
//		/*
//		 * addReturnVars(ModelMap model, int outcome, String reason,
//		 * ArrayList<ConnectionAttrs> devices, String instanceId, boolean connected,
//		 * boolean subscribed, boolean showIcon, boolean fromFcm, boolean connecting)
//		 */
//	}

	protected CompanyResponseHelper showPasswordIncorrect(HttpServletResponse response, ModelMap model) {
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		CompanyResponseHelper companyResponseHelper = new CompanyResponseHelper(response, model, "signinPage");
		model.addAttribute("errorMessage", "Your password or username was not right.");
		companyResponseHelper.setModel(model);
		new DataAccess().addLog("incorrect is true");
		return companyResponseHelper;
	}

	private AdminSignin isGroupAllowed(GroupDbObj group, HttpServletResponse response) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		boolean correctUserOrPw = false;
		boolean admin = false;
		boolean foundLocally = false;
		boolean allowed = false;
		int logLevel = LogConstants.TEMPORARILY_IMPORTANT;
		String reason = "";
		CompanyDbObj company = null;
		company = dataAccess.getCompanyByGroupId(group.getGroupId());
		if (company != null && company.isActive()) {
			if (isAdmin(group)) {
				admin = true;
				dataAccess.addLog("group is admin", logLevel);
				ArrayList<DeviceDbObj> devices = new DeviceDataAccess().getActiveDevicesByGroupId(group.getGroupId());
				if (hasDevices(devices)) {
					dataAccess.addLog("devices found");
					// TODO: this is flawed logic
					if (isAnyDeviceProximate(devices)) {
						dataAccess.addLog("group is proximate", logLevel);
						allowed = true;
						foundLocally = true;

					} else {
						dataAccess.addLog("devices not signed up yet", LogConstants.WARNING);
						reason = Constants.DEVICE_NOT_LOCAL;
					}
				} else {
					dataAccess.addLog("devices not signed up yet", LogConstants.WARNING);
					allowed = true;
				}
			} else {
				dataAccess.addLog("group is not admin", LogConstants.WARNING);
				reason = Constants.NOT_PERMITTED;
			}
			if (!allowed) {
				response = deleteAdminToken(response);
				dataAccess.addLog("deleted admin token", LogConstants.WARNING);
			}
		} else {
			dataAccess.addLog("company not found", LogConstants.WARNING);
			reason = Constants.COMPANY_NOT_FOUND;
		}
		return new AdminSignin(company, group, correctUserOrPw, foundLocally, admin, reason, allowed, response);
	}

	private boolean hasDevices(ArrayList<DeviceDbObj> devices) {
		return devices.size() > 1;
	}

	private boolean isAnyDeviceProximate(ArrayList<DeviceDbObj> devices) {
		boolean proximate = false;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		for (DeviceDbObj device : devices) {
			if (dataAccess.isAccessAllowed(device, "isAnyDeviceProximate")) {
				proximate = true;
				break;
			}
		}
		return proximate;
	}

	protected ModelMap getPageAttributes(ModelMap model, GroupDbObj group) {
		// we know group is not null
		CompanyDataAccess dataAccess = new CompanyDataAccess();

		CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
		if (company != null) {
			String loginUrl = company.getCompleteCompanyLoginUrl();
			model.addAttribute("loginUrl", loginUrl);
			model = getLicenseAttributes(dataAccess, model, group, company);
			model.addAttribute("userRole", group.getUserType().toString());
			dataAccess.addLog("userType: " + group.getUserType().toString());
			model = getUsersAttributes(dataAccess, model, company);
			model = getServerAttributes(dataAccess, model, company);
			dataAccess.addLog(group.getGroupId(), "exiting successfully");
		}
		return model;
	}

	public ModelMap getServerPageAttributes(ModelMap model, GroupDbObj group) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();

		CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
		if (company != null) {
			String loginUrl = "https://" + company.getCompleteCompanyLoginUrl();
			model.addAttribute("loginUrl", loginUrl);
			model = getLicenseAttributes(dataAccess, model, group, company);
			model.addAttribute("userRole", group.getUserType().toString());
			dataAccess.addLog("userType: " + group.getUserType().toString());
			model = getServerAttributes(dataAccess, model, company);
			dataAccess.addLog(group.getGroupId(), "exiting successfully");
		}
		return model;
	}

	protected ModelMap getLicenseAttributes(CompanyDataAccess dataAccess, ModelMap model, GroupDbObj group,
			String companyId) {
		CompanyDbObj company = dataAccess.getCompanyById(companyId);
		return this.getLicenseAttributes(dataAccess, model, group, company);
	}

	protected ModelMap getLicenseAttributes(CompanyDataAccess dataAccess, ModelMap model, GroupDbObj group,
			CompanyDbObj company) {
		model.addAttribute("groupId", group.getGroupId());
		model.addAttribute("companyId", company.getCompanyId());
		model.addAttribute("licenseCount", company.getLicenseCount());
		model.addAttribute("licensesInUse", dataAccess.getLicensesInUse(company.getCompanyId()));
		Date createDate = company.getCreateDate();
		LocalDate localDate = createDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		model.addAttribute("createDay", Integer.toString(localDate.getDayOfMonth()));
		model.addAttribute("createMonth", Integer.toString(localDate.getMonthValue()));
		model.addAttribute("createYear", Integer.toString(localDate.getYear()));
		model.addAttribute("activeStatus", "Active");
		model.addAttribute("apiKey1", company.getApiKey().substring(0, 4));
		model.addAttribute("apiKey2", company.getApiKey().substring(4, 9));
		model.addAttribute("apiKey3", company.getApiKey().substring(9, 11));
		model.addAttribute("companyId", company.getCompanyId());
		model.addAttribute("allowAllFromIdp", company.isAllowAllFromIdp());
		model.addAttribute("moveB2fUsersToIdp", company.isMoveB2fUsersToIdp());

		model = addMethodToModel(model, company, "f1Method", company.getF1Method());
		model = addMethodToModel(model, company, "f2Method", company.getF2Method());
		Date billingDate = DateTimeUtilities.nowPlusSeconds(60 * 60 * 24 * 30);
		model.addAttribute("billDate", new DateTimeUtilities().dateToString(billingDate));
		model.addAttribute("billingAmount", "$" + Integer.toString((company.getLicenseCount() * 2)) + ".00");

		model.addAttribute("companyName", company.getCompanyName());
		model.addAttribute("basePage", company.getCompanyBaseUrl());
		String completionUrl = company.getCompanyBaseUrl();
		model.addAttribute("baseUrl", completionUrl);
		String shortUrl = completionUrl;
		if (completionUrl != null && completionUrl.length() > 34) {
			shortUrl = completionUrl.substring(0, 31) + "...";
		} else {
			shortUrl = "";
		}
		model.addAttribute("shortUrl", shortUrl);
		String loginUrl = "https://" + company.getCompleteCompanyLoginUrl();
		model.addAttribute("loginUrl", loginUrl);
		String shortLoginUrl = loginUrl;
		if (loginUrl != null && loginUrl.length() > 34) {
			shortLoginUrl = loginUrl.substring(0, 31) + "...";
		}
		model.addAttribute("shortLoginUrl", shortLoginUrl);
		List<DeviceDbObj> devices = new DeviceDataAccess().getActiveDevicesFromGroup(group);
		model.addAttribute("noDevicesRegistered", devices.size() == 0);
		model = addSamlData(model, company);
		model = addWebServerKeyData(model, company);
		return model;
	}

	private ModelMap addSamlData(ModelMap model, CompanyDbObj company) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompany(company);
		if (samlIdp != null) {
			model.addAttribute("samlDataUploaded", true);
		} else {
			model.addAttribute("samlDataUploaded", false);
		}
		return model;
	}

	private ModelMap addWebServerKeyData(ModelMap model, CompanyDbObj company) {
		DataAccess dataAccess = new DataAccess();
		KeyDbObj key = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.WEB_SERVER_PUBLIC_KEY, company.getCompanyId());
		if (key != null) {
			model.addAttribute("webServerKeyUploaded", true);
		} else {
			model.addAttribute("webServerKeyUploaded", false);
		}
		return model;
	}

	private ModelMap addMethodToModel(ModelMap model, CompanyDbObj company, String fieldName,
			AuthorizationMethod method) {
		if (method.equals(AuthorizationMethod.API)) {
			model.addAttribute(fieldName + "_apiSelected", "selected");
		} else if (method.equals(AuthorizationMethod.I_FRAME)) {
			model.addAttribute(fieldName + "_iFrameSelected", "selected");
		} else if (method.equals(AuthorizationMethod.OPEN_ID_CONNECT)) {
			model.addAttribute(fieldName + "_openIdSelected", "selected");
		} else if (method.equals(AuthorizationMethod.SAML)) {
			model.addAttribute(fieldName + "_samlSelected", "selected");
		} else {
			model.addAttribute(fieldName + "_noneSelected", "selected");
		}
		model.addAttribute(fieldName, method.authMethodName());
		return model;
	}

	protected ModelMap getServerAttributes(DataAccess dataAccess, ModelMap model, CompanyDbObj company) {
		ArrayList<ServerDbObj> servers = dataAccess.getActiveServersByCompanyId(company.getCompanyId(), true);
		model.addAttribute("servers", servers);
		model.addAttribute("serverCount", servers.size());
		return model;
	}

	private ModelMap getUsersAttributes(CompanyDataAccess dataAccess, ModelMap model, CompanyDbObj company) {
		ArrayList<GroupDbObj> groups = dataAccess.getGroupsByCompanyWithoutAnonymous(company);
		ArrayList<User> users = new ArrayList<User>();
		User user;
		for (GroupDbObj group : groups) {
			int devicesInUse = dataAccess.getDeviceCountForGroup(group.getGroupId(), true);
			int totalDevices = dataAccess.getDeviceCountForGroup(group.getGroupId(), false);
			String uid = "";
			String nnUid = group.getUid();
			if (nnUid != null) {
				uid = nnUid;
			}
			boolean setup = dataAccess.doesConnectionExistByEmail(group.getGroupName());
			user = new User(group.getUsername(), group.getGroupName(), uid, totalDevices, devicesInUse,
					group.getGroupId(), setup, group.getUserType().toString());
			users.add(user);
		}
		dataAccess.addLog("getUsersAttributes", users.size() + " groups were found.", LogConstants.DEBUG);
		model.addAttribute("users", users);
		return model;
	}

	private String getMainGroupCookie(HttpServletRequest request) {
		String gid = getCookie(request, "gid");
		new DataAccess().addLog("gid cookie found: " + gid);
		return gid;
	}

	protected String getCookie(HttpServletRequest request, String cookieName) {
		String value = "";
		DataAccess dataAccess = new DataAccess();
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					value = cookie.getValue();
					dataAccess.addLog(cookieName + " = " + value);
					this.setSession(request, "F", value);
					break;
				}

			}
		} else {
			dataAccess.addLog("no cookies were found - we we looking for " + cookieName);
		}
		return value;
	}

	protected HttpServletRequest setSession(HttpServletRequest request, String name, Object value) {
		HttpSession session = request.getSession();
		session.setMaxInactiveInterval(-1);// never expire
		session.setAttribute(name, value);
		return request;
	}

	protected HttpServletResponse setMainGroupCookie(HttpServletResponse httpResponse, String token) {
		return setCookie(httpResponse, token, "gid", 3600, true);
	}

	protected HttpServletResponse setCookie(HttpServletResponse httpResponse, String value, String cookieName,
			int seconds, boolean httpOnly) {
		return this.setCookie(httpResponse, value, cookieName, seconds, httpOnly, "Lax");
	}

	protected HttpServletResponse setCookieOld(HttpServletResponse httpResponse, String value, String cookieName,
			int seconds, boolean httpOnly, String sameSite) {
		Cookie cookie = new Cookie(cookieName, value);
		cookie.setMaxAge(seconds);
//        cookie.setHttpOnly(httpOnly);
		cookie.setSecure(true);
		String path = "/";
		if (sameSite.toLowerCase().equals("none")) {
			new DataAccess().addLog("setting sameSite to None");
		} else if (sameSite.toLowerCase().equals("strict")) {
			new DataAccess().addLog("setting sameSite to Strict");
		} else {
			new DataAccess().addLog("setting sameSite to Lax");
		}
		cookie.setPath(path);

		httpResponse.addCookie(cookie);
		return httpResponse;
	}

	protected HttpServletResponse setCookie(HttpServletResponse httpResponse, String value, String cookieName,
			int seconds, boolean httpOnly, String sameSite) {
		new DataAccess().addLog("setting sameSite for: " + cookieName + " to: " + sameSite + 
				", httpOnly: " + httpOnly + ", to value: " + value, LogConstants.TEMPORARILY_IMPORTANT);
		final ResponseCookie responseCookie = ResponseCookie.from(cookieName, value).secure(true).httpOnly(httpOnly)
				.path("/").maxAge(seconds).sameSite(sameSite).domain(".nearAuth.ai").build();
		httpResponse.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
		return httpResponse;
	}

	protected RequestAndResponse signout(HttpServletResponse httpResponse, HttpServletRequest request) {
		request = setSession(request, "B2F_AUTH", "");
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("called");
		httpResponse = setCookie(httpResponse, "", "B2F_AUTH", 3600, false, dataAccess, "None");
		httpResponse = setCookie(httpResponse, "", "gid", 3600, false, dataAccess);
		return new RequestAndResponse(request, httpResponse);
	}

	protected HttpServletResponse setCookie(HttpServletResponse httpResponse, String value, String cookieName,
			int seconds, boolean httpOnly, DataAccess dataAccess) {
		dataAccess.addLog("setting " + cookieName + " = " + value);
		return setCookie(httpResponse, value, cookieName, seconds, httpOnly);
	}

	protected HttpServletResponse setCookie(HttpServletResponse httpResponse, String value, String cookieName,
			int seconds, boolean httpOnly, DataAccess dataAccess, String sameSite) {
		dataAccess.addLog("setting " + cookieName + " = " + value);
		return setCookie(httpResponse, value, cookieName, seconds, httpOnly, sameSite);
	}

	protected void updateConnectionAndCheck(DeviceConnectionDbObj connection, CheckDbObj check, String centralBssid,
			Boolean fromCentral, ConnectionType connType) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			dataAccess.expirePushAndBioChecksForConnection(connection);
			dataAccess.updateAsProximatelyConnected(connection, fromCentral);
			updateCheckAsSuccessful(check, centralBssid, connType);
			dataAccess.addLog(connection.getCentralDeviceId(), "check updated");
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
	}

	protected CheckDbObj updateCheckAsSuccessful(CheckDbObj check, String centralBssid, ConnectionType connType) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			check.setCentralBssid(centralBssid);
			check.setCompleted(true);
			check.setCompletionDate(DateTimeUtilities.getCurrentTimestamp());
			check.setOutcome(Outcomes.SUCCESS);
			check.setVerfiedReceipt(true);
			dataAccess.updateCheck(check);
			dataAccess.setPushFailedFromCheck(check, false);
			dataAccess.addLog("check updated as successful");
			dataAccess.addConnectionLogIfNeeded(check, true, check.getCentralDeviceId(), "updateCheckAsSuccessful",
					connType);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return check;
	}

	protected ModelMap addSetupToModel(ModelMap model) {
		model.addAttribute("message1", "This website you visited requires " + Constants.APP_NAME
				+ ". This web browser is either not set up, is out of sync, or had it's authorization " + "rescinded.");
		model.addAttribute("message2",
				"If you have set up " + Constants.APP_NAME + " on this device, open the app and select Add Browser.");
		model.addAttribute("message3",
				"If you have not set up this device yet, please go to "
						+ "<a href='https://www.blue2factor.com/downloads'>www.blue2factor.com/downloads</a> or your "
						+ "device's app store.");
		return model;
	}

	public IdentityObjectFromServer setIdentityObject(String accessCodeString) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		CompanyDbObj company = null;
		DeviceDbObj device = null;
		BrowserDbObj browser = null;
		AccessCodeDbObj accessCode = dataAccess.getAccessCodeFromAccessString(accessCodeString);
		if (accessCode != null) {
			device = dataAccess.getDeviceByDeviceId(accessCode.getDeviceId());
			browser = dataAccess.getBrowserByAccessCode(accessCode);
			company = dataAccess.getCompanyById(accessCode.getCompanyId());
			if (device != null) {
				dataAccess.addLog("setIdentityObject", "looks good");
			} else {
				dataAccess.addLog("setIdentityObject", "device was null");
			}
		}
		return new IdentityObjectFromServer(company, device, browser, accessCode);
	}

	public IdentityObjectFromServer setIdentityObject(ApiRequestWithJsonKey apiReq)
			throws UnsupportedEncodingException {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		CompanyDbObj company = null;
		DeviceDbObj device = null;
		BrowserDbObj browser = null;
		boolean fromJs = false;
		try {
			String browserSession = apiReq.getBrowserSession();
			fromJs = apiReq.isFromJs();
			if (!TextUtils.isEmpty(apiReq.getCoKey())) {
				dataAccess.addLog("fromJs: " + fromJs);
				String coKey = apiReq.getCoKey().replace(" ", "").replace("-", "").toUpperCase();
				company = dataAccess.getCompanyByApiKey(coKey);
				boolean coFound = company != null;
				dataAccess.addLog("company found for apiKey: " + coKey + "? " + coFound);
			}
			if (!TextUtils.isBlank(browserSession)) {
				dataAccess.addLog("browserSession: " + browserSession, LogConstants.WARNING);
				browserSession = URLDecoder.decode(browserSession, StandardCharsets.UTF_8.toString());
				browser = dataAccess.getBrowserByToken(browserSession, TokenDescription.BROWSER_SESSION);
				if (browser != null) {
					device = dataAccess.getDeviceByBrowserId(browser.getBrowserId());
					if (company == null && device != null) {
						company = dataAccess.getCompanyByDevId(device.getDeviceId());
					}
				} else {
					dataAccess.addLog("browserSession was null", LogConstants.WARNING);
				}
			} else {
				dataAccess.addLog("browserToken was blank");
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return new IdentityObjectFromServer(company, device, browser, // f1TokenObj, f2TokenObj,
				fromJs);
	}

	protected AccessCodeDbObj createAccessCodeWithIdObj(IdentityObjectFromServer idObj)
			throws UnsupportedEncodingException {
		return createAccessCode(idObj.getCompany(), idObj.getDevice(), idObj.getBrowser());
	}

	protected AccessCodeDbObj createAccessCode(CompanyDbObj company, DeviceDbObj device, BrowserDbObj browser)
			throws UnsupportedEncodingException {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("start");
		if (browser == null || browser.isExpired()) {
			browser = dataAccess.addBrowser(device, "setup");
			dataAccess.addLog("browser added");
		}
		AccessCodeDbObj accessCode = new AccessCodeDbObj(GeneralUtilities.randomString(), company.getCompanyId(),
				browser.getBrowserId(), device.getDeviceId(), 0, true, false);
		dataAccess.addAccessCode(accessCode, "updateAccessCode");
		dataAccess.expireAccessCodesBesides(accessCode.getAccessCode(), browser.getBrowserId());
		return accessCode;
	}

	protected HttpServletResponse updateAccessCodeWithoutExpiringRecent(HttpServletResponse httpResponse,
			CompanyDbObj company, DeviceDbObj device, BrowserDbObj browser) throws UnsupportedEncodingException {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("start");
		if (browser == null || browser.isExpired()) {
			browser = dataAccess.addBrowser(device, "setup");
			dataAccess.addLog("browser added");
		}
		AccessCodeDbObj accessCode = new AccessCodeDbObj(GeneralUtilities.randomString(), company.getCompanyId(),
				browser.getBrowserId(), device.getDeviceId(), 0, true, false);
		dataAccess.addAccessCode(accessCode, "updateAccessCode");
		dataAccess.addLog("b2faccessCode - " + accessCode.getAccessCode());
		return httpResponse;
	}

	protected BasicResponse addAuthorization(DeviceDbObj device, boolean fromCentral) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		DeviceDbObj centralDevice = null;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		dataAccess.addLog(device.getDeviceId(), "enter auth time: " + now + " - central: " + fromCentral);
		if (fromCentral) {
			centralDevice = device;
		} else {
			centralDevice = dataAccess.getConnectedCentral(device);
		}
		if (centralDevice != null) {
			AuthorizationDbObj auth = new AuthorizationDbObj(device.getUserId(), now, false, device.getDeviceId(),
					centralDevice.getDeviceId());
			dataAccess.addAuthorization(auth);
			CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
			if (company != null) {
				token = company.getApiKey();
				outcome = Outcomes.SUCCESS;
			}
		} else {
			reason = "central not found";
		}
		return new BasicResponse(outcome, reason, token, "");
	}

	protected BasicResponse checkForAuthorization(HttpServletRequest request, String authId, boolean fromCentral) {
		int outcome = Outcomes.FAILURE;
		String tokenStr = "";
		String reason = "";
		String instance = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();

		dataAccess.addLog(authId, "checking for authorization");
		DeviceDbObj authDevice = dataAccess.getDeviceByDeviceId(authId);
		if (authDevice != null) {
			if (fromCentral) {
				dataAccess.addLog(authId, "from Central");
				if (dataAccess.isCentralDeviceAuthorized(authDevice)) {
					dataAccess.setLastReset(authDevice, DateTimeUtilities.getCurrentTimestamp());
					outcome = Outcomes.SUCCESS;
					dataAccess.rescindAuth(authDevice);
				} else {
					reason = "central not authorized";
				}
			} else {
				dataAccess.addLog(authId, "from Peripheral");
				if (dataAccess.isPeripheralDeviceAuthorized(authDevice)) {
					CompanyDbObj company = dataAccess.getCompanyByDevId(authDevice.getDeviceId());
					if (company != null && company.isActive()) {
						AccessCodeDbObj access = new AccessCodeDbObj(DateTimeUtilities.getCurrentTimestamp(),
								GeneralUtilities.randomString(), company.getCompanyId(), "", authDevice.getDeviceId(),
								0, true, false);
						dataAccess.addAccessCode(access, "checkForAuthorization");
						dataAccess.rescindAuth(authDevice);
						reason = company.getCompleteCompanyLoginUrl();
						// tokenStr = access.getAccessCode();
						tokenStr = company.getApiKey();
						dataAccess.setLastReset(authDevice, DateTimeUtilities.getCurrentTimestamp());
						instance = access.getAccessCode();
						outcome = Outcomes.SUCCESS;
					} else {
						reason = "company was null or deactivated";
					}
				} else {
					reason = "peripheral not authorized";
				}
			}
		} else {
			reason = "authDevice was null for id: " + authId;
			dataAccess.addLog(authId, reason, LogConstants.WARNING);
		}
		return new BasicResponse(outcome, reason, tokenStr, instance);
	}

	protected void redirectRequestToUrl(HttpServletResponse httpResponse, String url) throws Exception {
		httpResponse.setHeader("Location", url);
		httpResponse.setStatus(302);
	}

	protected void handleUnsuccessfulConnections(HttpServletRequest request, DeviceConnectionDbObj connection,
			boolean connected, String key, String iv) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String unsuccessString = this.getEncryptedRequestValue(request, "naheunabetbsjaeo", key, iv);
			DeviceDbObj peripheral = dataAccess.getDeviceByDeviceId(connection.getPeripheralDeviceId());
			if (!unsuccessString.equals("") && connected) {
				int unsuccessCount = Integer.valueOf(unsuccessString);
				dataAccess.addLog(connection.getCentralDeviceId(), "unsuccessCount: " + unsuccessCount,
						LogConstants.DEBUG);
				if (unsuccessCount % 5 == 4) {
					if (peripheral != null) {
						dataAccess.setUnresponsive(peripheral, true);
					}
				}
			} else {
				if (peripheral != null) {
					dataAccess.setUnresponsive(peripheral, false);
				}
			}
		} catch (Exception notFound) {
			// only implemented on central
		}
	}
}

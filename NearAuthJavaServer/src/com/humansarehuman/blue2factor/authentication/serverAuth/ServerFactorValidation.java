package com.humansarehuman.blue2factor.authentication.serverAuth;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.jsonConversion.ApiRequestWithJsonKey;
import com.humansarehuman.blue2factor.entities.jsonConversion.RsaOaepJsonWebKey;
import com.humansarehuman.blue2factor.entities.jsonConversion.RsaOaepJsonWebPrivateKey;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.saml.Saml;

/**
 * Not sure if we should delete this
 * 
 * @author cjm10
 *
 */

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = Urls.SERVER_FACTOR_VALIDATION)
@SuppressWarnings("ucd")
public class ServerFactorValidation extends B2fApi {
	@RequestMapping(method = RequestMethod.OPTIONS)
	public void serverFactorValidationProcessOptions(HttpServletRequest request, HttpServletResponse httpResponse)
			throws IOException {
		GeneralUtilities generalUtilities = new GeneralUtilities();
		String origin = request.getHeader("origin");
		new DataAccess().addLog("OPTION from " + origin);
		httpResponse = generalUtilities.setResponseHeader(httpResponse, origin);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken serverFactorValidatioGetJson(HttpServletRequest request,
			HttpServletResponse httpResponse) throws IOException {
		ApiResponseWithToken response = new ApiResponseWithToken(Outcomes.API_F1_FAILURE, "Method not allowed", "");
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken serverFactorValidationPostJson(@RequestBody ApiRequestWithJsonKey apiReq,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) throws IOException {
		DataAccess dataAccess = new DataAccess();
		int logLevel = LogConstants.TRACE;
		dataAccess.addLog("ServerFactorValidation", "entry");
		IdentityObjectFromServer idObj = setIdentityObject(apiReq);
		String browserSession = apiReq.getBrowserSession();
		String reqUrl = apiReq.getReqUrl();
		String origin = request.getHeader("origin");
		RsaOaepJsonWebKey jwk = apiReq.getJwk();
		RsaOaepJsonWebPrivateKey jwpk = apiReq.getJwpk();

		String cmd = apiReq.getCmd();
		dataAccess.addLog("reqUrl: " + reqUrl + "; origin: " + origin, logLevel);
		dataAccess.addLog("cmd: " + cmd, logLevel);
		dataAccess.addLog("session: " + browserSession, logLevel);

		GeneralUtilities util = new GeneralUtilities();
		ApiResponseWithToken response = handleValidationRequest(idObj, browserSession, cmd, jwk, jwpk,
				GeneralUtilities.getUrlHost(reqUrl));
		if (response != null) {
			dataAccess.addLog("outcome: " + response.getOutcome() + ", reason: " + response.getReason(), logLevel);
		} else {
			dataAccess.addLog("response was null", LogConstants.WARNING);
		}
		httpResponse = util.setResponseHeader(httpResponse, reqUrl);
		return response;
	}

	private ApiResponseWithToken handleValidationRequest(IdentityObjectFromServer idObj, String browserSession,
			String cmd, RsaOaepJsonWebKey jwk, RsaOaepJsonWebPrivateKey jwpk, String url) {
		ApiResponseWithToken response = null;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("handleValidationRequest", "cmd: '" + cmd + "'");
		CompanyDbObj company = idObj.getCompany();
		switch (cmd) {
//            case Constants.SERVER_BOTH_FACTOR_VALIDATE: // box 3
//                dataAccess.addLog("handleValidationRequest", "validating both factors");
//                if (company != null) {
//                    response = this.validateServerUse(idObj, browserSession, url);
//                } else {
//                    response = new ApiResponseWithToken(Outcomes.FAILURE,
//                            Constants.COMPANY_NOT_FOUND, "");
//                }
//                break;
		case Constants.GET_SAML_DATA:
			// step between box 2 and box 8
			if (company != null && company.isActive()) {
				dataAccess.addLog("handleValidationRequest", "company Found");
				response = getCompanySamlData(company);
			} else {
				dataAccess.addLog("handleValidationRequest", "company null or inactive");
			}
			break;
		case Constants.JS_FACTOR1_VALIDATE: // box 6
			response = handleJsValidationRequest(idObj);
			break;
		case Constants.IDENTITY_PROVIDER_SUCCESS: // box 12
			// response = validateDataAndCreateToken();
			break;
		case Constants.CREATE_BROWSER_KEY: // box 16
			try {
				response = createBrowserKey(company, browserSession, jwk, url);
			} catch (Exception e) {
				dataAccess.addLog("handleValidationRequest", e);
			}
			break;
		case Constants.SAVE_SERVER_KEY: // box 16
			try {
				response = createServerKey(browserSession, jwpk, url);
			} catch (Exception e) {
				dataAccess.addLog("handleValidationRequest", e);
			}
			break;
		}
		return response;
	}

	private ApiResponseWithToken getCompanySamlData(CompanyDbObj company) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String tokenId = "";
		Saml saml = new Saml();
		DataAccess dataAccess = new DataAccess();
		try {
			String samlUrl = saml.getSamlAuthnRedirectUrl(company);
			dataAccess.addLog("getCompanySamlData", "samlData: " + samlUrl);
			if (!TextUtils.isEmpty(samlUrl)) {
				outcome = Outcomes.SUCCESS;
				reason = samlUrl;
			}
		} catch (Exception e) {
			dataAccess.addLog("getCompanySamlData", e);
		}
		return new ApiResponseWithToken(outcome, reason, tokenId);
	}

	private ApiResponseWithToken createServerKey(String tokenId, RsaOaepJsonWebPrivateKey jwpk, String url)
			throws Exception {
		int outcome = Outcomes.FAILURE;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String reason = "";
		BrowserDbObj browser = dataAccess.getBrowserByToken(tokenId, TokenDescription.BROWSER_SESSION);
		Encryption encryption = new Encryption();
		if (browser != null && !browser.isExpired()) {
			dataAccess.addLog("createServerKey", "browser is not null");
			KeyDbObj oldKey = dataAccess.getKeyByTypeAndBrowserTokenAndSite(KeyType.B2F_SERVER_PRIVATE_KEY_FOR_BROWSER,
					tokenId, url);
			if (oldKey != null) {
				dataAccess.deactivateKey(oldKey);
				dataAccess.addLog("createServerKey", "oldKey: " + oldKey.getKeyId());
			}
			PrivateKey privateKey = encryption.privateKeyFromJwk(jwpk);
			byte[] privateKeyBytes = privateKey.getEncoded();
			String privateKeyStr = Base64.getEncoder().encodeToString(privateKeyBytes);
			CompanyDbObj company = dataAccess.getCompanyByToken(tokenId);
			KeyDbObj key = new KeyDbObj(browser.getDeviceId(), browser.getBrowserId(), null, company.getCompanyId(),
					KeyType.B2F_SERVER_PRIVATE_KEY_FOR_BROWSER, privateKeyStr, true, jwpk.getAlg(), url);
			dataAccess.addKey(key);
			dataAccess.deactivateKeyTypeForDeviceAndUrlExcept(KeyType.B2F_SERVER_PRIVATE_KEY_FOR_BROWSER,
					browser.getDeviceId(), url, key.getKeyId());
			outcome = Outcomes.SUCCESS;
		} else {
			if (browser == null) {
				dataAccess.addLog("createServerKey", "browser is null");
			} else {
				dataAccess.addLog("createServerKey", "browser is expired");
			}
			reason = Constants.BROWSER_NOT_FOUND;
		}
		return new ApiResponseWithToken(outcome, reason, tokenId);
	}

	private ApiResponseWithToken createBrowserKey(CompanyDbObj company, String tokenId, RsaOaepJsonWebKey jwk,
			String url) throws Exception {
		int outcome = Outcomes.FAILURE;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String reason = "";
		String serverPublicKey = "";
		BrowserDbObj browser = dataAccess.getBrowserByToken(tokenId, TokenDescription.BROWSER_SESSION);
		Encryption encryption = new Encryption();
		if (browser != null) { // TODO: browser could be expired here && !browser.isExpired()) {
			company = dataAccess.getCompanyByDevId(browser.getDeviceId());
			if (company != null && company.isActive()) {
				DeviceDbObj device = dataAccess.getDeviceByDeviceId(browser.getDeviceId());
				String keyId = GeneralUtilities.randomString();
				dataAccess.addLog(browser.getDeviceId(), "creating browserKey for " + url);
				dataAccess.deactivateKeysByBrowserAndUrl(browser.getBrowserId(), url, keyId);
				PublicKey publicKey = encryption.publicKeyFromJwk(jwk);
				byte[] publicKeyBytes = publicKey.getEncoded();
				String publicKeyStr = Base64.getEncoder().encodeToString(publicKeyBytes);
				KeyDbObj key = new KeyDbObj(keyId, browser.getDeviceId(), browser.getBrowserId(), null,
						company.getCompanyId(), KeyType.BROWSER_PUBLIC_KEY, publicKeyStr, true, jwk.getAlg(), url);
				dataAccess.addKey(key);
				serverPublicKey = encryption.createAndSaveKeyForJavascript(device, browser, url);
				if (serverPublicKey != null) {
					outcome = Outcomes.SUCCESS;
				} else {
					reason = "jwk from deprovisioned device";
				}
			} else {
				reason = Constants.COMPANY_NOT_FOUND;
			}
		} else {
			reason = Constants.BROWSER_NOT_FOUND;
		}
		if (!TextUtils.isBlank(reason)) {
			dataAccess.addLog("createBrowserKey", reason);
		}
		return new ApiResponseWithToken(outcome, reason, serverPublicKey);
	}

	private ApiResponseWithToken handleJsValidationRequest(IdentityObjectFromServer idObj) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String tokenId = "";
		// TODO: Do we use this? if so, fix it. cjm
//        TokenDbObj f1Token = idObj.getF1Token();
//        if (f1Token != null) {
//            tokenId = f1Token.getTokenId();
//        } else {
//            reason = Constants.DEV_NOT_FOUND;
//        }
		return new ApiResponseWithToken(outcome, reason, tokenId);
	}

}

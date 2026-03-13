package com.humansarehuman.blue2factor.authentication.fingerprint;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
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
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.AuthResponse;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.FingerprintAuth;
import com.humansarehuman.blue2factor.entities.tables.AuthenticatorDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.PASSKEY_AUTHENTICATION_COMPLETION)
@SuppressWarnings("ucd")
public class FingerprintAuthenticationCompletion extends B2fApi {

	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody ApiResponse getJson(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		new DataAccess().addLog("FingerprintRegistrationCompletion", "Get");
		return new ApiResponse(Outcomes.FAILURE, Constants.METHOD_NOT_ALLOWED + ": GET");
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse postJson(@RequestBody FingerprintAuth auth, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		ApiResponse apiResponse;
		int logLevel = LogConstants.TEMPORARILY_IMPORTANT;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("auth: " + auth.toString(), logLevel);
		String browserSession = auth.getBrowserSession();

		dataAccess.addLog("b2fSession: " + browserSession, logLevel);
		String reqUrl = auth.getReqUrl();
		dataAccess.addLog("FingerprintAuthenticationCompletion", "reqUrl: " + reqUrl + "; ", logLevel);
		CompanyDbObj company = dataAccess.getCompanyByToken(browserSession);
		if (this.doesUrlMatchRegex(company, reqUrl, dataAccess) != "") {
			response = new GeneralUtilities().setResponseHeader(response, reqUrl);
			apiResponse = handleFingerprintCompletion(request, auth);
		} else {
			apiResponse = new ApiResponse(Outcomes.FAILURE, Constants.COMPANY_URL_MISMATCH);
		}
		return apiResponse;
	}

	private ApiResponse handleFingerprintCompletion(HttpServletRequest request, FingerprintAuth auth) {
		DataAccess dataAccess = new DataAccess();
		// Client properties
		int outcome = Outcomes.FAILURE;
		int logLevel = LogConstants.TEMPORARILY_IMPORTANT;
		String reason = "";
		AuthResponse authResp = auth.getResponse();
		String browserSession = auth.getBrowserSession();
		String credIdStr = auth.getId();
		byte[] credentialId = credIdStr.getBytes();
		byte[] authenticatorData = Base64.getUrlDecoder().decode(authResp.getAuthenticatorData());
		byte[] clientDataJSONBytes= Base64.getUrlDecoder().decode(authResp.getClientDataJSON());
		ObjectConverter objConverter = new ObjectConverter();
		CollectedClientDataConverter collectedClientDataConverter = new CollectedClientDataConverter(objConverter);
        
		CollectedClientData collectedClientData = collectedClientDataConverter.convert(clientDataJSONBytes);
		
		byte[] signature = Base64.getUrlDecoder().decode(authResp.getSignature());

		String url = auth.getReqUrl();
		Origin origin = new Origin(GeneralUtilities.getUrlProtocolAndHost(url));
		String rpId = GeneralUtilities.getNakedDomain(url);
		AuthenticatorDbObj authDbObj = dataAccess.getActiveAuthenticatorByCredentialId(credIdStr);
		if (authDbObj != null) {
			dataAccess.addLog("authDbObj found for: " + credIdStr, logLevel);
			dataAccess.addLog("origin: " + origin, logLevel);
			dataAccess.addLog("rpId: " + rpId, logLevel);
			dataAccess.addLog("challenge: " + authDbObj.getChallenge(), logLevel);
			Challenge challenge = new DefaultChallenge(authDbObj.getChallenge());
//			byte[] tokenBindingId = new byte[] { 0x01, 0x23, 0x45 };
//			ServerProperty serverPropertyOld = new ServerProperty(origin, rpId, challenge, tokenBindingId);
			ServerProperty serverProperty = ServerProperty.builder().origin(origin).rpId(rpId).challenge(challenge).build();
			List<byte[]> allowCredentials = null;
			boolean userVerificationRequired = true;
			boolean userPresenceRequired = true;

//			Authenticator authenticator = new AuthenticatorImpl(authDbObj.getAttestedCredentialData(),
//					authDbObj.getAttestationObject().getAttestationStatement(), authDbObj.getSignCount());
			CredentialRecord credentialRecord = new CredentialRecordImpl(authDbObj.getAttestationObject(), collectedClientData, null, null);
			dataAccess.addLog("authenticator created", logLevel);
			AuthenticationRequest authenticationRequest = new AuthenticationRequest(credentialId, authenticatorData,
					clientDataJSONBytes, signature);
			AuthenticationParameters authenticationParameters = new AuthenticationParameters(serverProperty,
					credentialRecord, allowCredentials, userVerificationRequired, userPresenceRequired);
			AuthenticationData authenticationData;
			WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
			try {
				authenticationData = webAuthnManager.parse(authenticationRequest);
				try {
					dataAccess.addLog("authenticationData parsed: " + authenticationData, logLevel);
					dataAccess.addLog("authenticationParameters: " + authenticationParameters, logLevel);
					webAuthnManager.verify(authenticationData, authenticationParameters);
//					webAuthnManager.validate(authenticationData, authenticationParameters);
					dataAccess.addLog("webAuthnManager validated", logLevel);
					String session = dataAccess.addCompletedCheckForFingerprint(this, browserSession, request);
					if (session != null) {
						reason = session;
						outcome = Outcomes.SUCCESS;
					}
				} catch (Exception e) {
					dataAccess.addLog("general exception", e);
					reason = Constants.SIGNATURE_VALIDATION_FAILED;
				}
				authDbObj.setSignCount(authenticationData.getAuthenticatorData().getSignCount());
				dataAccess.updateAuthenticatorByBrowserAndUrl(authDbObj);
			} catch (DataConversionException e) {
				dataAccess.addLog("data Error", e);
			}

		}
		return new ApiResponse(outcome, reason);
	}
}

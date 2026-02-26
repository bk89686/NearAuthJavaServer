package com.humansarehuman.blue2factor.authentication.fingerprint;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.CreateFingerprint;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.CreateResponse;
import com.humansarehuman.blue2factor.entities.tables.AuthenticatorDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.validator.exception.BadChallengeException;
import com.webauthn4j.validator.exception.ValidationException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.PASSKEY_REGISTRATION_COMPLETION)
@SuppressWarnings("ucd")
public class FingerprintRegistrationCompletion extends B2fApi {

	@RequestMapping(method = RequestMethod.OPTIONS, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse optionJson(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) throws IOException {
		GeneralUtilities generalUtilities = new GeneralUtilities();
		String url = request.getHeader("Origin");
		new DataAccess().addLog("FingerprintRegistrationCompletion", "OPTION");
		httpResponse = generalUtilities.setResponseHeader(httpResponse, url);
		return new ApiResponse(Outcomes.FAILURE, Constants.METHOD_NOT_ALLOWED);
	}

	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody ApiResponse getJson(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		new DataAccess().addLog("FingerprintRegistrationCompletion", "Get");
		return new ApiResponse(Outcomes.FAILURE, Constants.METHOD_NOT_ALLOWED + ": GET");
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse fingerprintRegistrationCompletionJsonPost(@RequestBody CreateFingerprint cred,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		String reason = "";
		int outcome = Outcomes.FAILURE;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int logLevel = LogConstants.TRACE;
		try {
			CreateResponse fpResponse = cred.getResponse();
			String browserSession = cred.getBrowserSession();
			byte[] attestationObject = Base64.getUrlDecoder().decode(fpResponse.getAttestationObject());
			byte[] clientDataJSON = Base64.getUrlDecoder().decode(fpResponse.getClientDataJSON());
			AuthenticatorDbObj authenticator = dataAccess.getActiveAuthenticatorByTokenIdAndUrl(browserSession,
					GeneralUtilities.getUrlHost(cred.getReqUrl()));
			if (authenticator != null) {
				DeviceDbObj device = dataAccess.getDeviceByBrowserId(authenticator.getBrowserId());
				dataAccess.addLog(device.getDeviceId(), "authenticator found", logLevel);
				// Server properties
				Origin origin = new Origin(request.getHeader("origin"));
				String rpId = cred.getRpId();
				dataAccess.addLog(device.getDeviceId(), "rpId: " + rpId, logLevel);
				Challenge challenge = new DefaultChallenge(authenticator.getChallenge());
				byte[] tokenBindingId = new byte[] { 0x01, 0x23, 0x45 };
				ServerProperty serverProperty = new ServerProperty(origin, rpId, challenge, tokenBindingId);

				// expectations
				boolean userVerificationRequired = false;
				boolean userPresenceRequired = true;

				RegistrationRequest registrationRequest = new RegistrationRequest(attestationObject, clientDataJSON);

				List<PublicKeyCredentialParameters> pubKeyCredParams = fpResponse.getPubKeyCredParams();

				RegistrationParameters registrationParameters = new RegistrationParameters(serverProperty,
						pubKeyCredParams, userVerificationRequired, userPresenceRequired);
				RegistrationData registrationData;
				WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
				try {
					dataAccess.addLog(device.getDeviceId(), "webAuthnManager created", logLevel);
					registrationData = webAuthnManager.parse(registrationRequest);

					try {
						webAuthnManager.validate(registrationData, registrationParameters);
						dataAccess.addLog(device.getDeviceId(), "webAuthnManager validated", logLevel);
					} catch (ValidationException e) {
						dataAccess.addLog(device.getDeviceId(), e);
					}
					String credId = cred.getId();
					authenticator.setCredId(credId);
					authenticator.setAttestedCredentialData(
							registrationData.getAttestationObject().getAuthenticatorData().getAttestedCredentialData());

					authenticator.setCollectedClientData(registrationData.getCollectedClientData());
					authenticator.setAttestationObject(registrationData.getAttestationObject());
					if (registrationData.getAttestationObject() != null) {
						authenticator.setFormat(registrationData.getAttestationObject().getFormat());
						if (registrationData.getAttestationObject().getAuthenticatorData() != null) {
							authenticator.setSignCount(
									registrationData.getAttestationObject().getAuthenticatorData().getSignCount());
						}
					}
					dataAccess.updateAuthenticatorByBrowserAndUrl(authenticator);
					if (dataAccess.updateAuthenticatorByCredId(authenticator)) {
						dataAccess.addLog(device.getDeviceId(), "auth updated DB", logLevel);
						addFingerprintRegistrationSuccess(browserSession, device, dataAccess);
						outcome = Outcomes.SUCCESS;
					} else {
						dataAccess.addLog(device.getDeviceId(), "save failed", logLevel);
						reason = "saveFailed";
					}
				} catch (DataConversionException e) {
					dataAccess.addLog(device.getDeviceId(), e);
				}
			} else {
				reason = "auth record not found";
				dataAccess.addLog(reason, LogConstants.ERROR);
			}
		} catch (BadChallengeException bce) {
			reason = "bad challange";
			dataAccess.addLog(bce);
			dataAccess.addLog("bad challenge", LogConstants.ERROR);
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		return new ApiResponse(outcome, reason);
	}

	private void addFingerprintRegistrationSuccess(String browserSession, DeviceDbObj device,
			CompanyDataAccess dataAccess) {
		if (device != null) {
			Timestamp expiration;
			if (device.isCentral()) {
				dataAccess.updateMostRecestIncompleteConnectionForCentral(device);

			} else {
				DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(device, true);
				if (!conn.isInstallComplete()) {
					dataAccess.updateAsInstallComplete(conn);
				}
			}
			CompanyDbObj company = dataAccess.getCompanyByGroupId(device.getGroupId());
			expiration = DateTimeUtilities.getCurrentTimestampPlusSeconds(company.getPasskeyTimeoutSeconds());
			// short term for signup
			dataAccess.addCompletedCheckForFingerprint(device, browserSession, expiration, dataAccess);
		}
	}
}

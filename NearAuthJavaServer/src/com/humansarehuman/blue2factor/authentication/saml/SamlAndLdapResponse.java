package com.humansarehuman.blue2factor.authentication.saml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.shibboleth.shared.xml.impl.BasicParserPool;

import org.apache.http.util.TextUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.springframework.ui.ModelMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.authentication.failures.Failure;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.RequestAndResponse;
import com.humansarehuman.blue2factor.entities.UrlAndModel;
import com.humansarehuman.blue2factor.entities.UrlModelAndHttpResponse;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.enums.NonMemberStrategy;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.enums.UserType;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.LdapServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlAuthnRequestDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlIdentityProviderDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.saml.Saml;


public class SamlAndLdapResponse extends B2fApi {
	public class OutcomeAndResponse {
		public int outcome;
		public HttpServletResponse httpServletResponse;

		public OutcomeAndResponse(int outcome, HttpServletResponse httpServletResponse) {
			super();
			this.outcome = outcome;
			this.httpServletResponse = httpServletResponse;
		}
	}

	public class OutcomeResponseNextPageAndModel extends OutcomeAndResponse {
		public String nextPage;
		public ModelMap model;

		public OutcomeResponseNextPageAndModel(int outcome, HttpServletResponse httpServletResponse, String nextPage,
				ModelMap model) {
			super(outcome, httpServletResponse);
			this.nextPage = nextPage;
			this.model = model;
		}
	}

	public class NextPageAndOutcome {
		public String nextPage;
		public int outcome;

		public NextPageAndOutcome(String nextPage, int outcome) {
			this.nextPage = nextPage;
			this.outcome = outcome;
		}
	}

	public class OutcomeAndResponseAndIdObj extends OutcomeAndResponse {
		public IdentityObjectFromServer idObj;

		public OutcomeAndResponseAndIdObj(int outcome, HttpServletResponse httpServletResponse,
				IdentityObjectFromServer idObj) {
			super(outcome, httpServletResponse);
			this.idObj = idObj;
		}
	}

	public class OutcomeRequestResponseNextPageIdObjAndEmail extends OutcomeAndResponseAndIdObj {
		public HttpServletRequest httpServletRequest;
		public String nextPage;
		public String email;

		public OutcomeRequestResponseNextPageIdObjAndEmail(OutcomeAndResponseAndIdObj outRequest,
				HttpServletRequest httpServletRequest, IdentityObjectFromServer idObj, String nextPage, String email) {
			super(outRequest.outcome, outRequest.httpServletResponse, outRequest.idObj);
			this.nextPage = nextPage;
			this.httpServletRequest = httpServletRequest;
			this.email = email;
		}

		public OutcomeRequestResponseNextPageIdObjAndEmail(int outcome, HttpServletRequest httpServletRequest,
				HttpServletResponse httpServletResponse, IdentityObjectFromServer idObj, String nextPage,
				String email) {
			super(outcome, httpServletResponse, idObj);
			this.nextPage = nextPage;
			this.httpServletRequest = httpServletRequest;
			this.email = email;
		}
	}

	public class OutcomeRequestResponseNextPageIdObjEmailAndModel extends OutcomeRequestResponseNextPageIdObjAndEmail {
		public ModelMap model;

		public OutcomeRequestResponseNextPageIdObjEmailAndModel(
				OutcomeRequestResponseNextPageIdObjAndEmail outcomeRequest, ModelMap model) {
			super(outcomeRequest.outcome, outcomeRequest.httpServletRequest, outcomeRequest.httpServletResponse,
					outcomeRequest.idObj, outcomeRequest.nextPage, outcomeRequest.email);
			this.model = model;
		}

		public OutcomeRequestResponseNextPageIdObjEmailAndModel(int outcome, HttpServletRequest httpServletRequest,
				HttpServletResponse httpServletResponse, IdentityObjectFromServer idObj, String nextPage, String email,
				ModelMap model) {
			super(outcome, httpServletRequest, httpServletResponse, idObj, nextPage, email);
			this.model = model;
		}
	}

	protected UrlModelAndHttpResponse handleSamlResponseFromIdentityProvider(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, Response response, SamlAuthnRequestDbObj samlAuthnRequest,
			CompanyDbObj company, String ipAddress, String relayState, String samlResponse, String email,
			SamlDataAccess dataAccess) throws Exception {
		IdentityObjectFromServer idObj = this.setIdentityObject(samlAuthnRequest.getToken());
		OutcomeRequestResponseNextPageIdObjEmailAndModel outcomeRequest;
		UrlModelAndHttpResponse urlModelAndHttpResponse;
		if (idObj != null && idObj.getCompany() != null && idObj.getDevice() != null
				&& company.getCompanyId().equals(idObj.getCompany().getCompanyId())) {
			dataAccess.addLog("idObj found");
			DeviceDbObj device = idObj.getDevice();
			if (device == null || dataAccess.deviceIsTemp(device) || device.isMultiUser()) {
				outcomeRequest = handleNullOrTempDevice(request, httpResponse, model, response, samlAuthnRequest, idObj,
						ipAddress, email, dataAccess);
			} else {
				outcomeRequest = handleResponseWithDevice(request, httpResponse, model, response, samlAuthnRequest,
						idObj, ipAddress, relayState, email, samlResponse, dataAccess);
			}
			samlAuthnRequest.setExpired(true);
			dataAccess.updateSamlAuthRequestByTableId(samlAuthnRequest);
			urlModelAndHttpResponse = new UrlModelAndHttpResponse(outcomeRequest.nextPage, outcomeRequest.model,
					outcomeRequest.httpServletResponse, outcomeRequest.outcome);
		} else {
			dataAccess.addLog("idObj not found", LogConstants.WARNING);
			GroupDbObj group = dataAccess.getGroupByEmail(email);
			boolean newlySignedUp = false;
			if (group != null) {
				newlySignedUp = dataAccess.isNewlySignedUp(group);
			}
			if (newlySignedUp || dataAccess.isPassThroughAllowed(company, group)) {
				outcomeRequest = handlePassThrough(request, httpResponse, model, response, samlAuthnRequest, company,
						group, ipAddress, email, dataAccess);
				urlModelAndHttpResponse = new UrlModelAndHttpResponse(outcomeRequest.nextPage, outcomeRequest.model,
						outcomeRequest.httpServletResponse, outcomeRequest.outcome);
			} else {
				urlModelAndHttpResponse = new UrlModelAndHttpResponse("notSignedUp", model, httpResponse,
						Outcomes.FAILURE);
			}
		}
		return urlModelAndHttpResponse;
	}

	/*
	 * String companyId, String groupId, String groupName, int acceptedTypes,
	 * boolean active, Timestamp createDate, int timeoutSecs, String groupPw, String
	 * salt, int devicesAllowed, int devicesInUse, int permissions, String username,
	 * Timestamp tokenDate, String uid, UserType userType, boolean userExempt,
	 * boolean pushAllowed, boolean textAllowed
	 */

	private OutcomeRequestResponseNextPageIdObjEmailAndModel handleResponseWithDevice(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, Response response, SamlAuthnRequestDbObj samlAuthnRequest,
			IdentityObjectFromServer idObj, String ipAddress, String relayState, String email, String samlResponse,
			SamlDataAccess dataAccess) {
		int outcome = Outcomes.FAILURE;
		dataAccess.addLog("device is not null", LogConstants.TRACE);
		String nextPage = null;
		GroupDbObj group = idObj.getGroup();
		if (group != null) {
			dataAccess.addLog("group is not null", LogConstants.TRACE);
			if (isGroupNameEquivalent(group.getGroupName(), email)) {
				try {
//					idObj.getDevice().setSignedIn(true);
					dataAccess.setSignedIn(idObj.getDevice(), true);
					OutcomeResponseNextPageAndModel outcomeAndResponse = handleSamlUserWithDevice(request, httpResponse,
							idObj, samlAuthnRequest, dataAccess, response, ipAddress, email, model, relayState,
							samlResponse);
					outcome = outcomeAndResponse.outcome;
					httpResponse = outcomeAndResponse.httpServletResponse;
					nextPage = outcomeAndResponse.nextPage;
					model = outcomeAndResponse.model;
					dataAccess.addLog("outcome: " + outcome + ", nextPage: " + nextPage);
				} catch (Exception e) {
					dataAccess.addLog(e);
				}
			} else {
				dataAccess.addLog(group.getGroupName() + " != " + email);
				nextPage = "wrongUser";
			}
		} else {
			dataAccess.addLog("group is null");
		}
		return new OutcomeRequestResponseNextPageIdObjEmailAndModel(outcome, request, httpResponse, idObj, nextPage,
				email, model);
	}

	private OutcomeRequestResponseNextPageIdObjEmailAndModel handleNullOrTempDevice(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, Response response, SamlAuthnRequestDbObj samlAuthnRequest,
			IdentityObjectFromServer idObj, String ipAddress, String email, SamlDataAccess dataAccess) {
		dataAccess.addLog("device is null, temp, or multiuser");
		String nextPage = null;
		int outcome = Outcomes.API_F1_FAILURE;
		try {
			GroupDbObj group = idObj.getGroup();
			if (group == null) {
				group = dataAccess.getGroupByEmail(email);
				idObj.setGroup(group);
			}
			OutcomeAndResponseAndIdObj outcomeResponseAndIdObj;
			if (group != null) {
				dataAccess.addLog("non-member strategy: " + idObj.getCompany().getNonMemberStrategy() + " , count: "
						+ dataAccess.getNonTempDevicesByGroupId(group.getGroupId(), true).size());
			}
			boolean passThrough = dataAccess.isPassThroughAllowed(idObj.getCompany(), group);

			if (passThrough) {
				// send them back to where they came from
				outcomeResponseAndIdObj = this.passThrough(httpResponse, idObj.getCompany(), samlAuthnRequest,
						dataAccess, ipAddress, group);
				idObj = outcomeResponseAndIdObj.idObj;
				UrlAndModel urlAndModel = this.reSetup(model, idObj, null, dataAccess);
				nextPage = urlAndModel.getUrl();
				dataAccess.addLog("nextPage: " + nextPage);
				model = urlAndModel.getModelMap();
			} else {
				outcomeResponseAndIdObj = handleOneTimeSamlIdpAccess(httpResponse, idObj, samlAuthnRequest, dataAccess,
						response, ipAddress, group);
				idObj = outcomeResponseAndIdObj.idObj;
				nextPage = "setupJavascript";
				outcome = Outcomes.NEEDS_SETUP;
				UrlAndModel urlAndModel = getSetupRedirect(model, outcome, email, dataAccess, idObj,
						samlAuthnRequest.getIncomingRequestId(), nextPage);
				model = urlAndModel.getModelMap();
			}
			dataAccess.addLog("idObj is null? " + (idObj == null));
			outcome = outcomeResponseAndIdObj.outcome;
			httpResponse = outcomeResponseAndIdObj.httpServletResponse;
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		RequestAndResponse reqRes = setTemporaryPersistantToken(request, httpResponse, dataAccess, idObj,
				GeneralUtilities.randomString());
		return new OutcomeRequestResponseNextPageIdObjEmailAndModel(outcome, reqRes.getRequest(), reqRes.getResponse(),
				idObj, nextPage, email, model);
	}

	private OutcomeRequestResponseNextPageIdObjEmailAndModel handlePassThrough(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, Response response, SamlAuthnRequestDbObj samlAuthnRequest,
			CompanyDbObj company, GroupDbObj group, String ipAddress, String email, SamlDataAccess dataAccess) {
		dataAccess.addLog("device is not found, but we may be setting it up");
		String nextPage = null;
		int outcome = Outcomes.API_F1_FAILURE;
		IdentityObjectFromServer idObj = null;
		try {
			OutcomeAndResponseAndIdObj outcomeResponseAndIdObj;
			// send them back to where they came from
			outcomeResponseAndIdObj = this.passThrough(httpResponse, company, samlAuthnRequest, dataAccess, ipAddress,
					group);
			idObj = outcomeResponseAndIdObj.idObj;
			UrlAndModel urlAndModel = this.reSetup(model, idObj, null, dataAccess);
			nextPage = urlAndModel.getUrl();
			dataAccess.addLog("nextPage: " + nextPage);
			model = urlAndModel.getModelMap();

			dataAccess.addLog("idObj is null? " + (idObj == null));
			outcome = outcomeResponseAndIdObj.outcome;
			httpResponse = outcomeResponseAndIdObj.httpServletResponse;
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		RequestAndResponse reqRes = setTemporaryPersistantToken(request, httpResponse, dataAccess, idObj,
				GeneralUtilities.randomString());
		return new OutcomeRequestResponseNextPageIdObjEmailAndModel(outcome, reqRes.getRequest(), reqRes.getResponse(),
				idObj, nextPage, email, model);
	}

	private boolean isGroupNameEquivalent(String groupName, String email) {
		boolean same = false;
		new DataAccess().addLog("groupName: " + groupName + ", email: " + email);
		if (groupName.equalsIgnoreCase(email)) {
			same = true;
		} else if ((groupName.equalsIgnoreCase("chris@humansarehuman.com")
				|| groupName.equalsIgnoreCase("chris@blue2factor.com"))
				&& (email.equalsIgnoreCase("chris@humansarehuman.com")
						|| email.equalsIgnoreCase("chris@blue2factor.com"))) {
			same = true;
		}
		return same;
	}

//	email from Saml(chris@blue2factor.com) didn't match email from request(chris@humansarehuman.com)
	public UrlModelAndHttpResponse evaluateResponse(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, String samlResponse, String relayState, String apiKey) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		int logLevel = LogConstants.TRACE;
		dataAccess.addLog("post or get received, " + samlResponse);
		UrlModelAndHttpResponse fnResponse = new UrlModelAndHttpResponse(null, model, httpResponse, Outcomes.FAILURE);
		SamlAuthnRequestDbObj samlAuthRequest = null;
		CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
		try {
			if (!TextUtils.isEmpty(relayState)) {
				byte[] decodedBytes = Base64.getDecoder().decode(samlResponse);
				String decryptedResponse = new String(decodedBytes, StandardCharsets.UTF_8);
				dataAccess.addLog("decoded bytes: '" + decryptedResponse + "' - RelayState: '" + relayState + "'", 
						logLevel);
				Response response = getSamlResponse(decodedBytes);
				new Saml().logAuthnResponse(response, dataAccess);

				if (company != null) {
					try {
						String ipAddress = GeneralUtilities.getClientIp(request);
						if (validateStatus(response)) {
							String inResponseTo = response.getInResponseTo();
							samlAuthRequest = dataAccess.getAuthRequestByOutgoingRequestId(inResponseTo);
							String email = getEmailFromResponse(response, dataAccess);
							if (samlAuthRequest != null) {
								dataAccess.addLog("sender: " + samlAuthRequest.getSender(), logLevel);
								fnResponse.setOutcome(validateIdpResponse(response, samlAuthRequest, company,
										dataAccess, ipAddress, true));
								if (fnResponse.getOutcome() == Outcomes.SUCCESS) {
									if (samlAuthRequest.getSender().equals("app")) {
										fnResponse.setUrl("success");
									} else {
										fnResponse = handleSamlResponseFromIdentityProvider(request, httpResponse,
												model, response, samlAuthRequest, company, ipAddress, relayState,
												samlResponse, email, dataAccess);
									}
								} else {
									fnResponse.setUrl("error");
									getErrorMessage(fnResponse.getOutcome());
									fnResponse.setModelMap(model.addAttribute("errorMessage",
											getErrorMessage(fnResponse.getOutcome())));
								}
							} else {
								fnResponse = handleNullSamlAuthnRequest(request, httpResponse, model, response,
										samlAuthRequest, company, apiKey, ipAddress, relayState, samlResponse,
										dataAccess);
							}
						} else {
							dataAccess.addLog("bad status", LogConstants.ERROR);
						}
					} catch (Exception e) {
						dataAccess.addLog(e);
					}
				} else {
					dataAccess.addLog("company not found", LogConstants.ERROR);
				}
				dataAccess.addLog("nextPage: " + fnResponse.getUrl(), logLevel);
			} else {
				dataAccess.addLog("relayState was null", LogConstants.ERROR);
			}
		} catch (Exception e1) {
			dataAccess.addLog(e1);
		}
		fnResponse = handleFailure(fnResponse, samlAuthRequest, company.getApiKey(), company.getEntityIdVal(),
				dataAccess);
		return fnResponse;
	}

	private String getErrorMessage(int outcome) {
		String error = "We could not authenticate you.";
		switch (outcome) {
		case Outcomes.SAML_INVALID_USER:
			error = "The user was not valid";
			break;
		case Outcomes.SAML_INVALID_CERT:
			error = "The certificate used is not valid.";
			break;
		case Outcomes.SAML_INVALID_ISSUER:
			error = "The issuer is not valid. Perhaps your saml is set up incorrectly";
			break;
		case Outcomes.SAML_INVALID_SIGNATURE:
			error = "The signature could not be validated";
			break;
		case Outcomes.SAML_TOO_OLD:
			error = error + " Perhaps you took too long to respond?";
		}
		return error;
	}

	private UrlModelAndHttpResponse handleFailure(UrlModelAndHttpResponse resp, SamlAuthnRequestDbObj samlAuthRequest,
			String apiKey, String entityIdVal, SamlDataAccess dataAccess) {
		if (resp.getOutcome() != Outcomes.SUCCESS) {
			if (samlAuthRequest != null) {
				if (!TextUtils.isBlank(samlAuthRequest.getIncomingRelayState())) {
					GroupDbObj group = getGroupFromSamlRequest(samlAuthRequest, dataAccess);
					if (group != null) {
						UrlAndModel urlAndModel = this.respondToSp(resp.getModelMap(), samlAuthRequest, group, apiKey,
								entityIdVal, resp.getOutcome(), dataAccess);
						resp.setUrl(urlAndModel.getUrl());
						resp.setModelMap(urlAndModel.getModelMap());
					}
				}
			}
		}
		return resp;
	}

	private GroupDbObj getGroupFromSamlRequest(SamlAuthnRequestDbObj samlAuthRequest, SamlDataAccess dataAccess) {
		GroupDbObj group;
		if (!TextUtils.isBlank(samlAuthRequest.getGroupId())) {
			group = dataAccess.getActiveGroupById(baseUrl);
		} else {
			group = dataAccess.getActiveGroupByDeviceId(samlAuthRequest.getDeviceId());
		}
		return group;
	}

	private Response getSamlResponse(byte[] xmlBytes) {
		Response response = null;
		DataAccess dataAccess = new DataAccess();
		try {
			BasicParserPool basicParserPool = new BasicParserPool();

			basicParserPool.setNamespaceAware(true);
			basicParserPool.initialize();
			InputStream stream = new ByteArrayInputStream(xmlBytes);
			Document doc = basicParserPool.parse(stream);

			Element samlElem = doc.getDocumentElement();
			String node = samlElem.getNodeName();
			dataAccess.addLog("samlElem: " + node);
			Saml.initializeSaml();
			UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
			if (unmarshallerFactory == null) {
				dataAccess.addLog("unmarshallerFactory is null");
			}
			if (samlElem.getLocalName() == null) {
				samlElem.setAttribute("localName", Response.DEFAULT_ELEMENT_LOCAL_NAME);
			}
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlElem);

			XMLObject requestXmlObj = unmarshaller.unmarshall(samlElem);
			if (requestXmlObj == null) {
				dataAccess.addLog("requestXmlObj is null");
			}

			response = (Response) requestXmlObj;

		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return response;
	}

	private UrlModelAndHttpResponse getSamlResponseNextPage(HttpServletRequest request, HttpServletResponse response,
			SamlAuthnRequestDbObj samlAuthnRequest, ModelMap model, IdentityObjectFromServer idObj, String relayState,
			String email, String nextPage, int outcome, SamlDataAccess dataAccess) {
		UrlAndModel urlAndModel;
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		try {
			dataAccess.addLog("nextPage: " + nextPage);
			if (nextPage == null || dataAccess.deviceIsTemp(idObj.getDevice())) {
				urlAndModel = getRedirect(request, response, samlAuthnRequest, model, outcome, relayState, email,
						dataAccess, idObj, nextPage);
			} else {
				urlAndModel = new UrlAndModel(nextPage, model);
			}
			String redirectUrl;
			if (urlAndModel.getUrl() == null) {
				Failure failure = new Failure();
				redirectUrl = failure.getReferrer(request, idObj.getCompany(), dataAccess);
				response.setHeader("Location", redirectUrl);
				response.setStatus(302);
				outcome = Outcomes.SUCCESS;
			}
		} catch (Exception e) {
			model.addAttribute("environment", Constants.ENVIRONMENT.toString());
			nextPage = "setupFailure";
			dataAccess.addLog(e);
			urlAndModel = new UrlAndModel(nextPage, model);
			outcome = Outcomes.FAILURE;
		}
		return new UrlModelAndHttpResponse(urlAndModel, response, outcome);
	}

	private UrlAndModel getRedirect(HttpServletRequest request, HttpServletResponse response,
			SamlAuthnRequestDbObj samlAuthnRequest, ModelMap model, int outcome, String relayState, String email,
			SamlDataAccess dataAccess, IdentityObjectFromServer idObj, String nextPage)
			throws UnsupportedEncodingException {
		UrlAndModel urlAndModel = new UrlAndModel("failure", model);
		if (samlAuthnRequest != null) {
			dataAccess.addLog("samlAuthnRequest != null, reqId: " + samlAuthnRequest.getIncomingRequestId());
			// TODO: this statement is sloppy - cjm
			ArrayList<String> sources = new ArrayList<String>(
					Arrays.asList("app", "signIn", "setup", "oneTimeAccess", "needsResync"));
			dataAccess.addLog("source: " + samlAuthnRequest.getIncomingRequestId());
			if (sources.contains(samlAuthnRequest.getIncomingRequestId())
					|| sources.contains(samlAuthnRequest.getSender()) || dataAccess.deviceIsTemp(idObj.getDevice())) {
				urlAndModel = getSetupRedirect(model, outcome, email, dataAccess, idObj,
						samlAuthnRequest.getIncomingRequestId(), nextPage);
			} else {
				if (idObj.getCompany() != null) {
					KeyDbObj certDbObj = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.SP_CERT,
							idObj.getCompany().getCompanyId());
					if (certDbObj != null) {
						urlAndModel = respondToSp(model, samlAuthnRequest, idObj.getGroup(),
								idObj.getCompany().getApiKey(), idObj.getCompany().getEntityIdVal(), outcome);
					} else {
						dataAccess.addLog("no service provider this sucks", LogConstants.WARNING);
					}
				} else {
					dataAccess.addLog("no company this sucks", LogConstants.WARNING);
				}
			}
		} else {
			dataAccess.addLog("samlAuthnRequest was null");
			getSetupRedirect(model, outcome, email, dataAccess, idObj, "", null);
		}
		return urlAndModel;
	}

	protected OutcomeRequestResponseNextPageIdObjEmailAndModel respondToServiceProviderFromLdap(
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			SamlAuthnRequestDbObj samlAuthnRequest, CompanyDbObj company, String ipAddress, String email,
			SamlDataAccess dataAccess) throws Exception {
		int outcome = Outcomes.API_F1_FAILURE;
		IdentityObjectFromServer idObj = this.setIdentityObject(samlAuthnRequest.getToken());
		String nextPage = null;
		if (idObj != null && idObj.getCompany() != null
				&& company.getCompanyId().equals(idObj.getCompany().getCompanyId())) {
			dataAccess.addLog("handleSamlResponseFromOther", "samlFound");
			RequestAndResponse reqRes = null;
			DeviceDbObj device = idObj.getDevice();
			if (device == null || dataAccess.deviceIsTemp(device)) {
				dataAccess.addLog("handleSamlResponseFromOther", "device is null (or temp)");
				GroupDbObj group = idObj.getGroup();
				if (group == null) {
					group = dataAccess.getGroupByEmail(email);
					idObj.setGroup(group);
				}
				OutcomeAndResponseAndIdObj outcomeResponseAndIdObj;
				if (group != null) {
					dataAccess.addLog("non-member strategy: " + company.getNonMemberStrategy() + " , count: "
							+ dataAccess.getNonTempDevicesByGroupId(group.getGroupId(), true).size());
				}
				boolean passThrough = dataAccess.isPassThroughAllowed(company, group);
				if (passThrough) {
					// send them back to where they came from
					outcomeResponseAndIdObj = this.passThrough(httpResponse, company, samlAuthnRequest, dataAccess,
							ipAddress, group);
					idObj = outcomeResponseAndIdObj.idObj;
					UrlAndModel urlAndModel = this.reSetup(model, idObj, null, dataAccess);
					nextPage = urlAndModel.getUrl();
					dataAccess.addLog("nextPage: " + nextPage);
					model = urlAndModel.getModelMap();
				} else {
					outcomeResponseAndIdObj = handleOneTimeLdapIdpAccess(httpResponse, idObj, company, samlAuthnRequest,
							dataAccess, email, ipAddress, group);
					idObj = outcomeResponseAndIdObj.idObj;
				}
				dataAccess.addLog("idObj is null? " + (idObj == null));
				outcome = outcomeResponseAndIdObj.outcome;
				httpResponse = outcomeResponseAndIdObj.httpServletResponse;
				String newTokenStr = GeneralUtilities.randomString();
				// I get it - it's an oxymoron.
				reqRes = setTemporaryPersistantToken(request, httpResponse, dataAccess, idObj, newTokenStr);
			} else {
				dataAccess.addLog("handleSamlResponseFromOther", "device is not null");
				GroupDbObj group = idObj.getGroup();
				if (group != null) {
					dataAccess.addLog("handleSamlResponseFromOther", "group is not null");
					if (isGroupNameEquivalent(group.getGroupName(), email)) {
						dataAccess.setSignedIn(device, true);
						OutcomeResponseNextPageAndModel outcomeAndResponse = handleLdapUserWithDevice(request,
								httpResponse, idObj, samlAuthnRequest, company, dataAccess, ipAddress, email, model);
						outcome = outcomeAndResponse.outcome;
						httpResponse = outcomeAndResponse.httpServletResponse;
						nextPage = outcomeAndResponse.nextPage;
						dataAccess.addLog("handleSamlResponseFromOther",
								"outcome: " + outcome + ", nextPage: " + nextPage);
						if (nextPage == null || !nextPage.equals("jsVerify")) {
							reqRes = setPersistantToken(request, httpResponse, dataAccess, idObj);
						} else {
							String accessKey = buildAccessCodeDbObj(idObj, dataAccess);
							request = setPreviousLdapRequest(request, accessKey);
							dataAccess.addLog("handleSamlResponseFromOther",
									"will verify fingerprint - not reseting token");
						}
						model = outcomeAndResponse.model;
					} else {
						dataAccess.addLog("handleSamlResponseFromOther", group.getGroupName() + " != " + email);
						nextPage = "wrongUser";
					}
				} else {
					dataAccess.addLog("handleSamlResponseFromOther", "group is null");
				}
			}
			if (reqRes != null) {
				request = reqRes.getRequest();
				httpResponse = reqRes.getResponse();
			}
		} else {
			NextPageAndOutcome nextPageAndOutcome = getAccessIfNeededToEmail(samlAuthnRequest, email, dataAccess);
			nextPage = nextPageAndOutcome.nextPage;
			outcome = nextPageAndOutcome.outcome;
		}
		return new OutcomeRequestResponseNextPageIdObjEmailAndModel(outcome, request, httpResponse, idObj, nextPage,
				email, model);
	}

	protected UrlModelAndHttpResponse handleNullSamlAuthnRequest(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, Response response, SamlAuthnRequestDbObj samlAuthnRequest,
			CompanyDbObj company, String apiKey, String ipAddress, String relayState, String samlResponse,
			SamlDataAccess dataAccess) {
		IdentityObjectFromServer idObj = this.getIdentityObjectFromCookie(request, apiKey);
		dataAccess.addLog("handleNullAuthnRequest", "auth request was null", LogConstants.WARNING);
		int outcome = Outcomes.API_F1_FAILURE;
		String nextPage = null;
		String email = getEmailFromResponse(response, dataAccess);
		if (idObj != null) {
			RequestAndResponse reqRes;
			NextPageAndOutcome nextPageAndOutcome = handleSamlResponseWithoutRequest(request, httpResponse, model,
					response, company, ipAddress, idObj, relayState, samlResponse, dataAccess);
			outcome = nextPageAndOutcome.outcome;
			nextPage = nextPageAndOutcome.nextPage;
			reqRes = setPersistantToken(request, httpResponse, dataAccess, idObj);
			request = reqRes.getRequest();
			httpResponse = reqRes.getResponse();
		} else {
			dataAccess.addLog("handleNullAuthnRequest", "idObj was null");
			nextPage = "needsResync";
		}
		dataAccess.addLog("outcome: " + outcome);
		return this.getSamlResponseNextPage(request, httpResponse, samlAuthnRequest, model, idObj, relayState, email,
				nextPage, outcome, dataAccess);
	}

	protected OutcomeRequestResponseNextPageIdObjEmailAndModel handleNullSamlAuthnRequestFromLdap(
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model, CompanyDbObj company,
			String apiKey, String ipAddress, String email, SamlDataAccess dataAccess) {
		IdentityObjectFromServer idObj = this.getIdentityObjectFromCookie(request, apiKey);
		dataAccess.addLog("handleNullAuthnRequest", "auth request was null", LogConstants.WARNING);
		int outcome = Outcomes.API_F1_FAILURE;
		String nextPage = null;
		if (idObj != null) {
			RequestAndResponse reqRes;
			NextPageAndOutcome nextPageAndOutcome = handleLdapResponseWithoutRequest(request, httpResponse, model,
					company, ipAddress, idObj, email, dataAccess);
			outcome = nextPageAndOutcome.outcome;
			nextPage = nextPageAndOutcome.nextPage;
			reqRes = setPersistantToken(request, httpResponse, dataAccess, idObj);
			request = reqRes.getRequest();
			httpResponse = reqRes.getResponse();
		} else {
			dataAccess.addLog("handleNullAuthnRequest", "idObj was null");
			nextPage = "needsResync";
		}
		return new OutcomeRequestResponseNextPageIdObjEmailAndModel(outcome, request, httpResponse, idObj, nextPage,
				email, model);
	}

	protected NextPageAndOutcome handleSamlResponseWithoutRequest(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, Response response, CompanyDbObj company, String ipAddress,
			IdentityObjectFromServer idObj, String relayState, String samlResponse, SamlDataAccess dataAccess) {
		int outcome = Outcomes.FAILURE;
		String email = getEmailFromResponse(response, dataAccess);
		String nextPage = null;
		if (!TextUtils.isBlank(email)) {
			GroupDbObj group = dataAccess.getGroupByEmail(email);
			if (group != null) {
				outcome = this.getOutcomeWithoutRequest(response, company, dataAccess, ipAddress);
				if (outcome == Outcomes.SUCCESS) {
					// see if they are set up. if not, let them go
					// this may be necessary to access email to set up B2F
					if (!dataAccess.doesConnectionExistByEmail(email)) {
						dataAccess.addLog("user found, not yet activated");
						outcome = Outcomes.SUCCESS;
					} else {
						// they are signed up
						dataAccess.addLog("user found, we are setting them up");

						GroupDbObj groupFromCode = idObj.getGroup();
						if (groupFromCode != null) {
							if (email.equals(groupFromCode.getGroupName())) {
								DeviceDbObj device = idObj.getDevice();
								if (device != null) {
									dataAccess.setSignedIn(device, true);
									dataAccess.addLog("signed in is true");
									if (dataAccess.isAccessAllowed(device, "handleSamlResponseWithoutRequest")) {
										outcome = Outcomes.SUCCESS;
									} else {
										dataAccess.addLog("device not local");
										model.addAttribute("environment", Constants.ENVIRONMENT.toString());
										model.addAttribute("fromIdp", true);
										Failure failure = new Failure();
										UrlAndModel urlAndModel = new Failure().checkForPushOrBiometrics(model, idObj,
												dataAccess);
										nextPage = urlAndModel.getUrl();
										model = urlAndModel.getModelMap();
										String successPage = Urls.SECURE_URL + Urls.SAML_RESPONSE_FROM_IDENTITY_PROVIDER
												+ "?RelayState=" + relayState + "&SamlResponse=" + samlResponse;
										dataAccess.addLog("successPage: " + successPage);
										failure.setTempSession(request, "src", successPage, 60 * 60 * 12);
									}
								} else {
									dataAccess.addLog(Constants.DEVICE_NOT_FOUND);
								}
							} else {
								nextPage = "wrongUser";
							}
						} else {
							dataAccess.addLog("group not found by access code");
						}
					}

				}
			}
		}
		if (outcome != Outcomes.SUCCESS) {
			dataAccess.addLog("email found, the user wasn't");
		}
		dataAccess.addLog("outcome: " + outcome);
		return new NextPageAndOutcome(nextPage, outcome);
	}

	public UrlAndModel respondToSp(ModelMap model, SamlAuthnRequestDbObj samlAuthnRequest, GroupDbObj group,
			String apiKey, String entityIdVal, int outcome) {
		DataAccess dataAccess = new DataAccess();
		return this.respondToSp(model, samlAuthnRequest, group, apiKey, entityIdVal, outcome, dataAccess);
	}

	public UrlAndModel respondToSp(ModelMap model, SamlAuthnRequestDbObj samlAuthnRequest, GroupDbObj group,
			String apiKey, String entityIdVal, int outcome, DataAccess dataAccess) {
		Saml saml = new Saml();
		boolean success = outcome == Outcomes.SUCCESS;
		saml.buildResponseFromIncomingSamlRecord(samlAuthnRequest, group, apiKey, entityIdVal, success, dataAccess);
		new DataAccess().addLog("found samlresponse - now we will redirect back to service provider", LogConstants.TRACE);
		String relayState = samlAuthnRequest.getIncomingRelayState();
		// post the relaystate and samlResponse
		model.addAttribute("action", samlAuthnRequest.getIncomingAcsUrl());
		model = addSamlResponse(model, saml, relayState);
		return new UrlAndModel("samlResponse", model);
	}

	public UrlAndModel addSamlResponseAndRelayState(UrlAndModel urlAndModel, SamlAuthnRequestDbObj samlAuthnRequest,
			GroupDbObj group, String apiKey, String entityIdVal, int outcome, DataAccess dataAccess) {
		ModelMap model = addSamlResponseAndRelayState(urlAndModel.getModelMap(), samlAuthnRequest, group, apiKey,
				entityIdVal, Outcomes.SUCCESS, dataAccess);
		urlAndModel.setModelMap(model);
		return urlAndModel;
	}

	public ModelMap addSamlResponseAndRelayState(ModelMap model, SamlAuthnRequestDbObj samlAuthnRequest,
			GroupDbObj group, String apiKey, String entityId, int outcome, DataAccess dataAccess) {
		Saml saml = new Saml();
		boolean success = outcome == Outcomes.SUCCESS;
		saml.buildResponseFromIncomingSamlRecord(samlAuthnRequest, group, apiKey, entityId, success, dataAccess);
		new DataAccess().addLog("found samlresponse - now we will redirect back to service provider");
		String relayState = samlAuthnRequest.getIncomingRelayState();
		// post the relaystate and samlResponse
		model.addAttribute("action", samlAuthnRequest.getIncomingAcsUrl());
		model = addSamlResponse(model, saml, relayState);
		return model;
	}

//	public UrlAndModel respondToSp(ModelMap model, AuthnRequest samlAuthnRequest, String relayState, GroupDbObj group,
//			CompanyDbObj company, boolean success, String ipAddress) {
//		DataAccess dataAccess = new DataAccess();
//		return this.respondToSp(model, samlAuthnRequest, relayState, group, company, success, ipAddress, dataAccess);
//	}

//	public UrlAndModel respondToSp(ModelMap model, AuthnRequest samlAuthnRequest, String relayState, GroupDbObj group,
//			CompanyDbObj company, boolean success, String ipAddress, DataAccess dataAccess) {
//		Saml saml = new Saml();
//		saml.buildResponseFromIncomingSamlRecord(samlAuthnRequest, group, company, success, ipAddress);
//		dataAccess.addLog("found samlresponse - now we will redirect back to service provider");
//		model.addAttribute("action", samlAuthnRequest.getAssertionConsumerServiceURL());
//		model = addSamlResponse(model, saml, relayState);
//		// see if we can directly forward this without showing our page
//		return new UrlAndModel("samlResponse", model);
//	}

//	public String respondToSpDirectly(HttpServletResponse httpResponse, ModelMap model, AuthnRequest samlAuthnRequest,
//			String relayState, GroupDbObj group, CompanyDbObj company, boolean success, String ipAddress,
//			DataAccess dataAccess) throws Exception {
//		Saml saml = new Saml();
//		saml.buildResponseFromIncomingSamlRecord(samlAuthnRequest, group, company, success, ipAddress);
//		dataAccess.addLog("found samlresponse - now we will redirect back to service provider");
//		// see if we can directly forward this without showing our page
//		String encoded = org.apache.commons.codec.binary.Base64
//				.encodeBase64String(saml.getSamlResponseAsString().getBytes());
//		String urlEncoded = URLEncoder.encode(encoded, StandardCharsets.UTF_8);
//		dataAccess.addLog("response = redirect:" + saml.getAction() + "?SamlResponse=" + urlEncoded + "&RelayState="
//				+ relayState);
//		return "redirect:" + saml.getAction() + "?SamlResponse=" + urlEncoded + "&RelayState=" + relayState;
//	}

	/**
	 * We may want to alter this to accommodate for accessing sites of IDPs like
	 * Google Workspace email when it is the IDP
	 * 
	 * Food for thought: This is a convoluted cluster fuck of a function
	 * 
	 * @param model
	 * @param samlAuthnRequest
	 * @param initialSaml
	 * @param group
	 * @param outcome
	 * @param relayState
	 * @param email
	 * @param dataAccess
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected UrlAndModel getSetupRedirect(ModelMap model, int outcome, String email, SamlDataAccess dataAccess,
			IdentityObjectFromServer idObj, String requestId, String nextPage) throws UnsupportedEncodingException {
		dataAccess.addLog("outcome: " + outcome);
		if (outcome == Outcomes.SUCCESS || outcome == Outcomes.NEEDS_SETUP) {
			String browserId = null;
			if (idObj != null) {
				BrowserDbObj browser = idObj.getBrowser();
				if (browser != null) {
					browserId = browser.getBrowserId();
				} else {
					dataAccess.addLog("I didn't expect to ever go through here");
				}
				String coId = "";
				if (idObj.getCompany() != null) {
					coId = idObj.getCompany().getCompanyId();
				}
				String deviceId = null;
				boolean deviceFound = false;
				if (idObj.getDevice() != null) {
					deviceFound = true;
					deviceId = idObj.getDevice().getDeviceId();
				}
				AccessCodeDbObj accessCode = new AccessCodeDbObj(GeneralUtilities.randomString(), coId, browserId,
						deviceId, 0, true, browserId, false);
				dataAccess.addAccessCode(accessCode, "getSetupRedirect");
				model.addAttribute("accessCode", accessCode.getAccessCode());
				if (idObj.getCompany() != null) {
					model.addAttribute("submitUrl", idObj.getCompany().getCompleteCompanyLoginUrl());
					dataAccess.addLog("companyLoginUrl = " + idObj.getCompany().getCompleteCompanyLoginUrl());
					String baseUrl = GeneralUtilities
							.getUrlProtocolAndHost(idObj.getCompany().getCompleteCompanyLoginUrl());
					boolean isCentral = false;
					if (deviceFound) {
						TokenDbObj session = dataAccess.addToken(idObj.getDevice(), browserId,
								TokenDescription.BROWSER_SESSION, baseUrl);
						TokenDbObj token = dataAccess.addToken(idObj.getDevice(), browserId,
								TokenDescription.BROWSER_TOKEN, baseUrl);

						String cookieString = session.getTokenId() + "**" + token.getTokenId();
						ArrayList<String> sources = new ArrayList<String>(
								Arrays.asList("signIn", "needsResync", "setup"));
						if (sources.contains(requestId)) {
							// make sure it's not a different user
							DeviceDbObj device = idObj.getDevice();
							isCentral = device.isCentral();
							GroupDbObj group = idObj.getGroup();
							if (group != null && isGroupNameEquivalent(group.getGroupName(), email)) {
								if (browser != null) {
									if (dataAccess.isFingerprintAvailable(browser)) {
										dataAccess.addLog("fpFound");
										cookieString += "**false";
										model.addAttribute("fpExists", true);
									} else {
										dataAccess.addLog("fp not found");
									}
								}
							}
						}
						model.addAttribute("b2fSetup", cookieString);
						model.addAttribute("central", isCentral);
						model.addAttribute("bleEnabled", idObj.getDevice().getHasBle());
						dataAccess.addLog("setting b2fSetup to " + cookieString);
					} else {
						// if we came from oneTimeAccess
						if (requestId.equals("oneTimeAccess")) {
							outcome = Outcomes.API_F1_FAILURE;
							model.addAttribute("message1", "No devices have been set up for: " + email + ".");
							model.addAttribute("message2",
									"Please download and install NearAuth.ai on two of your devices.");
						}
					}
				} else {
					dataAccess.addLog("Company not found in idObj");
				}
			}
		} else if (outcome == Outcomes.API_F1_FAILURE) {
			model.addAttribute("message1", "This service is not set up for the user: " + email + ".");
			model.addAttribute("message2", "Please contact your administrator about using NearAuth.ai.");
		} else {
			dataAccess.addLog("bad incoming outcome: " + outcome);
		}
		dataAccess.addLog("outcome: " + outcome);
		dataAccess.addLog("redirecting to: " + nextPage);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return new UrlAndModel(nextPage, model);
	}

	protected NextPageAndOutcome handleLdapResponseWithoutRequest(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, CompanyDbObj company, String ipAddress,
			IdentityObjectFromServer idObj, String email, SamlDataAccess dataAccess) {
		int outcome = Outcomes.FAILURE;
		String nextPage = null;
		if (!TextUtils.isBlank(email)) {
			GroupDbObj group = dataAccess.getGroupByEmail(email);
			if (group != null) {
				idObj.setGroup(group);
				// see if they are set up. if not, let them go
				// this may be necessary to access email to set up B2F
				if (!dataAccess.doesConnectionExistByEmail(email)) {
					dataAccess.addLog("user found, not yet activated");
					outcome = Outcomes.SUCCESS;
				} else {
					// they are signed up
					dataAccess.addLog("user found, we are setting them up");
					DeviceDbObj device = idObj.getDevice();
					if (device != null) {
						dataAccess.setSignedIn(device, true);
						dataAccess.addLog("signed in is true");
						if (dataAccess.isAccessAllowed(device, "handleLdapResponseWithoutRequest")) {
							outcome = Outcomes.SUCCESS;
						} else {
							dataAccess.addLog("device not local");
							model.addAttribute("environment", Constants.ENVIRONMENT.toString());
							model.addAttribute("fromIdp", true);
							Failure failure = new Failure();
							UrlAndModel urlAndModel = new Failure().checkForPushOrBiometrics(model, idObj, dataAccess);
							nextPage = urlAndModel.getUrl();
							model = urlAndModel.getModelMap();
							String accessKey = buildAccessCodeDbObj(idObj, dataAccess);
							String successPage = Urls.SECURE_URL + Urls.LDAP_SUBMIT + "?accessKey=" + accessKey;
							dataAccess.addLog("successPage: " + successPage);
							failure.setTempSession(request, "src", successPage, 60 * 60 * 12);
						}
					} else {
						dataAccess.addLog(Constants.DEVICE_NOT_FOUND);
					}
				}

			}
		}

		dataAccess.addLog("outcome: " + outcome);
		return new NextPageAndOutcome(nextPage, outcome);
	}

	private String buildAccessCodeDbObj(IdentityObjectFromServer idObj, SamlDataAccess dataAccess) {
		// String accessCode, String companyId, String serverId, String deviceId,
		// int permissions, boolean active, String browserId, boolean oneTimeAccess
		String companyId = idObj.getCompany().getCompanyId();
		String deviceId = null;
		if (idObj.getDevice() != null) {
			deviceId = idObj.getDevice().getDeviceId();
		}
		String browserId = null;
		if (idObj.getBrowser() != null) {
			browserId = idObj.getBrowser().getBrowserId();
		}
		String accessCodeStr = GeneralUtilities.randomString();
		AccessCodeDbObj accessCode = new AccessCodeDbObj(accessCodeStr, companyId, null, deviceId, 0, true, browserId,
				true);
		dataAccess.addAccessCode(accessCode, "handleLdapResponseWithoutRequest");
		return accessCodeStr;
	}

	/**
	 * if they're enrolled, but not active then let them procede
	 * 
	 * @param samlAuthnRequest
	 * @param samlDataAccess
	 * @param response
	 * @param ipAddress
	 * @param email
	 * @return
	 * @throws Exception
	 */
	private OutcomeResponseNextPageAndModel handleSamlUserWithDevice(HttpServletRequest request,
			HttpServletResponse httpResponse, IdentityObjectFromServer idObj, SamlAuthnRequestDbObj samlAuthnRequest,
			SamlDataAccess samlDataAccess, Response response, String ipAddress, String email, ModelMap model,
			String relayState, String samlResponse) throws Exception {
		GroupDbObj group = samlDataAccess.getGroupByEmail(email);
		String nextPage = null;
		int outcome = Outcomes.FAILURE;
		if (group != null) {
			if (!samlDataAccess.doesConnectionExistByEmail(email)) {
				samlDataAccess.addLog("user found, not yet activated", LogConstants.TRACE);
				outcome = Outcomes.SUCCESS;
			} else {
				// they are signed up
				samlDataAccess.addLog("user found, they are signed up", LogConstants.TRACE);
				DeviceDbObj device = idObj.getDevice();
				if (TextUtils.isBlank(samlAuthnRequest.getGroupId())) {
					samlAuthnRequest.setGroupId(group.getGroupId());
					samlDataAccess.updateSamlAuthRequestByRelayState(samlAuthnRequest);
				}
				if (!device.isBrowserInstallComplete() || device.isMultiUser()) {
					nextPage = "setupJavascript";
					outcome = Outcomes.NEEDS_SETUP;
					UrlAndModel urlAndModel = getSetupRedirect(model, outcome, email, samlDataAccess, idObj,
							samlAuthnRequest.getIncomingRequestId(), nextPage);
					model = urlAndModel.getModelMap();
				} else {
					if (samlDataAccess.isAccessAllowed(device, "handleSamlUserWithDevice")) {
						UrlModelAndHttpResponse successResponse = this.respondToIncomingRequest(request, httpResponse,
								model, idObj, samlAuthnRequest, samlDataAccess);
						nextPage = successResponse.getUrl();
						model = successResponse.getModelMap();
						httpResponse = successResponse.getHttpResponse();
						outcome = Outcomes.SUCCESS;
					} else {
						UrlAndModel urlAndModel = new Failure().checkForPushOrBiometrics(model, idObj, samlDataAccess);
						nextPage = urlAndModel.getUrl();
						model = urlAndModel.getModelMap();
						model.addAttribute("fromIdp", true);
						model.addAttribute("action", "/SAML2/SSO/" + idObj.getCompany().getApiKey() + "/fromIdp");
						model.addAttribute("relayState", relayState);
						model.addAttribute("samlText", samlResponse);
						outcome = Outcomes.GET_BIOMETRICS;
					}
				}
				RequestAndResponse reqRes = setPersistantToken(request, httpResponse, samlDataAccess, idObj);
				httpResponse = reqRes.getResponse();
			}
		} else {
			// go to a page that says B2F is not authorized for this user on this machine
			nextPage = "notSignedUp";
			samlDataAccess.addLog("email found, the user wasn't");
		}
		samlDataAccess.addLog("outcome: " + outcome + ", nextPage: " + nextPage);
		return new OutcomeResponseNextPageAndModel(outcome, httpResponse, nextPage, model);
	}

	private UrlModelAndHttpResponse respondToIncomingRequest(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, IdentityObjectFromServer idObj,
			SamlAuthnRequestDbObj samlAuthnRequest, SamlDataAccess samlDataAccess) {
		UrlModelAndHttpResponse response;
		if (!TextUtils.isEmpty(samlAuthnRequest.getIncomingRelayState())) {
			samlDataAccess.addLog("redirecting back to the SP", LogConstants.TRACE);
			UrlAndModel urlAndModel = this.respondToSp(model, samlAuthnRequest, idObj.getGroup(),
					idObj.getCompany().getApiKey(), idObj.getCompany().getEntityIdVal(), Outcomes.SUCCESS,
					samlDataAccess);
			response = new UrlModelAndHttpResponse(urlAndModel, httpResponse);
		} else {
			Failure failure = new Failure();
			String redirectUrl = failure.getReferrer(request, idObj.getCompany(), samlDataAccess);
			samlDataAccess.addLog("redirecting to: " + redirectUrl, LogConstants.TRACE);
			httpResponse.setHeader("Location", redirectUrl);
			httpResponse.setStatus(302);
			response = new UrlModelAndHttpResponse(null, model, httpResponse);
		}
		return response;
	}

	private OutcomeResponseNextPageAndModel handleLdapUserWithDevice(HttpServletRequest request,
			HttpServletResponse httpResponse, IdentityObjectFromServer idObj, SamlAuthnRequestDbObj samlAuthnRequest,
			CompanyDbObj company, SamlDataAccess samlDataAccess, String ipAddress, String email, ModelMap model)
			throws Exception {
		int outcome = Outcomes.API_F1_FAILURE;
		outcome = getOutcomeFromLdapResponse(samlAuthnRequest, idObj.getCompany(), samlDataAccess, email, ipAddress,
				idObj.getDevice().getSignedIn());
		GroupDbObj group = samlDataAccess.getGroupByEmail(email);
		samlDataAccess.addLog("handleUserWithDevice", "start");
		String nextPage = null;
		if (outcome == Outcomes.SUCCESS) {
			outcome = Outcomes.FAILURE;
			if (group != null) {
				// see if they are set up. if not, let them go
				// this may be necessary to access email to set up B2F
				// I don't think we'll ever come through here because device will be null
				if (!samlDataAccess.doesConnectionExistByEmail(email)) {
					samlDataAccess.addLog("handleUserWithDevice", "user found, not yet activated");
					outcome = Outcomes.SUCCESS;
				} else {
					// they are signed up
					samlDataAccess.addLog("handleUserWithDevice", "user found, we are setting them up");
					String accessCode = samlAuthnRequest.getToken();
					GroupDbObj groupFromCode = samlDataAccess.getGroupByAccessCode(accessCode);
					if (groupFromCode != null) {
						if (email.equals(groupFromCode.getGroupName())) {
							DeviceDbObj device = samlDataAccess.getDeviceByAccessCode(accessCode);
							if (device != null) {
								samlDataAccess.setSignedIn(device, true);
								samlDataAccess.addLog("handleUserWithDevice", "signed in is true");
								if (samlDataAccess.isAccessAllowed(device, "handleLdapUserWithDevice")) {

									outcome = Outcomes.SUCCESS;
									model = model.addAttribute("andVerify", false);
									samlDataAccess.addLog("handleUserWithDevice", "accessAllowed");
								} else {
									int checkCount = samlDataAccess.getCheckCount(device);
									if (checkCount == 0) {
										// this is the path we follow on the initial setup when we
										// cannot connect via BLE
										outcome = Outcomes.SUCCESS;
										model = model.addAttribute("andVerify", true);
									} else {
										model.addAttribute("environment", Constants.ENVIRONMENT.toString());
										model.addAttribute("fromIdp", true);
										Failure failure = new Failure();
										UrlAndModel urlAndModel = failure.checkForPushOrBiometrics(model, idObj,
												samlDataAccess);
										nextPage = urlAndModel.getUrl();
										model = urlAndModel.getModelMap();
										String accessKey = buildAccessCodeDbObj(idObj, samlDataAccess);
										String successPage = Urls.SECURE_URL + Urls.LDAP_SUBMIT + "?accessKey="
												+ accessKey;
										samlDataAccess.addLog("handleUserWithDevice", "successPage: " + successPage);
										failure.setTempSession(request, "src", successPage, 60 * 60 * 12);
									}
								}
								relayResponseToSamlAfterIdpOrLdap(request, httpResponse, model, company, group,
										samlAuthnRequest, samlDataAccess);
							} else {
								samlDataAccess.addLog("handleUserWithDevice", Constants.DEVICE_NOT_FOUND);
							}
						}
					} else {
						samlDataAccess.addLog("handleUserWithDevice", "group not found by access code");
					}
				}
			} else {
				// go to a page that says B2F is not authorized for this user on this machine
				samlDataAccess.addLog("handleUserWithDevice", "email found, the user wasn't");
			}
		}
		samlDataAccess.addLog("handleUserWithDevice", "outcome: " + outcome);
		return new OutcomeResponseNextPageAndModel(outcome, httpResponse, nextPage, model);
	}

	protected int validateIdpResponse(Response response, SamlAuthnRequestDbObj samlAuthnRequest, CompanyDbObj company,
			SamlDataAccess samlDataAccess, String ipAddress, boolean isSignedIn) {
		int outcome = Outcomes.FAILURE;
		SamlIdentityProviderDbObj samlIdp = samlDataAccess.getSamlIdpFromCompanyId(samlAuthnRequest.getCompanyId());
		if (samlAuthnRequest != null) {
			outcome = validateUser(response, samlAuthnRequest, company, samlDataAccess);
			if (outcome == Outcomes.SUCCESS) {
				outcome = validateIssuer(response, samlIdp);
				if (outcome == Outcomes.SUCCESS) {
					outcome = validateIssueInstance(response, samlAuthnRequest, isSignedIn);
					if (outcome == Outcomes.SUCCESS) {
						try {
							outcome = validateSignature(response, samlIdp, samlDataAccess);
							if (outcome == Outcomes.SUCCESS) {
								updateSignedIn(samlAuthnRequest, samlDataAccess);
								samlDataAccess.addLog("full steam ahead", LogConstants.TRACE);
							}
						} catch (Exception e) {
							samlDataAccess.addLog(e);
						}
					}
				}
			}
			samlAuthnRequest = samlDataAccess.updateSamlAuthnRequestOutcome(samlAuthnRequest, outcome, ipAddress);
		}
		return outcome;
	}

	private void updateSignedIn(SamlAuthnRequestDbObj samlAuthRequest, SamlDataAccess dataAccess) {
		String deviceId = samlAuthRequest.getDeviceId();
		if (deviceId != null) {
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "updateSignedIn");
			if (device != null) {
				dataAccess.setSignedIn(device, true);
			}
		}
	}

	/**
	 * called if we pass or fail
	 * 
	 * @param request
	 * @param httpResponse
	 * @param model
	 * @param relayState
	 * @param encryptedAuthnRequest
	 * @param apiKey
	 * @return
	 */
	private UrlAndModel relayResponseToSamlAfterIdpOrLdap(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, CompanyDbObj company, GroupDbObj group, SamlAuthnRequestDbObj samlAuthnRequest,
			SamlDataAccess dataAccess) {
		UrlAndModel urlAndModel = new UrlAndModel(null, model);
		int outcome = Outcomes.FAILURE;
		try {

			IdentityObjectFromServer idObj = this.getIdentityObjectFromCookie(request, company.getApiKey());
			if (idObj != null && idObj.getBrowser() != null && idObj.getDevice() != null
					&& idObj.getDevice().getSignedIn()) {
				if (dataAccess.isAccessAllowed(idObj.getDevice(), "relayResponseToSamlAfterIdpOrLdap")) {
					outcome = Outcomes.SUCCESS;
				}
				if (samlAuthnRequest != null) {
					urlAndModel = this.respondToSp(model, samlAuthnRequest, group, company.getApiKey(),
							company.getEntityIdVal(), outcome);
				}
			}
		} catch (Exception e) {

		}
		return urlAndModel;
	}

	protected int getOutcomeFromLdapResponse(SamlAuthnRequestDbObj samlAuthnRequest, CompanyDbObj company,
			SamlDataAccess samlDataAccess, String email, String ipAddress, boolean isSignedIn) {
		int outcome = Outcomes.API_F1_FAILURE;
		samlDataAccess.addLog("looking for the company");
		LdapServerDbObj ldapServer = samlDataAccess.getLdapServerFromCompany(company);
		if (ldapServer != null) {
			if (samlAuthnRequest != null) {
				if (validateUser(email, samlAuthnRequest, samlDataAccess)) {
					outcome = Outcomes.SUCCESS;
				} else {
					outcome = Outcomes.NEEDS_SETUP;
				}
				samlAuthnRequest = samlDataAccess.updateSamlAuthnRequestOutcome(samlAuthnRequest, outcome, ipAddress);
//						,ldapServer.getProviderUrl());
			}
		}
		return outcome;
	}

	private OutcomeAndResponseAndIdObj passThrough(HttpServletResponse httpResponse, CompanyDbObj company,
			SamlAuthnRequestDbObj samlAuthnRequest, SamlDataAccess dataAccess, String ipAddress, GroupDbObj group)
			throws Exception {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		if (group == null) {
			String groupId = GeneralUtilities.randomString();
			group = new GroupDbObj(company.getCompanyId(), groupId, Constants.ANONYMOUS_GROUP, 1, true, now, 5, null,
					null, 0, 0, 0, null, now, null, UserType.NONE, true, false, false);
			dataAccess.addGroup(group);
			dataAccess.addLog("group added");
		}
		IdentityObjectFromServer idObj = null;
		String accessCode = samlAuthnRequest.getToken();
		AccessCodeDbObj accessObj = dataAccess.getAccessCodeFromAccessString(accessCode);
		if (accessObj != null && accessObj.isActive()) {
			Timestamp baseTime = DateTimeUtilities.getBaseTimestamp();
			String userId = group.getGroupId();
			int seed = GeneralUtilities.randInt(0, 1000000000);
			DeviceDbObj tempDevice = new DeviceDbObj(group.getGroupId(), userId, GeneralUtilities.randomString(), seed,
					true, null, null, now, now, DeviceClass.UNKNOWN.toString(), OsClass.UNKNOWN, null, baseTime, 0,
					null, "", "", "", false, 651.0, false, 0, false, baseTime, false, false, "", "", false, now, false,
					baseTime, false, DeviceClass.TEMP, false, true, baseTime, null, false, false, null, false, false,
					null, false);

			dataAccess.addDevice(tempDevice);
			Timestamp fiveHours = DateTimeUtilities.getCurrentTimestampPlusSeconds(60 * 60 * 5);
			BrowserDbObj browser = dataAccess.addBrowser(tempDevice, GeneralUtilities.randomString(), "temp",
					fiveHours);
			dataAccess.addLog("device and browser Added");
			idObj = new IdentityObjectFromServer(company, group, tempDevice, browser, false);
		}
		return new OutcomeAndResponseAndIdObj(Outcomes.SUCCESS, httpResponse, idObj);
	}

	private OutcomeAndResponseAndIdObj handleOneTimeSamlIdpAccess(HttpServletResponse httpResponse,
			IdentityObjectFromServer idObj, SamlAuthnRequestDbObj samlAuthnRequest, SamlDataAccess dataAccess,
			Response response, String ipAddress, GroupDbObj group) throws Exception {
		int outcome = Outcomes.FAILURE;
		dataAccess.addLog("handleOneTimeAccess", "group exists");
		DeviceDbObj centralDevice = dataAccess.getCentralByGroupId(group.getGroupId());
		if (centralDevice != null) {
			String accessCode = samlAuthnRequest.getToken();
			AccessCodeDbObj accessObj = dataAccess.getAccessCodeFromAccessString(accessCode);
			if (accessObj != null && accessObj.isActive()) {
				Timestamp now = DateTimeUtilities.getCurrentTimestamp();
				Timestamp baseTime = DateTimeUtilities.getBaseTimestamp();
				DeviceDbObj tempDevice = idObj.getDevice();
				if (tempDevice == null) {
					String userId = group.getGroupId();
					int seed = GeneralUtilities.randInt(0, 1000000000);
					tempDevice = new DeviceDbObj(group.getGroupId(), userId, GeneralUtilities.randomString(), seed,
							true, null, null, now, now, DeviceClass.UNKNOWN.toString(), OsClass.UNKNOWN, null, baseTime,
							0, null, "", "", "", false, 651.0, false, 0, false, baseTime, false, false, "", "", false,
							now, false, baseTime, false, DeviceClass.TEMP, false, true, baseTime, null, false, false,
							null, false, false, null, false);

					dataAccess.addDevice(tempDevice);
				}
				Timestamp fiveHours = DateTimeUtilities.getCurrentTimestampPlusSeconds(60 * 60 * 5);
				BrowserDbObj browser = dataAccess.addBrowser(tempDevice, GeneralUtilities.randomString(), "temp",
						fiveHours);
				dataAccess.addLog("handleOneTimeAccess", "device and browser Added");
				idObj.setDevice(tempDevice);
				idObj.setBrowser(browser);
				DeviceConnectionDbObj connection = new DeviceConnectionDbObj(now, GeneralUtilities.randomString(),
						tempDevice.getDeviceId(), centralDevice.getDeviceId(), null, null, group.getGroupId(), true,
						baseTime, baseTime, false, baseTime, false, baseTime, baseTime, baseTime, false, baseTime, true,
						null, null, null, null, null, false, "");
				dataAccess.addLog("handleOneTimeAccess", "connectionAdded");
				dataAccess.addConnection(connection);
				outcome = Outcomes.SUCCESS;
			}
		}
		return new OutcomeAndResponseAndIdObj(outcome, httpResponse, idObj);
	}

	private OutcomeAndResponseAndIdObj handleOneTimeLdapIdpAccess(HttpServletResponse httpResponse,
			IdentityObjectFromServer idObj, CompanyDbObj company, SamlAuthnRequestDbObj samlAuthnRequest,
			SamlDataAccess dataAccess, String email, String ipAddress, GroupDbObj group) throws Exception {
		int outcome = Outcomes.API_F1_FAILURE;
		outcome = getOutcomeFromLdapResponse(samlAuthnRequest, company, dataAccess, email, ipAddress, false);
		if (outcome == Outcomes.SUCCESS || outcome == Outcomes.NEEDS_SETUP) {
			outcome = Outcomes.FAILURE;
			dataAccess.addLog("handleOneTimeAccess", "group exists");
			DeviceDbObj centralDevice = dataAccess.getCentralByGroupId(group.getGroupId());
			if (centralDevice != null) {
				String accessCode = samlAuthnRequest.getToken();
				AccessCodeDbObj accessObj = dataAccess.getAccessCodeFromAccessString(accessCode);
				if (accessObj != null && accessObj.isActive()) {
					// if (groupFromCode.getGroupId().equals(group.getGroupId())) {
					Timestamp now = DateTimeUtilities.getCurrentTimestamp();
					Timestamp baseTime = DateTimeUtilities.getBaseTimestamp();
					String userId = group.getGroupId();
					int seed = GeneralUtilities.randInt(0, 1000000000);
					DeviceDbObj tempDevice = new DeviceDbObj(group.getGroupId(), userId,
							GeneralUtilities.randomString(), seed, true, null, null, now, now,
							DeviceClass.UNKNOWN.toString(), OsClass.UNKNOWN, null, baseTime, 0, null, "", "", "", false,
							651.0, false, 0, false, baseTime, false, false, "", "", false, now, false, baseTime, false,
							DeviceClass.TEMP, false, true, baseTime, null, false, false, null, false, false, null,
							false);

					dataAccess.addDevice(tempDevice);
					Timestamp fiveHours = DateTimeUtilities.getCurrentTimestampPlusSeconds(60 * 60 * 5);
					BrowserDbObj browser = dataAccess.addBrowser(tempDevice, GeneralUtilities.randomString(), "temp",
							fiveHours);
					dataAccess.addLog("handleOneTimeAccess", "device and browser Added");
					idObj.setDevice(tempDevice);
					idObj.setBrowser(browser);
					idObj.setCompany(company);
					DeviceConnectionDbObj connection = new DeviceConnectionDbObj(now, GeneralUtilities.randomString(),
							tempDevice.getDeviceId(), centralDevice.getDeviceId(), null, null, group.getGroupId(), true,
							baseTime, baseTime, false, baseTime, false, baseTime, baseTime, baseTime, false, baseTime,
							true, null, null, null, null, null, false, "");

					dataAccess.addLog("handleOneTimeAccess", "connectionAdded");
					dataAccess.addConnection(connection);
					outcome = Outcomes.SUCCESS;
				}
			}
		}
		return new OutcomeAndResponseAndIdObj(outcome, httpResponse, idObj);
	}

	protected String getEmailFromResponse(Response response, SamlDataAccess dataAccess) {
		String email = null;
		try {
			List<Assertion> assertions = response.getAssertions();
			for (Assertion assertion : assertions) {
				try {
					NameID nameId = assertion.getSubject().getNameID();
					if (nameId.getFormat().equals(NameIdentifier.EMAIL)) {
						email = nameId.getValue();
						break;
					}
				} catch (Exception e1) {
					dataAccess.addLog("getEmailFromResponse", e1);
				}
			}
		} catch (Exception e) {
			dataAccess.addLog("getEmailFromResponse", e);
		}
		dataAccess.addLog("getEmailFromResponse", "email: " + email);
		return email;
	}

	protected String getUserFromResponse(Response response, SamlDataAccess dataAccess) {
		String user = null;
		try {
			List<Assertion> assertions = response.getAssertions();
			for (Assertion assertion : assertions) {
				try {
					NameID nameId = assertion.getSubject().getNameID();
					if (nameId.getFormat().equals(NameIdentifier.DEFAULT_ELEMENT_LOCAL_NAME)) {
						user = nameId.getValue();
						break;
					}
				} catch (Exception e1) {
					dataAccess.addLog(e1);
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		dataAccess.addLog("user: " + user);
		return user;
	}

	// accept so people can check email if they haven't signed up any devices yet
	private NextPageAndOutcome getAccessIfNeededToEmail(SamlAuthnRequestDbObj samlAuthnRequest, String email,
			SamlDataAccess dataAccess) {
		int outcome = Outcomes.API_F1_FAILURE;
		String nextPage = "needsResync";
		if (!TextUtils.isBlank(email)) {
			GroupDbObj group = dataAccess.getGroupByEmail(email);
			if (group != null) {
				ArrayList<DeviceDbObj> devices = dataAccess.getActiveDevicesFromGroup(group);
				if (devices.size() == 0) {
					if (dataAccess.isEmailIssuerForCompany(samlAuthnRequest.getIssuer(), group.getCompanyId())) {
						nextPage = null;
						outcome = Outcomes.SUCCESS;
					}
				}
			}

		}
		return new NextPageAndOutcome(nextPage, outcome);
	}

	@SuppressWarnings("unused")
	private HttpServletRequest setPreviousRequest(HttpServletRequest request, String relayState, String samlResponse) {
		String previousRequest = "RelayState=" + relayState + "&SamlResponse=" + samlResponse;
		return this.setSession(request, "src", previousRequest);
	}

	private HttpServletRequest setPreviousLdapRequest(HttpServletRequest request, String accessKey) {
		String previousRequest = "accessKey=" + accessKey;
		return this.setSession(request, "src", previousRequest);
	}

	private int validateUser(Response response, SamlAuthnRequestDbObj samlAuthnRequest, CompanyDbObj company,
			SamlDataAccess dataAccess) {
		int outcome = Outcomes.SAML_INVALID_USER;
		String email = getEmailFromResponse(response, dataAccess);
		GroupDbObj group = dataAccess.getGroupByEmail(email);
		String usernameWithCaps = null;
		if (group != null) {
			boolean update = false;
			if (TextUtils.isBlank(group.getUsername())) {
				// in case we added the group when we signed up the central
				String user = getUserFromResponse(response, dataAccess);
				if (user != null) {
					usernameWithCaps = user.substring(0, 1).toUpperCase() + user.substring(1);
					group.setUsername(usernameWithCaps);
					update = true;
				}
			}
			if (TextUtils.isBlank(group.getUid())) {
				String uid = GeneralUtilities.getUidFromEmail(email);
				group.setUid(uid);
				update = true;
			}
			if (update) {
				dataAccess.updateGroup(group);
			}
			dataAccess.addLog("group not was null.");
			outcome = Outcomes.SUCCESS;
		} else {
			dataAccess.addLog("group was null");
			NonMemberStrategy nms = company.getNonMemberStrategy();
			if (nms == NonMemberStrategy.ALLOW_NOT_SIGNED_UP
					|| nms == NonMemberStrategy.ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE) {
				outcome = Outcomes.SUCCESS;
			}
			if (company.isAllowAllFromIdp()) {
				Timestamp now = DateTimeUtilities.getCurrentTimestamp();
				String user = getUserFromResponse(response, dataAccess);
				String uid = GeneralUtilities.getUidFromEmail(email);
				if (user == null) {
					user = uid;
				}
				usernameWithCaps = user.substring(0, 1).toUpperCase() + user.substring(1);

				group = new GroupDbObj(company.getCompanyId(), GeneralUtilities.randomString(), email, 1, true, now, 5,
						null, null, 0, 0, 0, usernameWithCaps, now, uid, UserType.USER, false, company.isPushAllowed(),
						company.isTextAllowed());
				dataAccess.addGroup(group);
			}
		}
		dataAccess.addLog("success: " + (outcome == Outcomes.SUCCESS));
		return outcome;
	}

	private boolean validateUser(String email, SamlAuthnRequestDbObj samlAuthnRequest, SamlDataAccess dataAccess) {
		boolean success = false;
		GroupDbObj group = null;
		if (!TextUtils.isBlank(samlAuthnRequest.getGroupId())) {
			group = dataAccess.getGroupById(samlAuthnRequest.getGroupId());
		} else {
			if (!TextUtils.isBlank(samlAuthnRequest.getDeviceId())) {
				group = dataAccess.getGroupByDeviceId(samlAuthnRequest.getDeviceId());
			}
		}
		if (group != null) {
			dataAccess.addLog("group not was null.");
			if (email != null && isGroupNameEquivalent(group.getGroupName(), email)) {
				success = true;
			} else {
				if (email == null) {
					dataAccess.addLog("email was null in saml response");
				}
				dataAccess.addLog(
						"email from Saml(" + email + ") didn't match email from request(" + group.getGroupName() + ")");
			}
		} else {
			// we can come through here on one time access if the computer has never been
			// set up
			dataAccess.addLog("group was null");
		}
		dataAccess.addLog("success: " + success);
		return success;
	}

	protected int getOutcomeWithoutRequest(Response response, CompanyDbObj company, SamlDataAccess samlDataAccess,
			String ipAddress) {
		samlDataAccess.addLog("getOutcomeFromResponse", "looking for the company");
		SamlIdentityProviderDbObj samlIdp = samlDataAccess.getSamlIdpFromCompanyId(company.getCompanyId());
		int outcome = validateIssuer(response, samlIdp);
		if (outcome == Outcomes.SUCCESS) {
			try {
				outcome = validateSignature(response, samlIdp, samlDataAccess);
				if (outcome == Outcomes.SUCCESS) {
					samlDataAccess.addLog("getOutcomeFromResponse", "full steam ahead");
				}
			} catch (Exception e) {
				samlDataAccess.addLog("getOutcomeFromResponse", e);
			}
		}
		return outcome;
	}

	protected boolean validateStatus(Response response) {
		boolean success = false;
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.addLog("validateStatus", response.getStatus().getStatusCode().getValue());
			success = response.getStatus().getStatusCode().getValue().equals(StatusCode.SUCCESS);
		} catch (Exception e) {
			dataAccess.addLog("validateStatus", "bad status", LogConstants.WARNING);
		}
		dataAccess.addLog("validateStatus", "success: " + success);
		return success;
	}

	protected int validateIssuer(Response response, SamlIdentityProviderDbObj samlIdp) {
		int outcome = Outcomes.SAML_INVALID_ISSUER;
		String issuer = response.getIssuer().getValue();
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("validateIssuer", "does '" + issuer + "' == '" + samlIdp.getEntityId() + "'");
		if (issuer.equalsIgnoreCase(samlIdp.getEntityId())) {
			outcome = Outcomes.SUCCESS;
		}
		new DataAccess().addLog("validateIssuer", "success: " + (outcome == Outcomes.SUCCESS));
		return outcome;
	}

	protected int validateIssueInstance(Response response, SamlAuthnRequestDbObj samlAuthnRequest, boolean isSignedIn) {
		int outcome = Outcomes.SAML_TOO_OLD;
		Timestamp issueInstance = DateTimeUtilities.instantToTimestamp(response.getIssueInstant());
//        Timestamp issueInstance = new Timestamp(response.getIssueInstant().getMillis());
		Timestamp previousIssueInstance = samlAuthnRequest.getIssueInstance();
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		long timeDiff1 = DateTimeUtilities.absoluteTimestampDifferenceInSeconds(issueInstance, previousIssueInstance);
		long timeDiff2 = DateTimeUtilities.absoluteTimestampDifferenceInSeconds(issueInstance, now);
		long timeDiff3 = DateTimeUtilities.absoluteTimestampDifferenceInSeconds(now, previousIssueInstance);
		DataAccess dataAccess = new DataAccess();
		if (isSignedIn) {
			if (timeDiff1 < 600 && timeDiff2 < 600 && timeDiff3 < 600) {
				dataAccess.addLog("validateIssueInstance", "issueInstance validated");
				outcome = Outcomes.SUCCESS;
			} else {
				dataAccess.addLog("validateIssueInstance", "too fricken old");
			}
		} else {
			outcome = Outcomes.SUCCESS;
		}
		return outcome;
	}

	protected int validateSignature(Response response, SamlIdentityProviderDbObj samlIdp, SamlDataAccess dataAccess) {
		int outcome = Outcomes.SAML_INVALID_SIGNATURE;
		String certValue = samlIdp.getSigningCertificate();

		try {
			if (validateCertValue(response, certValue)) {
				dataAccess.addLog("certVal validated");
				Assertion assertion = response.getAssertions().get(0);
				String certificate = assertion.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates()
						.get(0).getValue();
				Saml saml = new Saml();
				BasicX509Credential credential = null;

				try {
					credential = saml.getCredentialFromCert(certificate);
				} catch (Exception e) {
					dataAccess.addLog(e);
					outcome = Outcomes.SAML_INVALID_CERT;
				}
				if (credential != null) {
					if (response.isSigned()) {
						dataAccess.addLog("response is signed");
						SignatureValidator.validate(response.getSignature(), credential);
						outcome = Outcomes.SUCCESS;
					}
					if (assertion.isSigned()) {

						SignatureValidator.validate(assertion.getSignature(), credential);
						outcome = Outcomes.SUCCESS;
					}
				} else {
					dataAccess.addLog("credential was null");
				}
				dataAccess.addLog("signature validated: " + (outcome == Outcomes.SUCCESS));
			} else {
				dataAccess.addLog("validateCertValue failed");
			}
		} catch (SignatureException se) {
			dataAccess.addLog(se);
			dataAccess.addLog("this is a major issue", LogConstants.FATAL);
			outcome = Outcomes.SUCCESS;
			// outcome = Outcomes.SAML_INVALID_SIGNATURE;
		}
		return outcome;
	}

	/**
	 * Make sure the cert value we received is the same as the one we have
	 * previously stored
	 * 
	 * @param response  from IDP
	 * @param certValue from the DB
	 * @return true if the certs match
	 */
	protected boolean validateCertValue(Response response, String certValue) {
		boolean certFound = false;
		certValue = certValue.replace("\n", "").replace("\r", "");
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.addLog("validateCertValue", "cert val: " + certValue);
			Signature signature = response.getSignature();
			List<X509Data> x509Datas = null;
			if (signature != null) {
				dataAccess.addLog("validateCertValue", "getting x509 list without assertion");
				x509Datas = response.getSignature().getKeyInfo().getX509Datas();
				dataAccess.addLog("validateCertValue", "signature found");
				certFound = doesCertMatch(x509Datas, certValue, dataAccess);
			} else {
				List<Assertion> assertions = response.getAssertions();
				dataAccess.addLog("validateCertValue", "getting x509 list from assertion");
				for (Assertion assertion : assertions) {
					x509Datas = assertion.getSignature().getKeyInfo().getX509Datas();
					certFound = doesCertMatch(x509Datas, certValue, dataAccess);
					if (certFound) {
						break;
					}
				}
			}
		} catch (Exception e) {
			new DataAccess().addLog(e);
		}
		return certFound;
	}

	protected boolean doesCertMatch(List<X509Data> x509Datas, String certValue, DataAccess dataAccess) {
		boolean certFound = false;
		for (X509Data x509Data : x509Datas) {
			List<X509Certificate> certificates = x509Data.getX509Certificates();
			int i = 0;
			for (X509Certificate x509Certificate : certificates) {
				String x509Value = x509Certificate.getValue().replace("\n", "").replace("\r", "");
				dataAccess.addLog("validateCertValue", "cert val: " + x509Value);
				if (certValue.equals(x509Value)) {
					dataAccess.addLog("validateCertValue", "certs equal when i = " + i);
					certFound = true;
					break;
				} else {
					dataAccess.addLog("validateCertValue", "certs not equal");
				}
				i++;
			}
			if (certFound) {
				break;
			}
		}
		return certFound;
	}
}

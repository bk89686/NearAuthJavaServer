package com.humansarehuman.blue2factor.utilities.saml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.saml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml.saml2.core.impl.LogoutRequestBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.saml.saml2.core.impl.ResponseBuilder;
import org.opensaml.saml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.saml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectConfirmationBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectConfirmationDataBuilder;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml.saml2.metadata.EmailAddress;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml.saml2.metadata.OrganizationName;
import org.opensaml.saml.saml2.metadata.OrganizationURL;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.ServiceName;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.SurName;
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.opensaml.saml.saml2.metadata.impl.ContactPersonBuilder;
import org.opensaml.saml.saml2.metadata.impl.EmailAddressBuilder;
import org.opensaml.saml.saml2.metadata.impl.EntityDescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.IDPSSODescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.KeyDescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.NameIDFormatBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationDisplayNameBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationNameBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationURLBuilder;
import org.opensaml.saml.saml2.metadata.impl.SPSSODescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.ServiceNameBuilder;
import org.opensaml.saml.saml2.metadata.impl.SingleLogoutServiceBuilder;
import org.opensaml.saml.saml2.metadata.impl.SingleSignOnServiceBuilder;
import org.opensaml.saml.saml2.metadata.impl.SurNameBuilder;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.impl.KeyInfoBuilder;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.impl.X509CertificateBuilder;
import org.opensaml.xmlsec.signature.impl.X509DataBuilder;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.enums.SignatureStatus;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.LdapServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlAuthnRequestDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlIdentityProviderDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlServiceProviderDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

import net.shibboleth.utilities.java.support.xml.BasicParserPool;

public class Saml extends BaseController {

	public static final String IDP_ENTITY_ID = Urls.SECURE_URL + Urls.SAML_ENTITY_ID;
	private static final String SP_URL = Urls.SECURE_URL + Urls.SAML_RESPONSE_FROM_IDENTITY_PROVIDER;
	private static final String IDP_URL = Urls.SECURE_URL + Urls.COMPANY_VALIDATE;
	public static final String LOGOUT_URL = Urls.SECURE_URL + Urls.COMPANY_SIGNOUT;

	private String samlResponseAsString = null;
	private Response samlResponse = null;
	private boolean samlSuccess = false;
	private String action = null;

	public String getAction() {
		return action;
	}

	public Response getSamlResponse() {
		return samlResponse;
	}

	public String getSamlResponseAsString() {
		return samlResponseAsString;
	}

	public boolean isSamlSuccess() {
		return samlSuccess;
	}

	private void addSubjectToAssertion(Assertion assertion, String issuer, String subjectAddress, String recipient,
			String spNameQualifier, String inResponseTo, String email) throws SecurityException {
		/* Create and add subject to assertion */
		DataAccess dataAccess = new DataAccess();
		NameIDBuilder nameIdBuilder = new NameIDBuilder();
		NameID nameId = nameIdBuilder.buildObject();
		nameId.setValue(email);
		// this is optional, we may want to add it in later
		// nameId.setSPNameQualifier(spNameQualifier);
		nameId.setFormat(NameIDType.EMAIL);
		dataAccess.addLog("addSubjectToAssertion", email);
		SubjectConfirmationDataBuilder subjectConfirmationDataBuilder = new SubjectConfirmationDataBuilder();
		SubjectConfirmationData subjectConfirmationData = subjectConfirmationDataBuilder.buildObject();
		subjectConfirmationData.setRecipient(recipient);
		Instant dateTime = Instant.now();
		Instant afterTime = dateTime.plus(5, ChronoUnit.MINUTES);
		subjectConfirmationData.setNotOnOrAfter(afterTime);
		subjectConfirmationData.setInResponseTo(inResponseTo);

		SubjectConfirmationBuilder subjectConfirmationBuilder = new SubjectConfirmationBuilder();

		SubjectConfirmation subjectConfirmation = subjectConfirmationBuilder.buildObject();
		subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);

		subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

		SubjectBuilder subjectBuilder = new SubjectBuilder();
		Subject subject = subjectBuilder.buildObject();
		subject.setNameID(nameId);
		subject.getSubjectConfirmations().add(subjectConfirmation);
		assertion.setSubject(subject);
	}

	private Status getStatus(boolean success) {
		StatusBuilder statusBuilder = new StatusBuilder();
		Status status = statusBuilder.buildObject();
		StatusCodeBuilder statusCodeBuilder = new StatusCodeBuilder();

		StatusCode statusCode = statusCodeBuilder.buildObject();
		if (success) {
			statusCode.setValue(StatusCode.SUCCESS);
		} else {
			statusCode.setValue(StatusCode.AUTHN_FAILED);
		}
		status.setStatusCode(statusCode);
		return status;
	}

	private void addConditionsToAssertion(Assertion assertion, String issuer) {
		Instant dateTime = Instant.now();

		ConditionsBuilder conditionsBuider = new ConditionsBuilder();
		Conditions conditions = conditionsBuider.buildObject();

		Instant aftersserTime = dateTime.plus(10, ChronoUnit.MINUTES);
		conditions.setNotOnOrAfter(aftersserTime);
		conditions.setNotBefore(dateTime);

		AudienceRestrictionBuilder audienceRestrictionBuilder = new AudienceRestrictionBuilder();
		AudienceRestriction audienceRestriction = audienceRestrictionBuilder.buildObject();

		AudienceBuilder audienceBuilder = new AudienceBuilder();
		Audience audience = audienceBuilder.buildObject();
		audience.setURI(issuer);

		audienceRestriction.getAudiences().add(audience);

		conditions.getAudienceRestrictions().add(audienceRestriction);
		assertion.setConditions(conditions);
	}

	private void addAuthenticationStatement(Assertion assertion, String sessionIndex) {

		AuthnStatementBuilder authnStatementBuilder = new AuthnStatementBuilder();
		AuthnStatement authnStatement = authnStatementBuilder.buildObject();

		Instant authTime = Instant.now();
		authnStatement.setAuthnInstant(authTime);
		authnStatement.setSessionNotOnOrAfter(authTime.plus(7, ChronoUnit.HOURS));

		authnStatement.setSessionIndex(sessionIndex);
		AuthnContextBuilder authnContextBuilder = new AuthnContextBuilder();
		AuthnContext authnContext = authnContextBuilder.buildObject();
		AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
		AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder.buildObject();
		authnContextClassRef.setURI(AuthnContext.PPT_AUTHN_CTX);
		authnContext.setAuthnContextClassRef(authnContextClassRef);

		authnStatement.setAuthnContext(authnContext);

		assertion.getAuthnStatements().add(authnStatement);
	}

	private X509Certificate convertToX509Certificate(String certificateString) throws Exception {
		X509Certificate certificate = null;
		CertificateFactory cf = null;
		if (certificateString != null && !certificateString.trim().isEmpty()) {
			certificateString = certificateString.replace("-----BEGIN CERTIFICATE-----\n", "")
					.replace("-----END CERTIFICATE-----", "");
			certificateString = certificateString.replaceAll("\\s", "");
			byte[] certificateData = Base64.getDecoder().decode(certificateString);
			cf = CertificateFactory.getInstance("X509");
			certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
		}
		return certificate;
	}

	private Signature getSignature(String x509Cert, String privateKey) throws Exception {
		DataAccess dataAccess = new DataAccess();
		PrivateKey pk = new Encryption().stringToPrivateKey(privateKey);
		Signature signature = null;

		X509Certificate certificate = convertToX509Certificate(x509Cert);
		try {
			certificate.checkValidity();
			dataAccess.addLog("cert was valid");
			BasicX509Credential signingCredential = new BasicX509Credential(certificate, pk);

			SignatureBuilder signatureBuilder = new SignatureBuilder();
			signature = signatureBuilder.buildObject();

			signature.setSigningCredential(signingCredential);
			signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
			signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

			/* The KeyInfo is optional - not sure if we want to add it */
			signature.setKeyInfo(getKeyInfo(x509Cert));

		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return signature;
	}

	public BasicX509Credential getCredentialFromCert(String x509Cert) throws Exception {
		X509Certificate certificate = convertToX509Certificate(x509Cert);
		return new BasicX509Credential(certificate);
	}

	private Assertion addSignatureToAssertion(Assertion assertion, String x509Cert, String privateKey)
			throws Exception {
		Signature signature = getSignature(x509Cert, privateKey);
		assertion.setSignature(signature);
		assertion.setNil(false);
		DataAccess dataAccess = new DataAccess();
		try {

			InitializationService.initialize();
			MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
			Marshaller marshaller = marshallerFactory.getMarshaller(assertion);

			marshaller.marshall(assertion);
			Signer.signObject(signature);
			dataAccess.addLog("assertion signed: " + signature.getCanonicalizationAlgorithm());
		} catch (MarshallingException e) {
			dataAccess.addLog("addSignatureToAssertion", e);
		}
		validateAssertionAsCheck(signature, x509Cert);
		return assertion;
	}

	private void validateAssertionAsCheck(Signature signature, String x509Cert) {
		DataAccess dataAccess = new DataAccess();
		try {
//            SAMLSignatureProfileValidator samlSignatureProfileValidator = new SAMLSignatureProfileValidator();
//            samlSignatureProfileValidator.validate(signature);
			X509Certificate x509Certificate = convertToX509Certificate(x509Cert);
			BasicX509Credential signingCredential = new BasicX509Credential(x509Certificate);
			SignatureValidator.validate(signature, signingCredential);
			dataAccess.addLog("validated: " + signature);
			dataAccess.addLog("with: " + signingCredential);
			dataAccess.addLog("signature validated");
			// end check

		} catch (Exception se) {
			dataAccess.addLog(se);
		}
	}

	private boolean validateSignature(Signature signature, String x509Cert) {
		DataAccess dataAccess = new DataAccess();
		boolean validated = false;
		try {
			SAMLSignatureProfileValidator samlSignatureProfileValidator = new SAMLSignatureProfileValidator();
			samlSignatureProfileValidator.validate(signature);
			X509Certificate x509Certificate = convertToX509Certificate(x509Cert);
			BasicX509Credential signingCredential = new BasicX509Credential(x509Certificate);
			SignatureValidator.validate(signature, signingCredential);
			dataAccess.addLog("signature validated");
			validated = true;

		} catch (Exception se) {
			dataAccess.addLog(se);
		}
		return validated;
	}

	/*
	 * InitializationService.initialize(); MarshallerFactory marshallFactory =
	 * XMLObjectProviderRegistrySupport .getMarshallerFactory(); Marshaller
	 * marshaller = marshallFactory.getMarshaller(entityDescriptor); plain =
	 * marshaller.marshall(entityDescriptor);
	 */

	private void addAttributeStatementToAssertion(Assertion assertion, Map<String, String> attributeValues) {
		AttributeStatement attStmt = null;
		if (attributeValues != null) {
			attStmt = new AttributeStatementBuilder().buildObject();
			Iterator<String> ite = attributeValues.keySet().iterator();
			Attribute attrib;
			for (int i = 0; i < attributeValues.size(); i++) {
				attrib = new AttributeBuilder().buildObject();
				String attributeName = ite.next();
				attrib.setName(attributeName);
				attrib.setNameFormat(Attribute.BASIC);
				XSStringBuilder xsStringBuilder = new XSStringBuilder();
				XSString stringValue = xsStringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
						XSString.TYPE_NAME);
				stringValue.setValue(attributeValues.get(attributeName));
				attrib.getAttributeValues().add(stringValue);
				attStmt.getAttributes().add(attrib);
			}
		}
		if (attStmt != null) {
			assertion.getAttributeStatements().add(attStmt);
		}
	}

	public Saml buildFailedResponse() {
		try {
			buildResponseFromComponents(null, null, null, null, null, null, null, null, null, null, null, null, false);
		} catch (Exception e) {
			new DataAccess().addLog("buildFailedResponse", e);
		}
		return this;
	}

	boolean testing = false;

	public static void main(String[] args) {
		try {
			Saml saml = new Saml();
			saml.testing = true;
//            saml.buildResponseFromComponents("google.com",
//                    "https://www.google.com/a/blue2factor.com/acs",
//                    "ilobooomkidmdjdoijeoghmekpanibfeknfdkpjd", "1.1.1.1",
//                    "chris@humansarehuman.com", "chris", "google.com/audience", saml.getIdpCert(),
//                    saml.getIdpPrivateKey(), "", true);
		} catch (Exception e) {
			new DataAccess().addLog("Saml.main", e);
		}
	}

//	public String buildResponseFromIncomingSamlRecord(SamlAuthnRequestDbObj incomingSaml, GroupDbObj group,
//			String apiKey, boolean success) {
//		DataAccess dataAccess = new DataAccess();
//		return this.buildResponseFromIncomingSamlRecord(incomingSaml, group, apiKey, success, dataAccess);
//	}

	public String buildResponseFromIncomingSamlRecord(SamlAuthnRequestDbObj incomingSaml, GroupDbObj group,
			String apiKey, String entityIdVal, boolean success, DataAccess dataAccess) {
		String result;
		try {
			String uid = "";
			if (group != null) {
				uid = group.getUid();
			}
			KeyDbObj coIdpPrivate = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_PRIVATE,
					group.getCompanyId());
			KeyDbObj coIdpCert = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_CERT, group.getCompanyId());
			result = buildResponseFromComponents(incomingSaml.getIncomingAcsUrl(), incomingSaml.getIncomingRequestId(),
					incomingSaml.getIpAddress(), group.getGroupName(), uid, incomingSaml.getSender(),
					incomingSaml.getIssuer(), coIdpCert.getKeyText(), coIdpPrivate.getKeyText(), apiKey, entityIdVal,
					incomingSaml.getIncomingAcsUrl(), success);
			dataAccess.addLog("buildResponseFromIncomingSamlRecord", "samlResponse: " + result);
		} catch (Exception e) {
			result = "ERROR";
			dataAccess.addLog("buildResponseFromIncomingSamlRecord", e);
		}
		return result;
	}

//	public String buildResponseFromIncomingSamlRecord(AuthnRequest incomingSaml, GroupDbObj group, CompanyDbObj company,
//			boolean success, String ipAddress) {
//		String result;
//		DataAccess dataAccess = new DataAccess();
//		try {
//			String uid = "";
//			if (group != null) {
//				uid = group.getUid();
//			}
//			KeyDbObj coIdpPrivate = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_PRIVATE,
//					group.getCompanyId());
//			String incomingId = incomingSaml.getID();
//			KeyDbObj coIdpCert = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_CERT, group.getCompanyId());
//			String acsUrl = incomingSaml.getAssertionConsumerServiceURL();
//			result = buildResponseFromComponents(acsUrl, incomingId, ipAddress, group.getGroupName(), uid,
//					incomingSaml.getID(), incomingSaml.getIssuer().getValue(), coIdpCert.getKeyText(),
//					coIdpPrivate.getKeyText(), company.getApiKey(), success);
//			dataAccess.addLog("buildResponseFromIncomingSamlRecord", "samlResponse: " + result);
//		} catch (Exception e) {
//			result = "ERROR";
//			dataAccess.addLog("buildResponseFromIncomingSamlRecord", e);
//		}
//		return result;
//	}

	private String buildResponseFromComponents(String acsUrl, String incomingId, String ipAddress, String email,
			String uid, String spNameQualifier, String issuer, String x509Cert, String privateKey, String apiKey,
			String entityIdVal, String incomingAcsUrl, boolean success) throws Exception {
		DataAccess dataAccess = new DataAccess();
		samlSuccess = success;
		if (email.equalsIgnoreCase("chris@blue2factor.com") && incomingAcsUrl != null
				&& incomingAcsUrl.contains("google")) {
			email = "chris@humansarehuman.com";
		}
		AssertionBuilder assertionBuilder = new AssertionBuilder();
		Assertion assertion = assertionBuilder.buildObject();
		String ourIssuerStr = Urls.SECURE_URL + Urls.SAML_ENTITY_ID.replace("{apiKey}", apiKey);
		if (success) {
			assertion.setID("_" + GeneralUtilities.randomString(42));
			assertion.setVersion(SAMLVersion.VERSION_20);
			assertion.setIssueInstant(Instant.now());
//			String entityId = Urls.SECURE_URL + Urls.SAML_ENTITY_ID.replace("{apiKey}", entityIdVal);

			addSubjectToAssertion(assertion, ourIssuerStr, ipAddress, acsUrl, spNameQualifier, incomingId, email);
			addConditionsToAssertion(assertion, ourIssuerStr);
			addAuthenticationStatement(assertion, "_" + GeneralUtilities.randomString(42));
			assertion.setIssuer(getIssuer(ourIssuerStr));
			HashMap<String, String> attributesMap = new HashMap<>();
			attributesMap.put("mail", email);
			attributesMap.put("uid", uid);
			addAttributeStatementToAssertion(assertion, attributesMap);
		}

		/* Create Response object */
		ResponseBuilder responseBuilder = new ResponseBuilder();
		Response response = responseBuilder.buildObject();

		response.setIssueInstant(Instant.now());
		// resp.set
		response.setStatus(getStatus(success));
//		response.setIssuer(getIssuer(Urls.SECURE_URL + Urls.SAML_ENTITY_ID.replace("{apiKey}", entityIdVal)));
		response.setIssuer(getIssuer(ourIssuerStr));
		response.setInResponseTo(incomingId);
		response.setDestination(acsUrl);
		action = acsUrl;
		dataAccess.addLog("buildResponseFromComponents", "action: " + action);
		response.setID("_" + GeneralUtilities.randomString(42).toLowerCase());

		if (success) {
			dataAccess.addLog("buildResponseFromComponents", x509Cert);
			assertion = addSignatureToAssertion(assertion, x509Cert, privateKey);
			response.getAssertions().add(assertion);
		}

		ResponseMarshaller marshaller = new ResponseMarshaller();
		Element plain = marshaller.marshall(response);

		samlResponseAsString = removeHeader(nodeToString(plain));
		samlResponse = response;

		dataAccess.addLog("buildResponseFromComponents", samlResponseAsString);
		return samlResponseAsString;
	}

	public String buildIdpEntityDescriptor(CompanyDbObj company) throws Exception {
		DataAccess dataAccess = new DataAccess();
		EntityDescriptorBuilder entityDescriptorBuilder = new EntityDescriptorBuilder();
		dataAccess.addLog("begin");
		EntityDescriptor entityDescriptor = entityDescriptorBuilder.buildObject();
		entityDescriptor.setEntityID(IDP_ENTITY_ID.replace("{apiKey}", company.getApiKey()));
		entityDescriptor.setCacheDuration(Duration.ofDays(7)); // a week
		entityDescriptor.setValidUntil(Instant.now().plus(1, ChronoUnit.DAYS));

		KeyDbObj coIdpPrivate = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_PRIVATE, company.getCompanyId());
		dataAccess.addLog("coIdpPrivateKey found: " + (coIdpPrivate != null));
		KeyDbObj coIdpCert = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_CERT, company.getCompanyId());
		dataAccess.addLog("coIdpCert found: " + (coIdpCert != null));
		Signature signature = getSignature(coIdpCert.getKeyText(), coIdpPrivate.getKeyText());
		entityDescriptor.getRoleDescriptors().add(buildIdpSsoDescriptor(company, coIdpCert.getKeyText()));
		entityDescriptor.setOrganization(buildOrganization());
		entityDescriptor.getContactPersons().add(buildContactPerson());
		entityDescriptor.setSignature(signature);
		Element plain;
		try {
			InitializationService.initialize();
			MarshallerFactory marshallFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
			Marshaller marshaller = marshallFactory.getMarshaller(entityDescriptor);
			plain = marshaller.marshall(entityDescriptor);
		} catch (Exception e) {
			ResponseMarshaller marshaller = new ResponseMarshaller();
			plain = marshaller.marshall(entityDescriptor);
		}
		Signer.signObject(signature);
		String entityDescriptorString = removeHeader(nodeToString(plain));
		dataAccess.addLog(entityDescriptorString.replace("><", ">\r\n<"));
		return entityDescriptorString;
	}

	private Organization buildOrganization() {
		OrganizationBuilder organizationBuilder = new OrganizationBuilder();
		Organization organization = organizationBuilder.buildObject();

		OrganizationNameBuilder organizationNameBuilder = new OrganizationNameBuilder();
		OrganizationName organizationName = organizationNameBuilder.buildObject();
		organizationName.setValue(Constants.APP_NAME);
		organizationName.setXMLLang("en-US");
		OrganizationDisplayNameBuilder organizationDisplayNameBuilder = new OrganizationDisplayNameBuilder();
		OrganizationDisplayName organizationDisplayName = organizationDisplayNameBuilder.buildObject();
		organizationDisplayName.setXMLLang("en-US");
		organizationDisplayName.setValue(Constants.APP_NAME);
		OrganizationURLBuilder organizationUrlBuilder = new OrganizationURLBuilder();
		OrganizationURL organizationUrl = organizationUrlBuilder.buildObject();
		organizationUrl.setXMLLang("en-US");
		organizationUrl.setURI(Urls.MAIN_URL);

		organization.getOrganizationNames().add(organizationName);
		organization.getDisplayNames().add(organizationDisplayName);
		organization.getURLs().add(organizationUrl);
		return organization;
	}

	private ContactPerson buildContactPerson() {
		ContactPersonBuilder contactPersonBuilder = new ContactPersonBuilder();
		ContactPerson contactPerson = contactPersonBuilder.buildObject();
		contactPerson.setType(ContactPersonTypeEnumeration.TECHNICAL);
		SurNameBuilder surNameBuilder = new SurNameBuilder();
		SurName surName = surNameBuilder.buildObject();
		surName.setValue("SAML Support");
		contactPerson.setSurName(surName);
		EmailAddressBuilder emailAddressBuilder = new EmailAddressBuilder();
		EmailAddress emailAddress = emailAddressBuilder.buildObject();
		emailAddress.setURI("saml-support@nearauth.ai");
		contactPerson.getEmailAddresses().add(emailAddress);
		return contactPerson;
	}

	private SPSSODescriptor buildSpSsoDescriptor(CompanyDbObj company) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		SPSSODescriptorBuilder spSsoDescriptorBuilder = new SPSSODescriptorBuilder();
		SPSSODescriptor spSsoDescriptor = spSsoDescriptorBuilder.buildObject();
		spSsoDescriptor.setAuthnRequestsSigned(false);
		spSsoDescriptor.setWantAssertionsSigned(true);
		spSsoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
		KeyDescriptorBuilder keyDescriptorBuilder = new KeyDescriptorBuilder();
		KeyDescriptor keyDescriptor1 = keyDescriptorBuilder.buildObject();
		keyDescriptor1.setUse(UsageType.SIGNING);
		KeyDbObj certDbObj = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.SP_CERT, company.getCompanyId());
		KeyInfo keyInfo = getKeyInfo(certDbObj.getKeyText());
		keyDescriptor1.setKeyInfo(keyInfo);
//        keyDescriptor2.setKeyInfo(getKeyInfo(false));
		spSsoDescriptor.getKeyDescriptors().add(keyDescriptor1);
//        spSsoDescriptor.getKeyDescriptors().add(keyDescriptor2);

		NameIDFormatBuilder nameIdFormatBuilder = new NameIDFormatBuilder();
		NameIDFormat nameIdFormat1 = nameIdFormatBuilder.buildObject();
		nameIdFormat1.setURI(NameID.TRANSIENT);
		NameIDFormat nameIdFormat2 = nameIdFormatBuilder.buildObject();
		nameIdFormat2.setURI(NameID.UNSPECIFIED);
		spSsoDescriptor.getNameIDFormats().add(nameIdFormat1);
		spSsoDescriptor.getNameIDFormats().add(nameIdFormat1);

		AssertionConsumerServiceBuilder assertionConsumerServiceBuilder = new AssertionConsumerServiceBuilder();

		AssertionConsumerService assertionConsumerService2 = assertionConsumerServiceBuilder.buildObject();
		assertionConsumerService2.setIsDefault(true);
		assertionConsumerService2.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		assertionConsumerService2.setLocation(SP_URL.replace("{apiKey}", company.getApiKey()));
		assertionConsumerService2.setIndex(1);

		spSsoDescriptor.getAssertionConsumerServices().add(assertionConsumerService2);
		SingleLogoutServiceBuilder singleLogoutServiceBuilder = new SingleLogoutServiceBuilder();
		SingleLogoutService singleLogoutService = singleLogoutServiceBuilder.buildObject();
		singleLogoutService.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
		singleLogoutService.setLocation(LOGOUT_URL);
		spSsoDescriptor.getSingleLogoutServices().add(singleLogoutService);

		ServiceNameBuilder serviceNameBuilder = new ServiceNameBuilder();
		ServiceName serviceName = serviceNameBuilder.buildObject();
		serviceName.setXMLLang("en-US");
		serviceName.setValue("NearAuth.ai");

		return spSsoDescriptor;
	}

	private IDPSSODescriptor buildIdpSsoDescriptor(CompanyDbObj company, String idpCert) {
		IDPSSODescriptorBuilder idpSsoDescriptorBuilder = new IDPSSODescriptorBuilder();
		IDPSSODescriptor idpSsoDescriptor = idpSsoDescriptorBuilder.buildObject();
		idpSsoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
		idpSsoDescriptor.setWantAuthnRequestsSigned(false);
		KeyDescriptorBuilder keyDescriptorBuilder = new KeyDescriptorBuilder();
		KeyDescriptor keyDescriptor1 = keyDescriptorBuilder.buildObject();
		keyDescriptor1.setUse(UsageType.SIGNING);
		KeyDescriptor keyDescriptor2 = keyDescriptorBuilder.buildObject();
		KeyInfo keyInfo = getKeyInfo(idpCert);
		KeyInfo keyInfo2 = getKeyInfo(idpCert);
		keyDescriptor2.setUse(UsageType.ENCRYPTION);
		keyDescriptor1.setKeyInfo(keyInfo);
		keyDescriptor2.setKeyInfo(keyInfo2);

		idpSsoDescriptor.getKeyDescriptors().add(keyDescriptor1);
		idpSsoDescriptor.getKeyDescriptors().add(keyDescriptor2);

		NameIDFormatBuilder nameIdFormatBuilder = new NameIDFormatBuilder();
		NameIDFormat nameIdFormat1 = nameIdFormatBuilder.buildObject();
		nameIdFormat1.setURI(NameID.TRANSIENT);
		NameIDFormat nameIdFormat2 = nameIdFormatBuilder.buildObject();
		nameIdFormat2.setURI(NameID.EMAIL);

		idpSsoDescriptor.getNameIDFormats().add(nameIdFormat1);
		idpSsoDescriptor.getNameIDFormats().add(nameIdFormat2);
		SingleLogoutServiceBuilder singleLogoutServiceBuilder = new SingleLogoutServiceBuilder();
		SingleLogoutService singleLogoutService = singleLogoutServiceBuilder.buildObject();
		singleLogoutService.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
		singleLogoutService.setLocation(LOGOUT_URL);
		idpSsoDescriptor.getSingleLogoutServices().add(singleLogoutService);

		SingleSignOnServiceBuilder singleSignOnServiceBuilder = new SingleSignOnServiceBuilder();
		SingleSignOnService singleSignOnService1 = singleSignOnServiceBuilder.buildObject();
		singleSignOnService1.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
		singleSignOnService1.setLocation(IDP_URL.replace("{apiKey}", company.getApiKey()));

		SingleSignOnService singleSignOnService2 = singleSignOnServiceBuilder.buildObject();
		singleSignOnService2.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		singleSignOnService2.setLocation(IDP_URL.replace("{apiKey}", company.getApiKey()));

		idpSsoDescriptor.getSingleSignOnServices().add(singleSignOnService1);
		idpSsoDescriptor.getSingleSignOnServices().add(singleSignOnService2);
		return idpSsoDescriptor;

	}

	public String buildSpEntityDescriptor(String companyApi) throws Exception {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		EntityDescriptorBuilder entityDescriptorBuilder = new EntityDescriptorBuilder();
		EntityDescriptor entityDescriptor = entityDescriptorBuilder.buildObject();
		CompanyDbObj company = dataAccess.getCompanyByApiKey(companyApi);
		entityDescriptor.setEntityID(Urls.SECURE_URL + Urls.SAML_ENTITY_ID.replace("{apiKey}", company.getApiKey()));
		entityDescriptor.setCacheDuration(Duration.ofDays(7)); // a week
		entityDescriptor.setValidUntil(Instant.now().plus(1, ChronoUnit.DAYS));

		entityDescriptor.getRoleDescriptors().add(buildSpSsoDescriptor(company));
		entityDescriptor.setOrganization(buildOrganization());
		entityDescriptor.getContactPersons().add(buildContactPerson());

		KeyDbObj coIdpPrivate = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_PRIVATE, company.getCompanyId());
		KeyDbObj coIdpCert = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_CERT, company.getCompanyId());
		Signature signature = getSignature(coIdpCert.getKeyText(), coIdpPrivate.getKeyText());
		entityDescriptor.setSignature(signature);
		Element plain;
		String entityDescriptorString = null;

		try {
			InitializationService.initialize();
			MarshallerFactory marshallFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
			Marshaller marshaller = marshallFactory.getMarshaller(entityDescriptor);
			plain = marshaller.marshall(entityDescriptor);
			Signer.signObject(signature);
			entityDescriptorString = removeHeader(nodeToString(plain));
		} catch (Exception e) {
			dataAccess.addLog("buildSpEntityDescriptor", e);
			entityDescriptorString = "";
		}
		dataAccess.addLog("buildSpEntityDescriptor", entityDescriptorString.replace("><", ">\r\n<"));
		return entityDescriptorString;

	}

	public SamlAuthnRequestDbObj buildAndSaveAuthnRequestObj(CompanyDbObj company, AccessCodeDbObj access,
			SamlIdentityProviderDbObj samlIdp, String authId, String relayState, String sender,
			String incomingRequestId) {
		SamlAuthnRequestDbObj samlAuthnRequest = this.buildAndSaveAuthnRequestObj(company, access, "", samlIdp, authId,
				relayState, sender, incomingRequestId);
		SamlDataAccess dataAccess = new SamlDataAccess();
		dataAccess.addLog("buildAndSaveAuthnRequestObj", "after addSamlAuthnRequest");
		return samlAuthnRequest;
	}

	public SamlAuthnRequestDbObj buildAndSaveAuthnRequestObjForLdap(CompanyDbObj company, AccessCodeDbObj access,
			LdapServerDbObj ldapServer, String authId, String relayState, String sender, String incomingRequestId) {
		SamlAuthnRequestDbObj samlAuthnRequest = this.buildAndSaveAuthnRequestObjForLdap(company, access, "",
				ldapServer, authId, relayState, sender, incomingRequestId);
		return samlAuthnRequest;

	}

	private SamlAuthnRequestDbObj buildAndSaveAuthnRequestObj(CompanyDbObj company, AccessCodeDbObj access,
			String encodedAuthnRequest, SamlIdentityProviderDbObj samlIdp, String authId, String outgoingRelayState,
			String sender, String incomingId) {
		SamlAuthnRequestDbObj samlAuthnRequest = null;
		SamlDataAccess dataAccess = new SamlDataAccess();
		if (access != null) {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			samlAuthnRequest = new SamlAuthnRequestDbObj(samlIdp.getIdentityProviderName(), now, samlIdp.getEntityId(),
					authId, incomingId, outgoingRelayState, null, now, null,
					IDP_URL.replace("{apiKey}", company.getApiKey()), null, SignatureStatus.UNVALIDATE,
					encodedAuthnRequest, null, false, company.getCompanyId(), "", access.getDeviceId(),
					access.getAccessCode(), Outcomes.UNKNOWN_STATUS, sender);

			dataAccess.addSamlAuthnRequest(samlAuthnRequest);
			dataAccess.addLog("buildAndSaveAuthnRequestObj", "(1) after addSamlAuthnRequest at " + now);
		} else {
			dataAccess.addLog("buildAndSaveAuthnRequestObj", "accessCode was null", LogConstants.ERROR);
		}
		return samlAuthnRequest;
	}

	private SamlAuthnRequestDbObj buildAndSaveAuthnRequestObjForLdap(CompanyDbObj company, AccessCodeDbObj access,
			String encodedAuthnRequest, LdapServerDbObj ldapServer, String authId, String outgoingRelayState,
			String sender, String incomingId) {
		SamlAuthnRequestDbObj samlAuthnRequest = null;
		SamlDataAccess dataAccess = new SamlDataAccess();
		if (access != null) {
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			samlAuthnRequest = new SamlAuthnRequestDbObj(ldapServer.getProviderUrl(), now, ldapServer.getTableId(),
					authId, incomingId, outgoingRelayState, null, now, null, ldapServer.getProviderUrl(), null,
					SignatureStatus.UNVALIDATE, encodedAuthnRequest, null, false, company.getCompanyId(), "",
					access.getDeviceId(), access.getAccessCode(), Outcomes.UNKNOWN_STATUS, sender);

			dataAccess.addSamlAuthnRequest(samlAuthnRequest);
			dataAccess.addLog("(1) after addSamlAuthnRequest at " + now);
		} else {
			dataAccess.addLog("accessCode was null", LogConstants.ERROR);
		}
		return samlAuthnRequest;
	}

	public SamlAuthnRequestDbObj buildAndSaveAuthnRequestObj(IdentityObjectFromServer idObj, CompanyDbObj company,
			SamlIdentityProviderDbObj samlIdp, SamlServiceProviderDbObj serviceProvider,
			AuthnRequest incomingAuthnRequest, AuthnRequest outgoingAuthnRequest, String incomingRelayState,
			String ipAddress, String encodedIncomingRequest, SamlDataAccess dataAccess) {
		SamlAuthnRequestDbObj samlAuthnRequest = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String outgoingAcsUrl = IDP_URL.replace("{apiKey}", company.getApiKey());
		KeyDbObj key = null;
		if (serviceProvider != null) {
			key = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.SP_CERT,
					serviceProvider.getServiceProviderEntityId());
		}
		SignatureStatus signatureStatus = SignatureStatus.FAILURE;
		if (key != null) {
			if (validateSignature(incomingAuthnRequest.getSignature(), key.getKeyText())) {
				signatureStatus = SignatureStatus.SUCCESS;
			}
		}
		// TODO: do we want to skip this if the key is null
		String deviceId = null;
		if (idObj.getDevice() != null) {
			deviceId = idObj.getDevice().getDeviceId();
		}
		String browserId = null;
		if (idObj.getBrowser() != null) {
			browserId = idObj.getBrowser().getBrowserId();
		}
		String outgoingRelayState = GeneralUtilities.randomString();
		AccessCodeDbObj accessCode = new AccessCodeDbObj(GeneralUtilities.randomString(), company.getCompanyId(), null,
				deviceId, 0, true, browserId, false);
		dataAccess.addAccessCode(accessCode, "buildAndSaveAuthnRequestObj");
		Timestamp issueInstant = DateTimeUtilities.instantToTimestamp(incomingAuthnRequest.getIssueInstant());
		samlAuthnRequest = new SamlAuthnRequestDbObj(samlIdp.getIdentityProviderName(), now, samlIdp.getTableId(),
				outgoingAuthnRequest.getID(), incomingAuthnRequest.getID(), outgoingRelayState, incomingRelayState,
				issueInstant, incomingAuthnRequest.getIssuer().getValue(), outgoingAcsUrl,
				incomingAuthnRequest.getAssertionConsumerServiceURL(), signatureStatus, encodedIncomingRequest,
				ipAddress, false, company.getCompanyId(), null, idObj.getDevice().getDeviceId(),
				accessCode.getAccessCode(), Outcomes.UNKNOWN_STATUS,
				incomingAuthnRequest.getAssertionConsumerServiceURL());
		dataAccess.addSamlAuthnRequest(samlAuthnRequest);
		return samlAuthnRequest;
	}

	public SamlAuthnRequestDbObj buildAndSaveAuthnRequestObjForLdap(CompanyDbObj company,
			SamlServiceProviderDbObj serviceProvider, AuthnRequest incomingAuthnRequest, LdapServerDbObj ldapServer,
			String incomingRelayState, String ipAddress, String encodedIncomingRequest, SamlDataAccess dataAccess) {
		SamlAuthnRequestDbObj samlAuthnRequest = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String outgoingAcsUrl = ldapServer.getProviderUrl();
		KeyDbObj key = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.SP_CERT,
				serviceProvider.getServiceProviderEntityId());
		if (key != null) {
			Timestamp issueInstant = DateTimeUtilities.instantToTimestamp(incomingAuthnRequest.getIssueInstant());
			SignatureStatus signatureStatus;
			if (validateSignature(incomingAuthnRequest.getSignature(), key.getKeyText())) {
				signatureStatus = SignatureStatus.SUCCESS;
			} else {
				signatureStatus = SignatureStatus.FAILURE;
			}
			String outgoingRelayState = GeneralUtilities.randomString();
			samlAuthnRequest = new SamlAuthnRequestDbObj(ldapServer.getProviderUrl(), now, ldapServer.getTableId(),
					GeneralUtilities.randomString(), incomingAuthnRequest.getID(), outgoingRelayState,
					incomingRelayState, issueInstant, incomingAuthnRequest.getIssuer().getValue(), outgoingAcsUrl,
					incomingAuthnRequest.getAssertionConsumerServiceURL(), signatureStatus, encodedIncomingRequest,
					ipAddress, false, company.getCompanyId(), null, null, null, Outcomes.UNKNOWN_STATUS,
					incomingAuthnRequest.getAssertionConsumerServiceURL());
			dataAccess.addSamlAuthnRequest(samlAuthnRequest);

		}
		return samlAuthnRequest;

	}

	public SamlAuthnRequestDbObj buildAndSaveAuthnRequestObjForLdapTesting(CompanyDbObj company,
			LdapServerDbObj ldapServer, String incomingRelayState, String ipAddress, String encodedIncomingRequest,
			SamlDataAccess dataAccess) {
		SamlAuthnRequestDbObj samlAuthnRequest = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String outgoingAcsUrl = ldapServer.getProviderUrl();

		String outgoingRelayState = GeneralUtilities.randomString();
		samlAuthnRequest = new SamlAuthnRequestDbObj(ldapServer.getProviderUrl(), now, ldapServer.getTableId(),
				GeneralUtilities.randomString(), "", outgoingRelayState, incomingRelayState, now, "", outgoingAcsUrl,
				"", SignatureStatus.SUCCESS, encodedIncomingRequest, ipAddress, false, company.getCompanyId(), null,
				null, null, Outcomes.UNKNOWN_STATUS, "");
		dataAccess.addSamlAuthnRequest(samlAuthnRequest);

		return samlAuthnRequest;

	}

	public SamlAuthnRequestDbObj buildAndSaveAuthnRequestObj(GroupDbObj group, SamlIdentityProviderDbObj samlIdp,
			String authId, String sender, String deviceId) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
		SamlAuthnRequestDbObj samlAuthnRequest = this.buildAndSaveAuthnRequestObj(company, group, "", samlIdp, authId,
				sender, deviceId);
		dataAccess.addLog("buildAndSaveAuthnRequestObj", "after addSamlAuthnRequest");
		return samlAuthnRequest;
	}

	public SamlAuthnRequestDbObj buildAndSaveAuthnRequestObjForLdapForInstallVerification(CompanyDbObj company,
			GroupDbObj group, LdapServerDbObj ldapServer, String ipAddress, String sender, SamlDataAccess dataAccess) {
		SamlAuthnRequestDbObj samlAuthnRequest = null;
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String outgoingAcsUrl = ldapServer.getProviderUrl();
		String outgoingRelayState = GeneralUtilities.randomString();
		String incomingRelayState = GeneralUtilities.randomString();
		String authId = GeneralUtilities.randomString();
		samlAuthnRequest = new SamlAuthnRequestDbObj(ldapServer.getProviderUrl(), now, ldapServer.getTableId(),
				GeneralUtilities.randomString(), authId, outgoingRelayState, incomingRelayState, now,
				ldapServer.getProviderUrl(), outgoingAcsUrl, ldapServer.getProviderUrl(), SignatureStatus.SUCCESS, "",
				ipAddress, false, company.getCompanyId(), group.getGroupId(), null, null, Outcomes.UNKNOWN_STATUS,
				sender);
		dataAccess.addSamlAuthnRequest(samlAuthnRequest);
		return samlAuthnRequest;
	}

	public SamlAuthnRequestDbObj buildAndSaveIncomingAuthnRequestObj(AuthnRequest authnRequest,
			String incomingRelayState, CompanyDbObj company, DeviceDbObj device, String encryptedRequest,
			String browserSession, String ipAddress) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		SamlAuthnRequestDbObj samlAuthnRequest = null;
		try {
			String incomingAcsUrl = authnRequest.getAssertionConsumerServiceURL();
			String issuer = authnRequest.getIssuer().getElementQName().toString();
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			String incomingRequestId = authnRequest.getID();
			String groupId = null;
			String deviceId = null;
			if (device != null) {
				groupId = device.getGroupId();
				deviceId = device.getDeviceId();
			} else {
				dataAccess.addLog("without device");
			}
			samlAuthnRequest = new SamlAuthnRequestDbObj("blue2factor", now, null, null, incomingRequestId, null,
					incomingRelayState, new Timestamp(authnRequest.getIssueInstant().toEpochMilli()), issuer, null,
					incomingAcsUrl, SignatureStatus.UNVALIDATE, encryptedRequest, ipAddress, false,
					company.getCompanyId(), groupId, deviceId, browserSession, Outcomes.UNKNOWN_STATUS,
					authnRequest.getProviderName());
			dataAccess.addSamlAuthnRequest(samlAuthnRequest);
			dataAccess.addLog("(2) after addSamlAuthnRequest at " + now + "; group id: " + groupId);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return samlAuthnRequest;
	}

	private SamlAuthnRequestDbObj buildAndSaveAuthnRequestObj(CompanyDbObj company, GroupDbObj group,
			String encodedAuthnRequest, SamlIdentityProviderDbObj samlIdp, String authId, String sender,
			String deviceId) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		String groupId;
		if (group == null) {
			groupId = null;
		} else {
			groupId = group.getGroupId();
		}
		String outgoingRelayState = GeneralUtilities.randomString();
		SamlAuthnRequestDbObj samlAuthnRequest = new SamlAuthnRequestDbObj(samlIdp.getIdentityProviderName(), now,
				samlIdp.getEntityId(), authId, null, outgoingRelayState, null, now, null,
				IDP_URL.replace("{apiKey}", company.getApiKey()), null, SignatureStatus.UNVALIDATE, encodedAuthnRequest,
				null, false, company.getCompanyId(), groupId, deviceId, "", Outcomes.UNKNOWN_STATUS, sender);
		SamlDataAccess dataAccess = new SamlDataAccess();
		dataAccess.addSamlAuthnRequest(samlAuthnRequest);
		dataAccess.addLog("buildAndSaveAuthnRequestObj", "(3) after addSamlAuthnRequest at " + now);
		return samlAuthnRequest;
	}

	public String getSamlAuthnRedirectUrl(CompanyDbObj company) throws Exception {
		SamlDataAccess dataAccess = new SamlDataAccess();
		SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompany(company);
		String authId = GeneralUtilities.randomString();
		String authnRequest = buildAuthnRequestAsString(samlIdp, company, authId, true);
		String codedAuthnRequest = this.encodeAuthnRequestOrResponse(authnRequest);
		String relayState = GeneralUtilities.randomString();
		String samlAuthnUrl = samlIdp.getRedirectUrl();
		if (samlAuthnUrl.contains("?")) {
			samlAuthnUrl += "&";
		} else {
			samlAuthnUrl += "?";
		}
		samlAuthnUrl += "SAMLRequest=" + codedAuthnRequest + "&RelayState=" + relayState;
		dataAccess.addLog("getSamlAuthnRedirectUrl", "samlAuthnUrl: " + samlAuthnUrl);
		return samlAuthnUrl;
	}

	private String buildAuthnRequestAsString(SamlIdentityProviderDbObj identityProvider, CompanyDbObj company,
			String authId, boolean forceReauth) throws Exception {
		AuthnRequest authnRequest = buildAuthnRequest(identityProvider, company, authId, forceReauth);
		return authnRequestToString(authnRequest, identityProvider.isSignRequest());
	}

	public String authnRequestToString(AuthnRequest authnRequest, boolean signRequest) {
		String authnRequestString = null;
		DataAccess dataAccess = new DataAccess();
		try {
			InitializationService.initialize();
			MarshallerFactory marshallFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
			Marshaller marshaller = marshallFactory.getMarshaller(authnRequest);
			Element plain = marshaller.marshall(authnRequest);
			if (signRequest) {
				Signer.signObject(authnRequest.getSignature());
			}
			authnRequestString = removeHeader(nodeToString(plain));
		} catch (Exception e) {
			dataAccess.addLog("authnRequestToString", e);
		}
		dataAccess.addLog(authnRequestString);
		return authnRequestString;
	}

	public String logoutRequestToString(LogoutRequest logoutRequest, boolean signRequest) {
		String logoutRequestString = null;
		DataAccess dataAccess = new DataAccess();
		try {
			InitializationService.initialize();
			MarshallerFactory marshallFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
			Marshaller marshaller = marshallFactory.getMarshaller(logoutRequest);
			Element plain = marshaller.marshall(logoutRequest);
			if (signRequest) {
				Signer.signObject(logoutRequest.getSignature());
			}
			logoutRequestString = removeHeader(nodeToString(plain));
		} catch (Exception e) {
			dataAccess.addLog("logoutRequestToString", e);
		}
		dataAccess.addLog("logoutRequestToString", logoutRequestString);
		return logoutRequestString;
	}

	public LogoutRequest buildSignoutRequest(SamlIdentityProviderDbObj idp, IdentityObjectFromServer idObj) {
		Saml saml = new Saml();
		DataAccess dataAccess = new DataAccess();
		CompanyDbObj company = idObj.getCompany();
		GroupDbObj group = idObj.getGroup();
		LogoutRequest logout = null;
		if (group != null && idp.getLogoutUrl() != null) {
			dataAccess.addLog("buildSignoutRequest", idp.getLogoutUrl());
			logout = new LogoutRequestBuilder().buildObject();
			logout.setIssuer(
					saml.getIssuer(Urls.SECURE_URL + Urls.SAML_ENTITY_ID.replace("{apiKey}", company.getApiKey())));
			logout.setID(GeneralUtilities.randomString());
			Instant dateTime = Instant.now();
			Instant afterTime = dateTime.plus(5, ChronoUnit.MINUTES);
			logout.setIssueInstant(dateTime);
			logout.setNotOnOrAfter(afterTime);
			logout.setReason(LogoutRequest.USER_REASON);
			String[] logoutParams = idp.getLogoutUrl().split("::");
			if (logoutParams.length > 1) {
				dataAccess.addLog("buildSignoutRequest", "params found");
				String destination = logoutParams[1];
				logout.setDestination(destination);
				NameID nameId = new NameIDBuilder().buildObject();
				logout.setDestination(destination);
				nameId.setFormat(NameIDType.EMAIL);
				nameId.setValue(group.getUsername());
				logout.setNameID(nameId);
			}
		} else {
			dataAccess.addLog("buildSignoutRequest", "group or logout was null3");
		}
		return logout;
	}

	public AuthnRequest buildAuthnRequest(SamlIdentityProviderDbObj identityProvider, CompanyDbObj company,
			String authId, boolean forceReauth) throws Exception {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		AuthnRequestBuilder authnRequestBuilder = new AuthnRequestBuilder();
		AuthnRequest authnRequest = authnRequestBuilder.buildObject();

		authnRequest.setID(authId);
		authnRequest.setIssueInstant(Instant.now());
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		authnRequest.setProviderName(Constants.APP_NAME);
		authnRequest.setDestination(identityProvider.getRedirectUrl());
		new DataAccess().addLog("acsUrl: " + Urls.SECURE_URL
				+ Urls.SAML_RESPONSE_FROM_IDENTITY_PROVIDER.replace("{apiKey}", company.getApiKey()));
		authnRequest.setAssertionConsumerServiceURL(
				Urls.SECURE_URL + Urls.SAML_RESPONSE_FROM_IDENTITY_PROVIDER.replace("{apiKey}", company.getApiKey()));
		authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		authnRequest
				.setIssuer(getIssuer(Urls.SECURE_URL + Urls.SAML_ENTITY_ID.replace("{apiKey}", company.getApiKey())));
		authnRequest.setForceAuthn(forceReauth);
		authnRequest.setIsPassive(false);

		NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
		NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
		nameIdPolicy.setFormat(NameIDType.EMAIL);
		RequestedAuthnContextBuilder requestedAuthnContextBuilder = new RequestedAuthnContextBuilder();
		RequestedAuthnContext requestedAuthnContext = requestedAuthnContextBuilder.buildObject();
		requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
		AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
		AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder.buildObject();
		authnContextClassRef.setURI(AuthnContext.PASSWORD_AUTHN_CTX);
		requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);

		authnRequest.setNameIDPolicy(nameIdPolicy);
		authnRequest.setRequestedAuthnContext(requestedAuthnContext);
		Signature signature = null;

		if (identityProvider.isSignRequest()) {
			KeyDbObj coSpPrivate = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.SP_PRIVATE,
					company.getCompanyId());
			KeyDbObj coSpCert = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.SP_CERT, company.getCompanyId());
			signature = getSignature(coSpCert.getKeyText(), coSpPrivate.getKeyText());
			authnRequest.setSignature(signature);
		}
		return authnRequest;
	}

	public Issuer getIssuer(String issuerStr) {
		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(issuerStr);
		return issuer;
	}

	private KeyInfo getKeyInfo(String certText) {// boolean identityProvider, Stri) {
		KeyInfoBuilder keyInfoBuilder = new KeyInfoBuilder();
		KeyInfo keyInfo = keyInfoBuilder.buildObject();
		X509DataBuilder x509DataBuilder = new X509DataBuilder();
		X509Data x509Data = x509DataBuilder.buildObject();
		X509CertificateBuilder x509CertificateBuilder = new X509CertificateBuilder();
		org.opensaml.xmlsec.signature.X509Certificate x509Certificate = x509CertificateBuilder.buildObject();

		String certificateString = certText.replace("-----BEGIN CERTIFICATE-----\n", "")
				.replace("-----END CERTIFICATE-----", "");
		certificateString = certificateString.replaceAll("\\s", "");

		x509Certificate.setValue(certificateString);

		x509Data.getX509Certificates().add(x509Certificate);
		keyInfo.getX509Datas().add(x509Data);
		return keyInfo;
	}

	public String removeHeader(String samlString) {
		if (samlString.contains("?>")) {
			DataAccess dataAccess = new DataAccess();
			dataAccess.addLog("removeHeader", "?> found");
			String[] samlSplit = samlString.split("\\?>", 2);
			if (samlSplit.length == 2) {
				dataAccess.addLog("removeHeader", "\\?> split");
				samlString = samlSplit[1];
			}
		}
		return samlString;
	}

	public String nodeToString(Node node) {
		Document document = node.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		return serializer.writeToString(node);
	}

	public AuthnRequest getAuthnRequest(String xmlString) {
		DataAccess dataAccess = new DataAccess();
		AuthnRequest authRequest = null;
		try {

			BasicParserPool basicParserPool = new BasicParserPool();

			basicParserPool.setNamespaceAware(true);
			basicParserPool.initialize();
			InputStream stream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
			Document doc = basicParserPool.parse(stream);
			Element samlElem = doc.getDocumentElement();
			String node = samlElem.getNodeName();
			dataAccess.addLog("samlElem: " + node);
			InitializationService.initialize();
			UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
			if (unmarshallerFactory == null) {
				dataAccess.addLog("unmarshallerFactory is null");
			}
			if (samlElem.getLocalName() == null) {
				NamedNodeMap nnm = samlElem.getAttributes();
				for (int i = 0; i < nnm.getLength(); i++) {
					Node node1 = nnm.item(i);
					String x = node1.getNodeName();
					System.out.println(x);
				}
				samlElem.setAttribute("localName", AuthnRequest.DEFAULT_ELEMENT_LOCAL_NAME);
			}
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlElem);

			XMLObject requestXmlObj = unmarshaller.unmarshall(samlElem);
			if (requestXmlObj == null) {
				dataAccess.addLog("requestXmlObj is null");
			}
			dataAccess.addLog(requestXmlObj.toString());
			authRequest = (AuthnRequest) requestXmlObj;
			logAuthnRequest(authRequest, dataAccess);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return authRequest;
	}

	public void logAuthnRequest(AuthnRequest authnRequest, DataAccess dataAccess) {
		dataAccess.addLog(authnRequestToString(authnRequest, false));
	}

	public void logAuthnResponse(Response resp, DataAccess dataAccess) {
		ResponseMarshaller marshaller = new ResponseMarshaller();
		Element plain;
		try {
			plain = marshaller.marshall(resp);
			String samlResponse = removeHeader(nodeToString(plain));
			dataAccess.addLog(samlResponse);
		} catch (MarshallingException e) {
			dataAccess.addLog(e);
		}

	}

	public String decryptAuthnRequest(String encrypted) throws Exception {
		DataAccess dataAccess = new DataAccess();
		try {
			String urlDecoded = java.net.URLDecoder.decode(encrypted, StandardCharsets.UTF_8.name()).replace(" ", "+");
			dataAccess.addLog("UrlDecoded: " + urlDecoded);
			byte[] decodedBytes = Base64.getDecoder().decode(urlDecoded);
			dataAccess.addLog("decoded bytes: " + new String(decodedBytes, StandardCharsets.UTF_8));
			String outputString;
			try {
				// Decompress the bytes
				Inflater inflater = new Inflater(true);
				inflater.setInput(decodedBytes, 0, decodedBytes.length);
				dataAccess.addLog("decoded bytes length: " + decodedBytes.length);
				byte[] result = new byte[1024];

				// byte[] result = new byte[100 * decodedBytes.length];
				int resultLength = inflater.inflate(result);
				inflater.end();

				// Decode the bytes into a String
				outputString = new String(result, 0, resultLength, StandardCharsets.UTF_8).trim();
			} catch (Exception e) {
				dataAccess.addLog("The data was not deflated?");
				outputString = new String(decodedBytes, StandardCharsets.UTF_8);
			}
			dataAccess.addLog("output: " + outputString);
			return outputString;
		} catch (Exception ex) {
			dataAccess.addLog(ex);
			throw new RuntimeException(ex.getCause());
		}
	}

	public String decryptAuthnRequest2(String encrypted) throws Exception {
		String decoded = null;
		DataAccess dataAccess = new DataAccess();
		try {
			decoded = java.net.URLDecoder.decode(encrypted, "UTF-8");
		} catch (Exception ex) {
			dataAccess.addLog(ex);
		}

		byte[] deflatedBytes = Base64.getDecoder().decode(decoded);
		byte[] inflatedBytes = new byte[100 * deflatedBytes.length];
		Inflater compressor = new Inflater(true);

		compressor.setInput(deflatedBytes, 0, deflatedBytes.length);

		try {
			compressor.inflate(inflatedBytes);
		} catch (Exception ex) {
			dataAccess.addLog(ex);
		}

		try {
			return new String(inflatedBytes, "UTF-8");
		} catch (Exception ex) {
			dataAccess.addLog(ex);
		}
		return null;
	}

	public String encodeAuthnRequestOrResponse(String req) {
		DataAccess dataAccess = new DataAccess();
		String urlEncoded = null;
		try {
			dataAccess.addLog("encoding: " + req);
			Deflater deflater = new Deflater();
			deflater.setInput(req.getBytes(StandardCharsets.UTF_8));
			deflater.finish();
			byte output[] = new byte[1024];
			deflater.deflate(output);
			byte[] encodedBytes = Base64.getEncoder().encode(output);
			String encodedByteString = new String(encodedBytes, StandardCharsets.UTF_8);
			dataAccess.addLog("encoded: " + encodedByteString);
			urlEncoded = URLEncoder.encode(encodedByteString, StandardCharsets.UTF_8.name());
			dataAccess.addLog("urlencoded: " + urlEncoded);
		} catch (Exception e) {
			dataAccess.addLog("encodeAuthnRequest", e);
		}
		return urlEncoded;
	}
}

package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.client.CollectedClientData;

public class AuthenticatorDbObj {
    private String credId;
    private String browserId;
    private String type;
    private long signCount;
    private AttestedCredentialData attestedCredentialData;
    private AttestationObject attestationObject;
    private CollectedClientData collectedClientData;
    private String format;
    private Timestamp createDate;
    private boolean expired;
    private String challenge;
    private String baseUrl;

    public AuthenticatorDbObj(String credId, String browserId, String type,
            AttestedCredentialData attestedCredentialData, AttestationObject attestationObject,
            CollectedClientData collectedClientData, long signCount, Timestamp createDate,
            boolean expired, String challenge, String baseUrl) {
        this.credId = credId;
        this.browserId = browserId;
        this.type = type;
        this.signCount = signCount;
        this.attestedCredentialData = attestedCredentialData;
        this.attestationObject = attestationObject;
        this.collectedClientData = collectedClientData;
        this.createDate = createDate;
        this.expired = expired;
        if (attestationObject != null) {
            if (attestationObject.getAttestationStatement() != null) {
                this.format = attestationObject.getFormat();
            }
        } else {
            this.format = null;
        }
        this.challenge = challenge;
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public CollectedClientData getCollectedClientData() {
        return collectedClientData;
    }

    public void setCollectedClientData(CollectedClientData collectedClientData) {
        this.collectedClientData = collectedClientData;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCredId() {
        return credId;
    }

    public void setCredId(String credId) {
        this.credId = credId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getBrowserId() {
        return browserId;
    }

    public void setBrowserId(String browserId) {
        this.browserId = browserId;
    }

    public long getSignCount() {
        return signCount;
    }

    public void setSignCount(long signCount) {
        this.signCount = signCount;
    }

    public AttestedCredentialData getAttestedCredentialData() {
        return attestedCredentialData;
    }

    public void setAttestedCredentialData(AttestedCredentialData attestedCredentialData) {
        this.attestedCredentialData = attestedCredentialData;
    }

    public AttestationObject getAttestationObject() {
        return attestationObject;
    }

    public void setAttestationObject(AttestationObject attestationObject) {
        this.attestationObject = attestationObject;
    }

    // AttestedCredentialData
//	private String aauuid;
//	private byte[] credentialId;
//	//AttestedCredentialData.COSEKey
//	private RSAPublicKey publicKey;
//	//AttestationStatement
//	private String format; // = "android-key"
//	private byte[] sig = null;
//    //AttestationStatement.COSEAlgorithmIdentifier
//	private long algorithm;
//    
//  //TPMSAttest
//	//    private TPMGenerated magic;
//	private byte[] tpmGenerated = null;
//	//    private TPMISTAttest type;
//	private byte[] tpmistAttest = null;
//	private byte[] qualifiedSigner = null;
//	private byte[] extraData = null;
//	//    private TPMSClockInfo clockInfo;
//	private BigInteger clock = null;
//	private long resetCount;
//	private long restartCount;
//	private boolean safe = true;
//	private BigInteger firmwareVersion;
//	//    private TPMUAttest attested; interface that I cannot find an implementation of
//    private int tpmuCertNameHashAlg;
//    private byte[] tpmuCertNameDigest;
//    private int tpmuCertQualifiedNameHashAlg;
//    private byte[] tpmuCertQualifiedNameDigest;
//	//TPMTPublic
//    private int tpmiAlgPublic;
//    private int tpmiAlgHash;
//    private byte[] tpmaObject = null;
//    private byte[] authPolicy = null;
//	//    private TPMUPublicParms parameters; interface that I cannot find an implementation of
//    private byte[] tpmuppSymmetric;
//    private byte[] tpmuppScheme;
//    private byte[] tpmuppKeyBits;
//    private byte[] tpmuppExponent;
//	//    private TPMUPublicId unique; interface that I cannot find an implementation ofy
//    private byte[] tpmuPublicId = null;
//    private String version = null;
//    //AttestationStatement.AttestationCertificatePath
//    //AttestationStatement.AttestationCertificatePath.List<X509Certificate>
//    private List<byte[]> certificates = new ArrayList<byte[]>();
//    private byte[] ecdaaKeyId = null;
//    
//
//	public AuthenticatorDbObj(String browserToken, AttestedCredentialData acd, AttestationStatement as, 
//			long signCount) throws CertificateEncodingException {
//		this.browserToken = browserToken;
//		this.aauuid = acd.getAaguid().getValue().toString();
//		this.credentialId = acd.getCredentialId();
//		COSEKey coskey = acd.getCOSEKey();
//		publicKey = (RSAPublicKey)coskey.getPublicKey();
//		this.format = as.getFormat();
//		X509Certificate x509;
//		AttestationCertificatePath acp;
//		switch (this.format) {
//		case "android-key":
//			AndroidKeyAttestationStatement akas = (AndroidKeyAttestationStatement) as;
//			this.sig = akas.getSig();
//			this.algorithm = akas.getAlg().getValue();
//			acp = akas.getX5c();
//			for (int i = 0; i < acp.size(); i++) {
//				x509 = acp.get(0);
//				certificates.add(x509.getEncoded());
//			}
//			break;
//		case "android-safetynet":
//			//AndroidSafetyNetAttestationStatement asnas = (AndroidSafetyNetAttestationStatement) as;
//			//not set up;
//			
//			break;
//		case "fido-u2f":
//			FIDOU2FAttestationStatement f2as = (FIDOU2FAttestationStatement) as;
//			this.sig = f2as.getSig();
//			acp = f2as.getX5c();
//			certificates = new ArrayList<byte[]>();
//			for (int i = 0; i < acp.size(); i++) {
//				x509 = acp.get(0);
//				certificates.add(x509.getEncoded());
//			}
//			break;
//		case "none":
//			//NoneAttestationStatement nas = (NoneAttestationStatement) as;
//			break;
//		case "packed":
//			PackedAttestationStatement pas = (PackedAttestationStatement) as;
//			this.ecdaaKeyId = pas.getEcdaaKeyId();
//			this.sig = pas.getSig();
//			this.algorithm = pas.getAlg().getValue();
//			acp = pas.getX5c();
//			certificates = new ArrayList<byte[]>();
//			for (int i = 0; i < acp.size(); i++) {
//				x509 = acp.get(0);
//				certificates.add(x509.getEncoded());
//			}
//			break;
//		case "tpm":
//			TPMAttestationStatement tpmas = (TPMAttestationStatement) as;
//			this.ecdaaKeyId = tpmas.getEcdaaKeyId();
//			this.sig = tpmas.getSig();
//			this.algorithm = tpmas.getAlg().getValue();
//			acp = tpmas.getX5c();
//			certificates = new ArrayList<byte[]>();
//			for (int i = 0; i < acp.size(); i++) {
//				x509 = acp.get(0);
//				certificates.add(x509.getEncoded());
//			}
//			this.tpmGenerated = tpmas.getCertInfo().getMagic().getValue();
//			this.tpmistAttest = tpmas.getCertInfo().getType().getValue();
//			this.qualifiedSigner = tpmas.getCertInfo().getQualifiedSigner();
//			this.extraData = tpmas.getCertInfo().getExtraData();
//			this.clock = tpmas.getCertInfo().getClockInfo().getClock();
//			this.resetCount = tpmas.getCertInfo().getClockInfo().getResetCount();
//			this.restartCount = tpmas.getCertInfo().getClockInfo().getRestartCount();
//			this.firmwareVersion = tpmas.getCertInfo().getFirmwareVersion();
//			TPMSCertifyInfo tpmsCert = (TPMSCertifyInfo) tpmas.getCertInfo().getAttested();
//			this.tpmuCertNameHashAlg = tpmsCert.getName().getHashAlg().getValue();
//			this.tpmuCertNameDigest = tpmsCert.getName().getDigest();
//			this.tpmuCertQualifiedNameHashAlg = tpmsCert.getQualifiedName().getHashAlg().getValue();
//			this.tpmuCertQualifiedNameDigest = tpmsCert.getQualifiedName().getDigest();
//			
//			this.tpmiAlgPublic = tpmas.getPubArea().getType().getValue();
//			this.tpmiAlgHash = tpmas.getPubArea().getNameAlg().getValue();
//			this.tpmaObject = tpmas.getPubArea().getObjectAttributes().getBytes();
//			this.authPolicy = tpmas.getPubArea().getAuthPolicy();
//			
//			TPMSRSAParms parms = (TPMSRSAParms)tpmas.getPubArea().getParameters();
//			this.tpmuppSymmetric = parms.getSymmetric();
//			this.tpmuppScheme = parms.getScheme();
//			this.tpmuppKeyBits = parms.getKeyBits();
//			this.tpmuppExponent = parms.getExponent();
//			RSAUnique rsaUnique = (RSAUnique) tpmas.getPubArea().getUnique();
//			this.tpmuPublicId = rsaUnique.getN();
//			break;
//		}
//		
//		this.signCount = signCount;
//	}
//
//	public Authenticator getAuthenticator() {
//		return new AuthenticatorImpl(
//        		getAttestedCredentialData(),
//        		getAttestationStatement(),
//        		signCount
//        );
//	}
//
//    private AttestationStatement getAttestationStatement() {
//    	AttestationStatement as = null;
//    	AttestationCertificatePath acp;
//    	switch (this.format) {
//    	case "android-key":
//        	acp = getAttCertPath();
//        	as = new AndroidKeyAttestationStatement(COSEAlgorithmIdentifier.create(algorithm), 
//        			sig, acp);
//    		break;
//    	case "android-safetynet":
//			//not set up;
//			break;
//    	case "fido-u2f":
//    		acp = getAttCertPath();
//			as = new FIDOU2FAttestationStatement(acp, sig);
//    	case "none":
//    		//not set up
//    		break;
//    	case "packed":
//    		acp = getAttCertPath();
//    		as = new PackedAttestationStatement(COSEAlgorithmIdentifier.create(algorithm), sig, acp, ecdaaKeyId);
//    		break;
//    	case "tpm":
//    		acp = getAttCertPath();
//    		TPMSClockInfo clockInfo = new TPMSClockInfo(clock, resetCount, restartCount, safe);
//    		
//    		TPMTHA name = new TPMTHA(TPMIAlgHash.create(tpmuCertNameHashAlg) , this.tpmuCertNameDigest);
//    		TPMTHA qualifiedName = new TPMTHA(TPMIAlgHash.create(tpmuCertQualifiedNameHashAlg) , 
//    				this.tpmuCertQualifiedNameDigest);
//    		TPMUAttest attest = new TPMSCertifyInfo(name, qualifiedName);
//    		TPMSAttest certInfo = new TPMSAttest(TPMGenerated.create(tpmGenerated), TPMISTAttest.create(tpmistAttest), 
//    				qualifiedSigner, extraData, clockInfo, firmwareVersion, attest);
//    		
//    		TPMSRSAParms pubParms = new TPMSRSAParms(tpmuppSymmetric, tpmuppScheme, tpmuppKeyBits, tpmuppExponent);
//    		TPMUPublicId pubId = new RSAUnique(tpmuPublicId);
//    		TPMAObject objectAttributes = new TPMAObject(GeneralUtilities.fromByteArray(this.tpmaObject));
//    		TPMTPublic pubArea = new TPMTPublic(TPMIAlgPublic.create(tpmiAlgPublic), TPMIAlgHash.create(tpmiAlgHash), 
//    				objectAttributes, authPolicy, pubParms, pubId);
//    		as = new TPMAttestationStatement(version, COSEAlgorithmIdentifier.create(algorithm),
//    	            acp, ecdaaKeyId, sig, certInfo, pubArea);
//    		break;
//    	}
//    	
//    	
//    	return as;
//    }
//    
//    private AttestationCertificatePath getAttCertPath() {
//    	List<X509Certificate> certList = new ArrayList<X509Certificate>();
//    	X509Certificate x509;
//    	for (byte[] certBytes : certificates) {
//    		 x509 = CertificateUtil.generateX509Certificate(certBytes);
//    		 certList.add(x509);
//    	}
//    	return new AttestationCertificatePath(certList);
//    }
//    
//    private AttestedCredentialData getAttestedCredentialData() {
//    	UUID uuid = UUID.fromString(aauuid);
//    	AAGUID newAauuid = new AAGUID(uuid);
//    	COSEKey coseKey = RSACOSEKey.create(publicKey);
//    	AttestedCredentialData attestedCredentialData = new AttestedCredentialData(newAauuid, credentialId, coseKey);
//    	return attestedCredentialData;
//    }

}

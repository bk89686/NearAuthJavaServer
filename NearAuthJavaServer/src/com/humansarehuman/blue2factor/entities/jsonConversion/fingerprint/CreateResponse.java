package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;
import java.util.List;

import com.webauthn4j.data.PublicKeyCredentialParameters;

public class CreateResponse implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 8982234376091813127L;
    private String clientDataJSON;
    private String attestationObject;
    private List<PublicKeyCredentialParameters> pubKeyCredParams;
//    private String[] transports;
//    private String clientExtensionJSON;

//    public Set<String> getTransports() {
//        Set<String> transportSet = new HashSet<String>(Arrays.asList(transports));
//        return transportSet;
//    }
//
//    public void setTransports(String[] transports) {
//        this.transports = transports;
//    }
//
//    public String getClientExtensionJSON() {
//        return clientExtensionJSON;
//    }
//
//    public void setClientExtensionJSON(String clientExtensionJSON) {
//        this.clientExtensionJSON = clientExtensionJSON;
//    }

    public String getClientDataJSON() {
        return clientDataJSON;
    }

    public void setClientDataJSON(String clientDataJSON) {
        this.clientDataJSON = clientDataJSON;
    }

    public String getAttestationObject() {
        return attestationObject;
    }

    public void setAttestationObject(String attestationObject) {
        this.attestationObject = attestationObject;
    }

    public List<PublicKeyCredentialParameters> getPubKeyCredParams() {
        return pubKeyCredParams;
    }

    public void setPubKeyCredParams(List<PublicKeyCredentialParameters> pubKeyCredParams) {
        this.pubKeyCredParams = pubKeyCredParams;
    }

}

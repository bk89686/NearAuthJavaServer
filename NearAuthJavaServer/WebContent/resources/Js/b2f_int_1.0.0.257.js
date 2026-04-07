 /*jshint esversion: 8 */
var rerunCount = 0;
var firstTry = true;
var firstFingerprintError = true;
var visibilityState = "prerender";
var fpRequest = false;
var lhTest = false;
var ENDPT = "";
const SUCCESS = 0;
var DB_VERSION = 16;
var checkingForPush = false;
var checkingFingerprint = false;
var credentialOptions;
var inFocus = false;
const TRACE = 0;
const LOG = 1;
const INFO = 2;
const WARN = 3;
const ERROR = 4;
const CONSOLE_LEVEL = TRACE;
const STORE_NAME = "b2fkeys";

function setEndpoint(){
    const envDom = document.getElementById("b2f-env");
    if (envDom) {
        const env = envDom.value;
        if (env === "" || env === "PROD") {
            ENDPT = "https://secure.nearauth.ai";
        } else if (env === "TEST") {
            ENDPT = "https://test.nearauth.ai";
        } else if (env === "DEV") {
            ENDPT = "https://dev.nearauth.ai";
        } else if (env === "LOCAL") {
            ENDPT = "https://127.0.0.1";
        }
    } else {
        ENDPT = "https://secure.nearauth.ai";
    }
}

function getEndpoint(){
    if (ENDPT === "") {
        setEndpoint();
    }
    return ENDPT;
}

function cLog(txt, lvl){
    if (lvl === undefined){
        lvl = TRACE;
    }
    if (CONSOLE_LEVEL <= lvl){
        const now = getCurrentTime();
        if (lvl === TRACE){
            console.log(now + ": " + txt);
        } else if (lvl === LOG){
            console.log(now + ": " + txt);
        } else if (lvl === INFO){
            console.info(now + ": " + txt);
        } else if (lvl === WARN){
            console.warn(now + ": " + txt);
        } else if (lvl === ERROR){
            console.error(now + ": " + txt);
            serverLog(txt);
        } else {
            console.log(now + ": " + txt);
        }
    }
}

function getCurrentTime(){
    const date = new Date();
    const hours = date.getHours();
    const minutes = "0" + date.getMinutes();
    const seconds = "0" + date.getSeconds();
    const millis = "." + date.getMilliseconds();
    return hours + ":" + minutes.substr(-2) + ":" + seconds.substr(-2) + millis;
}

$(function() {
    $(window).focus(function() {
        var credButton = document.getElementById("credentials");
        inFocus = true;
        checkForFingerprint();
        if (credButton) {
            credButton.onclick=function(){
                verifyCredentials(credentialOptions);
            };
        }
    });

    $(window).blur(function() {
        inFocus = false;
    });
});

function isHidden(el) {
    var hidden = true;
    if (el) {
        hidden = el.offsetParent === null;
    }
    return hidden;
}

async function checkForFingerprint(){
    const UVPAA = await canRegisterCredentials();
    if (UVPAA){
        checkFingerprint(UVPAA, false);
    }
}

document.addEventListener("DOMContentLoaded", function(){
    try{
        initialize();
    }catch (e){
        serverLog(e.message);
    }
});

function signInWithPush(){
    location.href="/signInWithPush";
}

function initialize(){
    setEndpoint();
    hideCredentialsButtonIfNeeded();
    if (document.hasFocus()){
        startBlue2FactorHome();
    } else {
        checkForSuccessfulPush();
        checkFingerprintOnce();
        setTimeout(function() {
            showRepushButton();
        }, 3000);
    }
}

function openDb(){
    return  new Promise (function (resolve, reject) {
        window.indexedDB = window.indexedDB || window.mozIndexedDB ||
            window.webkitIndexedDB ||  window.msIndexedDB || window.shimIndexedDB;
        if (window.indexedDB) {
            const request = window.indexedDB.open("blue2factor", DB_VERSION);
            request.onupgradeneeded = function() {
                upgradeDb(request);
            };
            request.onerror = function() {
                cLog("IndexedDb error", ERROR);
                reject(request.error);
            };
            request.onsuccess = function() {
                cLog("openDb success");
                resolve(request.result);
            };
            request.onblocked = function() {
                cLog("pending till unblocked", WARN);
            };
        } else {
            return false;
        }
    });
}

function deleteDb(){
    return  new Promise (function (resolve) {
        const indexedDB = window.indexedDB || window.mozIndexedDB ||
              window.webkitIndexedDB || window.msIndexedDB || window.shimIndexedDB;
        if (indexedDB) {
            try {
                const deleteRequest = window.indexedDB.deleteDatabase(STORE_NAME);
                deleteRequest.onerror = function() {
                    cLog("Error deleting database.");
                    resolve(false);
                };

                deleteRequest.onsuccess = function() {
                    cLog("Database deleted successfully");
                    resolve(true);
                };
            }catch (error){
                cLog("db probably doesn't exist yet.");
                resolve(false);
            }
        } else {
            resolve(false);
        }
    });
}

async function upgradeDb(request){
    try {
        const db = request.result;
        await db.createObjectStore(STORE_NAME, {keyPath: "keyType"});
    }catch(e) {
        serverLog(e.message);
    }
}

document.addEventListener("visibilitychange", function() {
    const newVisibilityState = document.visibilityState;
    cLog("new visibility state: " + newVisibilityState, INFO);
    if (visibilityState !== newVisibilityState) {
        switch (newVisibilityState) {
            case "hidden":
                break;
            case "prerender":
                checkFingerprint();
                break;
            case "visible":
                checkFingerprint();
                break;
        }
    }
});

function popupShowing(){
    var showing = false;
    try {
        const popupElement = document.getElementById("popup");
        const hidden = popupElement.style.display;
        showing = hidden === "block";
    } catch(error) {
        serverLog(error.message);
    }
    return showing;
}

async function startBlue2FactorHome(){
    const UVPAA = await canRegisterCredentials();
    if (UVPAA){
        checkForSuccessfulPush();
        checkFingerprintWithOnce(UVPAA, false);
    } else {
        checkForSuccessfulPush();
        setTimeout(function() {
            showRepushButton();
        }, 3000);
    }
}

function showRepushButton(){
    var pushAgain = document.getElementById("pushAgain");
    if (pushAgain) {
        pushAgain.style.visibility = "visible";
    }
}

async function canRegisterCredentials(){
    if (window.PublicKeyCredential && isCentral()) {
        const UVPAA = await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();
        return UVPAA;
    }
}

async function checkFingerprint(){
    const UVPAA = await canRegisterCredentials();
    if (UVPAA){
        checkFingerprintWithOnce(UVPAA, false);
    }
}

async function checkFingerprintOnce(){
    const UVPAA = await canRegisterCredentials();
    if (UVPAA){
        checkFingerprintWithOnce(UVPAA, true);
    }
}

function checkFingerprintWithOnce(UVPAA, once){
    if (window.PublicKeyCredential) {
        if (UVPAA) {
            if (firstTry && !checkingFingerprint) {
                firstTry = false;
                checkingFingerprint = true;
                const secOptions = JSON.stringify({
                    "attestation": "none",
                    "authenticatorSelection": {
                        "authenticatorAttachment": "platform",
                        "requireResidentKey": true,
                        "userVerification": "required"
                    },
                    "browserToken": getBrowserToken(),
                    "credId": localStorage.getItem("b2fcredid"),
                    "reqUrl": window.location.href
                });
                getFingerprintParameters(secOptions, UVPAA, once);
            }
        }
    }
}

function isCentral(){
    const element =  document.getElementById("biometrics");
    return (typeof(element) !== "undefined" && element !== null);
}

function getFingerprintParameters(options, UVPAA, once){
    fetch(getEndpoint() + "/fpAuth", {
        body:options,
        credentials: "omit",
        headers: {
            "Accept": "application/json",
            "Access-Control-Allow-Origin": getControlOrigin(),
            "Content-Type": "application/json; charset=utf-8"
        },
        method: "POST"
    })
    .then((response) => response.json())
    .then(function (data) {
        var cred;
        if (data) {
            if (data.allowCredentials) {
                credentialOptions = data;
                if (data.allowCredentials.length > 0){
                    data.allowCredentials.forEach(function (cred) {
                        cred.id = window.base64url.decode(cred.id);
                    });
                    data.challenge = window.base64url.decode(data.challenge);
                    data.extention = {uvm: true};
                    if (!once) {
                        verifyCredentials(data);
                    }
                } else {
                    checkingFingerprint = false;
                }
            } else {
                checkingFingerprint = false;
            }
        } else {
            checkingFingerprint = false;
            cLog("mothereffer");
        }
        if (!once) {
            checkFingerprint(UVPAA, false);
        }
    }).catch(function (err) {
        checkingFingerprint = false;
        serverLog(err.message);
        checkFingerprint(UVPAA, false);
    });
}

async function hideCredentialsButtonIfNeeded(){
    var credButton = document.getElementById("credentials");
    if (!isHidden(credButton)) {
        const UVPAA = await canRegisterCredentials();
        if (!UVPAA){
            credButton.style.visibility = "hidden";
        } else {
            checkForFingerprint();
            credButton.onclick=function(){
                verifyCredentials(credentialOptions);
            };
        }
    }
}

async function verifyCredentials(options){
    printObject(options);
    fpRequest = true;
    credentialOptions = options;
    try {
        const cred = await navigator.credentials.get({
            publicKey: options
        });
        fpRequest = false;
        const credential = {};
        credential.id = cred.id;
        credential.type = cred.type;
        credential.rawId =  window.base64url.encode(cred.rawId);
        if (cred.response) {
            const clientDataJSON = window.base64url.encode(
                                        cred.response.clientDataJSON);
            const authenticatorData = window.base64url.encode(cred.response.authenticatorData);
            cLog("post encode: " + authenticatorData);
            const signature = window.base64url.encode(cred.response.signature);
            const userHandle = window.base64url.encode(cred.response.userHandle);
            credential.response = {
                clientDataJSON,
                authenticatorData,
                signature,
                userHandle
            };
            credential.browserSession = getBrowserSession();
            credential.reqUrl = window.location.href;
            cLog(JSON.stringify(credential));
            await confirmOnServer(credential);
        } else {
            checkingFingerprint = false;
        }
    }catch (error) {
        checkingFingerprint = false;
        serverLog(error.message);
        if (firstFingerprintError){
            firstFingerprintError = false;
        }/*else {
            reloadWithoutParameters();
        }*/
    }
}

function printObject(obj){
    var key;
    try {
	    obj.forEach(function (key) {
	        if (obj.hasOwnProperty(key)) {
	            console.log(key, obj[key]);
	        }
	    });
    } catch (e) {
		serverLog(e.message);
	}
}

function confirmOnServer(options){
    const dataToSend = JSON.stringify(options);
    fetch(getEndpoint() + "/fpAuthComplete", {
        headers: {
            "Content-Type": "application/json; charset=utf-8",
            "Accept": "application/json",
            "Access-Control-Allow-Origin": getControlOrigin()
        },
        method:"post",
        body:dataToSend
    })
    .then((response) => response.json())
    .then(async function (data) {
        const outcome = data.outcome;
        if (outcome === SUCCESS) {
            await validateToken(data.reason);
            reloadWithoutParameters();
        }
        checkingFingerprint = false;
        return;
    }).catch(function (err) {
        checkingFingerprint = false;
        serverLog(err.message);
        return;
    });
}

async function validateToken(browserSession){
    const encToken = await encryptToken(browserSession);
    const dataToSend = JSON.stringify({"token": getBrowserToken(),
            "reqType": "confirm", "encryptedSession": encToken,
            "userAgent": "", "reqUrl": window.location.href, "fromBrowser": true,
            "companyIdFromUrl": getCompanyIdFromUrl()});
    fetch(getEndpoint() + "/b2fBrowser", {
        headers: {
            "Content-Type": "application/json; charset=utf-8",
            "Accept": "application/json",
            "Access-Control-Allow-Origin": getControlOrigin()
        },
        method:"post",
        body:dataToSend
    })
    .then((response) => response.json())
    .then(function (data) {
        if (data.outcome === SUCCESS) {
            setBrowserSession(browserSession);
        }
        cLog("validate success: " + data.outcome);
        return data.outcome;
    }).catch(function (err) {
        serverLog(err.message);
    });
}

async function encryptToken(browserSession) {
    var enc;
    const pubKey = await getPublicKeyFromIdb();
    if (pubKey) {
        const textEncoder = new TextEncoder();
        const textEncodedString =  textEncoder.encode(browserSession);
        cLog("textEncoded: '" + textEncodedString + "'\r\nlength: " +
                            textEncodedString.length);
        const encryptedArrayBuffer = await crypto.subtle.encrypt({name: "RSA-OAEP"},
                            pubKey, textEncodedString);
        const bytes = new Uint8Array(encryptedArrayBuffer);
        enc = arrayBufferToBase64(bytes);
        cLog("msg: " + enc + "'\r\nlength: " + enc.length);
    } else {
        await generateKeysAndExport();
        if (await getPublicKeyFromIdb()){
        //redundant, but I was having problems -- cjm
            enc = encryptToken(browserSession);
        }
    }
   return enc;
}

async function generateKeysAndExport() {
    const [publicOutcome] = await Promise.all([generateAndSendBrowserKey()]);
    return publicOutcome === true;
}

async function generateAndSendBrowserKey() {
    //Generate a key pair and send the public key to the server
    var success = false;
    try {
        const browserKey = await window.crypto.subtle.generateKey (
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([0x01, 0x00, 0x01]),
                hash: {name: "SHA-256"}
            },
            false,
            ["encrypt", "decrypt"]
        );
        await setPrivateKeyInIdb(browserKey.privateKey);
        const keyData = await window.crypto.subtle.exportKey(
            "jwk",
            browserKey.publicKey
        );
        sendPublicKeyData(keyData);
        cLog("server key was successfully created");
        success = true;
    }catch (error) {
        serverLog("generateAndSendBrowserKey: " + error);
    }
    return success;
}

function sendPublicKeyData(keyData){
    const dataToSend = JSON.stringify({"browserSession":
        getBrowserSession(), "browserToken": getBrowserToken(),
        "jwk": keyData, "coKey": "", "cmd":
        "createBrowserKey", "reqUrl": window.location.href, "fromJs": true});
    fetch(getEndpoint() + "/b2f-f1", {
        headers: {
            "Content-Type": "application/json; charset=utf-8",
            "Accept": "application/json",
            "Connection": "Keep-Alive",
            "Access-Control-Allow-Origin": getControlOrigin(),
            "Keep-Alive": "timeout=0, max=300"
        },
        credentials: "omit",
        method: "POST",
        body:dataToSend
    })
    .then((response) => response.json())
    .then(async function (data) {
        const outcome = data.outcome;
        cLog("submit success: " + data.outcome);
        if (outcome === SUCCESS){
            cLog("public key set");
            const pem = data.token;
            await importRsaKey(pem);
        }
        return data;
    }).catch(function (err) {
        return serverLog(err.message);
    });
}

async function importRsaKey(pem) {
    // fetch the part of the PEM string between header and footer
    const pemHeader = "-----BEGIN PUBLIC KEY-----";
    const pemFooter = "-----END PUBLIC KEY-----";
    if (pem.indexOf(pemHeader) !== -1 && pem.indexOf(pemFooter) !== -1){
        pem = pem.substring(pemHeader.length, pem.length - pemFooter.length - 1);
    }
    const pemContentsUpd = pem.replace(/[\r\n]+/g,"");
    const keyAsArrayBuffer = base64StringToArrayBuffer(pemContentsUpd);

    const serverKey = await window.crypto.subtle.importKey(
            "spki",
            keyAsArrayBuffer,
        {
            name: "RSA-OAEP",
            hash: {name: "SHA-256"}
        },
        false,
        ["encrypt"]
    );
    await setPublicKeyInIdb(serverKey);
    cLog("serverKey set");
}

function base64StringToArrayBuffer(b64str) {
    var i;
    var bytes;
    const byteStr = atob(b64str);
    bytes = new Uint8Array(byteStr.length);
    for (i = 0; i < byteStr.length; i+=1) {
        bytes[i] = byteStr.charCodeAt(i);
    }
    return bytes.buffer;
}

async function setPrivateKeyInIdb(privateKey){
    var success = false;
    var conn;
    try {
        conn = await openDb();
        success = await setKeyInDb(conn, "localKey", privateKey);
    }finally {
        if(conn) {
            try {
                conn.close();
            }catch (error) {
                cLog("connection not closed");
            }
        }
    }
    return success;
}

async function setPublicKeyInIdb(publicKey){
    var success = false;
    var conn;
    try {
        conn = await openDb();
        success = await setKeyInDb(conn, "serverKey", publicKey);
    }finally {
        if(conn) {
            try {
                conn.close();
            }catch (error) {
                cLog("connection not closed");
            }
        }
    }
    return success;
}

function setKeyInDb(conn, name, keyValue){
    return new Promise(function (resolve, reject) {
        const transaction = conn.transaction(STORE_NAME, "readwrite");
        const objectStore = transaction.objectStore(STORE_NAME);
        var newKey = {
            keyType: name,
            keyVal: keyValue,
            createDate: new Date()
        };
        const request = objectStore.put(newKey);
        request.onsuccess = function(){
            cLog("successfully put " + name + " in indexedDb");
            resolve(true);
        };
        request.onerror = function(){
            serverLog("failed to put " + name + " in indexedDb");
            reject(false);
        };
    });
}

async function getPublicKeyFromIdb(){
    var key;
    var conn;
    try {
        conn = await openDb();
        const result = await getKey(conn, "serverKey");
        if (result !== undefined) {
            key = result.keyVal;
        } else {
            serverLog("private key not found", WARN);
        }
    }finally {
        if(conn) {
            try {
                conn.close();
            }catch (error) {
                cLog("connection not closed");
            }
        }
    }
    return key;
}

function arrayBufferToBase64(buffer) {
    var binary = "";
    var i;
    const bytes = new Uint8Array(buffer);
    const len = bytes.byteLength;
    for (i = 0; i < len; i+=1) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
}


async function checkForSuccessfulPush(){
    if (!checkingForPush) {
        checkingForPush = true;
        const browserToken = getBrowserToken();
        const browserSession = await getEncryptedSession();
        const dataToSend = JSON.stringify({"encryptedSession": browserSession,
                                           "token": browserToken, "reqType": "factor2Check",
                                           "userAgent": "", "reqUrl": window.location.href,
                                           "fromBrowser": true, "companyIdFromUrl":
                                           getCompanyIdFromUrl()});
        fetch(getEndpoint() + "/b2fBrowser", {
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "Accept": "application/json",
                "Access-Control-Allow-Origin": getControlOrigin()
            },
            credentials: "omit",
            method: "POST",
            body:dataToSend
        })
        .then((response) => response.json())
        .then(async function (data) {
            checkingForPush = false;
            cLog("checkForSuccessfulPush success: " + data.outcome);
            if (data.outcome === SUCCESS) {
                reloadWithoutParameters();
            } else if (data.outcome === 1) {
                rerunCount+=1;
                if (rerunCount < 300) {
                    rerunQuickly();
                } else {
                    rerunSlowly();
                }
            } else if (data.outcome === 2 && data.reason === "decryptionError"){
                await generateKeysAndExport();
                checkForSuccessfulPush();
            }
            return;
        }).catch(function (err) {
            checkingForPush = false;
            return serverLog(err.message);
        });
    }
}

function getCompanyIdFromUrl(){
    var companyId = "";
    const path = window.location.pathname;
    const urlSplit = path.split("failure/");
    if (urlSplit.length === 2){
        companyId = urlSplit[1].split("/reset")[0];
        companyId = companyId.split("/recheck")[0];
    }
    return companyId;
}

function reloadWithoutParameters(){
    const reload = getUrlWithoutParameters();
    if (reload !== undefined) {
        window.location = reload;
    }
}

function getUrlWithoutParameters(){
    var nextUrl;
    const fromIdp = document.getElementById("fromIdp");
    if (fromIdp) {
        const fromIdpValue = fromIdp.textContent;
        if (fromIdpValue !== "true" && fromIdpValue !== true) {
            const urlArray = window.location.href.split("?");
            nextUrl = urlArray[0];
        } else {
            document.getElementById("samlForm").submit();
        }
    } else {
        document.getElementById("samlForm").submit();
    }
    return nextUrl;
}

function rerunQuickly(){
    setTimeout(function() {
        checkForSuccessfulPush();
    }, 300);
}

function rerunSlowly(){
    setTimeout(function() {
        checkForSuccessfulPush();
    }, 2000);
}

async function getEncryptedSession(){
    const browserSession = getBrowserSession();
    var enc;
    if (browserSession) {
        enc = await encryptToken(browserSession);
    }
    return enc;
}

function getBrowserSession(){
    var session = localStorage.getItem("B2F_AUTH");
    if (!session) {
        session = sessionStorage.getItem("B2F_AUTH");
    }
    return session;
}

function setBrowserSession(browserSession){
    localStorage.getItem("B2F_AUTH", browserSession);
}

function getBrowserToken(){
    var token = localStorage.getItem("B2F_TOKEN");
    if (!token) {
        token = sessionStorage.getItem("B2F_TOKEN");
    }
    return token;
}

function getKey(conn, name){
    return new Promise(function (resolve, reject) {
        const transaction = conn.transaction(["b2fkeys"], "readwrite");
        const objectStore = transaction.objectStore("b2fkeys");
        const request = objectStore.get(name);
        request.onsuccess = function(){
            cLog("getKey success");
            resolve(request.result);
        };
        request.onerror = function() {
            serverLog("error in getKey:" + request.error);
            reject(request.error);
        };
    });
}

function serverLog(text) {
     const dataToSend = JSON.stringify({"token": text, "reqType": "log",
                                        "encryptedSession": "", "reqUrl": window.location.href,
                                        "fromBrowser": true, "companyIdFromUrl": ""});
    fetch(getEndpoint() + "/b2fBrowser", {
        headers: {
            "Content-Type": "application/json; charset=utf-8",
            "Accept": "application/json",
            "Access-Control-Allow-Origin": getControlOrigin()
        },
        credentials: "omit",
        method: "POST",
        body:dataToSend
    });
}

function getControlOrigin(){
    var ep = ENDPT;
    if (lhTest){
       ep = "*";
    }
    return ep;
}

$("#pushAgain").click(function() {
    sendLoudPush();
});

async function sendLoudPush(){
    const browserSession = await getEncryptedSession();
    if (browserSession) {
        const dataToSend = JSON.stringify({"token": getBrowserToken(),
            "reqType": "sendPush", "encryptedSession": browserSession,
            "userAgent": "", "reqUrl": window.location.href, "fromBrowser": true,
            "companyIdFromUrl": getCompanyIdFromUrl()});
        fetch(getEndpoint() + "/b2fBrowser", {
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "Accept": "application/json",
                "Access-Control-Allow-Origin": getControlOrigin()
            },
            credentials: "omit",
            method: "POST",
            body:dataToSend
        })
        .then((response) => response.json())
        .then(function (data) {
            console.log("sendPush success: " + data.outcome);
            return;
        }).catch(function (err) {
            serverLog(err.message);
        });
    }
}
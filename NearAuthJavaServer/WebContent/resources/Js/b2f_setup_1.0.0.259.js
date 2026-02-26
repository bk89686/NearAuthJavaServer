/*jshint esversion: 8 */
var lhTest = false;
var ENDPT = "";
var firstTry = true;
const SUCCESS = 0;
const TEMPORARY = 4;
const MULTIUSER = 14;
var fromFailure = false;
var setupAlready = false;
const DB_VERSION = 16;
const TRACE = 0;
const LOG = 1;
const INFO = 2;
const WARN = 3;
const ERROR = 4;
const CONSOLE_LEVEL = TRACE;
const STORE_NAME = "b2fkeys";
const DB_NAME = "blue2factor";
var credentialOptions;
var pushStartTime;
var rerunCount = 0;

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
    var now;
    if (lvl === undefined){
        lvl = TRACE;
    }
    if (CONSOLE_LEVEL <= lvl){
        now = getCurrentTime();
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

document.addEventListener("DOMContentLoaded", function(){
    startSetup();
});

async function startSetup(){
    setEndpoint();
    const oldBrowserSession = getBrowserStorage();
    if (oldBrowserSession && oldBrowserSession !== ""){
        setupAlready = isAlreadySetup(oldBrowserSession);
    }
    if (!setupAlready){
        await deleteDb();
    }
    setupB2f(getBleNotAvailable());
}

function getBleNotAvailable(){
    var notAvail = false;
    const ble = document.getElementById("ble");
    if (ble) {
        notAvail = !ble.value;
    }
    return notAvail;
}

function openDb(){
    return  new Promise (function(resolve, reject) {
        window.indexedDB = window.indexedDB || window.mozIndexedDB || window.webkitIndexedDB ||
                window.msIndexedDB || window.shimIndexedDB;
        if (window.indexedDB) {
            const request = window.indexedDB.open(DB_NAME, DB_VERSION);
            request.onupgradeneeded = function() {
                upgradeDb(request);
            };
            request.onerror = function() {
                serverLog("IndexedDb error");
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
        const indexedDB = window.indexedDB || window.mozIndexedDB || window.webkitIndexedDB ||
                window.msIndexedDB || window.shimIndexedDB;
        if (indexedDB) {
            try {
                const deleteRequest = window.indexedDB.deleteDatabase(DB_NAME);
                deleteRequest.onerror = function() {
                    cLog("Error deleting database.");
                    resolve(false);
                };
                deleteRequest.onsuccess = function() {
                    cLog("Database deleted successfully");
                    resolve(true);
                };
            } catch (error){
                cLog("db probably doesn't exist yet.");
                resolve(false);
            }
        } else {
            resolve(false);
        }
    });
}

function upgradeDb(request){
    try {
        //await deleteDb();
        const db = request.result;
        db.createObjectStore(STORE_NAME, {keyPath: "keyType"});
    } catch(e) {
        serverLog("upgradeDb: " + e);
    }
}

function showFailure(){
    var childDiv;
    var elemDiv = document.createElement("div");
    elemDiv.style.cssText = "position:fixed;width:100%;height:100%;z-index:100000;background-color:#3F51B5;top:0;left:0;";
    elemDiv.setAttribute("id", "b2fOverlay");
    document.body.appendChild(elemDiv);
    childDiv = document.createElement("div");
    childDiv.style.cssText = "position:relative;margin-left:auto;margin-right:auto;margin-top:125px;" +
                                    "text-align:center;color:white;font-size:1.5em;";
    childDiv.textContent = "Your browser does not support NearAuth.ai. Please update your browser and select " +
                                    "Add Browser in the NearAuth.ai app.";
    serverLog("This browser doesn't support IndexedDB");
}

function setupB2f(andVerify, showText){
    const setupToken = getSetupToken();
    console.info("setup: " + setupToken);
    if (setupToken){
        showPleaseWait(showText);
        submitSetup(setupToken, andVerify);
    }
}

function showPleaseWait(showText){
    var elemDiv;
    var childDiv;
    var childDiv1;
    var childDiv1a;
    var childDiv2;
    var childDiv1a1;
    var childDiv1a1a;
    var credButton;
    try {
        elemDiv = document.createElement("div");
        elemDiv.style.cssText = "position:fixed;width:100%;height:100%;" +
            "z-index:100000;background-color:#3F51B5;top:0;left:0;";
        elemDiv.setAttribute("id", "b2fOverlay");
        document.body.appendChild(elemDiv);
        childDiv = document.createElement("div");
        childDiv.setAttribute("id", "mainText");
        childDiv.style.cssText = "position:relative;margin-left:auto;" +
                        "margin-right:auto;margin-top:125px;" +
                        "text-align:center;color:white;font-size:1.5em;";
        if (showText) {
            childDiv.textContent = "Please wait while NearAuth.ai sets up.  This " +
                        "may take a minute or two, and you may be asked to provide additional verification.";
        }
        childDiv1 = document.createElement("div");
        childDiv1.setAttribute("id", "pleaseWaitDiv");
        childDiv1.style.cssText = "position:relative;margin-left:auto;" +
                        "margin-right:auto;margin-top:25px;" +
                        "text-align:center;";
        childDiv1a = document.createElement("div");
        childDiv1a.classList.add("pleaseWait-stripe");
        childDiv1a1 = document.createElement("div");
        childDiv1a1.classList.add("pleaseWait");
        childDiv1a1a = document.createElement("div");

        childDiv2 = document.createElement("div");
        childDiv2.setAttribute("id", "createCredentialsDiv");
        childDiv2.style.cssText = "position:relative;margin-left:auto;" +
                        "margin-right:auto;margin-top:25px;" +
                        "text-align:center;visibility:hidden;";

        credButton = document.createElement("button");
        credButton.setAttribute("id", "createCredentials");
        credButton.classList.add("companyTableButton");
        credButton.textContent = "Enter Credentials";
        credButton.onclick=function(){
            createCredentials(credentialOptions);
        };
        document.getElementById("b2fOverlay").appendChild(childDiv);
        document.getElementById("b2fOverlay").appendChild(childDiv1);
        childDiv1.appendChild(childDiv1a);
        childDiv1a.appendChild(childDiv1a1);
        childDiv1a1.appendChild(childDiv1a1a);
        document.getElementById("b2fOverlay").appendChild(childDiv2);
        childDiv2.appendChild(credButton);
    } catch(error) {
        serverLog("showPleaseWait: " + error);
    }
}

function showRespondToPush(){
    var elem;
    try {
        elem = document.getElementById("mainText");
        elem.textContent = "To continue, respond to the push we " +
                        "sent to your phone.";
        pushStartTime = new Date();
    } catch(error) {
        serverLog("showRespondToPush: " + error);
    }
}

function showPushTimedOut(){
    var elem;
    try {
        elem = document.getElementById("mainText");
        elem.textContent = "The push we sent to your phone " +
                        "timed out.";
    } catch(error) {
        serverLog("showPushTimedOut: " + error);
    }
}

function isAlreadySetup(){
    var alreadySetup = false;
    try {
        const elem = document.getElementById("b2fSetup");
        const val = elem.value;
        const arr = val.split('\*\*');
        if (arr.length > 2){
            if (arr[2] === "false") {
                alreadySetup = true;
            }
        }
    } catch (error) {
        serverLog("isAlreadySetup:" + error);
    }
    serverLog("isAlreadySetup: " + alreadySetup);
    return alreadySetup;
}

function submitSetup(token, verify){
    cLog("submitSetup");
    const browserSession = randomUuid();
    setBrowserSession(browserSession);
    cLog("set session");
    const dataToSend = JSON.stringify({"token": token, "reqType":
        "setup", "encryptedSession": browserSession,
        "reqUrl": window.location.href, "fromBrowser": true, "companyIdFromUrl": ""});
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
        return handleSetup(data, verify);
    }).catch(function (err) {
        setBrowserSession("");
        return serverLog("submitSetup error: " + err);
    });
}

function serverLog(text) {
     const dataToSend = JSON.stringify({"token": text, "reqType":
        "log", "encryptedSession": "",
        "reqUrl": window.location.href, "fromBrowser": true, "companyIdFromUrl": ""});
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

async function handleSetup(data, andVerify){
    const outcome = data.outcome;
    const reason = data.session;
    cLog("submit success: " + outcome);
    if (outcome === SUCCESS){
        cLog("session: " + data.reason);
        setBrowserToken(data.token);
        if (!andVerify) {
            if (!fromFailure && data.reason) {
                serverLog("setup success, setting JWT to " + data.jwt);
                setJwt(data.jwt);
            } else {
                serverLog("setup success, from failure.");
            }
            if (!setupAlready) {
                startKeyGeneration(data, andVerify);
            } else {
                sendToLandingPage();
            }
        } else {
            startKeyGeneration(data, andVerify);
        }
    } else if (outcome === TEMPORARY || outcome === MULTIUSER) {
        serverLog("setting up temp device");
        if (outcome === TEMPORARY) {
            showRespondToPush();
        }
        setTempBrowserToken(data.token);
        resetTempBrowserSession();
        if (!fromFailure && data.reason) {
            setJwt(data.jwt);
        }
        await startKeyGenerationForTemp();
        checkForSuccessfulPush();
    } else {
        serverLog("setup failure, reason: " + reason);
    }
    return outcome;
}

async function checkForSuccessfulPush(){
    const browserToken = getBrowserToken();
    const browserSession = await getEncryptedSession();
    const dataToSend = JSON.stringify({
        "companyIdFromUrl": "",
        "encryptedSession": browserSession,
        "fromBrowser": true,
        "reqType": "factor2Check",
        "reqUrl": window.location.href,
        "token": browserToken,
        "userAgent": ""
    });
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
        cLog("checkForSuccessfulPush success: " + data.outcome);
        if (data.outcome === SUCCESS) {
            sendToLandingPage();
        } else if (data.outcome === 1) {
            if (!isTimedOut()){
                rerunCount+=1;
                if (rerunCount < 300) {
                    rerunQuickly();
                } else {
                    rerunSlowly();
                }
            } else {
                showPushTimedOut();
            }
        } else if (data.outcome === 2 && data.reason === "decryptionError"){
            await generateKeysAndExport(data, false);
            checkForSuccessfulPush();
        }
        return;
    }).catch(function (err) {
        return serverLog("validate error: " + err.message);
    });
}


function isTimedOut(){
    var timedOut = false;
    const now = new Date();
    if (pushStartTime){
        const seconds = (now - pushStartTime) / 1000;
        if (seconds > 123) {
            timedOut = true;
        }
    }
    return timedOut;
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
    var enc;
    const browserSession = getBrowserSession();
    if (browserSession) {
        enc = await encryptToken(browserSession);
    }
    return enc;
}

async function encryptToken(browserSession) {
    var enc;
    const pubKey = await getPublicKeyFromIdb();
    if (pubKey) {
        const textEncoder = new TextEncoder();
        const textEncodedString =  textEncoder.encode(browserSession);
        cLog("textEncoded: '" + textEncodedString + "'\r\nlength: " +
                            textEncodedString.length);
        const keyData = await window.crypto.subtle.exportKey(
            "spki", pubKey
        );
        cLog("using publicKey:\n" + keyData);
        const encryptedArrayBuffer = await crypto.subtle.encrypt({name: "RSA-OAEP"},
                            pubKey, textEncodedString);
        const bytes = new Uint8Array(encryptedArrayBuffer);
        enc = arrayBufferToBase64(bytes);
        cLog("msg: " + enc + "'\r\nlength: " + enc.length);
    }
   return enc;
}

function arrayBufferToBase64(buffer) {
    var i;
    var binary = "";
    const bytes = new Uint8Array(buffer);
    const len = bytes.byteLength;
    for (i = 0; i < len; i+=1) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
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
            cLog("public key not found", WARN);
        }
    } finally {
        if(conn) {
            try {
                conn.close();
            } catch (error) {
                cLog("connection not closed");
            }
        }
    }
    return key;
}


function getKey(conn, name){
    return new Promise(function (resolve, reject) {
        var transaction = conn.transaction(["b2fkeys"], "readwrite");
        var objectStore = transaction.objectStore("b2fkeys");
        var request = objectStore.get(name);
        request.onsuccess = function(){
            cLog("getKey success");
            resolve(request.result);
        };
        request.onerror = function() {
            cLog("getKey error");
            reject(request.error);
        };
    });
}

async function startKeyGenerationForTemp(){
    if (!("indexedDB" in window)) {
        cLog("This browser doesn't support IndexedDB");
        return;
    }
    await generateAndSendBrowserKey();
}

async function startKeyGeneration(data, andVerify){
    if (!("indexedDB" in window)) {
        cLog("This browser doesn't support IndexedDB");
        return;
    }
    await generateKeysAndExport(data, andVerify);
}

async function generateKeysAndExport(data, andVerify) {
    const [publicOutcome] = await Promise.all([generateAndSendBrowserKey()]);
    if (publicOutcome === true) {
        await registerCredentials(data, andVerify);
    } else {
        serverLog("outcome from generate and send browser key was false");
    }
}

function registerCredentials(data, andVerify){
    try {
        if (window.PublicKeyCredential) {
            PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable()
                .then(function(available){
                    if(available){
                        cLog("public key is available");
                          registerCredential(getBrowserSession());
                    } else {
                        cLog("public key not available");
                        reportNoPasskeys();
                        if (!andVerify){
                              sendToLandingPage();
                          } else {
                            showRespondToPush();
                            if (!fromFailure && data.reason) {
                                setJwt(data.jwt);
                            }
                            checkForSuccessfulPush();
                        }
                    }
                  }).catch(function(err){
                    console.error(err);
                  });
        } else {
            reportNoPasskeys();
            sendToLandingPage();
        }
    } catch (error) {
        serverLog(error);
        sendToLandingPage();
    }
}

function reportNoPasskeys(){
    const dataToSend = JSON.stringify({
        "browserSession": getBrowserSession(),
        "browserToken": getBrowserToken(),
        "cmd": "passkeyNotAvail",
        "coKey": "",
        "fromJs": true,
        "jwk": "",
        "reqUrl": window.location.href
    });
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
    .then(function (data){
        console.log("passkey not found");
    }).catch(function (err) {
        return serverLog("submitSetup PublicKey error: " + err);
    });
}

function sendToLandingPage(){
    serverLog("sending to: " + document.getElementById("setupForm").action);
    document.getElementById("setupForm").submit();
}

async function generateAndSendBrowserKey() {
    //Generate a key pair and send the public key to the server
    var success = false;
    var browserKey;
    try {
        browserKey = await window.crypto.subtle.generateKey (
            {
                hash: {name: "SHA-256"},
                modulusLength: 2048,
                name: "RSA-OAEP",
                publicExponent: new Uint8Array([0x01, 0x00, 0x01])
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
    } catch (error) {
        serverLog("generateAndSendBrowserKey: " + error);
    }
    return success;
}

function sendPublicKeyData(keyData){
    const dataToSend = JSON.stringify({
        "browserSession": getBrowserSession(),
        "browserToken": getBrowserToken(),
        "cmd": "createBrowserKey",
        "coKey": "",
        "fromJs": true,
        "jwk": keyData,
        "reqUrl": window.location.href
    });
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
    .then(async function(data) {
        const outcome = data.outcome;
        cLog("submit success: " + data.outcome);
        if (outcome === SUCCESS){
            cLog("public key set");
            const pem = data.token;
            await importRsaKey(pem);
        }
        return data;
    }).catch(function (err){
        return serverLog("submitSetup PublicKey error: " + err);
    });
}

function canRegisterCredentials(){
    if (window.PublicKeyCredential) {
        /*const UVPAA = await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();
        return UVPAA;*/
        PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable()
            .then(function(available){
                if(available){
                      return true;
                } else {
                      return false;
                }
              }).catch(function(err){
                console.error(err);
              });
    }
}

function isCentral(){
    const central = document.getElementById("central").textContent;
    return central === "true";
}

async function registerCredential(browserSession){
    const secOptions = JSON.stringify({
            "attestation": "none",
            "authenticatorSelection": {
                "authenticatorAttachment": "platform",
                "userVerification": "required",
                "requireResidentKey": false
            },
            "browserSession": browserSession,
            "reqUrl": window.location.href
    });
    getRegistrationData(secOptions);
}

function getRegistrationData(dataToSend){
    fetch(getEndpoint() + "/fpReg", {
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
        if (data) {
            printObject(data);
            validateRegistration(data);
        } else {
            serverLog("mothereffer");
        }
    }).catch(function (err) {
        return serverLog("registration error: " + err);
    });
}

async function validateRegistration(options){
    const userId = options.user.id;
    options.user.id = window.base64url.decode(userId);
    options.challenge = window.base64url.decode(options.challenge);
    if (options.excludeCredentials) {
        options.excludeCredentials.forEach( function (cred) {
            cred.id = window.base64url.decode(cred.id);
        });
    }
    await createCredentials(options);
}

//getting the fingerprint stuff
async function createCredentials(options){
    try{
        serverLog("creating credentials");
        credentialOptions = options;
        const cred = await navigator.credentials.create({
            publicKey: options
        });

        const credential = {};
        credential.id = cred.id;
        credential.rawId = window.base64url.encode(cred.rawId);
        credential.type = cred.type;

        if (cred.response) {
            const clientDataJSON = window.base64url.encode(cred.response.clientDataJSON);
            const attestationObject = window.base64url.encode(cred.response.attestationObject);
            const pubKeyCredParams = options.pubKeyCredParams;
            credential.response = {
                  clientDataJSON,
                  attestationObject,
                  pubKeyCredParams
            };
        }
        credential.rpId = options.rp.id;
        credential.rpName = options.rp.name;
        credential.browserSession = getBrowserSession();
        credential.reqUrl = window.location.href;
        localStorage.setItem("b2fcredid", credential.id);
        confirmRegistration(JSON.stringify(credential));
    } catch(error) {
        serverLog("setupError: " + error);
        if (error instanceof DOMException) {
            if (error.name === "NotAllowedError") {
                sendToLandingPage();
            } else if (error.name === "InvalidStateError") {
                alert("these credentials already exist");
                sendToLandingPage();
            }
        }
    }
}

function showCreateCredentialsButton(){
    var credButtonDiv = document.getElementById("createCredentialsDiv");
    var credButton = document.getElementById("createCredentials");
    var pleaseWait = document.getElementById("pleaseWaitDiv");
    credButtonDiv.style.visibility = "visible";
    credButton.style.visibility = "visible";
    pleaseWait.style.visibility = "hidden";
}

function confirmRegistration(dataToSend){
    serverLog("prior to confirmReg");
    console.log(dataToSend);
    fetch(getEndpoint() + "/b2fRegSub", {
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
        serverLog("resp from confirmReg: " + data);
        if (data.outcome === SUCCESS){
            serverLog("successfully saved credentials");
            sendToLandingPage();
        } else {
            serverLog("failed to save credentials", WARN);
        }
    }).catch(function (err) {
        return cLog("registration error: " + err, ERROR);
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
    cLog("importing keyQ\n" + pemContentsUpd);
    const keyAsArrayBuffer = base64StringToArrayBuffer(pemContentsUpd);

    const serverKey = await window.crypto.subtle.importKey(
            "spki",
            keyAsArrayBuffer,
        {
            name: "RSA-OAEP",
            hash: {name: "SHA-256"}
        },
        true, //change this to false (exportable)
        ["encrypt"]
    );
    setPublicKeyInIdb(serverKey);
    serverLog("serverKey set");
}

function base64StringToArrayBuffer(b64str) {
    var i;
    const byteStr = atob(b64str);
    var bytes = new Uint8Array(byteStr.length);
    for (i = 0; i < byteStr.length; i+=1) {
        bytes[i] = byteStr.charCodeAt(i);
    }
    return bytes.buffer;
}


function getSetupToken(){
    return document.getElementById("accessCode").textContent;
}

function setJwt(jwt){
    document.getElementById("B2F_AUTHN").value = jwt;
}

function setTemp(){
    document.getElementById("temp").value = true;
}

async function setPrivateKeyInIdb(privateKey){
    var success = false;
    var conn;
    try {
        conn = await openDb();
        success = await setKeyInDb(conn, "localKey", privateKey);
    } finally {
        if(conn) {
            conn.close();
        }
    }
    return success;
}

async function setPublicKeyInIdb(publicKey){
    var success = false;
    var conn;
    try {
        conn = await openDb();
        debugger;
        success = await setKeyInDb(conn, "serverKey", publicKey);
    } finally {
        if(conn) {
            conn.close();
        }
    }
    return success;
}

function setKeyInDb(conn, name, keyValue){
    return new Promise(function (resolve, reject) {
        const transaction = conn.transaction(STORE_NAME, "readwrite");
        const objectStore = transaction.objectStore(STORE_NAME);
        var newKey = {
            createDate: new Date(),
            keyType: name,
            keyVal: keyValue
        };
        const request = objectStore.put(newKey);
        request.onsuccess = function(){
			debugger;
            cLog("successfully put " + name + " in indexedDb");
            resolve(true);
        };
        request.onerror = function(){
            cLog("failed to put " + name + " in indexedDb");
            reject(false);
        };
    });
}

function getBrowserSession(){
    var browserId;
    try {
        if (typeof(Storage) !== "undefined") {
            browserId = localStorage.getItem("B2F_AUTH");
        }
        if (!browserId) {
            browserId = sessionStorage.getItem("B2F_AUTH");
        }
    } catch (error) {
        serverLog(error.message);
    }
    return browserId;
}

function getBrowserStorage(){
    var browserId;
    try {
        if (typeof(Storage) !== "undefined") {
            browserId = localStorage.getItem("B2F_AUTH");
        }
    } catch (error) {
        serverLog(error.message);
    }
    return browserId;
}

function getBrowserToken(){
    var browserToken;
    if (typeof(Storage) !== "undefined") {
        browserToken = localStorage.getItem("B2F_TOKEN");
    }
    if (!browserToken){
        browserToken = sessionStorage.getItem("B2F_TOKEN");
    }
    return browserToken;
}


function setBrowserSession(b2fId){
    if (typeof(Storage) !== "undefined") {
        localStorage.setItem("B2F_AUTH", b2fId);
    }
}

function setBrowserToken(b2fToken){
    if (typeof(Storage) !== "undefined") {
        localStorage.setItem("B2F_TOKEN", b2fToken);
    }
}

function resetTempBrowserSession(){
    const sess = getBrowserStorage();
    setTempBrowserSession(sess);
    setBrowserSession("");
}

function setTempBrowserSession(b2fId){
    if (typeof(Storage) !== "undefined") {
        sessionStorage.setItem("B2F_AUTH", b2fId);
    }
}

function setTempBrowserToken(b2fToken){
    if (typeof(Storage) !== "undefined") {
        sessionStorage.setItem("B2F_TOKEN", b2fToken);
        localStorage.setItem("B2F_TOKEN", "");
    }
}

function getControlOrigin(){
    var ep = ENDPT;
    if (lhTest){
       ep = "*";
    }
    return ep;
}

function printObject(obj){
	try {
    obj.forEach(function (key) {
        // check if the property/key is defined in the object itself, not in parent
        if (obj.hasOwnProperty(key)) {
            console.log(key, obj[key]);
        }
    });
    } catch(e) {
		serverLog("printObject: " + e.getMessage);
	}
}

function random() {
    const
        fourBytesOn = 0xffffffff, // 4 bytes, all 32 bits on: 4294967295
        c = typeof crypto === "object" ? crypto // node or most browsers
            : typeof msCrypto === "object" ? msCrypto // eslint-disable-line no-undef
                : null; // what old or bad environment are we running in?
        return c ? c.randomBytes ? parseInt(c.randomBytes(4).toString("hex"), 16) / (fourBytesOn + 1) - Number.EPSILON // node
                : c.getRandomValues(new Uint32Array(1))[0] / (fourBytesOn + 1) - Number.EPSILON // browsers
            : Math.random();
}

function randomUuid() { // eslint-disable-line complexity
    // if possible, generate a single random value,
    // 128 bits (16 bytes) in length
    // in an environment where that is not possible,
    // generate and make use of 4 32-bit (4-byte) random values
    // use crypto-grade randomness when available, else Math.random()
    var i;
    const
        c = typeof crypto === "object" ? crypto // node or most browsers
            : typeof msCrypto === "object" ? msCrypto // eslint-disable-line no-undef
            : null; // what old or bad environment are we running in?
    var
        byteArray = c ? c.randomBytes ? c.randomBytes(16) // node
                : c.getRandomValues(new Uint8Array(16)) // browsers
            : null,
        uuid = [ ];

    /* eslint-disable no-bitwise */
    if ( ! byteArray) {
        const
            int = [
                random() * 0xffffffff | 0,
                random() * 0xffffffff | 0,
                random() * 0xffffffff | 0,
                random() * 0xffffffff | 0
            ];
        byteArray = [ ];
        for (i = 0; i < 256; i+=1) {
            byteArray[i] = int[i < 4 ? 0 : i < 8 ? 1 : i < 12 ? 2 : 3] >> i % 4 * 8 & 0xff;
        }
    }
    byteArray[6] = byteArray[6] & 0x0f | 0x40;
    byteArray[8] = byteArray[8] & 0x3f | 0x80;
    for (i = 0; i < 16; ++i) {
        uuid[i] = (byteArray[i] < 16 ? "0" : "") + byteArray[i].toString(16);
    }
    uuid =
        uuid[ 0] + uuid[ 1] + uuid[ 2] + uuid[ 3] + "-" +
        uuid[ 4] + uuid[ 5]                       + "-" +
        uuid[ 6] + uuid[ 7]                       + "-" +
        uuid[ 8] + uuid[ 9]                       + "-" +
        uuid[10] + uuid[11] + uuid[12] + uuid[13] + uuid[14] + uuid[15];
    return uuid;
    /* eslint-enable no-bitwise */
}

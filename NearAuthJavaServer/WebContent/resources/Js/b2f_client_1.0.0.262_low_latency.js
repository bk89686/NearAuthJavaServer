 /*jshint esversion: 8 */
var currentlyValidating = false;
var lastValidation = null;
var visibilityState = "prerender";
var ENDPT = "";
var interval;
var lhTest = false;
var alreadyRunning = false;
const SUCCESS = 0;
const DB_VERSION = 16;
const TRACE = 0;
const LOG = 1;
const INFO = 2;
const WARN = 3;
const ERROR = 4;
const CONSOLE_LEVEL = WARN;
const STORE_NAME = "b2fkeys";
const DB_NAME = "blue2factor";
var navigateAway = false;

function setEndpoint(){
    if (ENDPT === "") {
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
    navigateAway = false;
    replaceState(); //this could cause some issues on the customer's end - in same cases we may have to comment it out'
    startBlue2Factor();
});

async function startBlue2Factor(){
    var runB2f = true;
    setEndpoint();
    if (!alreadyRunning) {
        alreadyRunning = true;
        if (!shouldIgnorePage()){
            const setup = getSetup();
            if (await setInitialSession(setup)){
                preventResubmit();
                runB2f = await generateKeysAndExport();
            }
            if (runB2f && (interval === undefined)){
                validateB2f();
                interval = setInterval(function(){
                    validateB2f();
                }, 2000);
                if (setup){
                    deleteCookie("b2fSetup");
                    replaceState();
                }
            }
        }
    }
}

function getSetup(){
    const setup = getCookie("b2fSetup");
    return setup;
}

function replaceState(){
    if (window.history.replaceState) {
        window.history.replaceState(null, null, window.location.href);
    }
}


function openDb(){
    return  new Promise ( function (resolve, reject) {
        window.indexedDB = window.indexedDB || window.mozIndexedDB || window.webkitIndexedDB ||
                window.msIndexedDB || window.shimIndexedDB;
        if (window.indexedDB) {
            const request = window.indexedDB.open(DB_NAME, DB_VERSION);
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

function upgradeDb(request){
    try {
        //await deleteDb();
        const db = request.result;
        db.createObjectStore(STORE_NAME, {keyPath: "keyType"});
        cLog("after createObjectStore");
    } catch(e) {
        cLog(e, ERROR);
    }
}

function deleteDb(){
    return new Promise (function (resolve) {
        window.indexedDB = window.indexedDB || window.mozIndexedDB ||
            window.webkitIndexedDB || window.msIndexedDB || window.shimIndexedDB;
        if (window.indexedDB) {
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
        }
    });
}

function shouldIgnorePage(){
    var ignorePage = false;
    const ignorePageFlag = document.getElementById("b2fIgnorePageFlag");
    if (ignorePageFlag) {
        const ignorePageFlagValue = ignorePageFlag.value;
        ignorePage = ignorePageFlagValue === "true";
    }
    return ignorePage;
}

function stopBlue2Factor(){
    if (interval !== undefined) {
        cLog("stopping", INFO);
        clearInterval(interval);
        interval = undefined;
        alreadyRunning = false;
    }
}


function isValidating(){
    var validating = false;
    if (currentlyValidating && lastValidation !== null && differenceInSecondsFromNow(lastValidation) < 5) {
        validating = true;
    }
    if (!validating) {
        currentlyValidating = true;
        lastValidation = new Date();
    }
    return validating;
}

function differenceInSecondsFromNow(dt){
    const now = new Date();
    const seconds = (now.getTime() - dt.getTime()) / 1000;
    return seconds;
}

/* See if the current browser should have access */
async function validateB2f(){
    const browserSession = await getEncryptedSession();
    if (!isValidating()) {
        cLog("checking B2F", INFO);
        if (browserSession && browserSession !== "error") {
            const browserToken = getBrowserToken();
            const dataToSend = JSON.stringify({"encryptedSession": browserSession,
                                               "reqUrl": window.location.href,
                                               "token": browserToken});
            fetch(getEndpoint() + "/verifyAccess", {
                body:dataToSend,
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
                handleValidateResponse(data);
            }).catch(function (err) {
                return cLog("validate error: " + err, ERROR);
            });
        } else {
            handleB2fFailure("");
        }
    }
}


async function handleValidateResponse(data){
    var success = false;
    var decryptedString;
    currentlyValidating = false;
    cLog("factor2Check success: " + data.accessAllowed, INFO);
    if (data.token){
        decryptedString = await decryptToken(data.token);
    }
    if (decryptedString) {
        setAuthnCookie(data.reason);
        if (data.accessAllowed) {
            if (isSettingUp()) {
                reloadWithoutParameters();
            }
            success = true;
        } else {
            handleB2fFailure(data.reason);
        }
    } else {
        handleB2fFailure(data.reason);
    }
    return success;
}

function isSettingUp(){
    var settingUp = false;
    if (getSetup()) {
        settingUp = true;
    }
    return settingUp;
}


function handleB2fFailure(reason){
    stopBlue2Factor();
    switch (reason) {
        case "browser was not found":
        case "deviceNotFound":
            navigateTo(getEndpoint() + "/notSignedUp");
            break;
        case "company user mismatch":
            navigateTo(getEndpoint() + "/wrongUser");
            break;
        default:
            deleteCookie("B2F_AUTHN");
            navigateTo(getEndpoint() + "/jsFailure?url=" + encodeURI(location.href) +
            "&tid=" + getBrowserSession());
    }
}


function navigateTo(site) {
    if (!navigateAway) {
        navigateAway = true;
        location.href = site;
    }
}

async function getEncryptedSession(){
    const browserSession = getBrowserSession();
    var enc;
    if (browserSession) {
        enc = await encryptString(browserSession, false);
    }
    return enc;
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

async function confirmToken(browserSession){
    const encToken = await encryptString(browserSession, false);
    const dataToSend = JSON.stringify({
        "encryptedSession": encToken,
        "reqUrl": window.location.href,
        "token": getBrowserToken(),
    });
    fetch(getEndpoint() + "/confirmToken", {
        body:dataToSend,
        headers: {
            "Accept": "application/json",
            "Access-Control-Allow-Origin": getControlOrigin(),
            "Content-Type": "application/json; charset=utf-8"
        },
        method:"post"
    })
    .then((response) => response.json())
    .then(function (data) {
        if (data.outcome === SUCCESS) {
            setBrowserSession(browserSession);
        }
        cLog("validate success: " + data.outcome === 0, INFO);
        return data.outcome;
    }).catch(function (err) {
        return cLog("validate error: " + err, ERROR);
    });
}



async function decryptToken(encryptedString) {
    var decryptedString;
    const pvtKey = await getPrivateKeyFromIdb();
    try {
        if (pvtKey) {
            const encryptedArrayBuffer = base64StringToArrayBuffer(encryptedString);
            const decryptedArrayBuffer = await crypto.subtle.decrypt(
                    {
                        hash: {name: "SHA-256"},
                        name: "RSA-OAEP"
                    }, pvtKey, encryptedArrayBuffer);
             //we'll throw an error if decrypt fails
            decryptedString = arrayBufferToString(decryptedArrayBuffer);
            await confirmToken(decryptedString);
        }
    } catch (error) {
        cLog("decryptToken: " + error, ERROR);
    }
    return decryptedString;
}

function arrayBufferToString(arrayBuffer) {
    var binary = "";
    var i;
    const bytes = new Uint8Array(arrayBuffer);
    const length = bytes.byteLength;
    for (i = 0; i < length; i+=1) {
        binary += String.fromCharCode(bytes[i]);
    }
    return binary;
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

async function encryptString(browserStr, secondTry) {
    var enc;
    const pubKey = await getPublicKeyFromIdb();
    if (pubKey) {
        const textEncoder = new TextEncoder();
        const textEncodedString =  textEncoder.encode(browserStr);
        try {
            const encryptedArrayBuffer = await crypto.subtle.encrypt({name: "RSA-OAEP"}, pubKey,
                textEncodedString);
            const bytes = new Uint8Array(encryptedArrayBuffer);
            enc = arrayBufferToBase64(bytes);
        } catch (err){
            cLog(err, ERROR);
        }
    } else {
        if (!secondTry){
            enc = await encryptString(browserStr, true);
        } else {
            signout();
        }
    }
    return enc;
}

document.addEventListener("visibilitychange", function() {
    const newVisibilityState = document.visibilityState;
    if (visibilityState !== newVisibilityState) {
        switch (newVisibilityState) {
            case "hidden":
                stopBlue2Factor();
                break;
            case "prerender":
                startBlue2Factor();
                break;
            case "visible":
                startBlue2Factor();
                break;
            default:
                cLog("new visibility state: " + newVisibilityState);
        }
    }
});

window.addEventListener("focus", function() {
    startBlue2Factor();
}, false);


window.addEventListener("blur", function() {
    stopBlue2Factor();
}, false);


function getBrowserSession(){
    var browserId;
    if (typeof(Storage) !== "undefined") {
        browserId = localStorage.getItem("B2F_SESSION");
        if (!browserId) {
            browserId = sessionStorage.getItem("B2F_SESSION");
        }
    }
    return browserId;
}

function getBrowserToken(){
    var browserToken;
    if (typeof(Storage) !== "undefined") {
        browserToken = localStorage.getItem("B2F_TOKEN");
        if (!browserToken){
            browserToken = sessionStorage.getItem("B2F_TOKEN");
        }
    }
    return browserToken;
}


function setBrowserSession(b2fId){
    if (typeof(Storage) !== "undefined") {
        if (!b2fId.endsWith("b2ft")){
            localStorage.setItem("B2F_SESSION", b2fId);
        } else {
            localStorage.setItem("B2F_SESSION", "");
            setTempBrowserSession(b2fId);
        }
    }
}

function setAuthnCookie(authn) {
    if (authn.length > 30) {
        setB2fCookie("B2F_AUTHN", authn, 90);
    }
}

function setBrowserToken(b2fToken){
    if (typeof(Storage) !== "undefined") {
        localStorage.setItem("B2F_TOKEN", b2fToken);
    }
}

function setTempBrowserSession(b2fId){
    if (typeof(Storage) !== "undefined") {
          sessionStorage.setItem("B2F_SESSION", b2fId);
    }
}

function setTempBrowserToken(b2fToken){
    if (typeof(Storage) !== "undefined") {
        sessionStorage.setItem("B2F_TOKEN", b2fToken);
    }
}

function getControlOrigin(){
    var ep = ENDPT;
    if (lhTest){
       ep = "*";
    }
    return ep;
}

function serverLog(text) {
    console.log(text);
    const dataToSend = JSON.stringify({
        "companyIdFromUrl": "",
        "encryptedSession": "",
        "fromBrowser": true,
        "reqType": "log",
        "reqUrl": window.location.href,
        "token": text
    });
    fetch(getEndpoint() + "/b2fBrowser", {
        body:dataToSend,
        credentials: "omit",
        headers: {
            "Accept": "application/json",
            "Access-Control-Allow-Origin": getControlOrigin(),
            "Content-Type": "application/json; charset=utf-8"
        },
        method: "POST"
    });
}

/* Key stuff*/

async function generateKeysAndExport() {
    const [publicOutcome] = await Promise.all([generateAndSendBrowserKey()]);
    return publicOutcome === true;
}

async function generateAndSendBrowserKey() {
    var browserKey;
    var keyData;
    //Generate a key pair and send the public key to the server
    var success = false;
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
        if (await setPrivateKeyInIdb(browserKey.privateKey)) {
            keyData = await window.crypto.subtle.exportKey(
                "jwk",
                browserKey.publicKey
            );
            if (await sendPublicKeyData(keyData)) {
                cLog("server key was successfully created");
                success = true;
            }
        }
    } catch (error) {
        cLog("generateAndSendBrowserKey: " + error, ERROR);
    }
    return success;
}

async function sendPublicKeyData(keyData){
    const dataToSend = JSON.stringify({
        "browserSession": getBrowserSession(),
        "browserToken": getBrowserToken(),
        "cmd": "createBrowserKey",
        "coKey": "",
        "fromJs": true,
        "jwk": keyData,
        "reqUrl": window.location.href
    });
    const response = await fetch(getEndpoint() + "/b2f-f1", {
        body:dataToSend,
        credentials: "omit",
        headers: {
            "Accept": "application/json",
            "Access-Control-Allow-Origin": getControlOrigin(),
            "Connection": "Keep-Alive",
            "Content-Type": "application/json; charset=utf-8",
            "Keep-Alive": "timeout=0, max=300"
        },
        method: "POST"
    });
    const resp = await response.json();
    const success = await handleRetrieveKey(resp);
    return success;
}

async function handleRetrieveKey(data){
    var success = false;
    cLog("submit success: " + data.outcome);
    if (data.outcome === SUCCESS){
        cLog("public key set");
        const pem = data.token;
        success = await importRsaKey(pem);
    }
    return success;
}

async function importRsaKey(pem) {
    // fetch the part of the PEM string between header and footer
    const pemHeader = "-----BEGIN PUBLIC KEY-----";
    const pemFooter = "-----END PUBLIC KEY-----";
    if (pem.indexOf(pemHeader) !== -1 && pem.indexOf(pemFooter) !== -1){
        pem = pem.substring(pemHeader.length,
                            pem.length - pemFooter.length - 1);
    }
    const pemContentsUpd = pem.replace(/[\r\n]+/g,"");
    const keyAsArrayBuffer = base64StringToArrayBuffer(pemContentsUpd);

    const serverKey = await window.crypto.subtle.importKey(
            "spki",
            keyAsArrayBuffer,
        {
            hash: {name: "SHA-256"},
            name: "RSA-OAEP"
        },
        false,
        ["encrypt"]
    );
    return await setPublicKeyInIdb(serverKey);
}

async function setPrivateKeyInIdb(privateKey){
    var success = false;
    var conn;
    try {
        conn = await openDb();
        success = await setKeyInDb(conn, "localKey", privateKey);
        const newlySetKey = await getKey(conn, "localKey");
        if (newlySetKey === undefined) {
            cLog("Key is undefined", WARN);
        } else {
            cLog("newlySetKey: " + newlySetKey.keyVal);
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
    return success;
}

async function setPublicKeyInIdb(publicKey){
    var success = false;
    var conn;
    try {
        conn = await openDb();
        success = await setKeyInDb(conn, "serverKey", publicKey);
        const newlySetKey = await getKey(conn, "serverKey");
        if (newlySetKey === undefined) {
            cLog("Key is undefined", WARN);
        } else {
            cLog("newlySetKey: " + newlySetKey.keyVal);
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
    return success;
}

function setKeyInDb(conn, name, keyValue){
    return new Promise(function (resolve, reject) {
        const transaction = conn.transaction(STORE_NAME, "readwrite");
        const objectStore = transaction.objectStore(STORE_NAME);
        const newKey = {
            createDate: new Date(),
            keyType: name,
            keyVal: keyValue
        };
        const request = objectStore.put(newKey);
        request.onsuccess = function(){
            cLog("successfully put " + name + " in indexedDb");
            resolve(true);
        };
        request.onerror = function(){
            cLog("failed to put " + name + " in indexedDb");
            reject(false);
        };
    });
}

async function getPrivateKeyFromIdb(){
    var key;
    var conn;
    try {
        conn = await openDb();
        const result = await getKey(conn, "localKey");
        if (result !== undefined) {
            key = result.keyVal;
        } else {
            cLog("private key not found", WARN);
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
        const transaction = conn.transaction(["b2fkeys"], "readwrite");
        const objectStore = transaction.objectStore("b2fkeys");
        const request = objectStore.get(name);
        request.onsuccess = function(){
            cLog("getKey success");
            resolve(request.result);
        };
        request.onerror = function() {
            cLog("getKey error", ERROR);
            reject(request.error);
        };
    });
}

async function setInitialSession(session){
    var needsCompletion = false;
    if (session && session !== "false") {
        const arr = session.split('\*\*');
        if (arr.length > 1){
            setBrowserToken(arr[1]);
            setBrowserSession(arr[0]);
            needsCompletion = true;
        }
        if (arr.length > 2){
            if (arr[2] === "false") {
                const pubKey = await getPublicKeyFromIdb();
                if (pubKey) {
                    needsCompletion = false;
                }
            }
        }
    }
    return needsCompletion;
}

function getCookie(cname) {
    var c;
    var i;
    const name = cname + "=";
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(";");
    for(i = 0; i <ca.length; i+=1) {
        c = ca[i];
        while (c.charAt(0) === " ") {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function setB2fCookie(c_name,value,expiredays){
    "use strict";
    const exdate=new Date();
    exdate.setDate(exdate.getDate()+expiredays);
    document.cookie=c_name+ "=" +value+((expiredays === null) ? "" : ";expires="+
        exdate.toUTCString())+";path=/;SameSite=Lax;secure";
}

function setB2fCookieWithDomain(c_name,value,expiredays){
    "use strict";
    const exdate = new Date();
    const domain = window.location.host;
    exdate.setDate(exdate.getDate()+expiredays);
    document.cookie=c_name+ "=" +value+((expiredays === null) ? "" : ";expires="+
        exdate.toUTCString())+";path=/;domain=" + domain + ";SameSite=Lax;secure";
}

function signout(){
    deleteCookie("B2F_AUTHN");
    const cookie = getCookie("B2F_AUTHN");
    cLog("cookie after delete: " + cookie, WARN);
    navigateTo(getEndpoint() + "/logout");
}

function deleteCookie(cookieName){
    "use strict";
    setB2fCookie(cookieName, "", -1);
    setB2fCookieWithDomain(cookieName, "", -1);
}

function reloadWithoutParameters(){
    deleteCookie("b2fSetup");
    navigateTo(getUrlWithoutParameters());
}

function getUrlWithoutParameters(){
    const urlArray = window.location.href.split("?");
    return urlArray[0];
}

function preventResubmit(){
    if (window.history.replaceState) {
        window.history.replaceState(null, null, window.location.href);
    }
}
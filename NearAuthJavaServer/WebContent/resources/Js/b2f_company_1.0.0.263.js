 /*jshint esversion: 8 */

var lhTest = false;
var ENDPT = "";
const TRACE = 0;
const LOG = 1;
const INFO = 2;
const WARN = 3;
const ERROR = 4;
const CONSOLE_LEVEL = TRACE;
var allowAllIdpUsersOrigVal;
var addToAdOrigVal;
var noConsoleAllowedOrigVal;
var noDeviceAllowedOrigVal;

function setEndpoint(){
    var envDom;
    var env;
    if (ENDPT === "") {
        envDom = document.getElementById("b2f-env");
        if (envDom) {
            env = envDom.value;
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
    var date = new Date();
    var hours = date.getHours();
    var minutes = "0" + date.getMinutes();
    var seconds = "0" + date.getSeconds();
    return hours + ":" + minutes + ":" + seconds;
}

document.addEventListener("DOMContentLoaded", function() {
    var cover;
    var mobileMenuSignOut;
    var popupCancel;
    var popupOk;
    var popupX;
    var signOutClick = document.querySelector("#signOutClick");
    var showApiButton;
    var showPrivateKeyButton;

    setEndpoint();
    replaceState(); //this could cause some issues on the customer's end - in same cases we may have to comment it out'
    if (signOutClick) {
        signOutClick.onclick = function(event) {
            signOut();
            event.preventDefault();
        };
    }

    mobileMenuSignOut = document.querySelector("#mobileMenuSignOut");
    if (mobileMenuSignOut) {
        mobileMenuSignOut.onclick = function(event) {
            signOut();
            event.preventDefault();
        };
    }

    popupX  = document.querySelector("#popupX");
    if (popupX) {
        popupX.onclick = function(event) {
            document.getElementById("selectUserRow").style.display = "none";
            cPp();
            event.preventDefault();
        };
    }

    popupCancel = document.querySelector("#popupCancel");
    if (popupCancel) {
        popupCancel.onclick = function(event) {
            document.getElementById("selectUserRow").style.display = "none";
            cPp();
            event.preventDefault();
        };
    }

    popupOk = document.querySelector("#popupOk");
    if (popupOk) {
        popupOk.onclick = function(event) {
            var skipSubmit = false;
            document.getElementById("selectUserRow").style.display = "none";
            var hidden = document.getElementById("popupHidden1");
            if (hidden) {
                var hidden1Val = hidden.value;
                if (hidden1Val === "recreateKeys") {
                    skipSubmit = true;
                    cPp();
                    requestKeys();
                } else if (hidden1Val === "showEncryptionKeys"){
                    skipSubmit = true;
                    cPp();
                } else if (hidden1Val === "uploadWebServerKeys") {
                    skipSubmit = true;
                    cPp();
                    updateWebServerKey();
                } else if (hidden1Val === "showWebKeyDirections"){
                    skipSubmit = true;
                    cPp();
                    showDirections();
                } else if (hidden1Val === "getAdminCodes") {
                    skipSubmit = true;
                    cPp();
                } else if (hidden1Val === "updateUserFlow") {
                    skipSubmit = true;
                    cPp();
                }
            }
            if (!skipSubmit) {
                document.body.style.cursor  = "wait";
                document.getElementById("companyForm").submit();
            }
            event.preventDefault();
        };
    }

    cover = document.querySelector("#cover");
    if (cover) {
        cover.onclick = function(event) {
            cPp();
            event.preventDefault();
        };
    }

    showPrivateKeyButton = document.querySelector("#showPrivateKeyButton");
    if (showPrivateKeyButton) {
        showPrivateKeyButton.onclick = function(event) {
            showPrivateKey();
            event.preventDefault();
        };
    }

    showApiButton = document.querySelector("#showApiButton");
    if (showApiButton) {
        showApiButton.onclick = function(event) {
            showApi();
            event.preventDefault();
        };
    }

    var addBase = document.querySelector("#addBase");
    if (addBase) {
        addBase.onclick = function(event) {
            var base = document.getElementById("currBaseUrl").value;
            showAddBase(base);
            event.preventDefault();
        };
    }

    var resetBase = document.querySelector("#resetBase");
    if (resetBase) {
        resetBase.onclick = function(event) {
            var base = document.getElementById("currBaseUrl").value;
            showAddBase(base);
            event.preventDefault();
        };
    }

    var addLogin = document.querySelector("#addLogin");
    if (addLogin) {
        addLogin.onclick = function(event) {
            var login = document.getElementById("currLoginUrl").value;
            showAddLogin(login);
            event.preventDefault();
        };
    }

    var updateLogin = document.querySelector("#updateLogin");
    if (updateLogin) {
        updateLogin.onclick = function(event) {
            var login = document.getElementById("currLoginUrl").value;
            showAddLogin(login);
            event.preventDefault();
        };
    }

    var updateProviderUrl = document.querySelector("#updateProviderUrl");
    if (updateProviderUrl) {
        updateProviderUrl.onclick = function(event) {
            var provider = document.getElementById("providerUrl").textContent;
            showChangeLdapUrl(provider);
            event.preventDefault();
        };
    }

    var updateSearchBase = document.querySelector("#updateSearchBase");
    if (updateSearchBase) {
        updateSearchBase.onclick = function(event) {
            var searchBase = document.getElementById("searchBase").textContent;
            changeSearchBase(searchBase);
            event.preventDefault();
        };
    }

    var updateIconUrl = document.querySelector("#updateIconUrl");
    if (updateIconUrl) {
        updateIconUrl.onclick = function(event) {
            var iconUrl = document.getElementById("iconUrl").textContent;
            changeIconUrl(iconUrl);
            event.preventDefault();
        };
    }

    var updateBackgroundColor = document.querySelector("#updateBackgroundColor");
    if (updateBackgroundColor) {
        updateBackgroundColor.onclick = function(event) {
            var backgroundColor = document.getElementById("backgroundColor").textContent;
            changeBackgroundColor(backgroundColor);
            event.preventDefault();
        };
    }

    var updatePrimaryColor = document.querySelector("#updatePrimaryColor");
    if (updatePrimaryColor) {
        updatePrimaryColor.onclick = function(event) {
            var foregroundColor = document.getElementById("primaryColor").textContent;
            changeForegroundColor(foregroundColor);
            event.preventDefault();
        };
    }

    var methodSelector = document.getElementById("f1Method");
    if (methodSelector) {
          methodSelector.onchange = function() {
              var methodDiv = document.getElementById("f1Method");
              var method = methodDiv.value;
              updateMethod("f1Method", method);
              showCorrectSection(method);
          };
    }

    var f2MethodSelector = document.getElementById("f2Method");
    if (f2MethodSelector) {
    f2MethodSelector.onchange = function() {
        var f2Method = document.getElementById("f2Method").value;
        updateMethod("f2Method", f2Method);
    };
    }

    var addUserButton = document.querySelector("#addUserButton");
    if (addUserButton) {
        addUserButton.onclick = function(event) {
            var companyId = document.getElementById("companyId").value;
            addUserToCompany(companyId);
            event.preventDefault();
        };
    }

    var addServiceProviderButton = document.querySelector("#addServiceProviderButton");
    if (addServiceProviderButton) {
        addServiceProviderButton.onclick = function(event) {
            var companyId = document.getElementById("companyId").value;
            addServiceProviderToCompany(companyId);
            event.preventDefault();
        };
    }

    var addServiceProviderMetadataButton = document.querySelector("#addServiceProviderMetadataButton");
    if (addServiceProviderMetadataButton) {
        addServiceProviderMetadataButton.onclick = function(event) {
            var companyId = document.getElementById("companyId").value;
            addServiceProviderMetadataToCompany(companyId);
            event.preventDefault();
        };
    }

    var addServerButton = document.querySelector("#addServerButton");
    if (addServerButton) {
        addServerButton.onclick = function(event) {
            var companyId = document.getElementById("companyId").value;
            addServerToCompany(companyId);
            event.preventDefault();
        };
    }
    var resetUserButton = document.querySelector(".resetUserButton");
    if (resetUserButton) {
        resetUserButton.addEventListener("click", function (event) {
            var rowNum = getNumFromEndOfString(callerId);
            var groupId = document.getElementById("groupId_" + rowNum).value;
            var callerId = event.target.id;
            var username = document.getElementById("username" + rowNum).value;
            sppu("You sure?", "This will reset " + username + "&#8217;s devices.", "11839", groupId);
            event.preventDefault();
        });
    }
    var resetUserButtons = document.getElementsByClassName("resetUserButton");
    var i;
    for (i = 0; i < resetUserButtons.length; i+=1) {
    resetUserButtons[i].addEventListener("click", function (event) {
        var callerId = event.target.id;
            var rowNum = getNumFromEndOfString(callerId);
            var groupId = document.getElementById("groupId_" + rowNum).value;
            var username = document.getElementById("username" + rowNum).textContent;
            sppu("You sure?", "This will reset " + username + "&#8217;s devices.", "11839", groupId);
            event.preventDefault();
        });
    }

    var j;
    var uidButtons = document.getElementsByClassName("addUidButton");
    for (j = 0; j < uidButtons.length; j+=1) {
        uidButtons[j].addEventListener("click", function (event) {
            var callerId = event.target.id;
            var rowNum = getNumFromEndOfString(callerId);
            var uid = document.getElementById("uid" + rowNum).textContent;
            var groupId = document.getElementById("groupId_" + rowNum).value;
            var username = document.getElementById("username" + rowNum).textContent;
            sppuw1i("Update Uid", "Please enter the UID for " + username + ".", "18973", groupId, "UID", uid);
            event.preventDefault();
        });
    }
    var k;
    var changeRoleButtons = document.getElementsByClassName("changeRoleButton");
    for (k = 0; k < changeRoleButtons.length; k+=1) {
        changeRoleButtons[k].addEventListener("click", function (event) {
            document.getElementById("selectUserRow").style.display = "block";
            var callerId = event.target.id;
            var rowNum = getNumFromEndOfString(callerId);
            var groupId = document.getElementById("groupId_" + rowNum).value;
            var username = document.getElementById("username" + rowNum).textContent;
            var currRole = document.getElementById("userType" + rowNum).textContent;
            if (currRole === "SUPER_ADMIN") {
                document.getElementById("superAdminPopup").selected = "selected";
            }
            if (currRole === "ADMIN") {
                document.getElementById("adminPopup").selected = "selected";
            }
            if (currRole === "AUDITOR") {
                document.getElementById("auditorPopup").selected = "selected";
            }
            if (currRole === "USER") {
                document.getElementById("userPopup").selected = "selected";
            }
            sppu("Update Role", "Please enter the role for " + username + ".", "18013", groupId);
            event.preventDefault();
        });
    }

    var downloadSpCerts = document.getElementsByClassName("downloadSpCert");
    var downloadId;
    var certUrl;
    var spName;
    var certk;
    for (certk = 0; certk < downloadSpCerts.length; certk+=1) {
        downloadSpCerts[certk].addEventListener("click", function (event) {
            downloadId = event.target.id;
            spName = event.target.name;
            certUrl = getEndpoint() + "/certDownload?cid=" + downloadId;
            downloadURI(certUrl, spName + ".cer");
        });
    }

    var y1;
    var editServiceProvider = document.getElementsByClassName("editServiceProviderButton");
    for (y1 = 0; y1 < editServiceProvider.length; y1+=1) {
        editServiceProvider[y1].addEventListener("click", function (event) {
            var callerId = event.target.id;
            var rowNum = getNumFromEndOfString(callerId);
            var name = document.getElementById("spName" + rowNum).textContent;
            var acs = document.getElementById("spAcs" + rowNum).textContent;
            var entityId = document.getElementById("spEntityId" + rowNum).textContent;
            var spId = document.getElementById("spId" + rowNum).textContent;
            sppuw3i("Update Service Provider", "Service Provider:",  "299866", spId, "Name", "EntityId", "ACS URL",
                name, entityId, acs);
        });
    }

    var z;
    var deleteServiceProvider = document.getElementsByClassName("removeServiceProviderButton");
    for (z = 0; z < deleteServiceProvider.length; z+=1) {
        deleteServiceProvider[z].addEventListener("click", function (event) {
            var callerId = event.target.id;
            var rowNum = getNumFromEndOfString(callerId);
            var name = document.getElementById("spName" + rowNum).textContent;
            var spId = document.getElementById("spId" + rowNum).textContent;
            sppu("You sure?", "This will remove the service provider " + name + " from your company.", "299877", spId);
        });
    }

    var z1;
    var removeUserButtons = document.getElementsByClassName("removeUserButton");
    for (z1 = 0; z1 < removeUserButtons.length; z1+=1) {
        removeUserButtons[z1].addEventListener("click", function onclick(event) {
            var callerId = event.target.id;
            var rowNum = getNumFromEndOfString(callerId);
            var groupId = document.getElementById("groupId_" + rowNum).value;
            var username = document.getElementById("username" + rowNum).textContent;
            sppu("You sure?", "This will remove " + username + " from your company.", "14839", groupId);
            event.preventDefault();
        });//4279505
    }

    var idx;
    var removeServerButtons = document.getElementsByClassName("removeServerButton");
    for (idx = 0; idx < removeServerButtons.length; idx+=1){
        removeServerButtons[idx].addEventListener("click", function (event) {
            var callerId = event.target.id;
            var rowNum = getNumFromEndOfString(callerId);
            var serverId = document.getElementById("serverId_" + rowNum).value;
            sppu("Danger!", "Make sure you uninstall NearAuth.ai on your server before you do this! If not, you could lock yourself out of a server by mistake!", "23998", serverId);
        });
    }

    var mobileMenu = document.querySelector("#mobileMenu");
    if (mobileMenu) {
        if (!isHidden(mobileMenu)){
            mobileMenu.onclick = function(event) {
                showMobileMenu();
                event.preventDefault();
            };
        }
    }

    var updateSamlButton = document.querySelector("#updateSamlData");
    if (updateSamlButton) {
        updateSamlButton.onclick = function(event){
            updateSamlData();
            event.preventDefault();
        };
    }

    var recreateKeysButton = document.querySelector("#recreateEncryptionKeys");
    if (recreateKeysButton) {
        recreateKeysButton.onclick = function(event){
            recreateEncryptionKeys();
            event.preventDefault();
        };
    }

    var createKeysButton = document.querySelector("#createEncryptionKeys");
    if (createKeysButton) {
        createKeysButton.onclick = function(event){
            createEncryptionKeys();
            event.preventDefault();
        };
    }

    if (window.location.href === getEndpoint() + "/company"){
        replaceState();
    } else if (window.location.href === getEndpoint() + "/documentation"){
        replaceState();
    }

    var downloadX509 = document.querySelector("#downloadX509");
    if (downloadX509) {
        downloadX509.onclick = function() {
            var companyId = document.getElementById("companyId").value;
            downloadURI("/cert/" + companyId + "/download", "certForServiceProvider.x509");
        };
    }

    var downloadMetadataForIdp = document.querySelector("#downloadMetadataForIdp");
    if (downloadMetadataForIdp) {
        downloadMetadataForIdp.onclick = function() {
            var companyId = document.getElementById("apiKey").textContent;
            downloadURI("/SAML2/SSO/" + companyId + "/blue2factorIdpMetadata", "B2fMetadataForIdp.xml");
        };
    }

    var downloadMetadataForSp = document.querySelector("#downloadMetadataForSp");
    if (downloadMetadataForSp) {
        downloadMetadataForSp.onclick = function() {
            var companyId = document.getElementById("apiKey").textContent;
            downloadURI("/SAML2/SSO/" + companyId + "/blue2factorSpMetadata", "B2fMetadataForSp.xml");
        };
    }

    var uploadWebServerPub = document.querySelector("#uploadWebServerPub");
    if (uploadWebServerPub) {
        uploadWebServerPub.onclick = function(event) {
            uploadWebServerKeyVerify();
            event.preventDefault();
        };
    }

    var requestAuthCodes = document.getElementById("requestAuthCodes");
    if (requestAuthCodes) {
        requestAuthCodes.onclick = function() {
            getAdminCodes();
        };
    }

    var f1Method = document.getElementById("f1MethodFromServer");
    if (f1Method) {
        showCorrectSection(f1Method.value);
    }

    var addToAd = document.getElementById("addToAd");
    var allowAllIdpUsers = document.getElementById("allowAllIdpUsers");
    if (allowAllIdpUsers) {
        allowAllIdpUsersOrigVal = allowAllIdpUsers.checked;
        allowAllIdpUsers.onchange = function() {
            updateUserFlow(allowAllIdpUsers.checked, addToAd.checked);
        };
    }

    if (addToAd) {
        addToAdOrigVal = addToAd.checked;
        addToAd.onchange = function() {
            updateUserFlow(allowAllIdpUsers.checked, addToAd.checked);
        };
    }

    var noDeviceAllowed = document.getElementById("noDeviceIn");
    var noDeviceNotAllowed = document.getElementById("noDeviceOut");
    var noConsoleAllowed = document.getElementById("noConsoleIn");
    var noConsoleNotAllowed = document.getElementById("noConsoleOut");
    if (noConsoleAllowed) {
        noConsoleAllowedOrigVal = noConsoleAllowed.checked;
        noConsoleAllowed.onchange = function(){
            updateNonMemberStrategy(noConsoleAllowed.checked, noDeviceAllowed.checked);
        };
        noConsoleNotAllowed.onchange = function(){
            updateNonMemberStrategy(noConsoleAllowed.checked, noDeviceAllowed.checked);
        };
    }

    if (noDeviceAllowed) {
        noDeviceAllowedOrigVal = noDeviceAllowed.checked;
        noDeviceAllowed.onchange = function(){
            updateNonMemberStrategy(noConsoleAllowed.checked, noDeviceAllowed.checked);
        };
        noDeviceNotAllowed.onchange = function(){
            updateNonMemberStrategy(noConsoleAllowed.checked, noDeviceAllowed.checked);
        };
    }

});

function updateUserFlow(allowAllIdpUsers, addToAd){
    //
    const dataToSend = JSON.stringify({
        "addToAd": addToAd,
        "allowAllIdpUsers": allowAllIdpUsers
    });
    fetch(getEndpoint() + "/nnanamjirmdui98nmouooiuu2", {
        body:dataToSend,
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json; charset=utf-8",
            "Accept": "application/json",
            "Access-Control-Allow-Origin": getControlOrigin()
        },
        method: "POST"
    })
    .then((response) => response.json())
    .then(function(data) {
        var outcome = data.outcome;
        var addToAd1 = document.getElementById("addToAd");
        var allowAllIdpUsers1 = document.getElementById("allowAllIdpUsers");
        if (outcome !== 0){
             sppu("Uh oh", "We failed to make the update.  Error code: " + data.reason, "updateUserFlow", "");
             allowAllIdpUsers1.checked = allowAllIdpUsersOrigVal;
             addToAd1.checked = addToAdOrigVal;
        } else {
            allowAllIdpUsersOrigVal = allowAllIdpUsers1.checked;
            addToAdOrigVal = addToAd1.checked;
        }
    }).catch(function (err) {
        return cLog("validate error: " + err, ERROR);
    });
}

function updateNonMemberStrategy(noConsoleAllowed, noDeviceAllowed){
    const dataToSend = JSON.stringify({"noConsoleAllowed": noConsoleAllowed,
        "noDeviceAllowed": noDeviceAllowed});
    fetch(getEndpoint() + "/aeu9aeu2sxchnit3", {
        body:dataToSend,
        credentials: "same-origin",
        headers: {
            "Accept": "application/json",
            "Access-Control-Allow-Origin": getControlOrigin(),
            "Content-Type": "application/json; charset=utf-8"
        },
        method: "POST"
    })
    .then((response) => response.json())
    .then(function(data) {
        var outcome = data.outcome;
        var noConsoleIn = document.getElementById("noConsoleIn");
        var noDeviceIn = document.getElementById("noDeviceIn");
        if (outcome !== 0){
             sppu("Uh oh", "We failed to make the update.  Error code: " + data.reason, "updateNonMemberStrategy", "");
             noConsoleIn.checked = noConsoleAllowedOrigVal;
             noDeviceIn.checked = noDeviceAllowedOrigVal;
        } else {
            noConsoleAllowedOrigVal = noConsoleIn.checked;
            noDeviceAllowedOrigVal = noDeviceIn.checked;
        }
    }).catch(function(err) {
        return cLog("validate error: " + err, ERROR);
    });
}

function downloadURI(uri, name) {
    var link = document.createElement("a");
    link.setAttribute("download", name);
    link.href = uri;
    document.body.appendChild(link);
    link.click();
    link.remove();
}

function getAdminCodes(){
    const dataToSend = JSON.stringify({"reqUrl": window.location.href});
    fetch(getEndpoint() + "/getAdminCodes", {
        body:dataToSend,
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json; charset=utf-8",
            "Accept": "application/json",
            "Access-Control-Allow-Origin": getControlOrigin()
        },
        method: "POST"
    })
    .then((response) => response.json())
    .then(function(data) {
        handleAdminCodes(data);
    }).catch(function(err) {
        return cLog("validate error: " + err, ERROR);
    });
}

function handleAdminCodes(data) {
    var i;
    var codeList = data.codeList;
    var codeListStr = "";
    for (i=0; i<codeList.length; i+=1){
        codeListStr += codeList[i] + "\r\n";
    }
    showAdminCodes(codeListStr);
}


function replaceState(){
    if (window.history.replaceState) {
        window.history.replaceState(null, null, window.location.href);
    }
}

function recreateEncryptionKeys(){
    sppu("Are you sure?", "This will deactivate any existing encryption keys you may be using.", "recreateKeys", "");
}

function uploadWebServerKeyVerify(){
    sppu("Are you sure?", "This will deactivate any existing webserver keys you may be using.", "showWebKeyDirections", "");
}

function showDirections(){
    sppu("OpenSSL directions", "If you are using openssl, you can generate keys by first running: <div class='openSsl'>" +
                                "openssl genrsa -out key.pem 2048</div> to generate the private key, " +
                                "and then running<div class='openSsl'>openssl rsa -in key.pem -outform PEM -pubout -out public.pem</div>" +
                                "to get the public key.<br><br>We want the text of the public key. Use the " +
                                "private key on your webserver.", "uploadWebServerKeys", "");
}

function createEncryptionKeys(){
    requestKeys();
}

function requestKeys(){
    document.body.style.cursor = "wait";
    var companyId = document.getElementById("companyId").value;
    const dataToSend = JSON.stringify({"coId": companyId});
    fetch(getEndpoint() + "/b2f-newKeys", {
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
    document.body.style.cursor = "default";
        var outcome = data.outcome;
        console.log("submit success: " + data.outcome);
        if (outcome === 0){
            var privateKey = data.pvt;
            var publicKey = data.pub;
            showNewEncryptionKeys(privateKey, publicKey);
        }
        return outcome;
    }).catch(function (err) {
        document.body.style.cursor = "default";
        console.log("submitSetup error: " + err);
        return;
    });
}

function showNewEncryptionKeys(key1, key2) {
    var popup = document.getElementById("popup");
    var msg = document.getElementById("popupMessage");
    var titleEl = document.getElementById("popupTitle");
    var hiddenEl1 = document.getElementById("popupHidden1");
    var popupTextArea = document.getElementById("popupTextAreaDiv");
    var popupTextArea2 = document.getElementById("popupTextAreaDiv2");
    var popupTextAreaLabel = document.getElementById("popupTextAreaDivLabel");
    var popupTextArea2Label = document.getElementById("popupTextAreaDiv2Label");
    var popupInputRow1 = document.getElementById("popupInputRow1");
    var popupInputRow2 = document.getElementById("popupInputRow2");
    var popupInputRow3 = document.getElementById("popupInputRow3");
    shCr();
    popup.style.top = "25%";
    popup.style.display = "block";
    popup.style.zIndex = 101;
    msg.textContent = "Please copy the encryption keys below.";
    titleEl.textContent = "New Encryption Keys";
    hiddenEl1.value = "showEncryptionKeys";
    popupTextAreaLabel.textContent = "Private Encryption Key";
    popupTextArea2Label.textContent = "Public Encryption Key";

    popupTextArea.style.display = "block";
    document.getElementById("popupTextArea").value = key1;
    popupTextArea2.style.display = "block";
    document.getElementById("popupTextArea2").value = key2;
    popupTextAreaLabel.style.display = "block";

    popupTextArea2Label.style.display = "block";
    popupInputRow1.style.display = "none";
    popupInputRow2.style.display = "none";
    popupInputRow3.style.display = "none";
}

function updateSamlData(){
    var popup = document.getElementById("popup");
    var msg = document.getElementById("popupMessage");
    var titleEl = document.getElementById("popupTitle");
    var hiddenEl1 = document.getElementById("popupHidden1");
    var popupTextArea = document.getElementById("popupTextAreaDiv");
    var popupInputRow1 = document.getElementById("popupInputRow1");
    var popupInputRow2 = document.getElementById("popupInputRow2");
    var popupInputRow3 = document.getElementById("popupInputRow3");
    shCr();
    popup.style.display = "block";
    popup.style.zIndex = 101;
    msg.textContent = "Please paste your SAML metadata in the box.";
    titleEl.textContent = "Upload SAML Metadata";
    hiddenEl1.value = "238989";

    popupTextArea.style.display = "block";
    popupInputRow1.style.display = "none";
    popupInputRow2.style.display = "none";
    popupInputRow3.style.display = "none";

}

function showAdminCodes(codeList){
    shCr();
    var popup = document.getElementById("popup");
    popup.style.top = "25%";
    popup.style.display = "block";
    popup.style.zIndex = 101;

    var msg = document.getElementById("popupMessage");
    msg.textContent = "Administrator Codes - handle with care";
    var titleEl = document.getElementById("popupTitle");
    titleEl.textContent = "New Admin Codes";

    var popupTextArea = document.getElementById("popupTextAreaDiv");
    var popupTextArea2 = document.getElementById("popupTextAreaDiv2");
    var popupTextAreaLabel = document.getElementById("popupTextAreaDivLabel");
    var popupTextArea2Label = document.getElementById("popupTextAreaDiv2Label");
    var popupInputRow1 = document.getElementById("popupInputRow1");
    var popupInputRow2 = document.getElementById("popupInputRow2");
    var popupInputRow3 = document.getElementById("popupInputRow3");
    var popupCancel = document.getElementById("popupCancel");

    document.getElementById("popupHidden1").value = "getAdminCodes";
    popupTextArea.style.display = "block";
    document.getElementById("popupTextArea").value = codeList;
    popupTextArea2.style.display = "none";
    popupTextAreaLabel.style.display = "block";

    popupTextArea2Label.style.display = "none";
    popupInputRow1.style.display = "none";
    popupInputRow2.style.display = "none";
    popupInputRow3.style.display = "none";
    popupCancel.style.display = "none";
}

function updateWebServerKey(){
    shCr();
    var popup = document.getElementById("popup");
    popup.style.display = "block";
    popup.style.zIndex = 101;

    var msg = document.getElementById("popupMessage");
    msg.textContent = "Please paste the text of your .pem formatted RSA public key in the box, the corresponding private key will go on your webserver.";
    var titleEl = document.getElementById("popupTitle");
    titleEl.textContent = "Upload Public Key";
    var hiddenEl1 = document.getElementById("popupHidden1");
    hiddenEl1.value = "298675";

    var popupTextArea = document.getElementById("popupTextAreaDiv");
    var popupInputRow1 = document.getElementById("popupInputRow1");
    var popupInputRow2 = document.getElementById("popupInputRow2");
    var popupInputRow3 = document.getElementById("popupInputRow3");

    popupTextArea.style.display = "block";
    popupInputRow1.style.display = "none";
    popupInputRow2.style.display = "none";
    popupInputRow3.style.display = "none";

}

function showMobileMenu(){
    shCr();
    var menu = document.getElementById("mobileMenuText");
    menu.style.display = "block";
    menu.style.zIndex = 110;
}

function hideMobileMenu(){
    hCr();
    var menu = document.getElementById("mobileMenuText");
    menu.style.display = "none";
    menu.style.zIndex = -100;
}

function isHidden(el) {
    var style = window.getComputedStyle(el);
    return (style.display === "none");
}

function getNumFromEndOfString(str){
    return parseInt(str.match(/\d+$/)[0], 10);
}

function showApi(){
    var apiKey = document.getElementById("apiKey");
    apiKey.style.display = "block";
    var button = document.getElementById("showApiButton");
    button.style.display = "none";
}

function setRegularCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+ d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/;secure;";
}

function setAllCookies(cname, cvalue){
    setRegularCookie(cname, cvalue, 365*30);
    if (window.localStorage){
        localStorage.setItem(cname, cvalue);
    }
}

function signOut(){
    setAllCookies("gid", "");
    location.href = "/signout";
}

function showPrivateKey(){
    var apiKey = document.getElementById("privateKey");
    apiKey.style.display = "block";
    var button = document.getElementById("showPrivateKeyButton");
    button.style.display = "none";
}

//get basic Cookie
function gck(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(";");
    var i;
    var c;
    for(i = 0; i <ca.length; i+=1) {
        c = ca[ca.length - i - 1];
        while (c.charAt(0) === " ") {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

//closePopup
function cPp(){
    var popup = document.getElementById("popup");
    popup.style.display = "none";
    popup.style.top = "50%";
    popup.style.zIndex = -100;
    hCr();

    document.getElementById("popupHidden1").value = "";
    document.getElementById("popupHidden2").value = "";
    document.getElementById("popupInput1").value = "";
    document.getElementById("popupInput2").value = "";
    document.getElementById("popupInput3").value = "";
    var popupTextArea = document.getElementById("popupTextAreaDiv");
    var popupTextArea2 = document.getElementById("popupTextAreaDiv2");
    var popupTextAreaLabel = document.getElementById("popupTextAreaDivLabel");
    var popupTextArea2Label = document.getElementById("popupTextAreaDiv2Label");
    popupTextArea.style.display = "none";
    document.getElementById("popupTextArea").style.display = "";
    popupTextArea2.style.display = "none";
    document.getElementById("popupTextArea2").style.display = "";
    popupTextAreaLabel.style.display = "none";
    popupTextAreaLabel.textContent = "";
    popupTextArea2Label.style.display = "none";
    popupTextArea2Label.textContent = "";

}

function updateMethod(methodName, newMethod){
    if (methodName === "f1Method") {
        document.getElementById("companyForm").action = "/settings";
        document.getElementById("popupHidden1").value = "24598";
    } else {
        document.getElementById("popupHidden1").value = "24599";
    }
    document.getElementById("popupHidden2").value = newMethod;
    document.getElementById("companyForm").submit();
}

function deleteServer(serverId) {
    location.href = "/company?snhtbntbsntoe=" + encodeURIComponent(serverId);
}

function updateCompletionUrl(newUrl){
    location.href = "/company?aeumnthdoeka3=" + encodeURIComponent(newUrl);
}

function addNewUser(companyId, name, email) {
    location.href = "/company?atnsuhcahis=" + companyId + "&sahuchkej=" + name + "&bmnniuotu=" + email + "&";
}

function addNewServer(companyId, serverName, serverId) {
    if (serverName !== "" && serverId !== "") {
        location.href = "/company?atnsuhcahis=" + companyId + "&shnththsnb=" + serverName + "&aundhsrcdk=" + serverId;
    }
}

function validateEmail(email) {
    var re = /^(([^<>()[\]\\.,;:\s@&quot;]+(\.[^<>()[\]\\.,;:\s@&quot;]+)*)|(&quot;.+&quot;))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}

function addUserToCompany(companyId) {
//    var baseUrl = document.getElementById("baseUrl");
    var loginUrl = document.getElementById("loginUrl");
    if (loginUrl) {
        if (loginUrl.value === "") {
            sppu("Before that...", "Please enter the login URL on the settings page before you do this.", "", "");
        } else {
            sppuw3i("Add User", "Please enter the user's info", "88952", companyId, "Name:", "Email:" , "UID:", "", "", "");
        }
    }
}

function addServiceProviderMetadataToCompany(companyId) {
    shCr();
    var popup = document.getElementById("popup");
    popup.style.display = "block";
    popup.style.zIndex = 101;

    var msg = document.getElementById("popupMessage");
    msg.textContent = "Please paste your SAML metadata in the box.";
    var titleEl = document.getElementById("popupTitle");
    titleEl.textContent = "Upload SAML Metadata";
    var hiddenEl1 = document.getElementById("popupHidden1");
    hiddenEl1.value = "299867";

    var popupTextArea = document.getElementById("popupTextAreaDiv");
    var popupInputRow1 = document.getElementById("popupInputRow1");
    var popupInputRow2 = document.getElementById("popupInputRow2");
    var popupInputRow3 = document.getElementById("popupInputRow3");

    popupTextArea.style.display = "block";
    popupInputRow1.style.display = "none";
    popupInputRow2.style.display = "none";
    popupInputRow3.style.display = "none";
}

function addServiceProviderToCompany(companyId) {
    sppuw3i("Add Service Provider", "Please enter the service provider info", "98399", companyId, "SP Name:", "Entity ID:", "ACS Url:", "", "", "");
}

function addServerToCompany(companyId) {
    if (document.getElementById("loginUrl").textContent === "") {
        sppu("Before that...", "Please enter the necessary URLs on the Setting page", "", "");
    } else {
        sppuw3i("Add Server", "Please enter a name for the server and the server ID provided to you when you ran the NearAuth.ai installation on the server",
            "23903", companyId, "Name:", "Server ID:", "Desc.", "", "", "");
    }
}


function removeUser(user){
    location.href = "/company?staheusnth=" + user;
}

function removeDevices(group){
    location.href = "/company?sunhaesnchbk=" + group;
}

//showPopup
function sppu(title, message, hidden1, hidden2){
    sppuw2i(title, message, hidden1, hidden2, "", "", "", "");
}

//showPopupWith3Inputs
function sppuw3i(title, message, hidden1, hidden2, inputLabel1, inputLabel2, inputLabel3,
        inputVal1, inputVal2, inputVal3){
    shCr();
    var popup = document.getElementById("popup");
    popup.style.display = "block";
    popup.style.zIndex = 101;
    var msg = document.getElementById("popupMessage");
    msg.textContent = message;
    var titleEl = document.getElementById("popupTitle");
    titleEl.textContent = title;
    var hiddenEl1 = document.getElementById("popupHidden1");
    hiddenEl1.value = hidden1;
    var hiddenEl2 = document.getElementById("popupHidden2");
    hiddenEl2.value = hidden2;
    var popupInputRow1 = document.getElementById("popupInputRow1");
    var popupInputRow2 = document.getElementById("popupInputRow2");
    var popupInputRow3 = document.getElementById("popupInputRow3");
    if (inputLabel1 !== "") {
        popupInputRow1.style.display = "block";
        var label1 = document.getElementById("popupInputLabel1");
        label1.textContent = inputLabel1;
        var input1 = document.getElementById("popupInput1");
        input1.value = inputVal1;
        input1.focus();
    } else {
        popupInputRow1.style.display = "none";
    }
    if (inputLabel2 !== "") {
        popupInputRow2.style.display = "block";
        var label2 = document.getElementById("popupInputLabel2");
        label2.textContent = inputLabel2;
        var input2 = document.getElementById("popupInput2");
        input2.value = inputVal2;
    } else {
        popupInputRow2.style.display = "none";
    }
    if (inputLabel3 !== "") {
        popupInputRow3.style.display = "block";
        var label3 = document.getElementById("popupInputLabel3");
        label3.textContent = inputLabel3;
        var input3 = document.getElementById("popupInput3");
        input3.value = inputVal3;
    } else {
        popupInputRow3.style.display = "none";
    }
    setEnterKeys();
}

function setEnterKeys(){
    var popupInputRow1 = document.getElementById("popupInputRow1");
    var popupInputRow2 = document.getElementById("popupInputRow2");
    var popupInputRow3 = document.getElementById("popupInputRow3");
    var popupTextArea = document.getElementById("popupTextArea");
    var popupOk = document.getElementById("popupOk");
    popupInputRow1.addEventListener("keyup", function(event) {
        event.preventDefault();
        var keyCode = event ? (event.which ? event.which : event.keyCode) : event.keyCode;
        if(keyCode === 13)
        {
            document.getElementById("companyForm").submit();
        }
    });
    popupInputRow2.addEventListener("keyup", function(event) {
        event.preventDefault();
        var keyCode = event ? (event.which ? event.which : event.keyCode) : event.keyCode;
        if(keyCode === 13)
        {
            document.getElementById("companyForm").click();
        }
    });
    popupInputRow3.addEventListener("keyup", function(event) {
        event.preventDefault();
        var keyCode = event ? (event.which ? event.which : event.keyCode) : event.keyCode;
        if(keyCode === 13)
        {
            document.getElementById("companyForm").click();
        }
    });
    popupTextArea.addEventListener("keyup", function(event) {
        event.preventDefault();
        var keyCode = event ? (event.which ? event.which : event.keyCode) : event.keyCode;
        if(keyCode === 13)
        {
            document.getElementById("companyForm").click();
        }
    });
    //popupOk.dispatchEvent(event);
}

//showPopupWith2Inputs
function sppuw2i(title, message, hidden1, hidden2, inputLabel1, inputLabel2, inputVal1, inputVal2){
    sppuw3i(title, message, hidden1, hidden2, inputLabel1, inputLabel2, "", inputVal1, inputVal2, "");
}

function showAddBase(currUrl){
    sppuw1i("Update Base Url", "Enter the URL of your company's base page. Please include the protocol (i.e. https://).", "19924", "", "Url", currUrl);
    document.getElementById("currBaseUrl").value = currUrl;
}

function showAddLogin(currUrl){
    sppuw1i("Update Login Url", "Enter your company's login URL. Please include the protocol (i.e. https://).", "19925", "", "Url", currUrl);
}

function showChangeLdapUrl(currUrl){
    sppuw1i("Update Provider URL", "Enter your company's LDAP URL. Please include the protocol and the port (e.g. ldaps://ldap.example.com:636).", "34525", "", "LDAP Url:", currUrl);
}

function changeSearchBase(searchBase) {
    sppuw1i("Add Search Base", "Enter the LDAP search base. ", "34339", "", "Search Base:", searchBase);
}

function changeForegroundColor(foregroundColor) {
    sppuw1i("Update Foreground Color", "Enter Hex value of the foreground color you want to use. (e.g. #ffffff)", "34342", "", "Foreground Color", foregroundColor);
}

function changeBackgroundColor(backgroundColor) {
    sppuw1i("Update Background Color", "Enter Hex value of the background color you want to use. (e.g. #000000)", "34343", "", "Foreground Color", backgroundColor);
}

function changeIconUrl(iconUrl) {
    sppuw1i("Update Background Color", "Enter the icon url of a square icon. Please include the protocol (i.e. https://).", "34344", "", "Foreground Color", iconUrl);

}

function sppuw1i(title, message, hidden1, hidden2, inputLabel1, inputVal1){
    sppuw2i(title, message, hidden1, hidden2, inputLabel1, "", inputVal1, "");
}

//showCover
function shCr(){
    var cover = document.getElementById("cover");
    cover.style.display = "block";
    cover.style.zIndex = 100;

}

//hideCover
function hCr(){
    var cover = document.getElementById("cover");
    cover.style.display = "none";
    cover.style.zIndex = -100;
    var menu = document.getElementById("mobileMenuText");
    menu.style.display = "none";
    menu.style.zIndex = -100;

}

function getBasicCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(";");
    var i;
    var c;
    for(i = 0; i <ca.length; i+=1) {
        c = ca[ca.length - i - 1];
        while (c.charAt(0) === " ") {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
                setAllCookies(cname, c.substring(name.length, c.length));
            // alert("basicCookie get: "+ c.substring(name.length, c.length));
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function getControlOrigin(){
    var ep = ENDPT;
    if (lhTest){
       ep = "*";
    }
    return ep;
}

function showCorrectSection(f1Method) {
    if (f1Method === "ldap") {
        document.getElementById("samlSection").style.display = "none";
        document.getElementById("ldapSection").style.display = "block";
    } else if (f1Method === "saml") {
        document.getElementById("samlSection").style.display = "block";
        document.getElementById("ldapSection").style.display = "none";
    }
}

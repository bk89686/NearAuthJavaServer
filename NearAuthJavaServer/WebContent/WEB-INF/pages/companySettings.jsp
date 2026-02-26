<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="/WEB-INF/tld/spring.tld"  %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
	<%@ include file="header.jsp" %>
</head>
<body>
	<div id="banner">
		<a href="/servers"><span class="users">Servers</span></a>
		<a href="/serviceProviders"><span class="users">Service Providers</span></a>
        <a href="/company"><span class="users">Users</span></a>
		<a href="/documentation"><span class="users">Documentation</span></a>
		<a href="#" id='signOutClick'><span class="users">Sign Out</span></a>
	</div>
	<div id="mobileBanner"><img id="mobileMenu" src="/imgFiles/white_hamburger.png" alt="menu button"></div>
	<div id="fullBody">
		<%@ include file="sidePanel.jsp" %>
		<div id="centerPanel">
			<div class="settingsRow">
		       <input type='hidden' id='currLoginUrl' value='${loginUrl}'>
		        <c:choose>
		            <c:when test="${loginUrl eq ''}">
		                <span id='loginUrl'></span>
		                <button id='addLogin' class='companyTableButton'>Set Internal URL</button>
		            </c:when>
		            <c:otherwise>
		                Login Url: <span id='loginUrl'><a href='${loginUrl}'>${shortLoginUrl}</a></span>
		                <button id='updateLogin' class='companyTableButton block'>Change</button>
		            </c:otherwise>
		        </c:choose>
		    </div>
		    <div class="settingsRow">
		        <label for="requestAuthCodes">Request Authorization Codes: </label>
		        <input type="button" id="requestAuthCodes" value="Get Auth Codes">
		        <div class='authExplanationSmall'>Authorization codes can be given out to use as
		        	an alternate second factor in the case where a user cannot access their 
		        	phone.</div>
		    </div> 
	        <div class="settingsRow">
	            Web Server Public Key:
	            <c:choose>
	                <c:when test="${webServerKeyUploaded}">
	                    <span class='bold'>Uploaded</span><br>
	                    <button id='uploadWebServerPub' class='companyTableButton'>Re-Upload</button>
	                </c:when>
	                <c:otherwise>
	                    <span class='bold'>Not Uploaded</span><br>
	                    <button id='uploadWebServerPub' class='companyTableButton'>Upload</button>
	                </c:otherwise>
	            </c:choose>
	        </div>    
		    <div class="settingsRow">
		        <label for="f1Method">First Factor Authorization Method: </label>
		        <select id="f1Method">
		            <%-- <option value="oAuth" ${f1Method_iFrameSelected}>OAuth</option> --%>
		            <%-- <option value="openId" ${f1Method_openIdSelected}>OpenID Connect</option> --%>
		            <c:if test="${f1Method eq ''}">
		            	<option value="" selected>Please Select</option>
		            </c:if>
		            <option value="ldap" ${f1Method_ldapSelected}>LDAP</option>
		            <option value="saml" ${f1Method_samlSelected}>SAML</option>
		        </select>
		    </div> 
		    <div class="settingsRow">Users not in the console:
		        <div class="radioButton">
		        	<input type="radio" id="noConsoleIn" name="noConsoleStatus" value="in" ${noConsoleInSelected}>
		        	<label for="noConsoleIn">Let them in with first factor auth</label>
		        </div>
		        <div class="radioButton">
		        	<input type="radio" id="noConsoleOut" name="noConsoleStatus" value="out" ${noConsoleOutSelected}>
		        	<label for="noConsoleOut">Keep them out</label>
		        </div>
		    </div>
		    <div class="settingsRow">Users in the console, but without devices
		    	<div class="radioButton">
		    		<input type="radio" id="noDeviceIn" name="noDeviceStatus" value="in" ${noDeviceInSelected}>
		        	<label for="noDeviceIn">Let them in with first factor auth</label>
		        </div>
		        <div class="radioButton">
		        	<input type="radio" id="noDeviceOut" name="noDeviceStatus" value="out" ${noDeviceOutSelected}>
		        	<label for="noDeviceOut">Keep them out</label>
		        </div>
		    </div> 
		    <div id='idpInfo'>
		    	<div class='settingsTitle'>Identity Provider Information</div>
		    	<div id='samlSection'>	
			    	<div class='settingsRow'>SAML Metadata from Your Identity Provider:
		                <span class='bold'>
		                    <c:choose>
		                        <c:when test="${samlDataUploaded}">
		                            Uploaded
		                        </c:when>
		                        <c:otherwise>
		                            Not uploaded
		                        </c:otherwise>
		                    </c:choose>
		                </span>
						<br>
	                    <c:choose>
	                        <c:when test="${samlDataUploaded}">
	                            <button id='updateSamlData' class='companyTableButton block'>Upload New</button>
	                        </c:when>
	                        <c:otherwise>
	                            <button id='updateSamlData' class='companyTableButton block'>Upload SAML</button>
	                        </c:otherwise>
	                    </c:choose>
                    </div>
	                <div class="settingsRow">
				    	SAML Metadata for Your Identity Providers:<br>
				    	<button id='downloadMetadataForIdp' class='companyTableButton'>Download</button>
				    </div>
				    <div class="settingsRow">
				    	<input type="checkbox" 
				    	<c:if test='${ allowAllFromIdp eq true}'>checked="true"</c:if>
				    	id='allowAllIdpUsers' name='allowAllIdpUsers' value='allowAll'>
				    	<label for='allowAllIdpUsers'>Allow All IDP Users (not just those in this portal) </label>
				    </div>
				    <div class="settingsRow">
				    	<input type="checkbox" 
				    	<c:if test='${ moveB2fUsersToIdp eq true}'>checked="true"</c:if>
				    	id='addToAd' name='addToAd' value='addUsersToAd'>
				    	<label for='addToAd'>Add users from this portal into Azure AD (Azure AD users only) </label>
				    </div>
				</div>
				<div id="ldapSection">
					<div class="settingsTitle">LDAP Settings</div>
					<div class="settingsRow">
						<c:choose>
				            <c:when test="${providerUrl eq ''}">
				                <span id='providerUrl'></span>
				                <button id='updateProviderUrl' class='companyTableButton'>Set Provider URL</button>
				            </c:when>
				            <c:otherwise>
				                Provider Url: <span id='providerUrl'>${providerUrl}</span>
				                <button id='updateProviderUrl' class='companyTableButton block'>Reset</button>
				            </c:otherwise>
				        </c:choose>
					</div>
					<div class="settingsRow">
						<c:choose>
				            <c:when test="${searchBase eq ''}">
				                <span id='searchBase'></span>
				                <button id='updateSearchBase' class='companyTableButton'>Set Provider URL</button>
				            </c:when>
				            <c:otherwise>
				                Search Base: <span id='searchBase'>${searchBase}</span>
				                <button id='updateSearchBase' class='companyTableButton block'>Reset</button>
				            </c:otherwise>
				        </c:choose>
					</div>
					<div class="settingsRow">
						.jks file: 
						<c:choose>
							<c:when test="${jksUploaded}">
								<span class='bold'>Uploaded</span><br>
								<button id='uploadJks' class='companyTableButton'>Upload New</button>
							</c:when>
							<c:otherwise>
								<button id='uploadJks' class='companyTableButton'>Upload</button>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
		    </div>
		    <div id='spInfo'>
		    	<div class='settingsTitle'>Information for Service Providers</div>
		    	<div class="settingsRow">
				    	SAML Metadata for Your Service Providers:<br>
				    	<button id='downloadMetadataForSp' class='companyTableButton'>Download</button>
				    </div>
			    <div class="settingsRow">
			        EntityID (Identifier):
			        <div class='url'>
			        	${secureUrl }/SAML2/SSO/${ apiKey1 }${ apiKey2 }${ apiKey3 }/EntityId
			        </div>
			    </div>
			    <div class="settingsRow">
			        ACS URL (Reply URL):
			        <div class='url'>
			        	${secureUrl }/SAML2/SSO/${ apiKey1 }${ apiKey2 }${ apiKey3 }/fromIdp
			        </div>
			    </div>
			    <div class="settingsRow">
			        Sign On:
			        <div class='url'>
			        	${secureUrl }/SAML2/SSO/${ apiKey1 }${ apiKey2 }${ apiKey3 }/SignIn
			        </div>
			    </div>
			    <div class="settingsRow">
			        Signout:
			        <div class='url'>
			        	${secureUrl }/signout
			        </div>
			    </div>
			    <div class="settingsRow">
			        x509 Cert for Service Providers:<br>
			        <button id='downloadX509' class='companyTableButton'>Download</button>
			    </div>
			</div>
		</div>
		<div class='bottomPadding'></div>
	</div>
	<input type='hidden' id='f1MethodFromServer' value="${f1Method}">
	<input type='hidden' id='companyId' value="${companyId}">
    <%@ include file="companyFooter.jsp" %>
</body>
</html>

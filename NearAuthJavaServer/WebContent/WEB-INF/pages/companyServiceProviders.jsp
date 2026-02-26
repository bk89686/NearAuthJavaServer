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
    <input type='hidden' id='publicKey' value='${ publicKey }'>
    <input type='hidden' id='noDevicesRegistered' value='${ noDevicesRegistered }'>
	<div id="banner">
		<a href="/servers"><span class="users">Servers</span></a>
		<a href="/settings"><span class="users">Settings</span></a>
        <a href="/"><span class="users">Users</span></a>
		<a href="/documentation"><span class="users">Documentation</span></a>
		<a href="#" id='signOutClick'><span class="users">Sign Out</span></a>
	</div>
	<div id="mobileBanner"><img id="mobileMenu" src="/imgFiles/white_hamburger.png" alt="menu button"></div>
	<div id="fullBody">
		<%@ include file="sidePanel.jsp" %>
		<div id="centerPanel">
			<div id="message" class='redMessage'>${ errorMessage }</div>
			<input type='hidden' id='companyId' value='${ companyId }'>
			<img id="licenseLogo" src="/imgFiles/b2f96.png" alt="NearAuth.ai logo">
			<div class="companyCenterPanelTable">
				<div class="sidePanelRow">
				Add Service Provider
				</div>
				<div>
					<button id='addServiceProviderMetadataButton' class='companyTableButton'>Using Metadata</button>
				</div>
				<div>
					<button id='addServiceProviderButton' class='companyTableButton'>Manually</button>
				</div>
				<div class="companyCenterPanelTable">
					<c:choose>
						<c:when test="${serviceProviderCount gt 0}">
							<div id="serviceProviderTable">
								<div id="serviceProviderTableHeader">
									<div class="serviceHeaderCell1 headerCell">SP Name</div>
									<div class="serviceHeaderCell2 headerCell">ACS URL</div>
									<div class="serviceHeaderCell3 headerCell">Change</div>
								</div>
								<c:set var="serviceProvidersCount" value="0" scope="page" />
								<c:forEach items="${serviceProviders}" var="serviceProvider" varStatus="serviceProviderStatus">
									<div class="serviceProviderRow">
										<div id="spName${serviceProvidersCount}" class="serviceProviderCell1 licenseCell">${ serviceProvider.serviceProviderName }</div>
										<div id="spAcs${serviceProvidersCount}" class="serviceProviderCell2 licenseCell">${ serviceProvider.acsUrl }</div>
										<div id="spId${serviceProvidersCount}" class="displayNone">${ serviceProvider.tableId}</div>
										<div id="spEntityId${serviceProvidersCount}" class="displayNone">${ serviceProvider.serviceProviderEntityId }</div>
										<div class="serviceProviderCell3 licenseCell">  
									    <div class="editServiceProvider">
										    <button id='editServiceProvider${serviceProvidersCount}' class='companyTableButton editServiceProviderButton'>
												Edit Service Provider
										    </button>
									    </div>
									    <%-- <div class="addCertServiceProvider">
										    <button id='addCertServiceProvider${serviceProvidersCount}' class='companyTableButton addCertServiceProviderButton'>
												Add Encryption Cert
										    </button> 
									    </div> --%>
									    <div class="removeServiceProvider">
										    <button id='removeServiceProvider${serviceProvidersCount}' class='companyTableButton removeServiceProviderButton'>
												Delete Service Provider
										    </button>
									    </div>
										</div>
									</div>
									<c:set var="serviceProvidersCount" value="${serviceProvidersCount + 1}" scope="page"/>
								</c:forEach>
							</div>
						</c:when>
						<c:otherwise>
							<div class='serverReason'>You have not registered any service providers.</div>
						</c:otherwise>
					</c:choose>
			    </div>
				
				<c:if test="${ noDevicesRegistered == true}">
					<!-- <div class='gotoInstall'>Continue to <a href='https://www.NearAuth.ai/installing'>Install Instructions</a></div> -->
				</c:if>
				<div class='bottomPadding'></div>
			</div>
		</div>
	</div>
	<%@ include file="companyFooter.jsp" %>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
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
		<a href="/serviceProviders"><span class="users">Service Providers</span></a>
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
				<div>
					    <c:if test='${userRole eq "ADMIN" || userRole eq "SUPER_ADMIN"}'>
							<button id='addServerButton' class='companyTableButton'>Add Server</button>
						</c:if>
					</div>
				<div class="companyCenterPanelTable">
					<c:choose>
						<c:when test="${serverCount gt 0}">
						    <div class="licenseRow licenseRowHeader">
							    <div class="serverCell1 licenseCell borderNone">Server Name</div>
							    <div class="serverCell2 licenseCell borderNone">Server ID</div>
							    <div class="serverCell3 licenseCell borderNone">Description</div>
						    </div>
						    <c:set var="serverCount" value="0" scope="page" />
							<c:forEach items="${servers}" var="server" varStatus="serverStatus">
								<div class="serverRow">
									<div class="serverCell1 licenseCell">${ server.serverName }</div>
									<div class="serverCell2 licenseCell">${ server.serverId }</div>
									<div class="serverCell3 licenseCell">${ server.description }</div>
		                            <input type="hidden" id="serverId_${serverCount}" value='${ server.b2fId }'>
									<c:if test='${userRole eq "SUPER_ADMIN"}'>    
									    <div class="removeServer">
										    <button id='removeServer${serverCount }'class='companyTableButton removeServerButton'>
												Delete Server
										    </button>
									    </div>
									</c:if>
								</div>
								<c:set var="serverCount" value="${serverCount + 1}" scope="page"/>
							</c:forEach>
						</c:when>
						<c:otherwise>
							<div class='serverReason'>You do not have any servers registered.</div>
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

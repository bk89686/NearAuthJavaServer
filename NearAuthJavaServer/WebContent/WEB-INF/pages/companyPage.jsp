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
    	<a href="/settings"><span class="users">Settings</span></a>
        <a href="/servers"><span class="users">Servers</span></a>
        <a href="/serviceProviders"><span class="users">Service Providers</span></a>
		<a href="/documentation"><span class="users">Documentation</span></a>
		<a href="#" id='signOutClick'><span class="users">Sign Out</span></a>
	</div>
	<div id="mobileBanner"><img id="mobileMenu" src="/imgFiles/white_hamburger.png" alt="menu button"></div>
	<div id="fullBody">
		<%@ include file="sidePanel.jsp" %>
		<div id="centerPanel">
			<div id="message" class='redMessage'>${ errorMessage }</div>
			<img id="licenseLogo" src="/imgFiles/b2f96.png" alt="NearAuth.ai logo">
			<div>
				<c:choose>
					<c:when test="${licensesInUse lt licenseCount}">
						<button id='addUserButton' class='companyTableButton'>Add User</button>
					</c:when>
					<c:otherwise>
						<%-- <button id='addUserButton' onClick='addLicensesToCompany("${companyId}")'>Add License</button> --%>
					</c:otherwise>
				</c:choose>
			</div>
			<div class="companyCenterPanelTable">
			    <input type='hidden' id='groupId' value='${ user.groupId }'>
			    <input type='hidden' id='companyId' value='${ companyId }'>
			    <input type='hidden' id='loginUrl' value='${loginUrl}'>
				<div class="licenseRow licenseRowHeader"> 
					<div class="licenseCell1 licenseCell borderNone">User Name</div>
					<div class="licenseCell2 licenseCell borderNone">email</div>
					<div class="licenseCell3 licenseCell borderNone">Uid</div>
					<div class="licenseCell3a licenseCell borderNone">Role</div>
					<div class="licenseCell4 licenseCell borderNone">Setup</div>
					<!-- <div class="licenseCell5 licenseCell borderNone">Registered Devices</div> -->
					<div class="licenseCell6 licenseCell borderNone">Devices in Use</div>
				</div>
				<c:set var="count" value="0" scope="page" />
				<c:forEach items="${users}" var="user" varStatus="userStatus">
					<div class="licenseRow">
					    <input type="hidden" id="groupId_${count }" value='${ user.groupId }'>
						<div id='username${count }' class="licenseCell1 licenseCell">${ user.username }</div>
						<div id='email${count }' class="licenseCell2 licenseCell">${ user.email }</div>
						<div id='uidField${count }' class="licenseCell3 licenseCell">
						    <div class='uidDiv'>
						        <span id='uid${count }'>${ user.uid }</span>
						    </div>
						</div>
						<div id='userType${count }' class="licenseCell3a licenseCell">${ user.userType }</div>
						<div id='setup${count }' class="licenseCell4 licenseCell">${user.setup }</div>
						<%-- <div id='registeredDevices${count }' class="licenseCell5 licenseCell">${ user.registeredDevices }</div> --%>
						<div id='devicesInUse${count }' class="licenseCell6 licenseCell">${ user.devicesInUse }</div>
						<c:if test='${userRole eq "ADMIN" || userRole eq "SUPER_ADMIN"}'>
							<div class="remove">
							    <c:if test='${userRole eq "SUPER_ADMIN"}'>
								    <button id='changeUserType${count}' class='companyTableButton changeRoleButton'>Change Role</button>
							    </c:if>
							    <button id='addUid${count }' class='companyTableButton addUidButton'>Add/Change UID</button>
							    <c:if test='${userRole eq "SUPER_ADMIN"}'>
								    <button class='companyTableButton resetUserButton' id='resetUserButton${count }'>Reset Devices</button>
								</c:if>
								<c:if test='${userRole eq "SUPER_ADMIN"}'>
								    <button class='companyTableButton removeUserButton' id='removeUserButton${count }'>Remove User</button>
							    </c:if>
							</div>
						</c:if>
					</div>
					<c:set var="count" value="${count + 1}" scope="page"/>
				</c:forEach>
				<c:if test="${ noDevicesRegistered == true}">
					<!-- <div class='gotoInstall'>Continue to <a href='https://www.NearAuth.ai/installing'>Install Instructions</a></div> -->
				</c:if>
			</div>
			<div class='bottomPadding'></div>
		</div>
	</div>
	<%@ include file="companyFooter.jsp" %>
	
</body>
</html>

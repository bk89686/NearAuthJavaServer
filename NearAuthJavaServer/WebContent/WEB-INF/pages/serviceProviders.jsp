<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="header.jsp" %>
</head>
<body>
    <input type='hidden' id='publicKey' value='${ publicKey }'>
    <input type='hidden' id='noDevicesRegistered' value='${ noDevicesRegistered }'>
    <div id="banner">
        <!-- <a href="/billing"><span id="companyLink">Billing</span></a> -->
        <a href="/documentation"><span class="users">Documentation</span></a>
        <a href="#" id='signOutClick'><span class="users">Sign Out</span></a>
    </div>
    <div id="mobileBanner"><img id="mobileMenu" src="/imgFiles/white_hamburger.png" alt="menu button"></div>
    <div id="fullBody">
        <%@ include file="sidePanel.jsp" %>
        <div id="centerPanel">
            <div id="message" class='redMessage'>${ errorMessage }</div>
            <img id="licenseLogo" src="/imgFiles/b2f96.png" alt="NearAuth.ai logo">
            <div class="companyCenterPanelTable">
                <input type='hidden' id='groupId' value='${ user.groupId }'>
                <input type='hidden' id='companyId' value='${ companyId }'>
                <div class="licenseRow licenseRowHeader">
                    <div class="licenseCell1 licenseCell borderNone">Service Provider</div>
                    <div class="licenseCell2 licenseCell borderNone">email</div>
                    <div class="licenseCell3 licenseCell borderNone">Uid</div>
                    <div class="licenseCell3a licenseCell borderNone">Role</div>
                    <div class="licenseCell4 licenseCell borderNone">Setup</div>
                    <!-- <div class="licenseCell5 licenseCell borderNone">Registered Devices</div> -->
                    <div class="licenseCell6 licenseCell borderNone">Devices in Use</div>
                </div>
                <c:set var="count" value="0" scope="page" />
                <c:forEach items="${serviceProviders}" var="serviceProvider">
                    <div class="licenseRow">
                        <div class="serviceProvider">
                            serviceProviders.name <button name='${serviceProvider.name}' id='${serviceProvider.id}' class='companyTableButton downloadSpCert'>Download Cert</button>
                            <button id='delete${serviceProvider.id}' class='companyTableButton deleteSp'>Delete Service Provider</button>
                        </div>    
                    </div>
                </c:forEach>
            </div>
            <button id='newServiceProvider' class='companyTableButton block'>New Service Provider</button>
        </div>
    </div>
    <%@ include file="companyFooter.jsp" %>
</body>
</html>

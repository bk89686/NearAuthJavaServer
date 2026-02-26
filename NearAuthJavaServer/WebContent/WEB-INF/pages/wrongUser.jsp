<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="/WEB-INF/tld/spring.tld"  %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Resync Needed</title>
    <%@ include file="header.jsp" %>
</head>
<body>
    <div id="banner">
    </div>
    <div id="failureBody">
        <div id="centerPanelCo">
            <img id="licenseLogoCentered" src="/imgFiles/Icon-App-76x76@3x.png" alt="NearAuth.ai logo">
            <div id="centerPanelTableWide">
                <div id="failureMessage1">The user that signed in is not the registered user of this device.</div> 
            </div>
            <div id="failureMessage1">${message}</div>
        </div>
    </div>
</body>
</html>

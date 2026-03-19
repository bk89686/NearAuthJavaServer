<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="header.jsp" %>
</head>
<body>
    <div id="banner">

    </div>
    <div id="failureBody">
        <div id="centerPanelCo">
            <img id="licenseLogoCentered" src="/imgFiles/Icon-App-76x76@3x.png" alt="NearAuth.ai logo">
            <div id="centerPanelTableWide">
                <div id="failureMessage1">We failed to confirm your identity.</div>
                <br><br>
                <input type='hidden' id='b2fFailureFlag' value='true'/>
            </div>
        </div>
    </div>
    <%@ include file="footer.jsp" %>
</body>
</html>
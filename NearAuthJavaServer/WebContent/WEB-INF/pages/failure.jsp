<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="/WEB-INF/tld/spring.tld"  %>
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
                <div id="failureMessage1">${ message1 }</div>
                <br>
                <div id="failureMessage2">${ message2 }</div>
                <input type='hidden' id='b2fFailureFlag' value='true'/>
            </div>
            <c:choose>
                <c:when test="${not empty previousUrl}">
                    <div>
	                    <form id='b2f'>
	                        <input type='hidden' id='previousUrl' value='${ previousUrl }'>
	                        <input type='hidden' id='b2fSetupFlag' value=${ setup }>
	                    </form>
		                <button id='credentials' class='companyTableButton'>Enter Credentials</button>
                    </div>
                </c:when>
            </c:choose>
        </div>
    </div>
    <%@ include file="footer.jsp" %>
</body>
</html>

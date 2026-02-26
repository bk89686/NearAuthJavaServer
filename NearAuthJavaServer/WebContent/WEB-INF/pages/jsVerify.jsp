<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="/WEB-INF/tld/spring.tld"  %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Verification</title>
    <%@ include file="header.jsp" %>
</head>
<body>
    <div id="failureBody">
    	<div class='hidden' id='fromIdp'>${fromIdp}</div>
        <div id="centerPanelCo">
            <img id="licenseLogoCentered" src="/imgFiles/Icon-App-76x76@3x.png" alt="NearAuth.ai logo">
            <c:if test="${biometrics}">
                <div id='biometrics'>
                    To access the site you requested, please click the button to provide addition credentials.
                    <div id='bioButtonDiv'><button id='credentials' class='companyTableButton'>Enter Credentials</button></div>
                </div>
                <div class='smallAlternative'>
                    Alternatively, you can ask your administrator for a one time code, and enter it 
                 	<a href='/adminstratorCode'>here</a>.
                </div>
            </c:if>
            <c:choose>
                <c:when test="${pushSent}">
                     <div id='pushSent'>
                         To access the site you requested, please respond to the push notification that has
                         been sent to you on another device.
                     </div>
                     <div><input type="button" value="Push Again" id="pushAgain" class="companyTableButton"/></div>
                     <div class='smallAlternative'>
                    	(Alternatively, you can ask your administrator for a one time code, and enter it 
                    	<a href='/adminstratorCode'>here</a>.
                    </div>
                </c:when>
            </c:choose>
            <c:choose>
                <c:when test="${textSent}">
                     <div id='pushSent'>
                         To access the site you requested, please respond to the push notification that has
                         been sent to you on another device.
                     </div>
                     <div><input type="button" value="Push Again" id="pushAgain" class="companyTableButton"/></div>
                     <div class='smallAlternative'>
                    	(Alternatively, you can ask your administrator for a one time code, and enter it 
                    	<a href='/adminstratorCode'>here</a>.)
                    </div>
                </c:when>
            </c:choose>
            <c:choose>
                <c:when test="${outOfRange}">
                    <div id='outOfRange'>
                        To access the site you requested, you must have another NearAuth.ai registered
                        device nearby. In your case, we could not find one.
                    </div>
                    <div class='smallAlternative'>
                    	(Alternatively, you can ask your administrator for a one time code, and enter it 
                    	<a href='/adminstratorCode'>here</a>.)
                    </div>
                </c:when>
            </c:choose>
            <c:choose>
                <c:when test="${dumbphoneNotEnabled}">
                    <div id='outOfRange'>
                        You may want to have your administrator turn on text messaging authentication for you.
                    </div>
                    <div class='smallAlternative'>
                    	(Alternatively, you can ask your administrator for a one time code, and enter it 
                    	<a href='/adminstratorCode'>here</a>.)
                    </div>
                </c:when>
            </c:choose>
        </div>
    </div>
    <form method="post" id="samlForm" class="displayNone" action="${action}">
        <input type="hidden" name="SAMLResponse" value="${samlText}"/>
        <input type="hidden" name="RelayState" value="${relayState}"/>
        <input type="submit" id="samlSubmit" value=""/>
    </form>
    <%@ include file="footer.jsp" %>
</body>
</html>

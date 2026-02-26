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
    <div id="signinBody">
    	<div class='hidden' id='fromIdp'>${fromIdp}</div>
        <div id="centerPanelCo">
            <img id="licenseLogoDelete" src="/imgFiles/Icon-App-76x76@3x.png" alt="NearAuth.ai logo">
            <div id="centerPanelTable">
            	<c:choose>
					<c:when test="${showForm}">
		                <form id='coForm' action="/adminstratorCode" method="post">
		                    <span id='signinLabelDelete'>
		                        Please enter the code provided by your administrator.
		                    </span>
			                <span id='textValidate'>
		                        <span id='usernameText' class='signinItem'>
		                            <input type='text' name='adminCode' id='adminCode' autocomplete='xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'>
		                        </span>
	                    	</span>
	                    	<div id='textSubmit'><button type='submit' class='companyTableButton'>Submit</button></div>
		                </form>
                	</c:when>
                	<c:otherwise>
                		<c:choose>
	                		<c:when test="${ errorFound eq false}">
		                		<span class='textLabel'>
			                        You were authenticated.
			                    </span>
		                    </c:when>
	                    </c:choose>
                	</c:otherwise>
                </c:choose>
                <div id='incorrectText'>${ errorMessage }</div>
            </div>
        </div>
    </div>
    <%@ include file="companyFooter.jsp" %>
</body>
</html>

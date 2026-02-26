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
        <div id="centerPanelCo">
            <div class='incorrect'>${ errorMessage }</div>
            <img id="licenseLogoDelete" src="/imgFiles/Icon-App-76x76@3x.png" alt="NearAuth.ai logo">
            <div id="centerPanelTable">
            	<c:choose>
					<c:when test="${showForm}">
		                <form id='coForm' action="/deleteMyData" method="post">
		                    <span id='signinLabelDelete'>
		                        Are you sure you want to delete your NearAuth.ai data? This is irreversible.
		                    </span>
		                    <div class='signinRowDelete'><button type='submit' class='companyTableButton'>Delete My Data</button></div>
		                    <input type='hidden' id='b2fIgnorePageFlag' value='true'/>
		                </form>
                	</c:when>
                	<c:otherwise>
                		<c:choose>
	                		<c:when test="${ errorFound eq false}">
		                		<span id='signinLabels'>
			                        Your data has been removed.
			                    </span>
		                    </c:when>
	                    </c:choose>
                	</c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    <%@ include file="companyFooter.jsp" %>
</body>
</html>

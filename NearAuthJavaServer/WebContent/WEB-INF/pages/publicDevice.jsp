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
                <div id="failureMessage1">If 
                    you have NearAuth.ai set up on another device, but not this one, you can click the button below to 
                    get one time access.</div>
                <br><br>
                <form id="oneTimeForm" action="/oneTimeAccess" method="post">
                   <input type='hidden' id='company' name='company' value="${company}"/>
                   <input type="submit" value="One Time Access" id="oneTimeAccess" class="companyTableButton"/>            
                </form>
                <div id="failureMessage3">
                    Since this is a public computer, please remember to sign out and quit your browser afterwards.
                </div>
                <input type='hidden' id='b2fFailureFlag' value='true'/>
            </div>
        </div>
    </div>
    <%@ include file="footer.jsp" %>
</body>
</html>

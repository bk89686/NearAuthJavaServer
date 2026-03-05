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
                <div id="failureMessage1">This service provider isn't registered with NearAuth.ai. Please talk to your
                identity management administrator if you think it needs to be.<!--<<br/><br/>If you do not have NearAuth.ai, you can install it from 
                <a href="https://www.NearAuth.ai/downloads">www.NearAuth.ai/downloads</a>.--></div>
                <br><br>
                <form id="oneTimeForm" action="/oneTimeAccess" method="post">
                   <input type='hidden' id='company' name='company' value="${company}"/>
                   <input type="submit" value="One Time Access" id="oneTimeAccess" class="companyTableButton"/>            
                </form>
                <div id="failureMessage3">
                    If you have questions, please feel free to contact us at <a href="mailto:help@NearAuth.ai">help@NearAuth.ai</a>.
                </div>
                <input type='hidden' id='b2fFailureFlag' value='true'/>
            </div>
        </div>
    </div>
    <%@ include file="footer.jsp" %>
</body>
</html>

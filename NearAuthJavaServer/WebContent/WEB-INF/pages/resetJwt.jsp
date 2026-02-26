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
    <div id='appPleaseWait'>
        <div class="pleaseWait-stripe"><div class="pleaseWait"><div></div></div></div>
    </div>
    <script type="text/javascript" src="/js/base64url-arraybuffer.js"></script>
    <form id='setupForm' method='POST' action='${submitUrl}'>
        <input type='hidden' id='B2F_AUTHN' name='B2F_AUTHN' value='${jwt}'>
        <input type='hidden' id='b2fSetup' name='b2fSetup' value='${b2fSetup}'>
    </form>
    <div id='central' style='display:none;'>${central}</div>
    <script>
        //alert("sending setup " + ${b2fSetup} + " to " + ${submitUrl});
        document.getElementById("setupForm").submit();
    </script>
</body>
</html>

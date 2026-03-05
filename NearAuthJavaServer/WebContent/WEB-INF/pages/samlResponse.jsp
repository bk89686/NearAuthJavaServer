<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="header.jsp" %>
    <script>
        function submit(){
            document.getElementById("samlForm").submit();
        }
    </script>
</head>
<body onload="submit()">
    <div id="fullBody">

        <div id="signInForm" class='displayNone'> 
            <form method="post" id="samlForm" action="${action}">
                <input type="hidden" name="SAMLResponse" value="${samlText}"/>
                <input type="hidden" name="RelayState" value="${relayState}"/>
                <input type="submit" id="samlSubmit" value=""/>
            </form>
        </div>
    </div>
</body>
</html>

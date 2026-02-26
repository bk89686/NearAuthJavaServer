<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="/WEB-INF/tld/spring.tld"  %>
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
        <div id="signInForm" style='display:hidden'> 
            <form method="post" id="samlForm" action="${action}">
                <input type="hidden" name="SAMLRequest" value="${samlText}"/>
                <input type="submit" value="Submit"/>
            </form>
        </div>
    </div>
</body>
</html>

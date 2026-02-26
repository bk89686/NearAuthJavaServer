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
    <div id='appPleaseWait'>You are being redirected to your login page.</div>
    <script>
    	sleep(1);
    	location.href = "/oneTimeAccess?company=${company}"
    </script>
</body>
</html>
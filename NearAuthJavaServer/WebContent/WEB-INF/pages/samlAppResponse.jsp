<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="header.jsp" %>
</head>
<body>
    <c:choose>
        <c:when test="${outcome eq 0}">
            <div id='appSuccess'>Success</div>
            <div id='appPleaseWait'><div class="pleaseWait-stripe"><div class="pleaseWait"><div></div></div></div></div>
        </c:when>
        <c:otherwise>
            <div id='appSuccess'>We could not authorize you.</div>
        </c:otherwise>
    </c:choose>
</body>
</html>

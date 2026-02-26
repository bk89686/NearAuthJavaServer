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
	<label for="companies">Choose a company name:</label> 
	<form method='POST'>
	<select id="company" name="company"> 
		<c:forEach items="${companies}" var="company">
			<option value="${company.companyId}">${company.companyName}</option> 
		</c:forEach>
		</select>
		<input type='submit'>
	</form>

    <c:forEach items="${groups}" var="group">
        <div class='h1'>${group.name}</div>
        <div class='connections'>
            <div class="h2">Connections</div>
            <c:forEach items="${group.connectionStatuses}" var="connStatus">
                <div class='connection'><span class="bold">${connStatus.peripheralType}</span> to <span class="bold">${connStatus.centralType}</span>: 
                    connected = <span class="bold">${connStatus.proximate}</span></div> 
                <div class='connection'>lastConnection: <span class="bold">${connStatus.lastConnection}</span></div> 
            </c:forEach>
            <div class="h2">Devices</div>
            <c:forEach items="${group.deviceStatuses}" var="devStatus">
                <div class='device'><span class="bold">${devStatus.deviceType}</span>: proximate: <span class="bold">${devStatus.proximate}</span>, permGiven: <span class="bold">${devStatus.permGiven}</span></div>
            </c:forEach>
        </div>
        <div class='separator'>***********</div>
    </c:forEach>
</body>
</html>
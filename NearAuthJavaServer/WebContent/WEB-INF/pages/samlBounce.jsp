<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="/WEB-INF/tld/spring.tld"  %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="header.jsp" %>
    <script>
        function reloadPage(){
            var coId = document.getElementById("coId").value;
            var newUrl = location.href.replace(coId, coId+"-r");
            location.href=newUrl;
        }
    </script>
</head>
<body onLoad="reloadPage()">
    <div id='appPleaseWait'><div class="pleaseWait-stripe"><div class="pleaseWait"><div></div></div></div></div>
    
    <input type="hidden" id='coId' value="${companyId}">
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="refresh" content="180">
    <title>Connection Information for ${deviceData.deviceType }</title>
    <style>
        table {
            border-collapse: collapse;
            width: 100%;
            margin: 20px 0;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
        }
        th {
            background-color: #f2f2f2;
        }
        .connected {
            background-color: #defae1;
        }
        
        .not_connected {
        	background-color: #fcdee1;
        }
        #timePresent tbody tr:nth-child(even) {
            background-color: #f9f9f9;
        }
        
    </style>
    <%@ include file="header.jsp" %>
</head>
<body style='margin:20px;font-family:san-serif;background:#fff'>
	<h2>Connection Information for ${deviceData.deviceType }</h2>
    <h5>Central type: ${deviceData.centralType}</h5>
    <c:if test="${deviceData.reason ne 'outcome: 0'}">
    	<p>${deviceData.reason}</p>
    </c:if>
    <h4 style='text-align:right; margin-top:-40px;'>${deviceData.currentTime}</h4>
    <table id='timePresent'>
    	<tr>
    		<th>Date</th>
    		<th>Time Present</th>
    	</tr>
    	<c:forEach items="${totalTime}" var="totalTimeEntry" varStatus="loop">
    		<%-- <c:if test="${!loop.last}"> --%>
	    		<tr>
	    			<td>${totalTimeEntry.key}</td>
	    			<td>${totalTimeEntry.value}</td>
	    		</tr>
    		<%-- </c:if> --%>
    	</c:forEach>
    </table>
    <table>
        <tr>
			<th>Event Time</th>
			<th>Event Date</th>
            <th>Connected</th>
            <th>Timespan</th>
            <th>From</th>
        </tr>
        <c:forEach items="${deviceData.deviceDataOneDevices}" var="device">
        	<c:choose>
            	<c:when test="${device.connected}">
	            	<tr class='connected' style='height:${device.cellHeight}px; font-size:${device.fontSize}em;'>
		                <td style='padding-top:${device.padding}px;padding-bottom:${device.padding}px;'>${device.sTime}</td>
		                <td style='padding-top:${device.padding}px;padding-bottom:${device.padding}px;'>${device.sDate}</td>
			            <td style='padding-top:${device.padding}px;padding-bottom:${device.padding}px;'>${device.sConnected}</td>
		                <td style='padding-top:${device.padding}px;padding-bottom:${device.padding}px;'>${device.timeSpan}</td>
		                <td style='padding-top:${device.padding}px;padding-bottom:${device.padding}px;'>${device.description}</td>
	                </tr>
                </c:when>
                <c:otherwise>
                	<tr class='not_connected' style='height:${device.cellHeight}px; font-size:${device.fontSize}em;'>
		                <td style='padding-top:${device.padding}px;padding-bottom:${device.padding}px;'>${device.sTime}</td>
		                <td style='padding-top:${device.padding}px;padding-bottom:${device.padding}px;'>${device.sDate}</td>
			            <td style='padding-top:${device.padding}px;padding-bottom:${device.padding}px;'>${device.sConnected}</td>
		                <td style='padding-top:${device.padding}px;padding-bottom:${device.padding}px;'>${device.timeSpan}</td>
		                <td style='padding-top:${device.padding}px;padding-bottom:${device.padding}px;'>${device.description}</td>
	                </tr>
                </c:otherwise>
                </c:choose>
            
        </c:forEach>
    </table>
    
</body>
</html>

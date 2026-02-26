<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Device Information</title>
    <style>
    	body {
    		padding: 50px;
    	}
    	@media screen and (max-width: 600px) {
	    	body {
	    		padding: 15px;
	    	}
	    	.hideMobile {
	    		display: none;
	    	}
    	}
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
        tr:nth-child(even) {
            background-color: #f9f9f9;
        }
        
        .connected {
            background-color: #defae1;
        }
        
        .not_connected {
        	background-color: #fcdee1;
        }
        
        #startTimeDiv {
        	padding: 10px;
        }
        
        #startDataDiv {
        	padding: 10px;
        }
        
        #endTimeDiv {
        	padding: 10px;
        	display: none;
        }
        
        #endDateDiv {
        	padding: 10px;
        	display: none;
        }
        
        #startSecondsLabel {
        	padding-left: 10px;
        }
        
        #startSeconds {
        	width: 30px;
        }
        
        #startMillisLabel {
        	padding-left: 10px;
        }
        
        #startMillis {
        	width: 30px;
        }
        
        #submitTime {
        	left: 276px;
        	position:relative;
        }
        
        #pageTime {
        	text-align: right;
        }
        
    </style>
    <%@ include file="header.jsp" %>
</head>
<body>
	<c:if test="${outcome != 0}">
		Bad outcome: ${reason}
	</c:if>
	<c:if test="${outcome == 0 }">
		<c:if test="${endTime == null }">
			<p>Showing devices that were connected at: <span style='font-weight: bold'>${startTime} (${tzString}) on ${startDate}</span></p>
		</c:if>
		<c:if test="${endTime != null }">
			<c:if test="${endDate == startDate}">
				<p>Showing devices that were connected between <span style='font-weight: bold'>${startTime}</span> and 
					<span style='font-weight: bold'>${endTime} (${tzString}) on ${endDate}</span>
				</p>
			</c:if>
			<c:if test="${endDate != startDate}">
				<p>Showing devices that were connected between <span style='font-weight: bold'>${startTime} on ${startDate}</span> and 
					<span style='font-weight: bold'>${endTime} (${tzString}) on ${endDate}</span>
				</p>
			</c:if>
		</c:if>
		
		<c:if test="${connCount == 0}">No devices were connected.</c:if>
		<c:if test="${connCount > 0}">
			<table>
				<tr>
					<th>Authenticated User</th>
					<th>Authenticated Device(s)</th>
					<th>Authentication Type</th>
					<th>Auth Start Time (${tzString})</th>
					<th>Auth End Time (${tzString})
				</tr>
				<c:forEach items="${groupConnectionEvents}" var="connectionEvents">
					
					<c:set var="group" value="${connectionEvents.group}"/>
					<c:set var="centralDevice" value="${connectionEvents.centralDevice}"/>
					<c:set var="devicesAndConns" value="${connectionEvents.devicesAndConns}"/>
					<c:forEach items="${devicesAndConns}" var="devicesAndConns">
						<tr>
							<c:set var="peripheralDevice" value="${devicesAndConns.peripheralDevice}"/>
							<td id='groupEventsName'>${group.username}</td>
							<td>
							<c:if test="${peripheralDevice != null}">
								${peripheralDevice.deviceType}
							</c:if>
							<c:if test="${peripheralDevice != null && centralDevice != null}">
								/
							</c:if>
							<c:if test="${centralDevice != null}">
								${centralDevice.deviceType}
							</c:if>
							</td>
							<td>${devicesAndConns.startConnectionTypeString}</td>
							<td>${devicesAndConns.startConnectionTimeString}</td>
							<td>${devicesAndConns.endConnectionTimeString}</td>
						</tr>
					</c:forEach>
				</c:forEach>
			</table>
		</c:if>
	</c:if>
</body>
</html>

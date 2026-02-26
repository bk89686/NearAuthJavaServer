<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang='en'>
<head>
	<%@ include file="header.jsp" %>
    <meta http-equiv="refresh" content="180">
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
        
        #startDateDiv {
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
        
        .timeSecondsLabel {
        	padding-left: 10px;
        }
        
        .timeSeconds {
        	width: 30px;
        }
        
        .timeMillisLabel {
        	padding-left: 10px;
        }
        
        .timeMillis {
        	width: 30px;
        }
        
        #submitTime {
        	left: 316px;
        	position:relative;
        }
        
        #pageTime {
        	text-align: right;
        }
        #searchTime {
        	background: #f9f9f9;
        	border: 1px solid #ddd;
        	width:413px;
        	margin:66px 20px 20px 0px;
        	padding: 20px;
        }
    </style>
</head>
<body>
    
    <c:if test="${deviceData.outcome == 1}">
    	<h2>Something went awry</h2>
    	<p>${deviceData.reason}</p>
    </c:if>
    <c:if test="${deviceData.outcome == 0}"> 
	    <h2>Device Information for ${username}</h2>
	    <c:set var="coName" value="${deviceData.companyName}"/>
	    <c:set var="b2f" value="B2X" />
	    <c:if test="${deviceData.outcome == 0}">
		    <table>
		        <tr>
		            <th>Device</th>
		            <th>Connected</th>
		            <c:if test="${fn:substring(coName, 0, 3) == b2f}">
		            	<th>Connection Type</th>
		            </c:if>
		            <th class="hideMobile">OS</th>
		            <c:if test="${fn:substring(coName, 0, 3) == b2f}">
		            	<th>Last Data Exchange</th>
		            </c:if>
		            <th>Distance</th>
		            <th>Distance Time</th>
		            <c:if test="${fn:substring(coName, 0, 3) == b2f}">
			            <th>Service</th>
			            <th>DeviceId</th>
			            <th>ConnId</th>
		            </c:if>
		            <th class="hideMobile">Device Class</th>
		            <th class="hideMobile">Has BLE</th>
		            <th class="hideMobile">Central</th>
		            <th class="hideMobile">Multiuser</th>
		            <c:if test="${fn:substring(coName, 0, 3) == b2f}">
		            	<th>User Email</th>
		            </c:if>
		        </tr>
		        <c:forEach items="${deviceData.deviceData}" var="device">
		            <tr>
				        <td class='bold'>
				            <c:choose>
		            			<c:when test="${device.central}">
					            	${device.deviceType}
				            	</c:when>
				                <c:otherwise>
				                	<a href='/shaneshuenhthubisa?did=${device.fullDeviceId}'>${device.deviceType}</a>
				                </c:otherwise>
				            </c:choose>
				        </td>
			                
		                <c:choose>
			                <c:when test="${device.connected}">
			                	<td class='connected'>${device.connected}</td>
			                </c:when>
			                <c:otherwise>
			                	<td class='not_connected'>${device.connected}</td>
			                </c:otherwise>
		                </c:choose>
		                <c:if test="${fn:substring(coName, 0, 3) == b2f}">
		                	<td>${device.connectionType}</td>
		                </c:if>
		                <td class="hideMobile">${device.operatingSystem}</td>
		                <c:if test="${fn:substring(coName, 0, 3) == b2f}">
		                	<td>${device.connectionTimeString}</td>
		                </c:if>
		                <td>${device.estimatedDistanceString}</td>
		                <td>${device.rssiTimeString}</td>
		                <c:if test="${fn:substring(coName, 0, 3) == b2f}">
			                <td>${device.serviceUuid}</td>
			                <td>${device.deviceId}</td>
			                <td>${device.connectionId}</td>
		                </c:if>
		                <td class="hideMobile">${device.deviceClass}</td>
		                <td class="hideMobile">${device.hasBle}</td>
		                <td class="hideMobile">${device.central}</td>
		                <td class="hideMobile">${device.multiuser}</td>
		                <c:if test="${fn:substring(coName, 0, 3) == b2f}">
		                	<td>${device.userEmail}</td>
		                </c:if>
		            </tr>
		        </c:forEach>
		    </table>
		    <c:if test="${admin == true}">
		    	<c:if test="${otherUserCount > 0}">
			    	<h3>Other Users</h3>
			    	<c:forEach items="${otherUsersData}" var="otherUser">
			    		<div>
				    		<a href='/deviceData?uid=${otherUser.groupId}'>
			    				${otherUser.userName}
			    			</a> (${otherUser.email})
			    		</div>
			    	</c:forEach>
		    	</c:if>
		    	<div id='searchTime'>
			    	<h3 style="margin-bottom: 5px;">Search Connected Users at Time</h3>
			    	<h5 id="timezoneName" style='margin-top: 5px;'>Please use timezone</h5>
			    	<form method="POST" action="/timeSearch">
		    		
			    		<div>
				    		<input type="checkbox" id="rangeSearch" name="rangeSearch">
				    		<label for="rangeSearch">Use a time range</label>
				    		<input type='hidden' id='tzString' name='tzString'>
				    	</div>
				    	<div id='startTimeDiv'>
				    		<label for='startTime' id='startTimeLabel'>Time:</label>
				    		<input type='time' id="startTime" name="startTime">
				    		<label for="startSeconds" id='startSecondsLabel' class='timeSecondsLabel'>Seconds:</label>
				    		<input id="startSeconds" name="startSeconds" value="00" class='timeSeconds'>
				    		<label for="startMillis" id='startMillisLabel' class='timeMillisLabel'>Millis:</label>
				    		<input id="startMillis" name="startMillis" value="000" class='timeMillis'>
				    	</div>
				    	<div id='startDateDiv'>
				    		<label for='startDate' id='startDateLabel'>Date:</label>
				    		<input type='date' id="startDate" name="startDate">
				    	</div>
				    	<div id='endTimeDiv'>
				    		<label for='endTime' id='endTimeLabel'>End time:</label>
				    		<input type='time' id="endTime" name="endTime">
				    		<label for="endSeconds" id='endSecondsLabel' class='timeSecondsLabel'>Seconds:</label>
				    		<input type="number" id="endSeconds" name="endSeconds" value="00" class='timeSeconds'>
				    		<label for="endMillis" id='endMillisLabel' class='timeMillisLabel'>Millis:</label>
				    		<input id="endMillis" name="endMillis" value="000" class='timeMillis'>
				    	</div>
				    	<div id='endDateDiv'>
				    		<label for='endTime' id='endDateLabel'>End date:</label>
				    		<input type='date' id="endDate" name="endDate">
				    	</div>
				    	<input type='hidden' id='tzOffset' name='tzOffset'>
				    	<div id='submitDiv'>
				    		<input type='submit' value="Submit" id="submitTime">
				    	</div>
		    		</form>
		    	</div>
		    </c:if>
		    <h4 id="pageTime">${deviceData.currentTime}</h4>
	    </c:if>
	</c:if>
    <script>
	    window.onpopstate = function(){
			location.reload();
		}
    </script>
    <script src="/js/b2f_device_data_search_1.0.0.19.js"></script>
    <%@ include file="footer.jsp" %>
</body>
</html>

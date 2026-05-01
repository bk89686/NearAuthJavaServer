<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>


<html>
<head>
	<meta http-equiv="refresh" content="180">
    <title>Device Timeline</title>
    <style>
	    @font-face {
			font-family: logoText;
			font-style: normal;
			src: url(/fonts/DidotTitle.otf);
		}
    	#timelineBody {
    		padding-top: 100px;
    		padding-left: 150px;
    		padding-right: 150px;
    		padding-bottom: 150px;
    		background: rgb(42, 50, 58) !important;
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
            background-color: #8a8a8a;
		    color: white;
		    font-weight: bold;
        }
        
        .leadCol {
    		width: 31%;
    	}
    	
    	.secondaryCol {
    		width: 23%;
    	}
    	
    	.white {
    		color: white;
    	}
        
        #timePresent tbody tr:nth-child(even) {
            background-color: #f9f9f9;
        }
        
        #weekData tbody tr:nth-child(even) {
            background-color: #f9f9f9;
        }
        
        #timePresent tbody tr:nth-child(odd) {
            background-color: #efefef;
        }
        
        
        #weekData tbody tr:nth-child(odd) {
    		background-color: #efefef;
    	}
    	
        .day-row {
            padding-bottom: 46px;
        }
        
        .day-label {
            font-weight: bold;
            margin-bottom: 5px;
            font-size: 1.1em;
            display: inline-block;
        }
        
        .timeline {
            display: flex;
            width: 100%;
            height: 60px;
            border: 1px solid #f9f9f9;;
            border-radius: 6px;
            position: relative;
            background: #efefef;
        }
        
        .overlay {
        	display: grid;
        	grid-template-columns: repeat(24, 1fr);
            width: 100%;
            height: 7px;
            border-radius: 6px;
            position: relative;
            top: 12px;
            
        }
        
        .now {
        	top: -34px;
        	position: relative;
        	left: -15px;
        }
        
        .nowArrow {
		    position: relative;
		    left: -34px;
		    top: -19px;
        }
        
        .line{
        	height: 5px;
        	border-left: 1px solid #efefef;
        	top: 57px;
        	position: relative;
        }
        
        .line0{
        	height: 0px;
        	border-left: 1px solid black;
        	position: relative;
        }
        
        .segment {
            height: 100%;
            position: relative;
        }
        
        .connected.proximity {
            background-color: #bbffb3;
            transition: transform 0.3s ease-in-out;
            z-index: 1;
        }
        
        .connected.proximity:hover {
        	background-color: darkgreen;
        	transform: scale(1, 1.1);
        	z-index: 2;
        }
        
        .connected.passkey {
            background-color: #dcf3fc;
            transition: transform 0.3s ease-in-out;
            z-index: 1;
        }
        
        .connected.passkey:hover {
        	background-color: darkblue;
        	transform: scale(1, 1.1);
        	z-index: 2;
        }
        
        .connected.administration {
            background-color: #dcf3fc;
            transition: transform 0.3s ease-in-out;
            z-index: 1;
        }
        
        .connected.administration:hover {
        	background-color: darkblue;
        	transform: scale(1, 1.1);
        	z-index: 2;
        }
        
        .connected.text {
            background-color: #dcf3fc;
            transition: transform 0.3s ease-in-out;
            z-index: 1;
        }
        
        .connected.text:hover {
        	background-color: darkblue;
        	transform: scale(1, 1.1);
        	z-index: 2;
        }
        
        .connected.push {
            background-color: #dcf3fc;
            transition: transform 0.3s ease-in-out;
            z-index: 1;
        }
        .connected.push:hover {
        	background-color: darkblue;
        	transform: scale(1, 1.1);
        	z-index: 2;
        }
        .disconnected {
            background-color: #fcdee1;
            transition: transform 0.3s ease-in-out;
            z-index: 1;
        }
        .disconnected:hover {
            background-color: darkred;
            transform: scale(1, 1.1);
        	z-index: 2;
        }
        .time-label {
            position: absolute;
            top: 62px;
            font-size: 11px;
            white-space: nowrap;
        }
        .hour {
        	top:4px;
        	position: relative;
        	left: -5px;	
        }
        
        .hourd {
        	top:4px;
        	position: relative;
        	left: -8px;	
        }
        
        .hour12 {
        	top:4px;
        	position: relative;
        	left: -15px;	
        }
        
        .bottomSpacer {
        	position: relative;
        	height: 200px;
        }
        
        #backLink {
			color: white;
        }
        
        .crosshatch {
		  background-color: #eee; /* Fallback color */
		  background-image: repeating-linear-gradient(
		      45deg,
		      transparent,
		      transparent 5px,
		      #ccc 6px,
		      #ccc 6px
		    ),
		    repeating-linear-gradient(
		      135deg,
		      transparent,
		      transparent 5px,
		      #ccc 6px,
		      #ccc 6px
		    );
		  background-size: 18px 9px; /* Controls the density of the hatch */
		}
        
        #branding_row {
        	text-align:center;
        	height: 200px;
        }
        
        .branding_text {
        	margin-left:auto;
        	margin-right:auto;
        	padding-top:20px;
        	font-family: logoText;
        	font-size: 1.9em;
        	font-weight: bold;
        	color: white;
        }
        
        .logoImage {
        	height:116px;
        }
        
        .branding_image {
        	margin-left:auto;
        	margin-right:auto;
        }
        
        .popupObj {
		  position: relative;
		  display: inline-block;
		  cursor: pointer;
		}
		
		.whiteLink {
			color: white;
		}
		
		
		/* The actual popup (appears on top) */
		.popuptext {
		  visibility: hidden;
		  width: 160px;
		  background-color: #555;
		  color: #fff;
		  text-align: center;
		  border-radius: 6px;
		  padding: 8px 0;
		  position: absolute;
		  z-index: 1;
		  bottom: 125%;
		  left: 50%;
		  margin-left: -80px;
		  padding: 10px;
		  transition: transform 0.3s ease-in-out;
		}
		
		/* Popup arrow */
		.popuptext::after {
		  content: "";
		  position: absolute;
		  top: 100%;
		  left: 50%;
		  margin-left: -5px;
		  border-width: 5px;
		  border-style: solid;
		  border-color: #555 transparent transparent transparent;
		}
		
		popuptext:hover {
			transform: scale(1, 0.909);
		}
		
		/* Add animation (fade in the popup) */
		@-webkit-keyframes fadeIn {
		  from {opacity: 0;}
		  to {opacity: 1;}
		}
		
		@keyframes fadeIn {
		  from {opacity: 0;}
		  to {opacity:1 ;}
		}
		
		@media screen and (max-width: 600px) {
	    	body {
	    		padding: 15px;
	    	}
	    	
	    	.hideMobile {
	    		display: none;
	    	}
	    	
	    	.hour12 {
	    		left: -8px;
	    	}
	    	
	    	.leadCol {
	    		width: 50%;
	    	}
	    	
	    	.secondaryCol {
	    		width: 50%;
	    	}
	    	.dayString {
	    		display: block;
	    	}
	    	.dateString {
	    		display: block;
	    	}
	    	
	    	#timelineBody {
	    		padding-top: 20px;
			    padding-left: 20px;
			    padding-right: 20px;
	    	}
    	}
    </style>
    <%@ include file="header.jsp" %>
</head>
<body id='timelineBody'>
	<c:if test="${demo == false}">
		<a id='backLink' href='/deviceData'>
			&lt;- Back 
		</a>
	</c:if>
	<div id='branding_row'>
		<div class='branding_image'><a href='https://www.nearauth.ai'><img src='/imgFiles/NearAuthLogoSquircle3d.svg' class='logoImage'></a></div>
		<div class='branding_text'><a href='https://www.nearauth.ai' class='whiteLink'>NearAuth.ai</a></div>
	</div>
    <h2 class='white'>Connection Information for ${username} on ${deviceData.deviceType}</h2>
    <c:if test="${demo == false}">
    	<button id='renameButton' class='popupButton'>Rename device</button>
    </c:if>
    <c:if test="${deviceData.central == false}">
    	<h5 class='white'>Central: ${deviceData.centralType}</h5>
    </c:if>
    <c:if test="${deviceData.central == true}">
    	<h5 class='white'>Peripheral(s): ${deviceData.peripherals}</h5>
    </c:if>
    <c:if test="${deviceData.outcome == 1}">
    	<p>${deviceData.reason}</p>
    </c:if>
    <c:if test="${deviceData.outcome == 0}">
    	<h4 class='white' style='text-align:right; margin-top:-40px;'>${deviceData.currentTime}</h4>
    	<table id='weekData'>
	    	<c:if test="${showWeeksTable}">
		    	<tr>
		    		<th class='leadCol'>Week</th>
		    		<th class='secondaryCol'>Authentication Time</th>
		    		<th class='hideMobile secondaryCol'>Authenticated by Proximity</th>
		    		<th class='hideMobile secondaryCol'>Authenticated using Other Method</th>
		    	</tr>
		    	<c:forEach items="${weekData}" var="weekTotalTime" varStatus="weekLoop">
		     		<c:if test="${!weekLoop.last || includeFinalWeek}">
			    		<tr>
			    			<td>${weekTotalTime.weekString}</td>
			    			<c:forEach items="${weekTotalTime.connectionEventsWithTypes}" var="connEvent">
			    				<c:if test="${connEvent.connectionType == 'total'}">
			    					<td>${connEvent.elapsedTimeText}</td>
			    				</c:if>
			    				<c:if test="${connEvent.connectionType != 'total'}">
			    					<td class='hideMobile'>${connEvent.elapsedTimeText}</td>
			    				</c:if>
			    			</c:forEach>
			    		</tr>
		    		</c:if>
		    	</c:forEach>
	    	</c:if>
	    </table>
	    
	    <table id='timePresent'>
	    	<tr>
	    		<th class='leadCol'>Day</th>
	    		<th class='secondaryCol'>Authentication Time</th>
	    		<th class='hideMobile secondaryCol'>Authenticated by Proximity</th>
		    	<th class='hideMobile secondaryCol'>Authenticated using Other Method</th>
	    	</tr>
	    	<c:forEach items="${dayData}" var="dayEntry" varStatus="loop0">
	    		<c:if test="${!loop0.last}">
		    		<tr>
		    			<td><span class='dayString'>${dayEntry.dayString}</span> <span class='dateString'>${dayEntry.dateString}</span></td>
		    			<c:forEach items="${dayEntry.connectionEventsWithTypes}" var="dayEvent">
		    				<c:if test="${dayEvent.connectionType == 'total'}">
		    					<td>${dayEvent.elapsedTimeText}</td>
		    				</c:if>
		    				<c:if test="${dayEvent.connectionType != 'total'}">
		    					<td class='hideMobile'>${dayEvent.elapsedTimeText}</td>
		    				</c:if>
		    			</c:forEach>
		    		</tr>
	    		</c:if>
	    	</c:forEach>
	    </table>
	    <c:forEach var="dayEntry" items="${deviceDataOneDevicesGroupedByDate}" varStatus="loop1">
	    	<c:if test="${!loop1.last}">
		        <div class="day-row white" id='dayRow${loop1.count}'>
		            <div class="day-label" id='dayLabel${loop1.count}'>
		            	<span class='dayString'>${dayEntry.value[0].sDay}</span>
		            	<span class='dateString'>${dayEntry.value[0].sDate}</span>
		            </div>
		            
		            <c:set var="totalTime" value="86400" />
					<div class='overlay'>
		            	<div class='line0'><div class='hour even'></div></div>
		            	<div class='line'><div class='hour odd'>1</div></div>
		            	<div class='line'><div class='hour even'>2</div></div>
		            	<div class='line'><div class='hour odd'>3</div></div>
		            	<div class='line'><div class='hour even'>4</div></div>
		            	<div class='line'><div class='hour odd'>5</div></div>
		            	<div class='line'><div class='hour even'>6</div></div>
		            	<div class='line'><div class='hour odd'>7</div></div>
		            	<div class='line'><div class='hour even'>8</div></div>
		            	<div class='line'><div class='hour odd'>9</div></div>
		            	<div class='line'><div class='hourd even'>10</div></div>
		            	<div class='line'><div class='hourd odd'>11</div></div>
		            	<div class='line'><div class='hour12 even'>12<span class='hideMobile' id='pm'>pm</span></div></div>
		            	<div class='line'><div class='hour odd'>1</div></div>
		            	<div class='line'><div class='hour even'>2</div></div>
		            	<div class='line'><div class='hour odd'>3</div></div>
		            	<div class='line'><div class='hour even'>4</div></div>
		            	<div class='line'><div class='hour odd'>5</div></div>
		            	<div class='line'><div class='hour even'>6</div></div>
		            	<div class='line'><div class='hour odd'>7</div></div>
		            	<div class='line'><div class='hour even'>8</div></div>
		            	<div class='line'><div class='hour odd'>9</div></div>
		            	<div class='line'><div class='hour even'>10</div></div>
		            	<div class='line'><div class='hour odd'>11</div></div>
		            </div>
		            <!-- timeline row -->
		            <div class="timeline crosshatch">
		                <c:set var="offset" value="0" />
		                <c:forEach var="device" items="${dayEntry.value}" varStatus="loop2">
		                    <c:set var="widthPercent" value="${(device.secondsLong * 100) / totalTime}" />
	                    	<span class="popuptext" id="segmentPopup_${loop1.count}_${loop2.count}">
	                    		<c:if test="${device.connectionType != 'proximity' && device.connectionType != ''}">
	                    			${device.connectionType} connection<br>
	                    		</c:if>
	                        	${device.timeSpan} &#013;starting at ${device.sTime}
	                        </span>
		                    <div class="segment ${device.connected ? 'connected' : 'disconnected'} ${device.connectionType} popupObj" id="segmentObj_${loop1.count}_${loop2.count}"
		                         style="width:${widthPercent}%" onclick="showPopupText('segmentPopup_${loop1.count}_${loop2.count}', 'segmentObj_${loop1.count}_${loop2.count}', 'dayRow${loop1.count}')" >
		                    </div>
		                    
		                    <c:if test="${loop1.first and loop2.last}">
		                    	<div class='now' id="nowText">Now</div>
								<div class='nowArrow' id='nowArrow'>&#x2193;</div>
							</c:if>
		                    <c:set var="offset" value="${offset + widthPercent}" />
		                </c:forEach>
		            </div>
		        </div>
	        </c:if>
	    </c:forEach>
	    <div class='bottomSpacer'></div>
	    <input type="hidden" id="fromPush" value="${fromPush}">
	    <c:if test="${demo == false}">
		    <div id='popup' class='popup'>
			    <div id='popupIcon'></div>
			    <div id='popupX'>x</div>
			    <div id='popupTitle'>Rename ${deviceData.deviceType}</div>
			    <form method='POST' id='renameForm' action='/shaneshuenhthubisa?did=${deviceData.deviceId}'>
			        <div class='popupInputRow' id='popupInputRow1'>
			        	<span id='popupInputLabel1'>New name: </span>
			            <input type='text' class='popupInput' id='popupInput1' name='popupInput1'/>
			        </div>
			     	<input type='hidden' id='did' name='did' value='${deviceData.deviceId}'/>
			        <div id='popupButtons'>
			            <button class='popupButton' id='popupCancel'>Cancel</button>
			            <button class='popupButton' id='popupOk' >OK</button>
			        </div>
			    </form>
			</div>
		</c:if>
		<div id='cover'></div>
    </c:if>
    <script src="/js/b2f_device_timeline_1.0.0.8.js"></script>
    <c:if test="${demo == false}">
    	<%@ include file="footerClient.jsp" %>
    </c:if>
</body>
</html>

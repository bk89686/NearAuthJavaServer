<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="/WEB-INF/tld/spring.tld"  %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

{"result": 
    {
    "outcome": ${outcome},
    "reason": "${reason}",
    "connected": ${connected},
    "subscribed": ${subscribed},
    "connectedToAll": ${connectedToAll},
    "showIcon": ${showIcon},
    "instanceId": "${instanceId}",
    "fromFcm": ${fromFcm},
    "devices":
        {
        <c:forEach items="${devices}" var="device">
        	<c:if test="${not empty device.devName}">
	            "${device.devName}":
	            {
	            "service": "${device.service}",
	            "characteristic": "${device.characteristic}",
	            "periphId": "${device.peripheralInstanceId}",
	            "centralId": "${device.centralInstanceId}",
	            "instanceId": "${device.instanceId}",
	            "connected": ${device.connected},
	            "centralConnected": ${device.centralConnected},
	            "peripheralConnected": ${device.peripheralConnected},
	            "subscribed": ${device.subscribed},
	            "lastSuccess": "${device.lastSuccess}",
	            "iddate": "${device.idDate}",
	            "status": "${device.command}",
	            "deviceClass": "${device.deviceClass}",
	            "hasBle": ${device.hasBle},
	            "identifier": "${device.peripheralIdentifier}"
	            },
            </c:if>
        </c:forEach>
        "deviceCount": ${deviceCount}
        }
    }
}

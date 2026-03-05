<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

{"result": 
    {
    "outcome": ${outcome},
    "reason": "${reason}",
    "b2fId": "${b2fId}",
    "connected": ${connected},
    "subscribed": ${subscribed},
    "connectedToAll": ${connectedToAll},
    "showIcon": ${showIcon},
    "instanceId": "${instanceId}",
    "pendingRequests": ${pendingRequests},
    "fromFcm": ${fromFcm},
    "hasBle": ${hasBle},
    "devices":
        {
        <c:forEach items="${devices}" var="device">
            "${device.devName}":
                {
                "service": "${device.service}",
                "characteristic": "${device.characteristic}",
                "connected": ${device.connected},
                "centralConnected": ${device.centralConnected},
	            "peripheralConnected": ${device.peripheralConnected},
	            "subscribed": ${device.subscribed},
                "iddate": "${device.idDate}",
                "lastSuccess": "${device.lastSuccess}",
                "devName": "${device.devName}",
                "status": "${device.command}",
                "hasBle": ${device.hasBle},
                "identifier": "${device.peripheralIdentifier}",
                "pushFailure": ${device.pushFailure}
                },
        </c:forEach>
        "deviceCount": ${deviceCount}
        }
    }
}

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div id="sidePanel">
    <div id="sidePanelTitle"><span class='bold'>${ companyName }</span></div>
    <div class="sidePanelRow">
        
        Company ID: <span class="bold" id="apiKey">${ apiKey1 }${ apiKey2 }${ apiKey3 }</span>
    </div>
    <div class="sidePanelRow">
        Licenses: <span class='bold'>${ licenseCount }</span>
    </div>
    <div class="sidePanelRow">
        Licenses in Use: <span class='bold'>${ licensesInUse }</span>
    </div>
    <div class="sidePanelRow">
        Status: <span class='bold'>${ activeStatus }</span>
    </div>
    <div class="sidePanelRow">
        Create Date: <span class='bold'>${ createMonth }/${ createDay }/${ createYear }</span>
    </div>
    <div class="sidePanelRow">
    	<a href="/pw/${companyId}/reset?token=${groupId}">Reset my Pw</a>
    </div>
    <div class="sidePanelRow">
    	Login Url: <span class='bold' id="loginUrl">${ loginUrl }</span>
    </div>
</div>

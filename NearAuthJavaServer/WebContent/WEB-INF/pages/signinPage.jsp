<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="/WEB-INF/tld/spring.tld"  %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="header.jsp" %>    
</head>
<body>
    <div id="banner">
        <!-- <a href="/billing"><span id="companyLink">Billing</span></a>
        <a href="/users"><span id="users">Users</span></a> -->
    </div>
    <div id="signinBody">
        <div id="centerPanelCo">
            <div class='incorrect'>${ errorMessage }</div>
            <img id="licenseLogo" src="/imgFiles/Icon-App-76x76@3x.png" alt="NearAuth.ai logo">
            <div id="centerPanelTable">
                <form id='coForm' action="${ action }" method="post">
                    <span id='signinLabels'>
                        <span id='usernameLabel' class='signinItem'>
                            Username:
                        </span>
                        <span id='pwLabel' class='signinItem'>
                            Password:
                        </span>
                    </span>
                    <span id='signinText'>
                        <span id='usernameText' class='signinItem'>
                            <input type='text' name='username' id='username' autocomplete='username'>
                        </span>
                        <span id='pwText' class='signinItem'>
                            <input type='password' name='pw' id='pw' autocomplete='current-password'>
                        </span>
                    </span>
                    <div class='signinRow'><button type='submit' class='companyTableButton'>Submit</button></div>
                    <input type='hidden' id='b2fIgnorePageFlag' value='true'/>
                </form>
                
            </div>
        </div>
    </div>
    <%@ include file="companyFooter.jsp" %>
</body>
</html>

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
    <input type='hidden' id='publicKey' value='${ publicKey }'>
    <input type='hidden' id='noDevicesRegistered' value='${ noDevicesRegistered }'>
    <div id="banner">
        <!-- <a href="/billing"><span id="companyLink">Billing</span></a> -->
        <a href="/company"><span class="users">Admin</span></a>
        <a href="#" id='signOutClick'><span class="users">Sign Out</span></a>
    </div>
    <div id="mobileBanner"><img id="mobileMenu" src="/imgFiles/white_hamburger.png" alt="menu button"></div>
    <div id="fullBody">
        <%@ include file="sidePanel.jsp" %>
        <div id="centerPanel">
            <div id="message" class='redMessage'>${ errorMessage }</div>
            <img id="licenseLogo" src="/imgFiles/b2f96.png" alt="NearAuth.ai logo">
            <div class='installText'>
                <div class='installInstructions'>
                    <div class='headline'>Validating a Javascript Web Token (JWT)</div>
                    <div class='stepNumber'>Step 1</div>
                    <div class='stepText'>First, get the url for the public key from the x5u value set in the 
                        header of the JWT.</div>
                    <div class='stepNumber'>Step 2</div>
                    <div class='stepText'>Go to the URL that was found in step 1 and retrieve the public key.</div>
                    <div class='stepNumber'>Step 3</div>
                    <div class='stepText'>Verify the JWT using the public key that you retrieved. The 
                        algorithm is RSA 256. The issuer is "${secureUrl}". The audience is 
                        the Login Url from the company page.</div>
                    <div id='contactIfNeeded' class='trailer'>If you need help with the process, please reach out to us. 
                        We have done this before and are more than happy to help.  You can email us at help@NearAuth.ai
                            or use the phone number on the 
                            <a href='https://www.NearAuth.ai/contactUs' class='underline' target='_blank'>contact us page</a>.</div>
                </div>
            </div>
        </div>
    </div>
    <div id='mobileMenuText'>
        <div id='mobileMenuDocumentation'><a href="/company" class="noUnderline black">Admin</a></div>
        <div id='mobileMenuSignOut'>Sign Out</div>
    
    </div>
    <input type='hidden' id='b2fIgnorePageFlag' value='true'></input>
    <%@ include file="popup.jsp" %>
    <%@ include file="footerClient.jsp" %>
</body>
</html>

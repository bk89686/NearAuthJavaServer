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
    <div id="mobileBanner">
        <img id="mobileMenu" src="/imgFiles/white_hamburger.png" alt="menu button">
    </div>
    <div id="fullBody">
        <%@ include file="sidePanel.jsp" %>
        <div id="centerPanel">
            <div id="message" class='redMessage'>${ errorMessage }</div>
            <img id="licenseLogo" src="/imgFiles/b2f96.png" alt="NearAuth.ai logo">
            <div class='installText'>
                <div class='installInstructions'>
                    <div class='headline'>Adding, Editing, or Removing Users</div>
                        (<a href='/company'>${secureUrl }/company</a>) by clicking on the Add User 
                        button.  When a user is added, they will receive an email telling them what they need
                        to do to proceed.
                    </div>
                    <div class='subheading'>Users can also be added or changed using the NearAuth.ai API</div>
                    <div id='docUrl'>
                        API URL: ${secureUrl }/b2fUserApi
                    </div>
                    <div class='subheading'>To add a new user, send the json below to the API URL</div>
                    <div class='jsonCode'>
                        <div class='parens'>{</div>
                        <div class='innerJson'>
                            <div class='jsonLine'>&quot;cokey&quot;: &quot;&lt;Your key from the admin dashboard&gt;&quot;,</div>
                            <div class='jsonLine'>&quot;uid&quot;: &quot;&lt;YOUR_SYSTEM_UID&gt;&quot;,</div>
                            <div class='jsonLine'>&quot;email&quot;: &quot;&lt;User email&gt;&quot;,</div>
                            <div class='jsonLine'>&quot;username&quot;: &quot;&lt;User's first and last name&gt;&quot;,</div>
                            <div class='jsonLine'>&quot;cmd&quot;: &quot;addUser&quot;</div>
                        </div>
                        <div class='parens'>}</div>
                    </div>
                    <div class='padded'>
                        where all of the fields are required except for username and uid, which are optional.
                    </div>
                    <div class='subheading'>
                        To edit a user, use the json below:
                    </div>
                    <div class='jsonCode'>
                        <div class='parens'>{</div>
                        <div class='innerJson'>
                            <div class='jsonLine'>&quot;cokey&quot;: &quot;&lt;Your key from the admin dashboard&gt;&quot;,</div>
                            <div class='jsonLine'>&quot;uid&quot;: &quot;&lt;YOUR_SYSTEM_UID&gt;&quot;,</div>
                            <div class='jsonLine'>&quot;email&quot;: &quot;&lt;User email&gt;&quot;,</div>
                            <div class='jsonLine'>&quot;username&quot;: &quot;&lt;User's first and last name&gt;&quot;,</div>
                            <div class='jsonLine'>&quot;cmd&quot;: &quot;editUser&quot;</div>
                        </div>
                        <div class='parens'>}</div>
                    </div>
                    <div class='padded'>
                        The user will be matched by the user email address. To
                        edit a user's email, create a new user with the updated address, 
                        and delete the old user.
                    </div>
                    <div class='subheading'>
                        To delete a user, use the json below:
                    </div>
                    <div class='jsonCode'>
                        <div class='parens'>{</div>
                        <div class='innerJson'>
                            <div class='jsonLine'>
                                &quot;cokey&quot;: &quot;&lt;Your key from the admin dashboard&gt;&quot;,
                            </div>
                            <div class='jsonLine'>
                                &quot;email&quot;: &quot;&lt;User email&gt;&quot;,
                            </div>
                            <div class='jsonLine'>
                                &quot;cmd&quot;: &quot;deleteUser&quot;
                            </div>
                        </div>
                        <div class='parens'>}</div>
                    </div>        
                    <div class='subheading'>
                        The response from the user API has the following form:
                    </div>
                    <div class='jsonCode'>
                        <div class='parens'>{</div>
                        <div class='innerJson'>
                            <div class='jsonLine'>&quot;outcome&quot;: &lt;Outcome code&gt;,</div>
                            <div class='jsonLine'>&quot;reason&quot;: &quot;&lt;Explanation for failure&gt;&quot;</div>
                        </div>
                        <div class='parens'>}</div>
                    </div>
                    <div id='contactIfNeeded' class='trailer'>
                        If you encounter any problems or just have questions,
                        please contact us.  You can email us at help@nearauth.ai
                        or use the phone number on the 
                      <a href='https://www.nearauth.ai/contactUs' class='underline' target='_blank'>contact us page</a>.
                    </div>
                </div>
            </div>
        </div>
    <div id='mobileMenuText'>
    <div id='mobileMenuDocumentation'>
        <a href="/company" class="noUnderline black">Admin</a>
    </div>
    <div id='mobileMenuSignOut'>Sign Out</div>
    </div>
    <input type='hidden' id='b2fIgnorePageFlag' value='true'></input>
<%@ include file="popup.jsp" %>
<%@ include file="footerClient.jsp" %>
</body>
</html>

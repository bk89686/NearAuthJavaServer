<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="/WEB-INF/tld/spring.tld"  %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
	<%@ include file="header.jsp" %>
	<style>
	    .pwError {
	        text-align: center;
			color: darkred;
			font-weight: bold;
	    }
	    #submitAuth:hover {
	    	cursor: pointer;
	    }
	    @media screen and (min-width: 501px) and (min-height: 501px){
			#username {
				display: block;
			    padding: 12px;
			    width: 83%;
			    border-radius: 6px;
			    border-width: thin;
			    border-color: lightgrey;
			    font-size: 17px;
			    margin: 20px;
		    }
		    #pw {
		    	display: block;
			    padding: 12px;
			    width: 83%;
			    border-radius: 6px;
			    border-width: thin;
			    border-color: lightgrey;
			    font-size: 17px;
			    margin: 20px;
		    }
		    #logo{
		    	background-image: url("${backgroundImage}");
		    	height: ${iconHeight}px;
		    	width: ${iconWidth}px;
		    	background-size: contain;
		    }
		    #signinBox{
		    	position: relative;
		    	top: 50vh;
		    	margin-top:-225px;
		    	margin-left:auto;
		    	margin-right:auto;
		    	background-color:#fff;
		    	padding: 20px;
	    		border-radius: 5px;
	    		width: 400px;
	    		box-shadow: 0 2px 4px rgb(0 0 0 / 10%), 0 8px 16px rgb(0 0 0 / 10%);
		    }
		    #submitAuth {
		    	background-color: ${backgroundColor};
			    color: ${foregroundColor};
			    width: 90%;	
			    border-width: 0;
			    padding: 12px;
			    font-size: 17px;
			    border-radius: 6px;
			    margin: 20px;
		    }
		    
		    #poweredBy {
		    	font-size: .7em;
			    text-align: right;
			    top: 38px;
			    position: relative;
			    margin-right: 0;
		    }
	    }
	    @media screen and (max-width: 500px){
	    	body {
	    		background-color: #fff;
	    	}
	    	
	    	#signinBox {
	    		position: relative;
			    top: 50vh;
			    margin-top: -125px;
			    margin-left: auto;
			    margin-right: auto;
			    background-color: #fff;
			    width: 100%;
	    	}
	    	
	    	#poweredBy {
	    		font-size: .75em;
			    top: 10px;
			    position: relative;
			    text-align: center;
			    width: 100vw;
	    	}
		    
		    #submitAuth {
			    background-color: ${backgroundColor};
			    color: ${foregroundColor};
			    width: 79vw;
			    border-width: 0;
			    padding: 15px;
			    font-size: 1.2em;
			    border-radius: 6px;
			    margin-right: auto;
			    margin-left: auto;
			    display: block;
			}
			
			#username {
				display: block;
			    padding: 12px;
			    width: 79%;
			    border-radius: 6px;
			    border-width: thin;
			    border-color: lightgrey;
			    font-size: 1.2em;
			    margin-top: 20px;
			    margin-bottom: 20px;
			    margin-left: auto;
			    margin-right: auto;
		    }
		    
		    #pw {
		    	display: block;
			    padding: 12px;
			    width: 79%;
			    border-radius: 6px;
			    border-width: thin;
			    border-color: lightgrey;
			    font-size: 1.2em;
			    margin-top: 20px;
			    margin-bottom: 20px;
			    margin-left: auto;
			    margin-right: auto;
		    }
			
			#logo {
				background-image: url("${backgroundImage}");
			    height: 100px;
			    width: 100px;
			    background-size: contain;
			    left: 50%;
			    position: absolute;
			    margin-top: -128px;
			    margin-left: -50px;
		    }
	    }
	    
	    @media screen and (max-height: 500px){
	    	body {
	    		background-color: #fff;
	    	}
	    	
	    	#signinBox {
	    		position: relative;
			    top: 50vh;
			    margin-top: -117px;
			    margin-left: auto;
			    margin-right: auto;
			    background-color: #fff;
			    width: 400px;
	    	}
	    	
	    	#poweredBy {
	    		    font-size: .7em;
				    text-align: right;
				    position: relative;
				    margin-right: 0;
				    text-align: center;
	    	}
	    	
	    	#username {
		    	display: block;
			    padding: 12px;
			    width: 75%;
			    border-radius: 6px;
			    border-width: thin;
			    border-color: lightgrey;
			    font-size: 1.2em;
			    margin-top: 20px;
			    margin-bottom: 20px;
			    position: relative;
			    margin-left: auto;
			    margin-right: auto;
		    }
		    
		    #pw {
		    	display: block;
			    padding: 12px;
			    width: 75%;
			    border-radius: 6px;
			    border-width: thin;
			    border-color: lightgrey;
			    font-size: 1.2em;
			    margin-top: 20px;
			    margin-bottom: 20px;
			    margin-left: auto;
			    margin-right: auto;
		    }
		    
		    #submitAuth {
			    background-color: ${backgroundColor};
			    color: ${foregroundColor};
			    width: 81%;
			    border-width: 0;
			    padding: 15px;
			    font-size: 1.2em;
			    border-radius: 6px;
			    margin-right: auto;
			    margin-left: auto;
			    display: block;
			}
			
			#logo {
				background-image: url("${backgroundImage}");
			    height: 100px;
			    width: 100px;
			    background-size: contain;
			    left: 50%;
			    background-repeat: no-repeat;
			    position: absolute;
			    margin-left: -292px;
			    padding-top: 50%;
			    top: 50%;
			    margin-top: -50px;
			}
	    }
	</style>
</head>
<body>
	<div id='signinBox'>
		<div id='logo'>
		
		</div>	
		<form action="${action}" method="post"> 
			<div id='usernamePw'>
				<input id='username' name='username' type="email" placeholder='email'>
				<input id='pw' name='pw' type='password' placeholder='password'>
				<input id='relayState' name='relayState' type='hidden' value="${relayState}">
				<div class='pwError'>${errorText}</div>
				<input id='submitAuth' type='submit' class='pointer' value='Log in'>
			</div>
		</form>
		<div id='poweredBy'>powered by NearAuth.ai and Google Workspace</div>
	</div>
	<%@ include file="companyFooter.jsp" %>
</body>
</html>

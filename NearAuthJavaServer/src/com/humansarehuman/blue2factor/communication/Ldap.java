package com.humansarehuman.blue2factor.communication;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.LdapServerDbObj;

public class Ldap {

	public boolean validateLdap(String email, String pw, CompanyDbObj company) {
		// TODO: this doesn't work. We're just returning true every time
		@SuppressWarnings("unused")
		boolean authenticated = false;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		LdapServerDbObj ldapServer = dataAccess.getLdapServerFromCompany(company);
		if (ldapServer != null) {
			try {
				File keystore = new File(ldapServer.getJksFile()).getAbsoluteFile();
				dataAccess.addLog("path: " + keystore.getAbsolutePath());
				dataAccess.addLog("pw: " + ldapServer.getJksPassword());

				// System.setProperty("javax.net.ssl.keyStore", ldapServer.getJksFile());
				// System.setProperty("javax.net.ssl.keyStorePassword",
				// ldapServer.getJksPassword());
				// System.setProperty("javax.net.ssl.trustStore", "clientTrustStore.key");
				// System.setProperty("javax.net.ssl.trustStorePassword",
				// ldapServer.getJksPassword());
				String uid = email.split("@")[0];
				Hashtable<String, String> environment = new Hashtable<String, String>();

				environment.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				environment.put(InitialContext.PROVIDER_URL, ldapServer.getProviderUrl());
				dataAccess.addLog("provider_url: " + ldapServer.getProviderUrl());
				environment.put(InitialContext.SECURITY_AUTHENTICATION, "simple");
				dataAccess.addLog("security_principal: " + "uid=" + uid + "," + ldapServer.getSearchBase());
				environment.put(InitialContext.SECURITY_PRINCIPAL, "uid=" + uid + "," + ldapServer.getSearchBase());
				dataAccess.addLog("creds: " + pw);
				environment.put(InitialContext.SECURITY_CREDENTIALS, pw);

				DirContext context = new InitialDirContext(environment);
				context.close();
				authenticated = true;
			} catch (Exception e) {
				dataAccess.addLog(e);
			}
		}
		return true;
	}

	public static void main(String[] args) throws NamingException, Exception, CertificateException, IOException {
		System.setProperty("javax.net.ssl.keyStore", "/var/java-application-ldap.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "jT32J%d&22p");

		Hashtable<String, String> environment = new Hashtable<String, String>();

		environment.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(InitialContext.PROVIDER_URL, "ldaps://ldap.google.com:636");
		environment.put(InitialContext.SECURITY_AUTHENTICATION, "simple");
		environment.put(InitialContext.SECURITY_PRINCIPAL, "uid=alex,ou=NearAuth.ai,ou=Users,dc=blue2factor,dc=com");
		environment.put(InitialContext.SECURITY_CREDENTIALS, "10Jonah02$");
		try {
			DirContext context = new InitialDirContext(environment);
			context.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean testLdap() throws NamingException, Exception, CertificateException, IOException {
		CompanyDbObj co = new CompanyDataAccess().getCompanyById("b2fd0fbd-2334-42a7-bfeb-d2760e2f8af7");
		return new Ldap().validateLdap("alex@blue2factor.com", "10Jonah02$", co);
	}
}

package com.humansarehuman.blue2factor.utilities.jwt;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import org.apache.http.util.TextUtils;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

public class JsonWebToken {
	private String jwtTokenId;

	public void updateJwtKey() {
		DataAccess dataAccess = new DataAccess();
		try {
			KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();
			String privateKey = Encoders.BASE64.encode(keyPair.getPrivate().getEncoded());
			String publicKey = Encoders.BASE64.encode(keyPair.getPublic().getEncoded());
			String rand = GeneralUtilities.randomString();
			dataAccess.deactivateOldJwts(rand + "b", rand + "v"); // stuck the keys in there because I was having issues
			Timestamp oneMonth = DateTimeUtilities.getCurrentTimestampPlusDays(31);
			dataAccess.addLog("adding public JWT Key");
			KeyDbObj pubKey = new KeyDbObj(rand + "b", null, null, null, null, KeyType.JWT, publicKey, true,
					Jwts.SIG.RS256.getId(), null, oneMonth);
			dataAccess.addLog("publicKey: " + publicKey);
			dataAccess.addLog("publicKey: " + keyPair.getPublic().toString());
			KeyDbObj pvtKey = new KeyDbObj(rand + "v", null, null, null, null, KeyType.JWT, privateKey, false,
					Jwts.SIG.RS256.getId(), null, oneMonth);
			dataAccess.addLog("added private JWT Key");
			dataAccess.addKey(pubKey);
			dataAccess.addKey(pvtKey);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
	}

	public String buildExpiredJwt(IdentityObjectFromServer idObj, String audience) {
		return buildJwt(idObj, audience, -6000); // 100 hundred minutes in the past
	}

	public String buildJwt(IdentityObjectFromServer idObj, String audience) {
		return buildJwt(idObj, audience, 60); // one minutes
	}

	public String buildJwtForServer(IdentityObjectFromServer idObj, String audience) {
		return buildJwt(idObj, audience, 30);
	}

	public String buildJwtForJs(IdentityObjectFromServer idObj, String audience) {
		return buildJwt(idObj, audience, 30); // 30 seconds
	}

	public String buildJwt(IdentityObjectFromServer idObj, String audience, int secondsToExpire) {
		Date expire;
		if (secondsToExpire < 0) {
			expire = DateTimeUtilities.nowMinusSeconds(secondsToExpire);
		} else {
			expire = DateTimeUtilities.nowPlusSeconds(480); // eight minutes
		}
		new DataAccess().addLog("building new JWT with expire date: " + expire);
		return buildJwt(idObj, audience, expire);
	}

	public String buildJwt(IdentityObjectFromServer idObj, String audience, Date expire) {
		String jwt = null;
		DataAccess dataAccess = new DataAccess();
		int logLevel = LogConstants.TRACE;
		try {
			DeviceDbObj device = idObj.getDevice();
			CompanyDbObj company = idObj.getCompany();
			BrowserDbObj browser = idObj.getBrowser();
			GroupDbObj group = idObj.getGroup();
			Key privateKey = getJwtPrivateKey(dataAccess);
			TokenDbObj token = null;
			if (group.isActive()) {
				if (device.isCentral()) {
					token = dataAccess.addTokenCentral(device, browser.getBrowserId(), TokenDescription.JWT,
							company.getCompleteCompanyLoginUrl());
				} else {
					token = dataAccess.addTokenPeripheral(device, browser.getBrowserId(), TokenDescription.JWT,
							company.getCompleteCompanyLoginUrl());
				}
				KeyDbObj publicKey = dataAccess.getCurrentJwtPublicKey();
				String url = Urls.SECURE_URL + Urls.GET_PUBLIC_KEY.replace("{keyId}", publicKey.getKeyId());
				String issuer = Urls.SECURE_URL + Urls.SAML_ENTITY_ID.replace("{apiKey}", company.getApiKey());
				dataAccess.addLog("issuer: " + issuer, logLevel);
				dataAccess.addLog(device.getDeviceId(), "login url: " + idObj.getCompany().getCompleteCompanyLoginUrl(),
						logLevel);
				dataAccess.addLog(device.getDeviceId(), "expiration: " + expire, logLevel);
				dataAccess.addLog(device.getDeviceId(), "audience: " + audience, logLevel);
				/* @formatter:off */
				if (token != null) {
					jwt = Jwts.builder()
                                .subject("blue2factor")
                                .expiration(expire)
                                .notBefore(new Date())
                                .issuedAt(new Date())
                                .id(token.getTokenId())
                                .issuer(issuer)
                                .audience().add(audience).and()
                                .header()
                                	.add("alg", "RSA256")
                                	.add("x5u", url).and()
                                .signWith(privateKey)
                                .compact();
				}
                /* @formatter:on */
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		dataAccess.addLog("new jwt: " + jwt, logLevel);
		return jwt;
	}

	public Claims decryptJwt(String jwsString, DataAccess dataAccess) {
		Jws<Claims> jws = null;
		Claims claims = null;
		try {
			PublicKey publicKey = getJwtPublicKey(dataAccess);
			JwtParser parser = Jwts.parser()
				    .verifyWith(publicKey) 
				    .build();
			jws = parser.parseSignedClaims(jwsString);
			claims = jws.getPayload();

		} catch (ExpiredJwtException e) {
			dataAccess.addLog("Expired key, setting claims");
			claims = e.getClaims();
		} catch (JwtException ex) {
			dataAccess.addLog(ex);
			try {
				KeyDbObj oldKey = dataAccess.getJustExpiredJwsPublicKey();
				if (oldKey != null) {
					PublicKey publicKey = getPublicKeyFromKeyObj(oldKey);
					JwtParser parser = Jwts.parser()
						    .verifyWith(publicKey) 
						    .build();
					jws = parser.parseSignedClaims(jwsString);
					claims = jws.getPayload();
//					jws = Jwts.parser().setSigningKey(publicKey).build().parseClaimsJws(jwsString);
//					claims = jws.getBody();
				}
			} catch (Exception e) {
				dataAccess.addLog("error decrypting old key", e);
			}
		}
		return claims;
	}

	private PublicKey getJwtPublicKey(DataAccess dataAccess) {
		KeyDbObj key = dataAccess.getCurrentJwtPublicKey();
		return getPublicKeyFromKeyObj(key);
	}

	private static PrivateKey getJwtPrivateKey(DataAccess dataAccess) {
		KeyDbObj key = dataAccess.getJwsSigningKey();
		PrivateKey pvtKey = null;
		if (key != null) {
			pvtKey = getPrivateKeyFromKeyObj(key);
		}
		return pvtKey;
	}

	private static PrivateKey getPrivateKeyFromKeyObj(KeyDbObj key) {
		String privateKeyStr = key.getKeyText();
		Encryption encryption = new Encryption();
		PrivateKey privateKey = encryption.stringToJwtPrivateKey(privateKeyStr);
		return privateKey;
	}

	private PublicKey getPublicKeyFromKeyObj(KeyDbObj key) {
		String publicKeyStr = key.getKeyText();
		Encryption encryption = new Encryption();
		PublicKey publicKey = encryption.stringToJwtPublicKey(publicKeyStr);
		return publicKey;
	}

	@SuppressWarnings("unused")
	private byte[] getKeyBytesFromKey(KeyDbObj key, DataAccess dataAccess) {
		String privateKeyStr = key.getKeyText();
		byte[] keyBytes = privateKeyStr.getBytes();
		// or
		Encryption encryption = new Encryption();
		PrivateKey privateKey = encryption.stringToJwtPrivateKey(privateKeyStr);
		byte[] keyBytesLong = privateKey.getEncoded();
		if (keyBytes == keyBytesLong) {
			dataAccess.addLog("bytes were the same, turn this off");
		} else {
			dataAccess.addLog("bytes were the different, turn this off");
		}
		return keyBytes;
	}

	public String getJwtTokenId() {
		return jwtTokenId;
	}

	public boolean validateJwt(String jwsString) {
		return validateJwt(jwsString, false);
	}

	public boolean validateSignedJwt(CompanyDbObj company, String jwsString, String signature,
			boolean ignoreExpiration) {
		boolean validated = false;
		Encryption encryption = new Encryption();
		if (encryption.verifyWebServerSignature(company, jwsString, signature)) {
			validated = validateJwt(jwsString, ignoreExpiration);
		}
		return validated;
	}

	public boolean validateJwt(String jwsString, boolean ignoreExpiration) {
		boolean validated = false;
		int logLevel = LogConstants.TRACE;
		CompanyDataAccess dataAccess = new CompanyDataAccess();

		Claims claims = decryptJwt(jwsString, dataAccess);
		if (claims != null) {
			dataAccess.addLog("claims were found", logLevel);
			Date exp = claims.getExpiration();
			Date notBefore = claims.getNotBefore();
			String issuer = claims.getIssuer();
			Set<String> audience = claims.getAudience();
			jwtTokenId = claims.getId();
			dataAccess.addLog("tokenId: " + jwtTokenId, logLevel);
			Date now = new Date();
			if (ignoreExpiration || exp.after(now)) {
				dataAccess.addLog("exp: " + exp, logLevel);
				if (now.after(notBefore)) {
					dataAccess.addLog("notBefore: " + notBefore, logLevel);
					if (!TextUtils.isEmpty(jwtTokenId)) {
						dataAccess.addLog("tokenId: " + jwtTokenId, logLevel);
						CompanyDbObj company = dataAccess.getCompanyByTokenAndDescription(jwtTokenId,
								TokenDescription.JWT, ignoreExpiration);
						if (company != null) {
							dataAccess.addLog("company found", logLevel);
							if (issuer.equals(
									Urls.SECURE_URL + Urls.SAML_ENTITY_ID.replace("{apiKey}", company.getApiKey()))) {
								dataAccess.addLog("issuer: " + issuer, logLevel);
								for (String audienceMember : audience) {
									dataAccess.addLog("audienceMember: " + audienceMember, logLevel);
									if (audienceMember.equals(company.getCompanyBaseUrl())) {
										validated = true;
										dataAccess.addLog("all claims were validated", logLevel);
									}
								}
							} else {
								dataAccess.addLog(
										"issuer violated: " + issuer + " and not " + Urls.SECURE_URL
												+ Urls.SAML_ENTITY_ID.replace("{apiKey}", company.getApiKey()),
										LogConstants.WARNING);
							}
						} else {
							dataAccess.addLog(Constants.COMPANY_NOT_FOUND, LogConstants.ERROR);
						}
					} else {
						dataAccess.addLog(Constants.TOKEN_NOT_FOUND, LogConstants.WARNING);
					}
				} else {
					dataAccess.addLog("notBefore violated", LogConstants.WARNING);
				}
			} else {
				dataAccess.addLog("exp violated", LogConstants.WARNING);
			}
		} else {
			dataAccess.addLog("claims were null", LogConstants.WARNING);
		}
		return validated;
	}
}

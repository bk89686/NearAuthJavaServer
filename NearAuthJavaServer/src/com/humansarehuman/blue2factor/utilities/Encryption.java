package com.humansarehuman.blue2factor.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.util.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.jsonConversion.RsaOaepJsonWebKey;
import com.humansarehuman.blue2factor.entities.jsonConversion.RsaOaepJsonWebPrivateKey;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.ServerDbObj;
import com.nimbusds.jose.jwk.RSAKey;

public class Encryption {

	public String createAndSaveKeyForJavascript(DeviceDbObj device, BrowserDbObj browser, String url)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IOException {
		KeyPair keyPair = this.generateNewRsaKey();
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String keyString = this.publicKeyToString(keyPair.getPublic());
		dataAccess.addLog(keyString);
		dataAccess.addLog("which should equal");
		PrivateKey privateKey = keyPair.getPrivate();
		Encryption encryption = new Encryption();
		String keyString2 = this.publicKeyToString(encryption.getPublicKeyFromPrivate(privateKey));
		dataAccess.addLog(keyString2);
		byte[] privateKeyBytes = privateKey.getEncoded();
		String privateKeyStr = Base64.getEncoder().encodeToString(privateKeyBytes);
		CompanyDbObj company = dataAccess.getCompanyByDevId(browser.getDeviceId());
		if (company != null) {
			KeyDbObj key = new KeyDbObj(browser.getDeviceId(), browser.getBrowserId(), device.getGroupId(),
					company.getCompanyId(), KeyType.B2F_SERVER_PRIVATE_KEY_FOR_BROWSER, privateKeyStr, true, "RSA",
					url);

			dataAccess.addKey(key);
			dataAccess.deactivateKeyTypeForBrowserAndUrlExcept(KeyType.B2F_SERVER_PRIVATE_KEY_FOR_BROWSER,
					browser.getBrowserId(), url, key.getKeyId());
			if (!url.equals(Urls.URL_WITHOUT_PROTOCOL)) {
				if (device.isCentral()) {
					dataAccess.updateDeviceBrowserSetupComplete(device, true);
				} else {
					dataAccess.updateDeviceBrowserSetupCompleteForAllPerfs(device, true);
				}
			}
		} else {
			dataAccess.addLog("company was null, device was probably deprovisioned");
			keyString = null;
		}
		return keyString;
	}

	public String createAndSaveKeyForLambda(CompanyDbObj company, GroupDbObj group, String deviceId,
			CompanyDataAccess dataAccess)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IOException {
		KeyPair keyPair = this.generateNewRsaKey();
		String keyString = this.publicKeyToString(keyPair.getPublic());
		dataAccess.addLog(keyString);
		dataAccess.addLog("which should equal");
		PrivateKey privateKey = keyPair.getPrivate();
		Encryption encryption = new Encryption();
		String keyString2 = this.publicKeyToString(encryption.getPublicKeyFromPrivate(privateKey));
		dataAccess.addLog(keyString2);
		byte[] privateKeyBytes = privateKey.getEncoded();
		String privateKeyStr = Base64.getEncoder().encodeToString(privateKeyBytes);
		if (company != null) {
			dataAccess.expireKeysByTypeAndDeviceId(KeyType.LAMBDA_PRIVATE_KEY, deviceId);
			KeyDbObj key = new KeyDbObj(deviceId, null, group.getGroupId(), company.getCompanyId(),
					KeyType.LAMBDA_PRIVATE_KEY, privateKeyStr, false, "RSA", null);
			dataAccess.addKey(key);
		} else {
			keyString = null;
		}
		return keyString;
	}

	public String createAndSaveKeySshTerminal(CompanyDbObj company, GroupDbObj group, String deviceId,
			CompanyDataAccess dataAccess)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IOException {
		KeyPair keyPair = this.generateNewRsaKey();
		String keyString = this.publicKeyToString(keyPair.getPublic());
		dataAccess.addLog(keyString);
		dataAccess.addLog("which should equal");
		PrivateKey privateKey = keyPair.getPrivate();
		Encryption encryption = new Encryption();
		String keyString2 = this.publicKeyToString(encryption.getPublicKeyFromPrivate(privateKey));
		dataAccess.addLog(keyString2);
		byte[] privateKeyBytes = privateKey.getEncoded();
		String privateKeyStr = Base64.getEncoder().encodeToString(privateKeyBytes);
		if (company != null) {
			dataAccess.expireKeysByTypeAndDeviceId(KeyType.TERMINAL_SSH_PRIVATE_KEY, deviceId);
			KeyDbObj key = new KeyDbObj(deviceId, null, group.getGroupId(), company.getCompanyId(),
					KeyType.TERMINAL_SSH_PRIVATE_KEY, privateKeyStr, false, "RSA", null);
			dataAccess.addKey(key);
		} else {
			keyString = null;
		}
		return keyString;
	}

	public String createAndSaveKeyForDevice(String companyId, String groupId, String deviceId,
			CompanyDataAccess dataAccess)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IOException {
		KeyPair keyPair = this.generateNewRsaKey();
		String keyString = this.publicKeyToString(keyPair.getPublic());
		dataAccess.addLog(keyString);
		dataAccess.addLog("which should equal");
		PrivateKey privateKey = keyPair.getPrivate();
		Encryption encryption = new Encryption();
		String keyString2 = this.publicKeyToString(encryption.getPublicKeyFromPrivate(privateKey));
		dataAccess.addLog(keyString2);
		byte[] privateKeyBytes = privateKey.getEncoded();
		String privateKeyStr = Base64.getEncoder().encodeToString(privateKeyBytes);
		dataAccess.expireKeysByTypeAndDeviceId(KeyType.SERVER_PRIVATE_KEY_FOR_DEVICE, deviceId);
		KeyDbObj key = new KeyDbObj(deviceId, null, groupId, companyId, KeyType.SERVER_PRIVATE_KEY_FOR_DEVICE,
				privateKeyStr, false, "RSA", null);
		dataAccess.addKey(key);
		return keyString;
	}

	public String addNewLinesToKeys(String keyText) {
		keyText = keyText.replaceAll("\n", "").replaceAll("\r", "");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keyText.length(); i++) {
			if (i > 0 && (i % 64 == 0)) {
				sb.append("\n");
			}
			sb.append(keyText.charAt(i));
		}
		sb.append("\n");
		keyText = sb.toString();
		return keyText;
	}

	public String privateKeyToStringAlt(PrivateKey privateKey) {
		String pkStr = null;
		DataAccess dataAccess = new DataAccess();
		KeyFactory fact;
		try {
			fact = KeyFactory.getInstance("DSA");
			PKCS8EncodedKeySpec spec = fact.getKeySpec(privateKey, PKCS8EncodedKeySpec.class);
			byte[] packed = spec.getEncoded();
			String key64 = Base64.getEncoder().encodeToString(packed);
			Arrays.fill(packed, (byte) 0);
			pkStr = key64;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			dataAccess.addLog(e);
		}
		return pkStr;
	}

	public String decryptClientServerText(String companyId, String encryptedString) {
		String decrypted = null;
		DataAccess dataAccess = new DataAccess();
		try {
			if (!TextUtils.isEmpty(companyId)) {
				dataAccess.addLog("company exists");
				KeyDbObj keyObj = dataAccess.getCompanySshPrivateKey(companyId);
				decrypted = decryptWithPrivateKey(keyObj, encryptedString);
			} else {
				dataAccess.addLog("company was null", LogConstants.WARNING);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return decrypted;
	}

	public String decryptClientServerText(CompanyDbObj company, String encryptedString) {
		String decrypted = null;
		DataAccess dataAccess = new DataAccess();
		try {
			if (company != null) {
				decrypted = decryptClientServerText(company.getCompanyId(), encryptedString);
			} else {
				dataAccess.addLog("company was null", LogConstants.WARNING);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return decrypted;
	}

	public String decryptClientSshText(CompanyDbObj company, String encryptedString) {
		String decrypted = null;
		DataAccess dataAccess = new DataAccess();
		try {
			if (company != null) {
				decrypted = decryptClientServerText(company.getCompanyId(), encryptedString);
			} else {
				dataAccess.addLog("company was null", LogConstants.WARNING);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return decrypted;
	}

	public String decryptWithPkcs8PrivateKey(KeyDbObj keyObj, String encryptedString) {
		String decrypted = null;
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.addLog("decrypting: " + encryptedString);
			byte[] decodedEncryptedBytes = encryptedString.getBytes();
			dataAccess.addLog("decrypting " + decodedEncryptedBytes.length + " bytes");
			byte[] encryptedBytes = Base64.getDecoder().decode(decodedEncryptedBytes);
			decrypted = decryptBytes(keyObj, encryptedBytes, "RSA/ECB/PKCS1Padding");
			dataAccess.addLog("decrypted: " + decrypted);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return decrypted;
	}

	public String decryptWithPrivateKey(KeyDbObj keyObj, String encryptedString) {
		String decrypted = null;
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.addLog("decrypting: '" + encryptedString + "'");
			byte[] decodedEncryptedBytes = encryptedString.getBytes();
			dataAccess.addLog(
					"decrypting " + decodedEncryptedBytes.length + " bytes with privateKey: " + keyObj.getKeyId());
			String pubKeyStr = this.getPublicKeyStringFromPrivate(keyObj);
			dataAccess.addLog("publicKey should have been " + pubKeyStr, LogConstants.TRACE);
			byte[] encryptedBytes = Base64.getDecoder().decode(decodedEncryptedBytes);
			decrypted = decryptBytes(keyObj, encryptedBytes, "RSA/ECB/PKCS1Padding");
			dataAccess.addLog("decrypted: " + decrypted);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return decrypted;
	}

	public String decryptUrlSafeWithPrivateKey(KeyDbObj keyObj, String encryptedString) {
		String decrypted = null;
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.addLog("decrypting: '" + encryptedString + "'");
			byte[] decodedEncryptedBytes = encryptedString.getBytes();
			dataAccess.addLog(
					"decrypting " + decodedEncryptedBytes.length + " bytes with privateKey: " + keyObj.getKeyId());
			byte[] encryptedBytes = Base64.getUrlDecoder().decode(decodedEncryptedBytes);
			decrypted = decryptBytes(keyObj, encryptedBytes, "RSA/ECB/PKCS1Padding");
			dataAccess.addLog("decrypted: " + decrypted);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return decrypted;
	}

	private String decryptBytes(KeyDbObj keyObj, byte[] encryptedBytes, String encoding) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String decrypted = null;
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("decrypting bytes: " + encryptedBytes.length);
		if (keyObj != null) {
			dataAccess.addLog("keyObj found\r\n" + keyObj.getKeyText());
			PrivateKey privateKey = stringToPrivateKey(keyObj.getKeyText());
			dataAccess.addLog("privateKey found", LogConstants.TRACE);
			dataAccess.addLog("key format: " + privateKey.getFormat());
			RSAPrivateCrtKey rsaCrtKey = (RSAPrivateCrtKey) privateKey;
			BigInteger modulus = rsaCrtKey.getModulus();
			dataAccess.addLog("modulus: " + modulus + "\r\nlength: " + modulus.toString().length());
			Cipher cipher = Cipher.getInstance(encoding);

			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
			decrypted = new String(decryptedBytes);
			dataAccess.addLog("decrypted: " + decrypted);
		} else {
			dataAccess.addLog("keyObj NOT found");
		}
		return decrypted;
	}

	private String decryptBytesUsingOeap(KeyDbObj keyObj, byte[] encryptedBytes) {
		String decrypted = null;
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.addLog("decrypting bytes: " + encryptedBytes.length, LogConstants.TRACE);
			PrivateKey privateKey = stringToPrivateKey(keyObj.getKeyText());
			RSAPrivateKey rsa = (RSAPrivateKey) privateKey;
			dataAccess.addLog("modulus length: " + rsa.getModulus().bitLength());
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
			OAEPParameterSpec oaepParameterSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
					PSource.PSpecified.DEFAULT);
			cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParameterSpec);
			this.getPublicKeyStringFromPrivate(keyObj);
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
			decrypted = new String(decryptedBytes);
			dataAccess.addLog("decrypted: " + decrypted, LogConstants.TRACE);

		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return decrypted;
	}

	public boolean verifySignature(ServerDbObj server, String plainText, String signature) {
		boolean success = false;
		DataAccess dataAccess = new DataAccess();
		try {
			KeyDbObj serverKey = getServerPublicKey(server.getServerId());
			if (serverKey != null) {
				dataAccess.addLog("key was found for server " + server.getServerName());
				String serverPublicKeyString = serverKey.getKeyText();
				dataAccess.addLog("for device: " + server.getServerName() + " - " + server.getServerId());
				dataAccess.addLog("verifySignature", "using keyText: " + serverPublicKeyString);
				PublicKey publicKey = stringToPublicKey(serverPublicKeyString, OsClass.LINUX);
				if (publicKey != null) {
					dataAccess.addLog("built publicKey from string");
					success = verifySignature(plainText, signature, publicKey, OsClass.LINUX);
				} else {
					dataAccess.addLog("publicKey could not be built");
				}

			} else {
				dataAccess.addLog("key is null for server: " + server.getServerName());
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}

	public boolean verifyPreSshSignature(KeyDbObj key, String plainText, String signature, OsClass osClass,
			DataAccess dataAccess) throws Exception, InvalidKeySpecException {
		boolean success = false;
		dataAccess.addLog("key was found");
		String publicKeyString = key.getKeyText();
		dataAccess.addLog("using keyText: " + publicKeyString);
		PublicKey publicKey = stringToPublicKey(publicKeyString, osClass);
		if (publicKey != null) {
			dataAccess.addLog("built publicKey from string");
			success = verifySignature(plainText, signature, publicKey, osClass);
		} else {
			dataAccess.addLog("publicKey could not be built");
		}
		return success;
	}

	public boolean verifyPostSshSignature(KeyDbObj key, String plainText, String signature, DataAccess dataAccess)
			throws Exception, InvalidKeySpecException {
		boolean success = false;
		dataAccess.addLog("key was found");
		OsClass osClass = OsClass.LINUX;
		String publicKeyString = key.getKeyText();
		dataAccess.addLog("using keyText: " + publicKeyString);
		PublicKey publicKey = stringToPublicKey(publicKeyString, osClass);
		if (publicKey != null) {
			dataAccess.addLog("built publicKey from string");
			success = verifySignature(plainText, signature, publicKey, osClass);
		} else {
			dataAccess.addLog("publicKey could not be built");
		}
		return success;
	}

	public boolean verifySignatureWithForegroundKey(DeviceDbObj device, String plainText, String signature) {
		boolean success = false;
		DataAccess dataAccess = new DataAccess();
		int logVal = LogConstants.TRACE;
		try {
			KeyDbObj deviceKey = getDevicePublicKeyForeground(device);
			if (deviceKey != null) {
				dataAccess.addLog(device.getDeviceId(), "key was found for " + device.getDeviceType()
						+ " it was created at " + deviceKey.getCreateDate(), logVal);
				String devicePublicKeyString = deviceKey.getKeyText();
				dataAccess.addLog(device.getDeviceId(), "using keyText: " + devicePublicKeyString, logVal);
				PublicKey publicKey = stringToPublicKey(devicePublicKeyString, device.getOperatingSystem());
				if (publicKey != null) {
					dataAccess.addLog(device.getDeviceId(), "built publicKey from string", logVal);
					success = verifySignature(plainText, signature, publicKey, device.getOperatingSystem());
				} else {
					dataAccess.addLog("publicKey could not be built", logVal);
				}

			} else {
				dataAccess.addLog(device.getDeviceId(), "key is null for device: " + device.getDeviceId(),
						LogConstants.ERROR);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}

	public boolean verifySignatureWithBackgroundKey(DeviceDbObj device, String plainText, String signature) {
		boolean success = false;
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.addLog(device.getDeviceId(), "key was found for " + device.getDeviceType(),
					LogConstants.TRACE);
			KeyDbObj deviceKey = getDevicePublicKey(device);
			if (deviceKey != null) {
				String devicePublicKeyString = deviceKey.getKeyText();
				dataAccess.addLog("for device: " + device.getDeviceType() + " - " + device.getDeviceId());
				dataAccess.addLog("using keyText: " + devicePublicKeyString);
				PublicKey publicKey = stringToPublicKey(devicePublicKeyString, device.getOperatingSystem());
				if (publicKey != null) {
					dataAccess.addLog("built publicKey from string");
					success = verifySignature(plainText, signature, publicKey, device.getOperatingSystem());
				} else {
					dataAccess.addLog(device.getDeviceId(), "publicKey could not be built", LogConstants.ERROR);
				}

			} else {
				dataAccess.addLog("key is null for device: " + device.getDeviceId());
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}

	public boolean verifySignature(CompanyDbObj company, String plainText, String signature) {
		boolean success = false;
		DataAccess dataAccess = new DataAccess();
		try {
			Encryption encryption = new Encryption();
			KeyDbObj privatekey = dataAccess.getCompanySshPrivateKey(company);

			if (privatekey != null) {
				dataAccess.addLog("key was found for company " + company.getCompanyName());
				String devicePublicKeyString = encryption.getPublicKeyStringFromPrivate(privatekey);
				dataAccess.addLog("using keyText: " + devicePublicKeyString);
				PublicKey publicKey = stringToPublicKey(devicePublicKeyString, OsClass.LINUX);
				success = verifySignature(plainText, signature, publicKey, OsClass.LINUX);
			} else {
				dataAccess.addLog("key is null for device: " + company.getCompanyName());
			}
		} catch (Exception e) {
			dataAccess.addLog("verifySignature", e);
		}
		dataAccess.addLog("success2: " + success);
		return success;
	}

	public boolean verifyWebServerSignature(CompanyDbObj company, String plainText, String signature) {
		boolean success = false;
		DataAccess dataAccess = new DataAccess();
		int logLevel = LogConstants.IMPORTANT;
		try {
			dataAccess.addLog("key was found for company " + company.getCompanyName(), logLevel);
			KeyDbObj coPublicKey = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.WEB_SERVER_PUBLIC_KEY,
					company.getCompanyId());
			if (coPublicKey != null) {
				dataAccess.addLog("using keyText: " + coPublicKey.getKeyText(), logLevel);
				PublicKey publicKey = stringToPublicKey(coPublicKey.getKeyText(), OsClass.LINUX);
				success = verifySignature(plainText, signature, publicKey, OsClass.LINUX);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		dataAccess.addLog("success: " + success, logLevel);
		return success;
	}

	public String getPublicKeyStringFromPrivate(KeyDbObj key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		Encryption encryption = new Encryption();
		PrivateKey privateKey = encryption.stringToPrivateKey(key.getKeyText());
		String publicKeyStr = null;
		DataAccess dataAccess = new DataAccess();
		if (privateKey != null) {
			dataAccess.addLog("privateKey object built from string");
			PublicKey publicKey = getPublicKeyFromPrivate(privateKey);
			publicKeyStr = Base64.getMimeEncoder().encodeToString(publicKey.getEncoded());
			dataAccess.addLog("public generated:\n" + publicKeyStr);
		}
		return publicKeyStr;
	}

	public PublicKey getPublicKeyFromPrivate(PrivateKey privateKey) {
		PublicKey publicKey = null;
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("privateKey object built from string");
		try {
			RSAPrivateCrtKey privk = (RSAPrivateCrtKey) privateKey;
			RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(),
					privk.getPublicExponent());
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			publicKey = keyFactory.generatePublic(publicKeySpec);
		} catch (Exception e) {
			dataAccess.addLog("getPublicKeyFromPrivate", e);
		}
		return publicKey;
	}

	private boolean verifySignature(String plainText, String signature, PublicKey publicKey, OsClass osClass)
			throws Exception {
		DataAccess dataAccess = new DataAccess();
		boolean success = false;
		int logLevel = LogConstants.TRACE;
		signature = signature.replaceAll("%2B", "+");
		signature = signature.replaceAll("%2F", "/");
		signature = signature.replaceAll("%3D", "=");
		dataAccess.addLog("signature: " + signature, logLevel);
		dataAccess.addLog("class: " + osClass.toString(), logLevel);
		if (osClass.equals(OsClass.OSX)) {
			success = verifySignatureMacOs(plainText, signature, publicKey);
		} else if (osClass.equals(OsClass.IOS) || osClass.equals(OsClass.WATCHOS)) {
			success = verifySignatureApple(plainText, signature, publicKey);
		} else {
			success = verifySignatureNotApple(dataAccess, plainText, signature, publicKey);
		}
		dataAccess.addLog("success: " + success);
		return success;
	}

	private boolean verifySignatureNotApple(DataAccess dataAccess, String plainText, String signature,
			PublicKey publicKey) {
		boolean success = false;
		int logLevel = LogConstants.TRACE;
		try {
			Signature publicSignature = Signature.getInstance("SHA256withRSA");
			publicSignature.initVerify(publicKey);
			publicSignature.update(plainText.getBytes(StandardCharsets.UTF_8));
			byte[] signatureBytes = org.apache.commons.codec.binary.Base64.decodeBase64(signature);
			dataAccess.addLog("plainText: " + plainText, logLevel);
			dataAccess.addLog("signature: " + signature, logLevel);
			dataAccess.addLog("signatureBytes: " + signatureBytes.length);
			success = publicSignature.verify(signatureBytes);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		dataAccess.addLog("success: " + success, logLevel);
		return success;
	}

	boolean useTerriblyInsecureHack = true;

	private boolean verifySignatureApple(String plainText, String signatureString, PublicKey publicKey)
			throws Exception {
		DataAccess dataAccess = new DataAccess();
		int logLevel = LogConstants.TRACE;
		boolean success = false;
		try {
			Signature sign = Signature.getInstance("SHA256withRSA");
			sign.initVerify(publicKey);
			sign.update(plainText.getBytes(StandardCharsets.UTF_8));
			byte[] signature = org.apache.commons.codec.binary.Base64.decodeBase64(signatureString);
			dataAccess.addLog("plainText: " + plainText, logLevel);
			dataAccess.addLog("signature: " + signatureString, logLevel);
			dataAccess.addLog("signatureBytes :" + Arrays.toString(signature), logLevel);
			success = sign.verify(signature);
			if (!success && useTerriblyInsecureHack) {

			}

			dataAccess.addLog("success: " + success, logLevel);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}

	private boolean verifySignatureMacOs(String plainText, String signatureString, PublicKey publicKey)
			throws Exception {
		DataAccess dataAccess = new DataAccess();
		int logLevel = LogConstants.TRACE;
		boolean success = false;
		try {
			byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
			byte[] signatureBytes = Base64.getDecoder().decode(signatureString);

			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initVerify(publicKey);
			signature.update(plainTextBytes);
			dataAccess.addLog("plainText: " + plainText, logLevel);
			dataAccess.addLog("signature: " + signatureString, logLevel);
			dataAccess.addLog("signatureBytes :" + Arrays.toString(signatureBytes), logLevel);
			success = signature.verify(signatureBytes);
			if (!success && useTerriblyInsecureHack) {

			}

			dataAccess.addLog("success: " + success, logLevel);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}

	public String decryptBasedOnBrowserOrServerId(String token, String encryptedString, String url) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		KeyType keyType = KeyType.B2F_SERVER_PRIVATE_KEY_FOR_BROWSER;
		KeyDbObj key = dataAccess.getKeyByTypeAndBrowserTokenAndSite(keyType, token, url);
		return decryptBasedWithOeapAndKey(encryptedString, key, dataAccess);
	}

	public String decryptBasedWithOeapAndKey(String encryptedString, KeyDbObj key, CompanyDataAccess dataAccess) {
		String retStr;
		if (key != null && encryptedString != null) {
			dataAccess.addLog("encryptedString: '" + encryptedString + "'");
			byte[] encryptedBytes = encryptedString.getBytes(StandardCharsets.UTF_8);
			dataAccess.addLog("encryptedBytes(UTF-8) length: " + encryptedBytes.length);
			byte[] encryptedBytesDecoded = Base64.getDecoder().decode(encryptedBytes);
			dataAccess.addLog(" from Base64 length: " + encryptedBytesDecoded.length);
			retStr = this.decryptBytesUsingOeap(key, encryptedBytesDecoded);
		} else {
			retStr = Constants.KEY_NOT_FOUND;
		}
		return retStr;
	}

	public String decryptForLambda(String encryptedString, String deviceId) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		KeyDbObj key = dataAccess.getKeyByTypeAndDeviceId(KeyType.LAMBDA_PRIVATE_KEY, deviceId);
		return decryptBasedWithOeapAndKey(encryptedString, key, dataAccess);
	}

	public String encryptBrowserData(DeviceDbObj device, String data, String url)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		String encryptedText = null;
		DataAccess dataAccess = new DataAccess();
		try {
			KeyDbObj key = dataAccess.getB2fBrowserPublicKeyByDevice(device, url);
			String browserPublicKeyText = key.getKeyText();
			dataAccess.addLog("encrypting: " + data);
			PublicKey publicKey = stringToPublicKey(browserPublicKeyText);// keyFactory.generatePublic(keySpec);
			encryptedText = new String(encrypt(data, publicKey));
			dataAccess.addLog("encryptedText: " + encryptedText);
		} catch (Exception e) {
			dataAccess.addLog("encryptBrowserData", e);
		}
		return encryptedText;
	}

	public String encryptBrowserData(BrowserDbObj browser, String data, String url)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		String encryptedText = null;
		DataAccess dataAccess = new DataAccess();
		try {
			KeyDbObj key = dataAccess.getB2fBrowserPublicKeyByBrowser(browser, url);
			if (key != null) {
				String browserPublicKeyText = key.getKeyText();
				dataAccess.addLog("encrypting: " + data);
				dataAccess.addLog("with Key: " + browserPublicKeyText);
				PublicKey publicKey = stringToPublicKey(browserPublicKeyText);
				encryptedText = new String(encryptUsingOaep(data, publicKey));
				dataAccess.addLog("encryptedText: " + encryptedText);
			} else {
				dataAccess.addLog("browser key was null");
			}
		} catch (Exception e) {
			dataAccess.addLog("encryptBrowserData", e);
		}
		return encryptedText;
	}

	public PrivateKey stringToPrivateKey(String privateKeyStr) {
		PrivateKey privateKey = null;
		privateKeyStr = privateKeyStr.replaceAll("-----BEGIN PRIVATE KEY-----", "");
		privateKeyStr = privateKeyStr.replaceAll("-----END PRIVATE KEY-----", "");

		privateKeyStr = privateKeyStr.replaceAll("\\s+", "");
		DataAccess dataAccess = new DataAccess();
		byte[] pkBytes = privateKeyStr.getBytes();
		byte[] decoded = Base64.getDecoder().decode(pkBytes);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
		KeyFactory keyFactory = null;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			privateKey = keyFactory.generatePrivate(keySpec);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			new DataAccess().addLog("stringToPrivateKey", e);
		}
		dataAccess.addLog("success: " + (privateKey != null));
		return privateKey;
	}

	public PrivateKey pemKeyStringToPrivateKey(String privateKeyStr) {
		PrivateKey privateKey = null;
		String privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----", "").replaceAll(System.lineSeparator(), "")
				.replace("-----END PRIVATE KEY-----", "");

		try {
			byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			privateKey = keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			new DataAccess().addLog("pemKeyStringToPrivateKey", e);
		}
		return privateKey;
	}

	public String signStringForSsh(KeyDbObj key, String stringToSign)
			throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException {
		PrivateKey privateKey = stringToPrivateKey(key.getKeyText());
		return signString(privateKey, stringToSign);
	}

	private String signString(PrivateKey privateKey, String stringToSign)
			throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException {
		byte[] data = stringToSign.getBytes(StandardCharsets.UTF_8);
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initSign(privateKey);
		sig.update(data);
		byte[] signatureBytes = sig.sign();
		String encryptedValue = Base64.getEncoder().encodeToString(signatureBytes);
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("plainText: " + stringToSign);
		dataAccess.addLog("signed string:" + Arrays.toString(signatureBytes));
		dataAccess.addLog(encryptedValue);
		return encryptedValue;
	}

	public PrivateKey stringToJwtPrivateKey(String privateKeyStr) {
		PrivateKey privateKey = null;
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr.getBytes()));
		KeyFactory keyFactory = null;
		DataAccess dataAccess = new DataAccess();
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			privateKey = keyFactory.generatePrivate(keySpec);
			dataAccess.addLog("private Key created: " + (privateKey != null));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			new DataAccess().addLog(e);
		}
		return privateKey;
	}

	public PublicKey stringToJwtPublicKey(String publicKeyStr) {
		PublicKey generatedPublic = null;
		String keyString = publicKeyStr.replace("\n", "");
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			byte[] decoded = Base64.getDecoder().decode(keyString);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
			generatedPublic = kf.generatePublic(keySpec);
		} catch (Exception e) {
			new DataAccess().addLog(e);
		}
		return generatedPublic;
	}

	public KeyPair generateNewRsaKey() throws NoSuchAlgorithmException, NoSuchProviderException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair keyPair = generator.generateKeyPair();
		printKeys(keyPair);
		return keyPair;
	}

	public void printKeys(KeyPair keyPair) {
		DataAccess dataAccess = new DataAccess();
		String pbk = this.publicKeyToString(keyPair.getPublic()).replaceAll("\\s+", "");
		String pvt = this.privateKeyToString(keyPair.getPrivate()).replaceAll("\\s+", "");
		dataAccess.addLog("publicKey: '" + pbk + "'");
		dataAccess.addLog("privateKey: '" + pvt + "'");
	}

	public String rsaToJwkString(RSAPublicKey publicKey) {
		RSAKey rsaKey = new RSAKey.Builder(publicKey).build();
		String jsonString = rsaKey.toJSONString();
		new DataAccess().addLog(jsonString);
		return jsonString;
	}

	public PrivateKey privateKeyFromJwk(RsaOaepJsonWebPrivateKey jwk) throws Exception {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String jsonStr = ow.writeValueAsString(jwk);
		RSAKey rsaKey = RSAKey.parse(jsonStr);
		PrivateKey privateKey = rsaKey.toRSAPrivateKey();
		return privateKey;
	}

	public PublicKey publicKeyFromJwk(RsaOaepJsonWebKey jwk) throws Exception {
		DataAccess dataAccess = new DataAccess();
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String jsonStr = ow.writeValueAsString(jwk);
		dataAccess.addLog(jsonStr);
		RSAKey rsaKey = RSAKey.parse(jsonStr);
		PublicKey publicKey = rsaKey.toRSAPublicKey();
		return publicKey;
	}

	public String publicKeyToString(PublicKey publicKey) {
		byte[] bytes = publicKey.getEncoded();
		String strKey = Base64.getEncoder().encodeToString(bytes);
		strKey = strKey.replaceAll("\\s+", "");
		new DataAccess().addLog(strKey);
		return strKey;
	}

	public String privateKeyToString(PrivateKey privateKey) {
		byte[] bytes = privateKey.getEncoded();
		String strKey = Base64.getEncoder().encodeToString(bytes);
		strKey = strKey.replaceAll("\\s+", "");
		return strKey;
	}

	//SecKeyAlgorithm.rsaEncryptionPKCS1
	public String encrypt(String plainText, PublicKey publicKey) throws BadPaddingException, IllegalBlockSizeException,
			InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		String base64 = null;
		int logLevel = LogConstants.TEMPORARILY_IMPORTANT;
		if (publicKey != null) {
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			DataAccess dataAccess = new DataAccess();
			dataAccess.addLog("encrypting: " + plainText, logLevel);
			dataAccess.addLog("with:\r"
					+ GeneralUtilities.addCarriageReturnEvery64(new Encryption().publicKeyToString(publicKey)));
			dataAccess.addLog("keyData: " + getPublicKeyData(publicKey));
			byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			String byteStr = getByteString(encryptedBytes);
			base64 = Base64.getEncoder().encodeToString(encryptedBytes);
			dataAccess.addLog("bytes: " + byteStr);
			dataAccess.addLog("encrypted: " + GeneralUtilities.addCarriageReturnEvery64(base64));
		}
		return base64;
	}
	

	public String getPublicKeyData(PublicKey pk) {
		RSAPublicKey rsaPub = (RSAPublicKey) (pk);
		BigInteger publicKeyModulus = rsaPub.getModulus();
		BigInteger publicKeyExponent = rsaPub.getPublicExponent();
		String keyData = "publicKeyModulus: " + publicKeyModulus;
		keyData += "\r\npublicKeyExponent: " + publicKeyExponent;
		keyData += "\r\nalgorithm: " + rsaPub.getAlgorithm();
		keyData += "\r\nformat: " + rsaPub.getFormat();
		return keyData;
	}

	public String getByteString(byte[] bytes) {
		String byteStr = "";
		int len = bytes.length;
		for (int i = 0; i < len; i++) {
			if (bytes[i] < 0) {
				byteStr += (bytes[i] + 256);
			} else {
				byteStr += bytes[i];
			}
			if (i < len - 1) {
				byteStr += ", ";
			}
		}
		return byteStr;
	}

	public String encryptForLambda(GroupDbObj group, String deviceId, CompanyDataAccess dataAccess) {
		String encryptedText = null;
		String tokenStr = GeneralUtilities.randomString();
		Timestamp tenYears = DateTimeUtilities.getCurrentTimestampPlusDays(3652);
		dataAccess.addTokenWithId(group.getGroupId(), deviceId, null, tokenStr, TokenDescription.LAMBDA, 0, null,
				tenYears);
		KeyDbObj pubKey = dataAccess.getKeyByTypeAndDeviceId(KeyType.LAMBDA_PUBLIC_KEY, deviceId);
		try {
			String keyString = pubKey.getKeyText().replace("-----BEGIN PUBLIC KEY-----", "");
			keyString = keyString.replace("-----END PUBLIC KEY-----", "");
			PublicKey publicKey = stringToPublicKey(keyString);
			encryptedText = this.encryptUsingOaep(tokenStr, publicKey);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return encryptedText;

	}

	private String encryptUsingOaep(String data, PublicKey publicKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
		OAEPParameterSpec oaepParameterSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
				PSource.PSpecified.DEFAULT);
		String encrypted = null;
		if (publicKey != null) {
			cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParameterSpec);
			byte[] bytes = cipher.doFinal(data.getBytes());
			DataAccess dataAccess = new DataAccess();
			encrypted = Base64.getEncoder().encodeToString(bytes);
			dataAccess.addLog("unencrypted: \r\n" + data);
			dataAccess.addLog("bytes: \r\n" + bytes);
			dataAccess.addLog("base64: \r\n" + encrypted);
		}
		return encrypted;
	}

	public PublicKey stringToPublicKey(String keyString, OsClass osClass)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		PublicKey generatedPublic = null;
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("class: " + osClass.name().toLowerCase());
		String keyType;
		if (osClass.name().toLowerCase().equals(OsClass.IOS.osClassName())
				|| osClass.name().toLowerCase().equals(OsClass.OSX.osClassName())
				|| osClass.name().toLowerCase().equals(OsClass.WATCHOS.osClassName())) {
			generatedPublic = getAppleKey(keyString);
			keyType = "apple";
		} else if (osClass.name().toLowerCase().equals(OsClass.WINDOWS.osClassName())) {
			generatedPublic = getWindowsKey(keyString);
			keyType = "windows";
		} else if (osClass.name().toLowerCase().equals(OsClass.LINUX.osClassName())) {
			generatedPublic = getPemKey(keyString, OsClass.LINUX.osClassName());
			keyType = "linux";
		} else {
			generatedPublic = getAndroidKey(keyString);
			keyType = "android";
		}
		dataAccess.addLog("decryted as " + keyType + " key");
		return generatedPublic;
	}

	private PublicKey stringToPublicKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return getAndroidKey(keyString);
	}

	private PublicKey getAndroidKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException {

		PublicKey generatedPublic = null;
		keyString = keyString.replace("\n", "");
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			new DataAccess().addLog("keyString after replace: " + keyString);
			byte[] decoded = Base64.getDecoder().decode(keyString);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
			generatedPublic = kf.generatePublic(keySpec);
			keyToString(generatedPublic);
		} catch (Exception e) {
			new DataAccess().addLog("getAndroidKey", e);
		}
		return generatedPublic;
	}

	private PublicKey getAppleKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
		PublicKey generatedPublic = null;
		keyString = keyString.replace("-----BEGIN PUBLIC KEY-----", "").replace("\n", "").replace("\r", "")
				.replace("-----END PUBLIC KEY-----", "");
		DataAccess dataAccess = new DataAccess();

		try {
			byte[] decoded = Base64.getDecoder().decode(keyString);
			org.bouncycastle.asn1.pkcs.RSAPublicKey pkcs1PublicKey = org.bouncycastle.asn1.pkcs.RSAPublicKey
					.getInstance(decoded);
			BigInteger modulus = pkcs1PublicKey.getModulus();
			byte[] modBytes = modulus.toByteArray();
			if (modBytes.length == 257 && modBytes[0] == 0) {
			    byte[] reformat = new byte[256];
			    System.arraycopy(modBytes, 1, reformat, 0, 256);
			    modBytes = reformat;
			}
			BigInteger publicExponent = pkcs1PublicKey.getPublicExponent();
			dataAccess.addLog("modulus: " + modulus, LogConstants.TEMPORARILY_IMPORTANT);
			dataAccess.addLog("publicExponent: " + publicExponent, LogConstants.TEMPORARILY_IMPORTANT);
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(1, modBytes), publicExponent);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			generatedPublic = kf.generatePublic(keySpec);
			keyToString(generatedPublic);

		} catch (Exception e) {
			dataAccess.addLog("getAppleKey", e);

		}
		return generatedPublic;
	}

	private String keyToString(PublicKey publicKey) {
		byte[] byte_pubkey = publicKey.getEncoded();
		String str_key = Base64.getEncoder().encodeToString(byte_pubkey);
		new DataAccess().addLog("stringKey:\r\n" + str_key);
		return str_key;
	}

	private PublicKey getWindowsKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
//        PublicKey generatedPublic = null;
//        keyString = keyString.replace("\n", "");
//        DataAccess dataAccess = new DataAccess();
//        byte[] decoded = Base64.getDecoder().decode(keyString.replace("\n", "").replace("\r", ""));
//        dataAccess.addLog("getWindowsKey", "keyString after decode: " + decoded.toString());
//        try {
//            org.bouncycastle.asn1.pkcs.RSAPublicKey pkcs1PublicKey = org.bouncycastle.asn1.pkcs.RSAPublicKey
//                    .getInstance(decoded);
//            BigInteger modulus = pkcs1PublicKey.getModulus();
//            BigInteger publicExponent = pkcs1PublicKey.getPublicExponent();
//            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);
//            KeyFactory kf = KeyFactory.getInstance("RSA");
//            generatedPublic = kf.generatePublic(keySpec);
//            keyToString(generatedPublic);
//        } catch (Exception e) {
//            dataAccess.addLog("getWindowsKey", e);
//        }
//        return generatedPublic;

		return getPemKey(keyString, OsClass.WINDOWS.osClassName());
	}

	public PublicKey getPemKey(String keyString, String src) throws NoSuchAlgorithmException, InvalidKeySpecException {

		String publicKeyPEM = keyString.replace("-----BEGIN PUBLIC KEY-----", "").replace("\n", "").replace("\r", "")
				.replace("-----END PUBLIC KEY-----", "");
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("publicKeyPEM: " + publicKeyPEM);
		dataAccess.addLog("src: " + src);
		byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
		return keyFactory.generatePublic(keySpec);
	}

	// returns an array of length 2 like [text, encryptedText]
	private String[] createEncryptedInstanceIdWithKeyText(String keyText, OsClass osClass, String firstLetter) {
		String[] encPair = new String[2];
		DataAccess dataAccess = new DataAccess();
		try {
			PublicKey pk = stringToPublicKey(keyText, osClass);
			if (pk != null) {
				encPair = this.createEncryptedInstanceIdWithPublicKey(pk, firstLetter, osClass);
				dataAccess.addLog(osClass + ": encrypted: " + encPair[0], LogConstants.TEMPORARILY_IMPORTANT);
				dataAccess.addLog(osClass + ": to: " + encPair[1], LogConstants.TEMPORARILY_IMPORTANT);
				dataAccess.addLog(osClass + ": with " + keyText, LogConstants.TEMPORARILY_IMPORTANT);
			} else {
				dataAccess.addLog("the text '" + keyText + "' is not a valid key", LogConstants.ERROR);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return encPair;
	}

	public String encryptStringWithSshPublicKey(String deviceId, String unencrypted, OsClass osClass) {

		String encryptedString = null;
		DataAccess dataAccess = new DataAccess();
		try {
			KeyDbObj key = dataAccess.getKeyByTypeAndDeviceId(KeyType.SERVER_SSH_PUBLIC_KEY, deviceId);
			if (key != null) {
				String keyText = key.getKeyText();
				dataAccess.addLog("keyText: " + keyText);
				PublicKey pk = stringToPublicKey(keyText, osClass);
				encryptedString = recreateEncryptedInstanceIdWithPublicKey(pk, unencrypted);
				dataAccess.addLog("encryptedString: " + encryptedString);
			} else {
				dataAccess.addLog("key was null for device: " + deviceId);
			}
		} catch (Exception e) {
			dataAccess.addLog("encryptStringWithSshPublicKey", e);
		}
		return encryptedString;
	}

	public String encryptWithKeyDbObj(DeviceDbObj device, KeyDbObj key, String unencrypted, DataAccess dataAccess)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		String keyText = key.getKeyText();
		dataAccess.addLog("keyText: " + keyText);
		OsClass osClass = device.getOperatingSystem();
		PublicKey pk = stringToPublicKey(keyText, osClass);
		String encryptedString = recreateEncryptedInstanceIdWithPublicKey(pk, unencrypted);
		dataAccess.addLog("encryptedString: " + encryptedString);
		return encryptedString;
	}

	public String encryptStringWithPublicKey(DeviceDbObj device, String unencrypted) {

		String encryptedString = null;
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.addLog(device.getDeviceId(), "encryptStringWithPublicKey called",
					LogConstants.TRACE);
			KeyDbObj key = getDevicePublicKey(device.getDeviceId());
			if (key != null) {
				encryptedString = encryptWithKeyDbObj(device, key, unencrypted, dataAccess);
			} else {
				dataAccess.addLog("key was null for device: " + device.getDeviceId());
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return encryptedString;
	}

	public String encryptWithPublicKey(KeyDbObj key, String unencrypted)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		DataAccess dataAccess = new DataAccess();
		String keyText = key.getKeyText();
		dataAccess.addLog("plainText: " + unencrypted);
		OsClass osClass = OsClass.LINUX;
		PublicKey pk = stringToPublicKey(keyText, osClass);
		String encryptedString = recreateEncryptedInstanceIdWithPublicKey(pk, unencrypted);
		dataAccess.addLog("encryptedString: " + encryptedString);
		return encryptedString;
	}

	private String recreateEncryptedInstanceIdWithPublicKey(PublicKey pk, String instanceId) {
		String encInstanceId = null;
		try {
			encInstanceId = new String(encrypt(instanceId, pk));
		} catch (Exception e) {
			new DataAccess().addLog(e);
		}
		return encInstanceId;
	}

	// returns an array of length 2 like [text, encryptedText]
	private String[] createEncryptedInstanceIdWithPublicKey(PublicKey pk, String firstLetter, OsClass osClass) {
		String[] encPair = new String[2];
		DataAccess dataAccess = new DataAccess();
		try {
			encPair[0] = firstLetter + GeneralUtilities.randomString(19);
			encPair[1] = new String(encrypt(encPair[0], pk));
			dataAccess.addLog("encrypted: " + encPair[0]);
			dataAccess.addLog("using public key: " + publicKeyToString(pk));
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return encPair;
	}

	// returns an array of length 2 like [random text, encryptedText]
	public String[] createEncryptedInstanceId(KeyDbObj key, DeviceDbObj device, String firstLetter) {
		String[] enc = null;
		DataAccess dataAccess = new DataAccess();
		if (key != null) {
			String keyText = key.getKeyText();
			if (keyText != null) {
				enc = this.createEncryptedInstanceIdWithKeyText(keyText, device.getOperatingSystem(), firstLetter);
			} else {
				dataAccess.addLog("keyText was null for " + device.getDeviceId(), LogConstants.ERROR);
			}
		} else {
			dataAccess.addLog("key was null for " + device.getDeviceId(), LogConstants.ERROR);
		}

		return enc;
	}

	// returns an array of length 2 like [random text, encryptedText]
	public String[] createEncryptedInstanceId(DeviceDbObj device, String firstLetter) {
		String[] enc = null;

		KeyDbObj key = getDevicePublicKey(device.getDeviceId());
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog(device.getDeviceId(), "creating new encrypted instance Ids, central: " + device.isCentral(),
				LogConstants.TRACE);
		if (key != null) {
			enc = createEncryptedInstanceId(key, device, firstLetter);
		} else {
			dataAccess.addLog("key was null for " + device.getDeviceId(), LogConstants.ERROR);
		}

		return enc;
	}

	// returns an array of length 2 like [random text, encryptedText]
	public String[] createEncryptedInstanceIdForSsh(DeviceDbObj device, String firstLetter) {
		String[] enc = null;
		KeyDbObj key = getDevicePublicKeyForSsh(device.getDeviceId());
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("finding key for device " + device.getDeviceType());
		if (key != null) {
			enc = createEncryptedInstanceId(key, device, firstLetter);
		} else {
			dataAccess.addLog("key was null for " + device.getDeviceId(), LogConstants.ERROR);
		}

		return enc;
	}

	public static KeyDbObj getDevicePublicKey(String deviceId) {
		return new DataAccess().getKeyByTypeAndDeviceId(KeyType.DEVICE_PUBLIC_KEY, deviceId);
	}

	public static KeyDbObj getDevicePublicKeyForSsh(String deviceId) {
		return new DataAccess().getKeyByTypeAndDeviceId(KeyType.TERMINAL_SSH_PUBLIC_KEY, deviceId);
	}

	public static KeyDbObj getDevicePublicKeyForeground(DeviceDbObj device) {
		KeyDbObj key;
		if (device.getOperatingSystem() == OsClass.OSX || device.getOperatingSystem() == OsClass.WINDOWS) {
			key = new DataAccess().getKeyByTypeAndDeviceId(KeyType.DEVICE_PUBLIC_KEY_FOREGROUND, device.getDeviceId());
		} else {
			new DataAccess().addLog(device.getDeviceId(), "this would be foreground but it's a mobile device",
					LogConstants.TRACE);
			key = getDevicePublicKey(device);
		}
		return key;
	}

	public static KeyDbObj getServerPublicKey(String serverId) {
		return new DataAccess().getKeyByTypeAndDeviceId(KeyType.SERVER_SSH_PUBLIC_KEY, serverId);
	}

	public static KeyDbObj getDevicePublicKey(DeviceDbObj device) {
		return new DataAccess().getKeyByTypeAndDeviceId(KeyType.DEVICE_PUBLIC_KEY, device.getDeviceId());
	}

	public static KeyDbObj getDeviceSshTerminalKey(DeviceDbObj device) {
		return new DataAccess().getKeyByTypeAndDeviceId(KeyType.TERMINAL_SSH_PUBLIC_KEY, device.getDeviceId());
	}

	@SuppressWarnings("ucd")
	public String b2fEncrypt(String text) {
		text = text.replace("E ", "{bk89686}#!");
		text = text.replace("W", "E ");
		text = text.replace("&quot; ", "W");
		text = text.replace(": ", "&quot; ");
		text = text.replace("th ", ": ");
		text = text.replace("j", "th ");
		text = text.replace("O", "j");
		text = text.replace("er", "O");
		text = text.replace("h", "er");
		text = text.replace("ent ", "h");
		text = text.replace("K", "ent ");
		text = text.replace("ion ", "K");
		text = text.replace("THER", "ion ");
		text = text.replace(" HE", "THER");
		text = text.replace(" the", " HE");
		text = text.replace("AND", " the");
		text = text.replace(" A d", "AND");
		text = text.replace("CAN I", " A d");
		text = text.replace("re", "CAN I");
		text = text.replace(" D", "re");
		text = text.replace("w", " D");
		text = text.replace("tt ", "w");
		text = text.replace("thr", "tt ");
		text = text.replace("? ", "thr");
		text = text.replace("u", "? ");
		text = text.replace("he", "u");
		text = text.replace(". ", "he");
		text = text.replace("ion", ". ");
		text = text.replace(" The", "ion");
		text = text.replace(">", " The");
		text = text.replace("an", ">");
		text = text.replace(" in", "an");
		text = text.replace("the", " in");
		text = text.replace("ING ", "the");
		text = text.replace("ent", "ING ");
		text = text.replace(" AN", "ent");
		text = text.replace("INK", " AN");
		text = text.replace("k", "INK");
		text = text.replace("(", "k");
		text = text.replace("IN", "(");
		text = text.replace(" TH", "IN");
		text = text.replace(" a", " TH");
		text = text.replace("Ch", " a");
		text = text.replace("st", "Ch");
		text = text.replace(" S", "st");
		text = text.replace(" he", " S");
		text = text.replace("3", " he");
		text = text.replace("s", "3");
		text = text.replace("&quot;", "s");
		text = text.replace("tre", "&quot;");
		text = text.replace("an ", "tre");
		text = text.replace("4", "an ");
		text = text.replace("y", "4");
		text = text.replace("ss ", "y");
		text = text.replace("6", "ss ");
		text = text.replace("TH", "6");
		text = text.replace("I", "TH");
		text = text.replace("Then", "I");
		text = text.replace("est", "Then");
		text = text.replace("T", "est");
		text = text.replace("d ", "T");
		text = text.replace("ION", "d ");
		text = text.replace("ier", "ION");
		text = text.replace("P", "ier");
		text = text.replace("er It", "P");
		text = text.replace(" IN", "er It");
		text = text.replace("T h", " IN");
		text = text.replace("D", "T h");
		text = text.replace("ch", "D");
		text = text.replace("d", "ch");
		text = text.replace("ll ", "d");
		text = text.replace("z", "ll ");
		text = text.replace("Y", "z");
		text = text.replace("HE", "Y");
		text = text.replace(" An", "HE");
		text = text.replace("AND ", " An");
		text = text.replace("ENT S", "AND ");
		text = text.replace(" He", "ENT S");
		text = text.replace("qu", " He");
		text = text.replace("o", "qu");
		text = text.replace("<", "o");
		text = text.replace("in", "<");
		text = text.replace("In", "in");
		text = text.replace("in ", "In");
		text = text.replace("SS ", "in ");
		text = text.replace("gh", "SS ");
		text = text.replace("Thr", "gh");
		text = text.replace("and ", "Thr");
		text = text.replace("THE", "and ");
		text = text.replace("V", "THE");
		text = text.replace(" E", "V");
		text = text.replace(" d", " E");
		text = text.replace(" t", " d");
		text = text.replace("ENT", " t");
		text = text.replace("Z", "ENT");
		text = text.replace("AN", "Z");
		text = text.replace("}", "AN");
		text = text.replace("2", "}");
		text = text.replace("sh", "2");
		text = text.replace("H", "sh");
		text = text.replace("TT ", "H");
		text = text.replace("li", "TT ");
		text = text.replace("0", "li");
		text = text.replace("C", "0");
		text = text.replace("ff l", "C");
		text = text.replace(" s", "ff l");
		text = text.replace(")", " s");
		text = text.replace("b", ")");
		text = text.replace("B", "b");
		text = text.replace("FF ", "B");
		text = text.replace("ewt", "FF ");
		text = text.replace(" S", "ewt");
		text = text.replace("ER", " S");
		text = text.replace("i", "ER");
		text = text.replace("TH ", "i");
		text = text.replace("s an", "TH ");
		text = text.replace("ly", "s an");
		text = text.replace("th", "ly");
		text = text.replace("p", "th");
		text = text.replace(" TH", "p");
		text = text.replace("m", " TH");
		text = text.replace(" RE", "m");
		text = text.replace("r", " RE");
		text = text.replace("ING", "r");
		text = text.replace("_", "ING");
		text = text.replace(" In", "_");
		text = text.replace("s t", " In");
		text = text.replace("e", "s t");
		text = text.replace("l", "e");
		text = text.replace("g", "l");
		text = text.replace(" t", "g");
		text = text.replace("9", " t");
		text = text.replace("LL ", "9");
		text = text.replace("and", "LL ");
		text = text.replace("x", "and");
		text = text.replace("S ", "x");
		text = text.replace("8", "S ");
		text = text.replace(" e", "8");
		text = text.replace(" RE", " e");
		text = text.replace("M", " RE");
		text = text.replace("X", "M");
		text = text.replace(" HE", "X");
		text = text.replace("5", " HE");
		text = text.replace("t", "5");
		text = text.replace("id", "t");
		text = text.replace("n", "id");
		text = text.replace("E", "n");
		text = text.replace("ing", "E");
		text = text.replace(" Ti", "ing");
		text = text.replace("THE", " Ti");
		text = text.replace("ing ", "THE");
		text = text.replace("ER ", "ing ");
		text = text.replace("L", "ER ");
		text = text.replace("ION ", "L");
		text = text.replace("c", "ION ");
		text = text.replace("S", "c");
		text = text.replace("A", "S");
		text = text.replace("R", "A");
		text = text.replace("Qu", "R");
		text = text.replace("7", "Qu");
		text = text.replace(" th", "7");
		text = text.replace("RE", " th");
		text = text.replace(" I", "RE");
		text = text.replace("U", " I");
		text = text.replace("; ", "U");
		text = text.replace("G", "; ");
		text = text.replace(" Th", "G");
		text = text.replace("v", " Th");
		text = text.replace(" Re", "v");
		text = text.replace("{", " Re");
		text = text.replace("ss ", "{");
		text = text.replace("J", "ss ");
		text = text.replace("N", "J");
		text = text.replace("'", "N");
		text = text.replace("s0", "'");
		text = text.replace(" &quot;", "s0");
		text = text.replace(":", " &quot;");
		text = text.replace("1", ":");
		text = text.replace("s5", "1");
		text = text.replace("rt89", "s5");
		return text;
	}

	@SuppressWarnings("ucd")
	public String b2fDecrypt(String text) {
		text = text.replace("s5", "rt89");
		text = text.replace("1", "s5");
		text = text.replace(":", "1");
		text = text.replace(" &quot;", ":");
		text = text.replace("s0", " &quot;");
		text = text.replace("'", "s0");
		text = text.replace("N", "'");
		text = text.replace("J", "N");
		text = text.replace("ss ", "J");
		text = text.replace("{", "ss ");
		text = text.replace(" Re", "{");
		text = text.replace("v", " Re");
		text = text.replace(" Th", "v");
		text = text.replace("G", " Th");
		text = text.replace("; ", "G");
		text = text.replace("U", "; ");
		text = text.replace(" I", "U");
		text = text.replace("RE", " I");
		text = text.replace(" th", "RE");
		text = text.replace("7", " th");
		text = text.replace("Qu", "7");
		text = text.replace("R", "Qu");
		text = text.replace("A", "R");
		text = text.replace("S", "A");
		text = text.replace("c", "S");
		text = text.replace("ION ", "c");
		text = text.replace("L", "ION ");
		text = text.replace("ER ", "L");
		text = text.replace("ing ", "ER ");
		text = text.replace("THE", "ing ");
		text = text.replace(" Ti", "THE");
		text = text.replace("ing", " Ti");
		text = text.replace("E", "ing");
		text = text.replace("n", "E");
		text = text.replace("id", "n");
		text = text.replace("t", "id");
		text = text.replace("5", "t");
		text = text.replace(" HE", "5");
		text = text.replace("X", " HE");
		text = text.replace("M", "X");
		text = text.replace(" RE", "M");
		text = text.replace(" e", " RE");
		text = text.replace("8", " e");
		text = text.replace("S ", "8");
		text = text.replace("x", "S ");
		text = text.replace("and", "x");
		text = text.replace("LL ", "and");
		text = text.replace("9", "LL ");
		text = text.replace(" t", "9");
		text = text.replace("g", " t");
		text = text.replace("l", "g");
		text = text.replace("e", "l");
		text = text.replace("s t", "e");
		text = text.replace(" In", "s t");
		text = text.replace("_", " In");
		text = text.replace("ING", "_");
		text = text.replace("r", "ING");
		text = text.replace(" RE", "r");
		text = text.replace("m", " RE");
		text = text.replace(" TH", "m");
		text = text.replace("p", " TH");
		text = text.replace("th", "p");
		text = text.replace("ly", "th");
		text = text.replace("s an", "ly");
		text = text.replace("TH ", "s an");
		text = text.replace("i", "TH ");
		text = text.replace("ER", "i");
		text = text.replace(" S", "ER");
		text = text.replace("ewt", " S");
		text = text.replace("FF ", "ewt");
		text = text.replace("B", "FF ");
		text = text.replace("b", "B");
		text = text.replace(")", "b");
		text = text.replace(" s", ")");
		text = text.replace("ff l", " s");
		text = text.replace("C", "ff l");
		text = text.replace("0", "C");
		text = text.replace("li", "0");
		text = text.replace("TT ", "li");
		text = text.replace("H", "TT ");
		text = text.replace("sh", "H");
		text = text.replace("2", "sh");
		text = text.replace("}", "2");
		text = text.replace("AN", "}");
		text = text.replace("Z", "AN");
		text = text.replace("ENT", "Z");
		text = text.replace(" t", "ENT");
		text = text.replace(" d", " t");
		text = text.replace(" E", " d");
		text = text.replace("V", " E");
		text = text.replace("THE", "V");
		text = text.replace("and ", "THE");
		text = text.replace("Thr", "and ");
		text = text.replace("gh", "Thr");
		text = text.replace("SS ", "gh");
		text = text.replace("in ", "SS ");
		text = text.replace("In", "in ");
		text = text.replace("in", "In");
		text = text.replace("<", "in");
		text = text.replace("o", "<");
		text = text.replace("qu", "o");
		text = text.replace(" He", "qu");
		text = text.replace("ENT S", " He");
		text = text.replace("AND ", "ENT S");
		text = text.replace(" An", "AND ");
		text = text.replace("HE", " An");
		text = text.replace("Y", "HE");
		text = text.replace("z", "Y");
		text = text.replace("ll ", "z");
		text = text.replace("d", "ll ");
		text = text.replace("ch", "d");
		text = text.replace("D", "ch");
		text = text.replace("T h", "D");
		text = text.replace(" IN", "T h");
		text = text.replace("er It", " IN");
		text = text.replace("P", "er It");
		text = text.replace("ier", "P");
		text = text.replace("ION", "ier");
		text = text.replace("d ", "ION");
		text = text.replace("T", "d ");
		text = text.replace("est", "T");
		text = text.replace("Then", "est");
		text = text.replace("I", "Then");
		text = text.replace("TH", "I");
		text = text.replace("6", "TH");
		text = text.replace("ss ", "6");
		text = text.replace("y", "ss ");
		text = text.replace("4", "y");
		text = text.replace("an ", "4");
		text = text.replace("tre", "an ");
		text = text.replace("&quot;", "tre");
		text = text.replace("s", "&quot;");
		text = text.replace("3", "s");
		text = text.replace(" he", "3");
		text = text.replace(" S", " he");
		text = text.replace("st", " S");
		text = text.replace("Ch", "st");
		text = text.replace(" a", "Ch");
		text = text.replace(" TH", " a");
		text = text.replace("IN", " TH");
		text = text.replace("(", "IN");
		text = text.replace("k", "(");
		text = text.replace("INK", "k");
		text = text.replace(" AN", "INK");
		text = text.replace("ent", " AN");
		text = text.replace("ING ", "ent");
		text = text.replace("the", "ING ");
		text = text.replace(" in", "the");
		text = text.replace("an", " in");
		text = text.replace(">", "an");
		text = text.replace(" The", ">");
		text = text.replace("ion", " The");
		text = text.replace(". ", "ion");
		text = text.replace("he", ". ");
		text = text.replace("u", "he");
		text = text.replace("? ", "u");
		text = text.replace("thr", "? ");
		text = text.replace("tt ", "thr");
		text = text.replace("w", "tt ");
		text = text.replace(" D", "w");
		text = text.replace("re", " D");
		text = text.replace("CAN I", "re");
		text = text.replace(" A d", "CAN I");
		text = text.replace("AND", " A d");
		text = text.replace(" the", "AND");
		text = text.replace(" HE", " the");
		text = text.replace("THER", " HE");
		text = text.replace("ion ", "THER");
		text = text.replace("K", "ion ");
		text = text.replace("ent ", "K");
		text = text.replace("h", "ent ");
		text = text.replace("er", "h");
		text = text.replace("O", "er");
		text = text.replace("j", "O");
		text = text.replace("th ", "j");
		text = text.replace(": ", "th ");
		text = text.replace("&quot; ", ": ");
		text = text.replace("W", "&quot; ");
		text = text.replace("E ", "W");
		text = text.replace("{bk89686}#!", "E ");
		return text;
	}

	public String decrypt(String encrypted, String key, String initVector) {
		String decrypted = null;
		DataAccess dataAccess = new DataAccess();
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(org.apache.commons.codec.binary.Base64.decodeBase64(encrypted));
			// decrypted = java.net.URLDecoder.decode(new String(original), "UTF-8");
			decrypted = new String(original);
			dataAccess.addLog("decrypted: " + decrypted);
		} catch (Exception ex) {
			dataAccess.addLog("decrypt", ex);
			dataAccess.addLog("val: " + encrypted);
		}
		return decrypted;
	}

	private static String key = null;
	private static String initVector = null;

	private String getSyncKey() {
		if (key == null) {
			try {
				InputStream propFile = getClass().getResourceAsStream("/application.properties");
				if (propFile != null) {
					Properties prop = new Properties();
					prop.load(propFile);
					key = prop.getProperty("sync.key");
					initVector = prop.getProperty("sync.iv");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return key;
	}

	public String decryptKey(String keyStr) {
		String key = getSyncKey();
		String decrypted = null;
		if (key != null && initVector != null) {
			decrypted = this.decrypt(keyStr, key, initVector);
			if (decrypted.length() > 16) {
				decrypted = decrypted.substring(decrypted.length() - 16, decrypted.length());
			}
		}
		return decrypted;
	}

	public String encryptPw(String pw, String salt) {
		return this.oneWayEncrypt(pw, salt);
	}

	private String oneWayEncrypt(String pw, String saltStr) {
		byte[] salt = saltStr.getBytes();
		String generatedPassword = null;
		DataAccess dataAccess = new DataAccess();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-384");
			md.update(salt);
			byte[] bytes = md.digest(pw.getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			dataAccess.addLog("oneWayEncrypt", e);
		}

		dataAccess.addLog("desktop", pw + " = " + generatedPassword, LogConstants.DEBUG);
		return generatedPassword;
	}
}

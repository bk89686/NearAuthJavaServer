package com.humansarehuman.blue2factor.utilities;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.http.util.TextUtils;

import com.google.common.net.InternetDomainName;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

public class GeneralUtilities {
	private static String deviceName = null;
	private static boolean localhostTesting = false;

	public static int estimateDistance(DeviceDbObj device, int rssi) {
		Double interference = 2.35;
		String deviceType = device.getDeviceType().toLowerCase();
		int txPower;
		if (deviceType.contains("iphone")) {
			txPower = -69;
		} else if (deviceType.contains("pixel")) {
			txPower = -57;
		} else if (deviceType.contains("arm64")) {
			txPower = -58;
		} else {
			if (device.getOperatingSystem().toString().contains("WINDOWS")) {
				txPower = -65;
			} else {
				txPower = -59;
			}
		}
		double var1 = txPower - rssi;
		double dist1 = Math.pow(10, ((var1) / (10 * interference)));
		return (int) (Math.round(dist1));
	}

	public static String getEmailDomain(String email) {
		String domain = null;
		String[] emailArr = email.split("@");
		if (emailArr.length == 2) {
			domain = emailArr[1];
		}
		return domain;
	}

	public static String getUidFromEmail(String email) {
		return email.split("@")[0];
	}

	public String getTextFromFile(String fileLoc) throws IOException {
		String text = null;
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileLoc);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			text = sb.toString();
		} finally {
			br.close();
		}
		return text;
	}

	public static boolean isUuid(String uuid) {
		boolean success = false;
		Pattern UUID_REGEX = Pattern
				.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
		if (UUID_REGEX.matcher("26929514-237c-11ed-861d-0242ac120002").matches()) {
			success = true;
		}
		return success;
	}

	public static String getRandomListLetter(String[] list) {
		int rand = randomInt(0, list.length);
		return list[rand];
	}

	public static String getRandomUuid() {
		return UUID.randomUUID().toString().toUpperCase();
	}

	static int randomInt(int min, int max) {
		Random rand = new Random();
		return (rand.nextInt(max - min)) + min;
	}

	static String getAorAn(String text) {
		String aOrAn = "an";
		if (!TextUtils.isBlank(text)) {
			String firstLetter = text.substring(0, 1).toLowerCase();
			String sVowels = "aeiou";
			if ((sVowels.indexOf(firstLetter)) < 0) {
				aOrAn = "a";
			}
		}
		return aOrAn;
	}

	public static int getIndexOfArrayElement(char[] array, char target) {
		int indx = -1;
		for (int i = 0; i < array.length; i++) {
			if ((array[i] == target)) {
				indx = i;
				break;
			}
		}
		return indx;
	}

	public static String addNewLineEveryXCharacters(String originalString, int x) {
		int chunkStart = 0;
		int newLength = originalString.length() + (int) Math.ceil(originalString.length() / 100.0);

		StringBuilder builder = new StringBuilder(newLength);
		while (chunkStart < originalString.length()) {
			int endOfThisChunk = Math.min(chunkStart + 100, originalString.length());
			builder.append(originalString.substring(chunkStart, endOfThisChunk));
			builder.append('\n');
			chunkStart = endOfThisChunk;
		}

		return builder.toString();
	}

	public HttpServletResponse setResponseHeaderWithoutCors(HttpServletResponse httpResponse) {
		DataAccess dataAccess = new DataAccess();
		try {
			httpResponse.setContentType("application/json; charset=utf-8");
			httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
			httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
			httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			if (localhostTesting) {
				httpResponse.setHeader("Access-Control-Allow-Origin", "*");
				httpResponse.setHeader("Allow-Origin", "*");
				dataAccess.addLog("setResponseHeader", "Access-Control-Allow-Origin = *");
			}
			httpResponse.setHeader("Access-Control-Max-Age", "600");
		} catch (Exception e) {
			dataAccess.addLog("setResponseHeader", e);
		}
		return httpResponse;
	}

	public HttpServletResponse setResponseHeader(HttpServletResponse httpResponse, String reqUrl) {
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.addLog("setResponseHeader", "reqUrl: " + reqUrl);
			httpResponse.setContentType("application/json; charset=utf-8");
			httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
			httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
			httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			if (localhostTesting) {
				httpResponse.setHeader("Access-Control-Allow-Origin", "*");
				httpResponse.setHeader("Allow-Origin", "*");
				dataAccess.addLog("setResponseHeader", "Access-Control-Allow-Origin = *");
			} else {
				String formattedUrl = getUrlProtocolAndHost(reqUrl);
				dataAccess.addLog("formattedUrl: " + formattedUrl, LogConstants.TRACE);
				httpResponse.setHeader("Access-Control-Allow-Origin", formattedUrl);
			}
			httpResponse.setHeader("Access-Control-Max-Age", "600");
		} catch (Exception e) {
			dataAccess.addLog("setResponseHeader", e);
		}
		return httpResponse;
	}
	
	public HttpServletResponse setResponseHeaderForOptions(HttpServletResponse httpResponse, String reqUrl) {
		DataAccess dataAccess = new DataAccess();
		try {
			httpResponse.setContentType("application/json; charset=utf-8");
			httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
			httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type");
			httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			String formattedUrl = getUrlProtocolAndHost(reqUrl);
			dataAccess.addLog("formattedUrl: " + formattedUrl, LogConstants.TRACE);
			httpResponse.setHeader("Access-Control-Allow-Origin", formattedUrl);
			httpResponse.setHeader("Access-Control-Max-Age", "600");
		} catch (Exception e) {
			dataAccess.addLog("setResponseHeader", e);
		}
		return httpResponse;
	}

	public static String getClientIp(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		return ipAddress;
	}

	public String readUrl(String urlString) {
		URL url;
		String result = "";
		DataAccess dataAccess = new DataAccess();
		try {
			URI uri = new URI(urlString);
			url = uri.toURL();
			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();
			result = IOUtils.toString(is, StandardCharsets.UTF_8);
		} catch (IOException | URISyntaxException e) {
			dataAccess.addLog("readUrl", e);
		}
		return result;

	}

	public String getMacId() {
		if (deviceName == null) {
			try {
				InetAddress address = InetAddress.getLocalHost();
				NetworkInterface nwi = NetworkInterface.getByInetAddress(address);
				byte mac[] = nwi.getHardwareAddress();
				deviceName = mac.toString();
			} catch (Exception e) {
				new DataAccess().addLog("getMacId", e);
			}
		}
		return deviceName;
	}

	public static void logError(Exception e) {
		logError(e, null);
	}

	private static void logError(Exception e, String errorComment) {
		try {
			if (errorComment != null) {
				localLog(Constants.APP_NAME, errorComment);
			}
			localLog(Constants.APP_NAME, e.getLocalizedMessage());
			localLog(Constants.APP_NAME, stackTraceToString(e));
			e.printStackTrace();
		} catch (Exception e2) {
			localLog(Constants.APP_NAME, "println without message");
		}
	}

	public static String stackTraceToString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	public static String stackTraceToString(StackTraceElement[] ste, int skip) {
		StringWriter sw = new StringWriter();
		for (int i = 0; i < ste.length; i++) {
			sw.append(ste[i].toString());
		}
		return sw.toString();
	}

	private static void localLog(String text, String msg) {
		System.out.println(text);
		System.out.println(msg);
	}

	public static int randInt(int min, int max) {
		Random rand = new Random();
		return rand.nextInt((max - min) + 1) + min;
	}

	public static String randomString() {
		String uuidStr = randomUuid().toString();
		String first3 = uuidStr.substring(0, 3);
		return uuidStr.replaceFirst(first3, "b2f");
	}

	private static UUID randomUuid() {
		return UUID.randomUUID();
	}

	public static String randomString(int len) {
		RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('0', 'z')
				.filteredBy(LETTERS, DIGITS).get();
		String randomLetters = generator.generate(len);
		return randomLetters;
	}

	public static String randomHex() {
		Integer randHexNum = randInt(0, 15);
		return Integer.toHexString(randHexNum);
	}

	public static String randomNumberString(int len) {
		RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('0', '9').filteredBy(DIGITS)
				.get();
		String randomStr = generator.generate(len);
		return randomStr;
	}

	public static String randomLetters(int len) {
		RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('A', 'z').filteredBy(LETTERS)
				.get();

		String randomLetters = generator.generate(len);
		randomLetters.replace("o", "R").replace("O", "w").replace("l", "P").replace("I", "U");
		return randomLetters;
	}

	@SuppressWarnings("ucd")
	public static String randomStringWithSymbols(int len) {
		String codeString = "";
		for (int i = 0; i < len; i++) {
			int currentNum = randInt(32, 126);
			char currentChar = (char) currentNum;
			codeString += currentChar;
		}
		return codeString;
	}

	public static String createApiKey() {
		return GeneralUtilities.randomString(10) + "-" + GeneralUtilities.randomString(10) + "-"
				+ GeneralUtilities.randomString(8) + "-" + GeneralUtilities.randomString(4);
	}

	public static String addCarriageReturnEvery64(String text) {
		String newText = text.replaceAll("(.{64})", "$1\r");
		return newText;
	}

	// like blue2factor.com from https://www.blue2factor.com/example
	public static String getNakedDomain(String urlString) {
		String nakedDomain = null;
		try {
			URI uri = new URI(urlString);
			URL url = uri.toURL();
			String host = url.getHost();
			InternetDomainName idf = InternetDomainName.from(host);
			nakedDomain = idf.topPrivateDomain().toString();
		} catch (Exception e) {
			new DataAccess().addLog("getMainUrlPart", e);
		}
		return nakedDomain;
	}

	// like  https://www.blue2factor.com from blue2factor.com
	public static String getUrlProtocolAndHost(String url) {
		String urlAndHost = null;
		if (!url.startsWith("https://")) {
			url = "https://" + url;
		}
		try {
			new DataAccess().addLog("url: " + url, LogConstants.TRACE);
			URI uri = new URI(url);
			URL aURL = uri.toURL();
			urlAndHost = aURL.getProtocol() + "://" + aURL.getHost();
		} catch (MalformedURLException | URISyntaxException e) {
			new DataAccess().addLog("bad url: " + url, LogConstants.ERROR);
		}
		return urlAndHost;
	}

	public static String getUrlHost(String baseUrl) {
		if (!baseUrl.startsWith("http")) {
			baseUrl = "https://" + baseUrl;
		}
		String host = null;
		try {
			URI uri = new URI(baseUrl);
			URL aURL = uri.toURL();
			host = aURL.getHost();
			if (host.endsWith("/")) {
				host = host.substring(0, host.length() - 1);
			}
		} catch (MalformedURLException | URISyntaxException e) {
			new DataAccess().addLog("bad url: " + baseUrl, LogConstants.ERROR);
		}
		return host;
	}

	public static String getUrlPath(String url) {
		if (!url.startsWith("http")) {
			url = "https://" + url;
		}
		String path = null;
		try {
			URI uri = new URI(url);
			URL aURL = uri.toURL();
			path = aURL.getPath();
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			new DataAccess().addLog("getUrlPath: " + path, LogConstants.TRACE);
		} catch (MalformedURLException | URISyntaxException e) {
			new DataAccess().addLog("bad url: " + url, LogConstants.ERROR);
		}
		return path;
	}
}

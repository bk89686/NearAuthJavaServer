package com.humansarehuman.blue2factor.communication;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class Internet {

	@SuppressWarnings("unused")
	private String getPostParameterString(Map<String, String> parameters) {
		StringBuilder result = new StringBuilder();
		try {
			boolean first = true;
			for (Map.Entry<String, String> parameter : parameters.entrySet()) {
				if (first) {
					first = false;
				} else {
					result.append("&");
				}
				result.append(URLEncoder.encode(parameter.getKey(), StandardCharsets.UTF_8.name()));
				result.append("=");
				result.append(URLEncoder.encode(parameter.getValue(), StandardCharsets.UTF_8.name()));
			}
		} catch (Exception e) {
			GeneralUtilities.logError(e);
		}

		return result.toString();
	}

	public String sendPost(String urlStr, String[] params, int trialNum) throws IOException, InterruptedException {
		InputStream in = null;
		HttpsURLConnection conn = null;
		DataAccess dataAccess = new DataAccess();
		boolean first = true;
		String paramStr = "";
		String result = "";
		for (String param : params) {
			if (first) {
				first = false;
			} else {
				paramStr += "&";
			}
			paramStr += param;
		}
		try {
			URI uri = new URI(urlStr);
			URL url = uri.toURL();
			conn = (HttpsURLConnection) url.openConnection();
			conn.setConnectTimeout(25000);
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("Pragma", "no-cache");
			conn.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.toString());
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type", "multipart/form-data");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");

			OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
			os.write(paramStr);
			os.flush();
			os.close();

			// read the response
			in = new BufferedInputStream(conn.getInputStream());
			result = org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);
			dataAccess.addLog("sendPost", "from " + urlStr + ": " + result, LogConstants.INFO);
		} catch (Exception e) {
			dataAccess.addLog("sendPost", e);
			if (trialNum < 5) {
				Thread.sleep(100);
				result = sendPost(urlStr, params, trialNum + 1);
			}
		}
		if (in != null) {
			in.close();
		}
		if (conn != null) {
			conn.disconnect();
		}

		return result;
	}

	public String sendGet(String urlStr, int trialNum) throws InterruptedException, IOException {
		InputStream in = null;
		HttpsURLConnection conn = null;
		DataAccess dataAccess = new DataAccess();
		String result = "";
		try {
			URI uri = new URI(urlStr);
			URL url = uri.toURL();
			conn = (HttpsURLConnection) url.openConnection();
			conn.setConnectTimeout(25000);
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("Pragma", "no-cache");
			conn.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.toString());
			conn.setUseCaches(false);
			conn.setRequestMethod("GET");
			int responseCode = conn.getResponseCode();
			dataAccess.addLog("sendGet", "responseCode: " + responseCode);
			// read the response
			in = new BufferedInputStream(conn.getInputStream());
			result = org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);
			dataAccess.addLog("sendGet", "from " + urlStr + ": " + result, LogConstants.INFO);
		} catch (Exception e) {
			dataAccess.addLog("sendGet", e);
			if (trialNum < 5) {
				Thread.sleep(100);
				result = sendGet(urlStr, trialNum + 1);
			}
		}
		if (in != null) {
			in.close();
		}
		if (conn != null) {
			conn.disconnect();
		}

		return result;
	}

//	private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
//	private static final String[] SCOPES = { MESSAGING_SCOPE };
	private static final String FB_JSON_PATH = "/etc/blue2factor-backend/blue2factor-firebase-adminsdk-mvf5g-79cb3dde89.json";
//
//	private static String getAccessToken() throws IOException {
//		GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new FileInputStream(FB_JSON_PATH))
//				.createScoped(Arrays.asList(SCOPES));
//		googleCredentials.refresh();
//		return googleCredentials.getAccessToken().getTokenValue();
//	}

	private static FirebaseApp firebaseApp;

	public static synchronized FirebaseApp getFirebaseApp() throws IOException {
		if (firebaseApp == null) {
			firebaseApp = initializeFirebase();
		}
		return firebaseApp;
	}

	private static FirebaseApp initializeFirebase() throws IOException {
//		FileInputStream refreshToken = new FileInputStream(FB_JSON_PATH);
//		FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(refreshToken))
//				.build();
//		DataAccess dataAccess = new DataAccess();
//		dataAccess.addLog("optionsSet");
//		return FirebaseApp.initializeApp(options);

		FileInputStream serviceAccount = new FileInputStream(FB_JSON_PATH);

		FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.setDatabaseUrl("https://blue2factor.firebaseio.com").build();

		return FirebaseApp.initializeApp(options);
	}

	public String sendFcmMessage(Message message, int trialNum)
			throws IOException, JSONException, InterruptedException {
		DataAccess dataAccess = new DataAccess();
		String messageId = null;
//		String errMsg = "";
		try {
			getFirebaseApp();
			// TODO: change this back to async
			messageId = FirebaseMessaging.getInstance().send(message);
//			messageId = FirebaseMessaging.getInstance().sendAsync(message).get();
			dataAccess.addLog("send firebase message response: " + messageId, LogConstants.TRACE);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		if (messageId == null) {
			if (trialNum > 1) {
				dataAccess.addLog("trial number " + trialNum + " failed. Tried to send " + message, LogConstants.ERROR);
			}
			if (trialNum < 3) {
				Thread.sleep(1000);
				messageId = sendFcmMessage(message, trialNum + 1);
			}
		}
		return messageId;
	}

//	public boolean sendFcmJson(JSONObject json, DeviceDbObj device, int trialNum)
//			throws IOException, JSONException, InterruptedException {
//		boolean success = false;
////		InputStream in = null;
////		HttpsURLConnection conn = null;
//		DataAccess dataAccess = new DataAccess();
////		OutputStream os = null;
//		try {
//			String jsonStr = json.toString();
//			Gson gson = new Gson();
//			Map<String, String> jsonMap = gson.fromJson(jsonStr, Map.class);
//			dataAccess.addLog("map created");
//			initializeFirebase();
//			dataAccess.addLog("Firebase initialized");
//			Message message = Message.builder().putAllData(jsonMap).setToken(device.getFcmId()).build();
//			dataAccess.addLog(jsonStr);
//			String response = FirebaseMessaging.getInstance().send(message);
//			dataAccess.addLog("firebase resp: " + response);
//			success = true;
//
////			URL url = new URL(Constants.FCM_URL);
////			conn = (HttpsURLConnection) url.openConnection();
////			conn.setConnectTimeout(25000);
////
////			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
////			initializeFirebase();
////			String token = getAccessToken();
////			dataAccess.addLog("token: " + token);
////			conn.setRequestProperty("Authorization", "Bearer " + token);
////			conn.setDoOutput(true);
////			conn.setDoInput(true);
////			conn.setRequestMethod("POST");
////
////			os = conn.getOutputStream();
////			os.write(jsonStr.getBytes(StandardCharsets.UTF_8));
////			os.flush();
////			os.close();
////
////			// read the response
////			in = new BufferedInputStream(conn.getInputStream());
////			String result = org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);
////			dataAccess.addLog("result: " + result);
////			success = getSuccessFromPushResult(result);
//			dataAccess.addLog("success: " + success);
//		} catch (Exception e) {
//			if (trialNum == 1) {
//				dataAccess.addLog("sendFcmPush", e);
//			} else {
//				dataAccess.addLog("sendFcmPush", "trial number " + trialNum + " failed.");
//			}
////			if (os != null) {
////				os.flush();
////				os.close();
////				if (in != null) {
////					in.close();
////				}
////			}
//			if (trialNum < 5) {
//				Thread.sleep(1000);
//				success = sendFcmJson(json, device, trialNum + 1);
//			}
//		}
////		if (in != null) {
////			in.close();
////		}
////		if (conn != null) {
////			conn.disconnect();
////		}
//
//		return success;
//	}

//	private boolean getSuccessFromPushResult(String result) {
//		boolean success = false;
//		JSONObject json = new JSONObject(result);
//		if (json != null) {
//			int successCount = json.getInt("success");
//			new DataAccess().addLog("getSuccessFromPushResult", "successCount: " + successCount);
//			success = successCount > 0;
//		}
//		return success;
//	}

	public JSONObject sendApiJson(JSONObject json) {
		JSONObject resp = null;
		try {
			resp = sendApi(Constants.API_URL, json, 0);
		} catch (JSONException | IOException | InterruptedException e) {
			new DataAccess().addLog("sendProxJson", e);
		}
		return resp;
	}

	private JSONObject sendApi(String sUrl, JSONObject json, int trialNum)
			throws IOException, JSONException, InterruptedException {
		JSONObject jsonObject = null;
		InputStream in = null;
		HttpsURLConnection conn = null;
		DataAccess dataAccess = new DataAccess();
		try {
			String jsonStr = json.toString();

			dataAccess.addLog("sendProxCheck", jsonStr, LogConstants.INFO);
			URI uri = new URI(sUrl + "?_=" + System.currentTimeMillis());
			URL url = uri.toURL();
			conn = (HttpsURLConnection) url.openConnection();
			conn.setConnectTimeout(25000);
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("Pragma", "no-cache");
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");

			OutputStream os = conn.getOutputStream();
			os.write(jsonStr.getBytes(StandardCharsets.UTF_8));
			os.flush();
			os.close();

			// read the response
			in = new BufferedInputStream(conn.getInputStream());
			String result = org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);
			dataAccess.addLog("sendProxCheck", result, LogConstants.INFO);
			jsonObject = new JSONObject(result);
		} catch (Exception e) {
			dataAccess.addLog("sendProxCheck #" + trialNum, e);
			if (trialNum < 5) {
				Thread.sleep(100);
				sendApi(sUrl, json, trialNum + 1);
			}
		}
		if (in != null) {
			in.close();
		}
		if (conn != null) {
			conn.disconnect();
		}

		return jsonObject;
	}
}

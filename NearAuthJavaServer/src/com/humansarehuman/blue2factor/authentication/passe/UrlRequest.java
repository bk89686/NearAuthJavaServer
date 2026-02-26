package com.humansarehuman.blue2factor.authentication.passe;

public class UrlRequest { // NO_UCD (unused code)

//
//    private String postToServer(String sUrl, Map<String, String> parameters, boolean secondTry) {
//        String response = null;
//        HttpURLConnection conn = null;
//        parameters.put("secondTry", Boolean.toString(secondTry));
//        boolean tryAgain = false;
//        try {
//            URL url = new URL(sUrl);
//            conn = (HttpURLConnection) url.openConnection();
//
//            conn.setReadTimeout(10000);
//            conn.setConnectTimeout(15000);
//            conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//
//            OutputStream os = conn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(
//                    new OutputStreamWriter(os, StandardCharsets.UTF_8));
//            writer.write(getPostParameterString(parameters));
//            writer.flush();
//            writer.close();
//            os.close();
//            conn.connect();
//            response = httpResultToString(conn);
//            // System.out.println("response: " + response);
//        } catch (UnknownHostException uhe) {
//
//            tryAgain = true;
//        } catch (SocketTimeoutException ste) {
//
//            tryAgain = true;
//        } catch (Exception e) {
//
//        } finally {
//            closeConnection(conn);
//        }
//        if (!tryAgain) {
//            tryAgain = true;
//        }
//        if (tryAgain) {
//            if (!secondTry) {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                response = postToServer(sUrl, parameters, true);
//            } else {
//
//            }
//        }
//        return response;
//    }
//
//    private String getPostParameterString(Map<String, String> parameters) {
//        StringBuilder result = new StringBuilder();
//        try {
//            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
//                result.append("&");
//                String paramKey = URLEncoder.encode(parameter.getKey(), "UTF-8");
//                result.append(paramKey);
//                result.append("=");
//                result.append(parameter);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result.toString();
//    }
//
//    private String httpResultToString(HttpURLConnection urlConn) {
//        String response = null;
//        BufferedReader br = null;
//        try {
//            int httpResult = urlConn.getResponseCode();
//            if (httpResult == HttpURLConnection.HTTP_OK) {
//                StringBuilder sb = new StringBuilder();
//                br = new BufferedReader(
//                        new InputStreamReader(urlConn.getInputStream(), StandardCharsets.UTF_8));
//                String line;
//
//                // noinspection NestedAssignment
//                while ((line = br.readLine()) != null) {
//                    sb.append(line.concat("\n"));
//                }
//                response = sb.toString();
//                // System.out.println("response: " + response);
//            } else {
//                System.out.println("httpResultToString: " + urlConn.getResponseMessage());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (br != null) {
//                    br.close();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//        }
//        return response;
//    }
//
//    private void closeConnection(HttpURLConnection connection) {
//        if (connection != null) {
//            connection.disconnect();
//        }
//    }
//
//    public int getOutcome(JSONObject json) {
//        int outcome = -1;
//        try {
//            JSONObject result = json.getJSONObject("result");
//            outcome = result.getInt("outcome");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return outcome;
//    }
//
//    public ValidityResponse intToValidityResponse(int outcome) {
//        ValidityResponse response;
//        switch (outcome) {
//            case 0:
//                response = ValidityResponse.SUCCESS;
//                break;
//            case 1:
//                response = ValidityResponse.F1_INVALID;
//                break;
//            case 2:
//                response = ValidityResponse.F2_INVALID;
//                break;
//            default:
//                response = ValidityResponse.ERROR;
//        }
//        return response;
//    }
//
//    public ValidityResponse verifyTwoFactors(String f1Token, String f2Token, String b2fSession,
//            String coKey) {
//        HashMap<String, String> parameters = new HashMap<>();
//        parameters.put("f1Token", f1Token);
//        parameters.put("f2Token", f2Token);
//        parameters.put("b2fSession", b2fSession);
//        parameters.put("coKey", coKey);
//        String url = B2F_SERVER + "verifyTwoFactors";
//        String urlResponse = postToServer(url, parameters, false);
//        JSONObject json = new JsonUtilities().stringToJson(urlResponse);
//        int outcome = getOutcome(json);
//        return intToValidityResponse(outcome);
//    }
}

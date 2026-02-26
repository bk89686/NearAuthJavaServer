package com.humansarehuman.blue2factor.authentication.passe;

@SuppressWarnings("ucd")
public class Blue2FactorJava {
//
//    /**** change this *****/
//    public final static String COMPANY_KEY = "En8Vp07IruaGsLIHzd";
//
//    public class B2fResponse {
//        int outcome;
//        String completionUrl;
//        String token;
//
//        public B2fResponse(int outcome, String completionUrl) {
//            this.outcome = outcome;
//            this.completionUrl = completionUrl;
//        }
//
//        public B2fResponse(int outcome, String completionUrl, String token) {
//            this.outcome = outcome;
//            this.completionUrl = completionUrl;
//            this.token = token;
//        }
//
//        public int getOutcome() {
//            return outcome;
//        }
//
//        public void setOutcome(int outcome) {
//            this.outcome = outcome;
//        }
//
//        public String getCompletionUrl() {
//            return completionUrl;
//        }
//
//        public void setCompletionUrl(String completionUrl) {
//            this.completionUrl = completionUrl;
//        }
//
//        public String getToken() {
//            if (token == null) {
//                token = "";
//            }
//            return token;
//        }
//
//        public void setToken(String token) {
//            this.token = token;
//        }
//
//    }
//
//    public final int SUCCESS = 1;
//    public final int UNRESPONSIVE = -2;
//    public final int FAILURE = -1;
//    public final int COOKIE_NOT_FOUND = -3;
//    public final int LOUD_PUSH_SENT = -5;
//    private final String endpoint = WEB_ADDRESS + "/b2f-prox";
//
////    public boolean getBlue2FactorSuccess(HttpServletRequest request) {
////        B2fResponse resp = b2fValidated(request);
////        return resp.getOutcome() == SUCCESS;
////    }
//
////    public B2fResponse b2fValidated(HttpServletRequest request) {
////        String referrer = request.getHeader("referer");
////        return b2fValidated(getClientIp(request), getB2fCookie(request), referrer);
////    }
//
//    public static String getClientIp(HttpServletRequest request) {
//        String ipAddress = request.getHeader("X-FORWARDED-FOR");
//        if (ipAddress == null) {
//            ipAddress = request.getRemoteAddr();
//        }
//        return ipAddress;
//    }
//
//    public B2fResponse b2fValidated(String ipAddress, String b2fId, String referrer) {
//        int outcome = FAILURE;
//        String completionUrl = "";
//        String token = "";
//        try {
//            if (b2fId == null || b2fId.equals("")) {
//                outcome = COOKIE_NOT_FOUND;
//            } else {
//                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//                params.add(new BasicNameValuePair("tok", b2fId));
//                params.add(new BasicNameValuePair("uip", ipAddress));
//                params.add(new BasicNameValuePair("coId", Blue2FactorJava.COMPANY_KEY));
//                params.add(new BasicNameValuePair("b2fbe", "bak28"));
//                params.add(new BasicNameValuePair("referrer", referrer));
//                JSONObject jsonResponse = sendRequest(params);
//                if (jsonResponse != null) {
//                    JSONObject jsonOutcome = (JSONObject) jsonResponse.get("result");
//                    if (jsonOutcome != null) {
//                        outcome = ((Long) jsonOutcome.get("outcome")).intValue();
//                        String reason = (String) jsonOutcome.get("reason");
//                        if (outcome != SUCCESS) {
//                            if (reason != null && reason.equals("unresponsive")) {
//                                outcome = UNRESPONSIVE;
//                            }
//                        } else {
//                            token = (String) jsonOutcome.get("token");
//                            completionUrl = reason;
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return new B2fResponse(outcome, completionUrl, token);
//    }
//
//    private JSONObject sendRequest(ArrayList<NameValuePair> params)
//            throws ClientProtocolException, IOException {
//        HttpClient httpclient = HttpClients.createDefault();
//        HttpPost httppost = new HttpPost(endpoint);
//        httppost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
//        HttpResponse response = httpclient.execute(httppost);
//        return responseToJson(response);
//    }
//
//    public JSONObject responseToJson(HttpResponse response) throws IOException {
//        JSONObject obj = null;
//        if (response != null) {
//            BufferedReader rd = new BufferedReader(
//                    new InputStreamReader(response.getEntity().getContent()));
//            StringBuffer printResult = new StringBuffer();
//            String line = "";
//            while ((line = rd.readLine()) != null) {
//                printResult.append(line + "\n");
//            }
//            String result = printResult.toString().replaceAll("\\n", "");
//            JSONParser parser = new JSONParser();
//
//            try {
//                obj = (JSONObject) parser.parse(result);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
//        return obj;
//    }
//
//    public boolean getBooleanRequestValue(HttpServletRequest request, String value) {
//        boolean requestValue = false;
//        String strRequest = getRequestValue(request, value);
//        if (strRequest != null) {
//            requestValue = strRequest.equals("true");
//        }
//        return requestValue;
//    }
//
//    public String getRequestValue(HttpServletRequest request, String value) {
//        String requestValue = "";
//        if (request.getParameter(value) == null) {
//            requestValue = (String) request.getAttribute(value);
//        } else {
//            requestValue = request.getParameter(value).trim();
//        }
//        if (requestValue == null) {
//            requestValue = "";
//        }
//        return requestValue;
//    }

//    public String getB2fCookie(HttpServletRequest request) {
//        String b2fCookieVal = null;
//        Cookie b2fCookie = WebUtils.getCookie(request, "b2fIdb");
//        if (b2fCookie != null) {
//            b2fCookieVal = b2fCookie.getValue();
//        } else {
//            Cookie[] cookies = request.getCookies();
//            if (cookies != null) {
//                for (Cookie cookie : cookies) {
//                    if (cookie.getName().equals("b2fIdb")) {
//                        b2fCookieVal = cookie.getValue();
//                        break;
//                    }
//                }
//            }
//        }
//        return b2fCookieVal;
//    }
}

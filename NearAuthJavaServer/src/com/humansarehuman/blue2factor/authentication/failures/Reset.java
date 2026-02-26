package com.humansarehuman.blue2factor.authentication.failures;

//@CrossOrigin(origins = "*", allowedHeaders = "*")
//@Controller
//@RequestMapping(Urls.RESET_FAILURE)
@SuppressWarnings("ucd")
public class Reset extends Failure {
	/*
	 * @RequestMapping(method = RequestMethod.GET) public String
	 * processResetFailureGet(HttpServletRequest request, HttpServletResponse
	 * httpResponse, ModelMap model,
	 * 
	 * @PathVariable("CompanyID") String apiKey) { String authToken =
	 * getPersistentToken(request); return this.handleReset(request, httpResponse,
	 * model, apiKey, authToken); }
	 * 
	 * @RequestMapping(method = RequestMethod.POST) public String
	 * processResetFailurePost(HttpServletRequest request, HttpServletResponse
	 * httpResponse, ModelMap model,
	 * 
	 * @PathVariable("CompanyID") String apiKey) { String authToken =
	 * getPersistentToken(request); return this.handleReset(request, httpResponse,
	 * model, apiKey, authToken); }
	 * 
	 * public String handleReset(HttpServletRequest request, HttpServletResponse
	 * httpResponse, ModelMap model, String apiKey, String authToken) {
	 * CompanyDataAccess dataAccess = new CompanyDataAccess();
	 * dataAccess.addLog("Reset", "reseting"); String nextPage = "notSignedUp"; try
	 * { IdentityObjectFromServer idObj = this.getIdObj(authToken, apiKey,
	 * dataAccess); String url = this.getRequestValue(request, "url"); request =
	 * setReferrer(request, url); if (idObj != null && idObj.getCompany() != null) {
	 * model.addAttribute("company", idObj.getCompany().getCompanyId()); DeviceDbObj
	 * device = idObj.getDevice(); if (device != null) { if (device.getSignedIn()) {
	 * if (dataAccess.companyMatchesDevice(idObj.getCompany(), device)) { request =
	 * this.clearReferrer(request); if
	 * (dataAccess.urlMatchesCompany(idObj.getCompany(), url)) { if
	 * (dataAccess.isAccessAllowed(device, "handleReset")) {
	 * dataAccess.addLog("access was allowed"); UrlAndModel urlAndModel =
	 * this.addNewToken(model, idObj, dataAccess); dataAccess.addLog("token added");
	 * nextPage = urlAndModel.getUrl(); model = urlAndModel.getModelMap(); } else {
	 * if (!dataAccess.deviceIsTemp(device)) { dataAccess.addLog("Reset",
	 * "not temp browser"); Failure failure = new Failure(); UrlAndModel urlAndModel
	 * = failure.checkForPushOrBiometrics(model, idObj, dataAccess); nextPage =
	 * urlAndModel.getUrl(); model = urlAndModel.getModelMap();
	 * model.addAttribute("fromIdp", false); String successPage = Urls.SECURE_URL +
	 * Urls.SAML_SIGN_IN.replace("{apiKey}", apiKey); dataAccess.addLog("Reset",
	 * "successPage: " + successPage); failure.setTempSession(request, "src",
	 * successPage, 60 * 60 * 12); } else { if (idObj.getBrowser() == null) {
	 * dataAccess.addLog("Reset", "browser was null"); } else if
	 * (idObj.getBrowser().getDescription() == null) { dataAccess.addLog("Reset",
	 * "desc was null"); } else { dataAccess.addLog("Reset", "desc: " +
	 * idObj.getBrowser().getDescription()); } } } } else {
	 * dataAccess.addLog("referrer doesn't match device"); } } else {
	 * dataAccess.addLog("company doesn't match device"); } } else {
	 * dataAccess.addLog("Reset", "not signed in"); new
	 * BrowserInstallation().reSignIn(request, httpResponse, model, idObj,
	 * dataAccess); nextPage = null; } } else { dataAccess.addLog("Reset",
	 * "device was null"); } } else { CompanyDbObj company =
	 * dataAccess.getCompanyByApiKey(apiKey); if (company != null) { // TODO: go to
	 * the SAML page
	 * 
	 * model.addAttribute("company", company.getCompanyId()); } } } catch (Exception
	 * e) { dataAccess.addLog("fuck!"); dataAccess.addLog(e); }
	 * model.addAttribute("environment", Constants.ENVIRONMENT.toString());
	 * dataAccess.addLog("nextPage: " + nextPage); return nextPage; }
	 */
}

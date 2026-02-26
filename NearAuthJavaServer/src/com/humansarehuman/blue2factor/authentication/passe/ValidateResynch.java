package com.humansarehuman.blue2factor.authentication.passe;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.humansarehuman.blue2factor.authentication.BaseController;

@Controller
@RequestMapping("/qbcapbuh234eu")
public class ValidateResynch extends BaseController { // NO_UCD (unused code)

//    @RequestMapping(method = RequestMethod.GET)
//    public String processGet(HttpServletRequest request, ModelMap model) {
//        BasicResponse response = new BasicResponse(Constants.FAILURE, "method not allowed");
//        model = this.addBasicResponse(model, response);
//        return "result";
//    }
//
//    @RequestMapping(method = RequestMethod.POST)
//    public String processPost(HttpServletRequest request, ModelMap model) {
//        int outcome = Constants.FAILURE;
//        String reason = "";
//        DeviceDataAccess dataAccess = new DeviceDataAccess();
//        getVersion(request);
//        String key = getKey(request);
//        String iv = getInitVector(request);
//        String deviceId = this.getEncryptedRequestValue(request, "asnehurlch", key, iv);
//        String computerId = this.getEncryptedRequestValue(request, "ljao98sajnmf", key, iv);
//        String serviceUuid = this.getEncryptedRequestValue(request, "nhdngdbb", key, iv);
//        try {
//            DeviceDbObj central = dataAccess.getDeviceByDeviceId(deviceId);
//            DeviceDbObj peripheral = dataAccess.getDeviceByDeviceId(computerId);
//            if (central != null && peripheral != null) {
//                if (central.getGroupId().equals(peripheral.getGroupId())) {
//                    DeviceConnectionDbObj connection = dataAccess
//                            .getConnectionByServiceUuid(serviceUuid);
//                    if (connection != null) {
//                        outcome = Constants.SUCCESS;
//                        reason = GeneralUtilities.randomString();
//                        dataAccess.updateDevice(central, "validateResync");
//                    } else {
//                        reason = "the service UUID did not match";
//                    }
//                } else {
//                    reason = "devices are not registered to the same user";
//                }
//            } else {
//                reason = "either the centralId or peripheralId was null";
//            }
//        } catch (Exception e) {
//            reason = e.getMessage();
//            dataAccess.addLog(deviceId, "validateResynch", e);
//        }
//        BasicResponse response = new BasicResponse(outcome, reason);
//        model = this.addBasicResponse(model, response);
//        return "result";
//    }
}

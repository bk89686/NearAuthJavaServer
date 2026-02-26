package com.humansarehuman.blue2factor.utilities;

import java.util.ArrayList;

import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.enums.CompanyAllowed2FaMethods;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

public class CompanySettings {

//    public static boolean isPushAllowed(DeviceDbObj device) {
//        boolean allowed = false;
//        CompanyDataAccess dataAccess = new CompanyDataAccess();
//        CompanyDbObj company = dataAccess.getCompanyByGroupId(device.getGroupId());
//        if (company != null) {
//            allowed = isPushAllowed(company);
//        }
//        return allowed;
//    }

//    private static boolean isPushAllowed(CompanyDbObj company) {
//        boolean allowed = false;
//        int acceptedTypes = company.getAcceptedTypes();
//        if (acceptedTypes % 2 == 1) {
//            return true;
//        }
//        return allowed;
//    }

    public static ArrayList<CompanyAllowed2FaMethods> getAllowedTypes(DeviceDbObj device) {
        CompanyDataAccess dataAccess = new CompanyDataAccess();
        ArrayList<CompanyAllowed2FaMethods> authMethods = new ArrayList<>();
        CompanyDbObj company = dataAccess.getCompanyByGroupId(device.getGroupId());
        if (company != null) {
            authMethods = getAllowedTypes(company);
        }
        return authMethods;
    }

    private static ArrayList<CompanyAllowed2FaMethods> getAllowedTypes(CompanyDbObj company) {
        ArrayList<CompanyAllowed2FaMethods> authMethods = new ArrayList<>();
        int acceptedTypes = company.getAcceptedTypes();
        if (acceptedTypes == CompanyAllowed2FaMethods.ALL.getValue()) {
            authMethods.add(CompanyAllowed2FaMethods.NFC);
            authMethods.add(CompanyAllowed2FaMethods.BLE);
            authMethods.add(CompanyAllowed2FaMethods.VOICE);
            authMethods.add(CompanyAllowed2FaMethods.TEXT);
            authMethods.add(CompanyAllowed2FaMethods.PUSH);
        } else {
            if (acceptedTypes > CompanyAllowed2FaMethods.NFC.getValue()) {
                authMethods.add(CompanyAllowed2FaMethods.NFC);
                acceptedTypes -= CompanyAllowed2FaMethods.NFC.getValue();
            }
            if (acceptedTypes > CompanyAllowed2FaMethods.BLE.getValue()) {
                authMethods.add(CompanyAllowed2FaMethods.BLE);
                acceptedTypes -= CompanyAllowed2FaMethods.BLE.getValue();
            }
            if (acceptedTypes > CompanyAllowed2FaMethods.VOICE.getValue()) {
                authMethods.add(CompanyAllowed2FaMethods.VOICE);
                acceptedTypes -= CompanyAllowed2FaMethods.VOICE.getValue();
            }
            if (acceptedTypes > CompanyAllowed2FaMethods.TEXT.getValue()) {
                authMethods.add(CompanyAllowed2FaMethods.TEXT);
                acceptedTypes -= CompanyAllowed2FaMethods.TEXT.getValue();
            }
            if (acceptedTypes > CompanyAllowed2FaMethods.PUSH.getValue()) {
                authMethods.add(CompanyAllowed2FaMethods.PUSH);
                acceptedTypes -= CompanyAllowed2FaMethods.PUSH.getValue();
            }
        }
        return authMethods;
    }
}

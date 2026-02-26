package com.humansarehuman.blue2factor.utilities;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.enums.UserType;

public class Converters {

    public static UserType stringToUserType(String userTypeStr, String src) {
        DataAccess dataAccess = new DataAccess();
        UserType userType = null;
        if (userTypeStr == null) {
            userType = UserType.USER;
        } else {
            dataAccess.addLog("stringToUserType", "userType: " + userTypeStr + ", src: " + src);
            switch (userTypeStr) {
                case "SUPER_ADMIN":
                    userType = UserType.SUPER_ADMIN;
                    break;
                case "ADMIN":
                    userType = UserType.ADMIN;
                    break;
                case "USER":
                    userType = UserType.USER;
                    break;
                case "AUDITOR":
                    userType = UserType.AUDITOR;
                    break;
                case "UPDATE_USERS_AND_SERVERS":
                    userType = UserType.UPDATE_USERS_AND_SERVERS;
                    break;
                case "UPDATE_USERS":
                    userType = UserType.UPDATE_USERS;
                    break;
                case "UPDATE_SERVERS":
                    userType = UserType.UPDATE_SERVERS;
                    break;
                case "ADMIN_VIEWER":
                    userType = UserType.ADMIN_VIEWER;
                    break;
                default:
                    userType = UserType.NONE;
            }
            dataAccess.addLog("stringToUserType", "userType-" + userType.toString());
        }
        return userType;
    }
}

package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class BrowserDbObj {
    private String deviceId;
    private String browserId;
    private Timestamp createDate;
    private Timestamp expireDate;
    private Timestamp lastUpdate;
    private String description;
    private boolean hasFailed;

    public BrowserDbObj(String deviceId, String browserId, Timestamp createDate,
            Timestamp expireDate, Timestamp lastUpdate, String description, Boolean hasFailed) {
        super();
        this.deviceId = deviceId;
        this.browserId = browserId;
        this.createDate = createDate;
        this.expireDate = expireDate;
        this.lastUpdate = lastUpdate;
        this.description = description;
        this.hasFailed = hasFailed;
    }

    public boolean getHasFailed() {
        return hasFailed;
    }

    public void setHasFailed(boolean hasFailed) {
        this.hasFailed = hasFailed;
    }

    public boolean isExpired() {
        boolean expired = expireDate.before(DateTimeUtilities.getCurrentTimestamp());
        new DataAccess().addLog("BrowserDbObj", "isExpired: " + expired);
        return expired;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getBrowserId() {
        return browserId;
    }

    public void setBrowserId(String browserId) {
        this.browserId = browserId;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Timestamp expireDate) {
        this.expireDate = expireDate;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}

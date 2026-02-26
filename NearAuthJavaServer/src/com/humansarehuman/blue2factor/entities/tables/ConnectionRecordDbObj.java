package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

public class ConnectionRecordDbObj {
    Timestamp createDate;
    String connectionId;
    Boolean connected;
    String src;

    public ConnectionRecordDbObj(Timestamp createDate, String connectionId, Boolean connected,
            String src) {
        super();
        this.createDate = createDate;
        this.connectionId = connectionId;
        this.connected = connected;
        this.src = src;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

}

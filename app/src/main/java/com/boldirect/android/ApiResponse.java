package com.boldirect.android;

/**
 * Created by Rashida on 28/05/17.
 */

public class ApiResponse {
    private boolean status;
    private String msg;
    private float apiVersion;
    private String systemStatus;
    private boolean maintenanceMode;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public float getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(float apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }
}

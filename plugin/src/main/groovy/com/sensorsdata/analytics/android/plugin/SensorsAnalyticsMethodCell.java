package com.sensorsdata.analytics.android.plugin;

public class SensorsAnalyticsMethodCell {
    private String interfaces;
    private String methodDesc;
    private int paramStart;
    private int paramCount;

    SensorsAnalyticsMethodCell(String interfaces, String methodDesc, int paramStart, int paramCount) {
        this.interfaces = interfaces;
        this.methodDesc = methodDesc;
        this.paramStart = paramStart;
        this.paramCount = paramCount;
    }

    public String getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(String interfaces) {
        this.interfaces = interfaces;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public int getParamStart() {
        return paramStart;
    }

    public void setParamStart(int paramStart) {
        this.paramStart = paramStart;
    }

    public int getParamCount() {
        return paramCount;
    }

    public void setParamCount(int paramCount) {
        this.paramCount = paramCount;
    }
}

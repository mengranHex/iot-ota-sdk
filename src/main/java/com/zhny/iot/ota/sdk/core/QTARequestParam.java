package com.zhny.iot.ota.sdk.core;

public class QTARequestParam {
    private final String imei;
    private final String version;
    public QTARequestParam(String imei, String version){
        this.imei = imei;
        this.version = version;
    }

    public String getImei() {
        return imei;
    }

    public String getVersion() {
        return version;
    }
}

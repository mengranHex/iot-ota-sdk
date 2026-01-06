package com.zhny.iot.ota.sdk.core;

public class OTARequestParam {
    private final String imei;
    private final String version;
    public OTARequestParam(String imei, String version){
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

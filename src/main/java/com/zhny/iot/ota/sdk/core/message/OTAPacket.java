package com.zhny.iot.ota.sdk.core.message;

import io.netty.util.internal.StringUtil;

public class OTAPacket {
    private String imei;
    private String version;
    public OTAPacket(String str){
        if(StringUtil.isNullOrEmpty( str))
            throw new IllegalArgumentException("str is null or empty");
        String[] arr = str.split(",");
        int len = arr.length;
        if(len < 2)
            throw new IllegalArgumentException("str is invalid");
        imei = arr[0];
        if(arr[len - 1].contains("~||")) {
            version = arr[len - 1].replace("~||", "");
        }else {
            throw new IllegalArgumentException("str not contains '~||' chars");
        }
    }

    public String getImei() {
        return imei;
    }

    public String getVersion() {
        return version;
    }
}

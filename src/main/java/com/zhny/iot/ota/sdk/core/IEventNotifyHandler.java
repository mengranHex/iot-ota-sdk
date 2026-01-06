package com.zhny.iot.ota.sdk.core;

public interface IEventNotifyHandler {
    void onOTAStart(String imeiCode,Long otaId);
    void onOTAProgress(String imeiCode,Long otaId, int progress);
    void onOTAEnd(String imeiCode, Long otaId, boolean isSuccess);
    void onOTAError(String imeiCode,Long otaId, String errorMsg);
}

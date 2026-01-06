package com.zhny.iot.ota.sdk.core;

public interface IFirmwareFileHandler {
    boolean onIsUpgradeFile(OTARequestParam param);
    OTAUpgradeFileResponse onGetUpgradeFile(OTARequestParam param);
}
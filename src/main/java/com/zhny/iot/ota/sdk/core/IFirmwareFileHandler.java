package com.zhny.iot.ota.sdk.core;

import java.io.File;

public interface IFirmwareFileHandler {
    boolean onIsUpgradeFile(String imei);
    File onGetUpgradeFile(String imei);
}
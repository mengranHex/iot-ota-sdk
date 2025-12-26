package com.zhny.iot.ota.sdk.core;

import java.io.File;

public interface IFirmwareFileHandler {
    boolean onIsUpgradeFile(QTARequestParam param);
    File onGetUpgradeFile(QTARequestParam param);
}
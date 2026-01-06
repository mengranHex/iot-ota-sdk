package com.zhny.iot.ota.sdk;

import com.zhny.iot.ota.sdk.core.IEventNotifyHandler;
import com.zhny.iot.ota.sdk.core.IFirmwareFileHandler;
import com.zhny.iot.ota.sdk.core.OTARequestParam;
import com.zhny.iot.ota.sdk.core.OTAUpgradeFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestMain {
    private static YModemServer server;
    private static final Logger logger = LoggerFactory.getLogger(TestMain.class);
    public static void main(String[] args) {
        System.setProperty("io.netty.leakDetection.level", "PARANOID");
        IFirmwareFileHandler handler = new EventNotifyHandler();
        server = YModemServer.getInstance();
        server.getEngine().setEventHandler(handler);
        logger.info("start");
        String leakLevel = System.getProperty("io.netty.leakDetection.level");
        System.out.println("Netty 泄漏检测级别：" + leakLevel); // 输出 PARANOID 则生效
    }
}

class EventNotifyHandler implements IFirmwareFileHandler, IEventNotifyHandler {
    @Override
    public boolean onIsUpgradeFile(OTARequestParam param) {
        return true;
    }

    @Override
    public OTAUpgradeFileResponse onGetUpgradeFile(OTARequestParam param) {
        File tempFile = new File("temp.bin");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(createSampleFirmwareData());
        }catch (IOException e){

        }
        return new OTAUpgradeFileResponse(1L, tempFile);
    }

    private byte[] createSampleFirmwareData() {
        // 创建示例固件数据，实际应用中这将来自文件系统
        int size = 1000*10; // 100KB
        byte[] data = new byte[size];

        // 填充示例数据
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 256);
        }

        return data;
    }
    @Override
    public void onOTAStart(String imeiCode, Long otaId) {

    }

    @Override
    public void onOTAProgress(String imeiCode, Long otaId, int progress) {

    }

    @Override
    public void onOTAEnd(String imeiCode, Long otaId, boolean isSuccess) {

    }

    @Override
    public void onOTAError(String imeiCode, Long otaId, String errorMsg) {

    }
}

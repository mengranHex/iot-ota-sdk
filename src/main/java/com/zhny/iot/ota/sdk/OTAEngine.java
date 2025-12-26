package com.zhny.iot.ota.sdk;

import com.zhny.iot.ota.sdk.core.IEventNotifyHandler;
import com.zhny.iot.ota.sdk.core.IFirmwareFileHandler;
import com.zhny.iot.ota.sdk.core.QTARequestParam;
import com.zhny.iot.ota.sdk.core.message.YModemFramePacket;
import com.zhny.iot.ota.sdk.core.message.*;
import com.zhny.iot.ota.sdk.core.repository.*;
import com.zhny.iot.ota.sdk.model.ChannelDevice;
import com.zhny.iot.ota.sdk.model.ChannelOTADevice;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class OTAEngine {
    private final IChannelRepository<ChannelDevice> repository;
    private static final Logger logger = LoggerFactory.getLogger(OTAEngine.class);
    private IFirmwareFileHandler handler;

    public OTAEngine() {
        repository = MemoryRepositoryFactory.build();
    }

    public void setEventHandler(IFirmwareFileHandler handler) {
        this.handler = handler;
    }

    public void onOTARequest(Channel channel, OTAPacket packet) {
        ChannelDevice otaDevice = repository.getCode(packet.getImei());
        if (otaDevice != null) {
            otaDevice.dispose();
            repository.remove(channel.id());
        }
        if(!this.handler.onIsUpgradeFile(new QTARequestParam(packet.getImei(), packet.getVersion()))) {
            logger.info("device imei: {} upgrade file not found,device forcibly close", packet.getImei());
            channel.writeAndFlush(new YModemFramePacket((byte) YModemPacketType.CAN.getI()));
            channel.close();
            return;
        }
        File file = this.handler.onGetUpgradeFile(new QTARequestParam(packet.getImei(), packet.getVersion()));
        if (file == null || file.length() == 0) {
            logger.info("device imei: {} upgrade file not found,device forcibly close", packet.getImei());
            channel.writeAndFlush(new YModemFramePacket((byte) YModemPacketType.CAN.getI()));
            channel.close();
            return;
        }
        try {
            otaDevice = new ChannelOTADevice(channel, packet.getImei(), file, (IEventNotifyHandler) this.handler);
        } catch (FileNotFoundException e) {
            logger.error("device imei: {} upgrade file error,device forcibly close", packet.getImei());
            channel.writeAndFlush(new YModemPacket((byte) YModemPacketType.CAN.getI()));
            channel.close();
            return;
        }

        repository.put(otaDevice);
        logger.info("device imei: {} start OTA request", packet.getImei());
        otaDevice.onPass();
//        }
    }

    public void onFileInfoStart(Channel channel, YModemFileInfoStartPacket packet) {

        ChannelDevice otaDevice = repository.getKey(channel.id());
        if (otaDevice != null) {
            logger.info("device imei: {} ready receive [upgrade file info]", otaDevice.getKey());
            if (otaDevice instanceof ChannelOTADevice)
                ((ChannelOTADevice) otaDevice).onFileInfo();
            otaDevice.reviceMsgNotify();
        }
    }

    public void onFileInfoAck(Channel channel, YModemFileInfoAckPacket packet) {
        ChannelDevice otaDevice = repository.getKey(channel.id());
        if (otaDevice != null) {
//            logger.info("device imei: {} upgrade file info answer {}", otaDevice.getKey(), packet.getType());
            otaDevice.reviceMsgNotify();
            switch (packet.getType()) {
                case ACK:
                    logger.info("device imei: {} receive [upgrade file info] ACK", otaDevice.getKey());
                    break;
                case NAK:
                    logger.info("device imei: {} receive [upgrade file info] NAN,device resend", otaDevice.getKey());
                    if (otaDevice instanceof ChannelOTADevice)
                        ((ChannelOTADevice) otaDevice).onResend();
                    break;
                case CAN:
                    logger.info("device imei: {} receive [upgrade file info] CAN,device forcibly close", otaDevice.getKey());
                    otaDevice.dispose();
                default:
                    logger.info("device imei: {} unrecognized [upgrade file info] {} ack,device forcibly close", otaDevice.getKey(), packet.getType());
                    otaDevice.dispose();
                    break;
            }
        }
    }

    public void onFileDataStart(Channel channel, YModemFileDataStartPacket packet) throws IOException {
        ChannelDevice otaDevice = repository.getKey(channel.id());
        if (otaDevice != null) {
            logger.info("device imei: {} ready receive [upgrade file data] {}", otaDevice.getKey(), packet.getType());
            if (otaDevice instanceof ChannelOTADevice)
                ((ChannelOTADevice) otaDevice).onFileData();
        }
    }

    public void onFileDataAck(Channel channel, YModemFileDataEotFinalAckPacket packet) throws IOException {
        ChannelDevice otaDevice = repository.getKey(channel.id());
        if (otaDevice != null) {
            if (otaDevice instanceof ChannelOTADevice) {
                ChannelOTADevice ota = (ChannelOTADevice) otaDevice;
                otaDevice.reviceMsgNotify();
                switch (packet.getType()) {
                    case ACK:
                        logger.info("device imei: {} receive [upgrade file data] ACK", otaDevice.getKey());
                        ota.onFileData();
                        break;
                    case NAK:
                        logger.info("device imei: {} receive [upgrade file data] NAN,device resend", otaDevice.getKey());
                        ota.onResend();
                        break;
                    case CAN:
                        logger.info("device imei: {} receive [upgrade file data] CAN,device forcibly close", otaDevice.getKey());
                        otaDevice.dispose();
                        break;
                    case CRC_REQUEST:
                        logger.info("device imei: {} receive [upgrade file data] EOT", otaDevice.getKey());
                        ota.onFileEnd();
                        break;
                    default:
                        logger.info("device imei: {} unrecognized [upgrade file data] {} ack,device forcibly close", otaDevice.getKey(), packet.getType());
                        otaDevice.dispose();
                        break;
                }
            }
        }
    }

    public void onExceptionCaught(Channel channel, Throwable cause) {
        ChannelDevice otaDevice = repository.getKey(channel.id());
        if (otaDevice != null) {
            logger.error("device imei: {} Exception: {}", otaDevice.getKey(), cause.getMessage(), cause);
            otaDevice.dispose();
        } else {
            logger.error("Exception in YModem handler: {}", cause.getMessage(), cause);
            channel.close();
        }
    }

    public void onDisconnect(Channel channel) {
        ChannelDevice otaDevice = repository.getKey(channel.id());
        if (otaDevice != null) {
            logger.info("device imei: {} disconnect", otaDevice.getKey());
            otaDevice.dispose();
            repository.remove(channel.id());
        }
    }

    public void onStop(){
        repository.getAll().forEach(ChannelDevice::dispose);
    }
}

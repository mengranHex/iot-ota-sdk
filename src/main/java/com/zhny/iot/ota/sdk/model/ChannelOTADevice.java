package com.zhny.iot.ota.sdk.model;

import com.zhny.iot.ota.sdk.core.IEventNotifyHandler;
import com.zhny.iot.ota.sdk.core.message.YModemFramePacket;
import com.zhny.iot.ota.sdk.core.message.YModemPacketType;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ChannelOTADevice extends ChannelDevice{
    private final Logger logger = LoggerFactory.getLogger (ChannelOTADevice.class);
    private final File upgradeFile;
    private IEventNotifyHandler notifyHandler;
    private boolean transferStarted = false;
    private final FileInputStream inputStream;
    private int totalPackets =0;
    private int molPackets =0 ;
    private int currentPacket =0;
    private YModemFramePacket executePacket;
    private boolean isTransferEnded = false;
    public ChannelOTADevice(Channel channel, String code, File upgradeFile, IEventNotifyHandler notifyHandler) throws FileNotFoundException {
        super(channel, code);
        this.upgradeFile = upgradeFile;
        this.notifyHandler = notifyHandler;
        inputStream = new FileInputStream(upgradeFile);
        totalPackets = (int) (upgradeFile.length() / 1024);
        molPackets = (int) (upgradeFile.length() % 1024);
    }
    @Override
    public YModemFramePacket loadCommand() {
        return get();
    }

    public void onFileInfo() {
        YModemFramePacket packet = null;
        if(!transferStarted){
            packet = buildFileInfo();
            transferStarted = true;
            executePacket = packet;
        }
        put( packet).exceptionally(throwable -> {
            logger.error("device IMEI [{}],send file info error{}",getKey(),throwable);
            return null;
        });
    }

    public void onFileData() throws IOException {
        if(transferStarted){
            if(!isTransferEnded) {
                YModemFramePacket packet = buildFileData();
                executePacket = packet;
                put(packet).exceptionally(throwable -> {
                    logger.error("device IMEI [{}],send file data error{}",getKey(),throwable);
                    return null;
                });
            }else {
                this.dispose();
            }
        }
    }

    public void onResend() {
        if(this.executePacket != null)
            put(this.executePacket).exceptionally(throwable -> {
                logger.error("device IMEI [{}],resend error{}",getKey(),throwable);
                return null;
            });
    }
    public void onFileEnd() {
        byte[] end = "0 0 0".getBytes();
        YModemFramePacket packet = new YModemFramePacket((byte) YModemPacketType.SOH.getI(),end,0,true,true);
        put(packet).exceptionally(throwable -> {
            logger.error("device IMEI [{}],send file end error{}",getKey(),throwable);
            return null;
        });
    }
    private YModemFramePacket buildFileInfo(){
        StringBuilder fileInfo = new StringBuilder();
        fileInfo.append(upgradeFile.getName()).append('\0');
        fileInfo.append(upgradeFile.length()).append('\0');
        byte[] fileInfoBytes = fileInfo.toString().getBytes();
        return new YModemFramePacket((byte) YModemPacketType.SOH.getI(),fileInfoBytes,0,true,true);
    }

    private YModemFramePacket buildFileData() throws IOException {
        if(inputStream.available() > 0){
            int readLen = Math.min(1024,inputStream.available());
            byte packetType = readLen > 128? (byte) 0x02 : (byte) 0x01;
            byte[] data = new byte[readLen];
            int availableLen = inputStream.read(data);
            YModemFramePacket packet = new YModemFramePacket(packetType,data,currentPacket,false,true);
            currentPacket++;
            return packet;
        }else{
            currentPacket = 0 ;
            isTransferEnded = true;
            return new YModemFramePacket((byte) YModemPacketType.EOT.getI());
        }

    }

    public int getProgress() {
        if (totalPackets == 0) {
            return 0;
        }
        int progress = (int) (((double) currentPacket / (totalPackets + 1)) * 100);

        // 确保进度不会超过100%
        return Math.min(progress, 100);
    }
    @Override
    public void dispose() {
        if(this.notifyHandler != null)
            this.notifyHandler = null;
        try {
            if(inputStream != null)
                inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(executePacket != null) {
            this.executePacket.clear();
            this.executePacket = null;
        }
        super.dispose();
    }


}

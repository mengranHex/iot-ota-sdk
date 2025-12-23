package com.zhny.iot.ota.sdk.core;

import com.zhny.iot.ota.sdk.core.message.YModemPacketType;

import java.util.Arrays;

/**
 * YModem协议帧表示类
 */
public class YModemFramePacket {
    private byte frameType;           // 帧类型 (SOH/STX/EOT等)
    private int sequenceNumber;       // 序列号
    private byte[] data;             // 数据内容
    private boolean isFileInfo = false;
    private boolean hasCrc;          // 是否使用CRC校验

    public YModemFramePacket(byte frameType, byte[] data, int sequenceNumber, boolean isFileInfo, boolean hasCrc) {
        this.frameType = frameType;
        this.data = data;
        this.sequenceNumber = sequenceNumber;
        this.isFileInfo = isFileInfo;
        this.hasCrc = hasCrc;
    }

    public YModemFramePacket(byte frameType) {
        this.frameType = frameType;
        data = new byte[0];
        this.hasCrc = true;
    }

    public byte[] build() {
        if (data == null || data.length == 0)
            return new byte[0];
        int length = frameType == YModemPacketType.SOH.getI() ? 128 : 1024;
        byte[] packet = new byte[length];
        System.arraycopy(data, 0, packet, 0, Math.min(data.length, length));
        for (int i = data.length; i < length; i++) {
            if (!this.isFileInfo)
                packet[i] = (byte) 0x1A;
        }
        return packet;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FrameType: ").append(frameType).append("\n");
        builder.append("SequenceNumber: ").append(sequenceNumber).append("\n");
        builder.append("Data Size: ").append(data==null?0:data.length).append("\n");
        return builder.toString();
    }

    public void clear() {
        if (data != null) {
            Arrays.fill(data, (byte) 0);
        }
    }

    // Getter 和 Setter 方法
    public byte getFrameType() {
        return frameType;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public boolean isHasCrc() {
        return hasCrc;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isFileInfo() {
        return isFileInfo;
    }
}
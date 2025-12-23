package com.zhny.iot.ota.sdk.core.message;


/**
 * YModem数据包类
 */
public class YModemPacket {

    private  YModemPacketType type ;
    public YModemPacket(byte type) {
        if(type ==YModemPacketType.ACK.getI())
            this.type = YModemPacketType.ACK;
        else if(type ==YModemPacketType.NAK.getI())
            this.type = YModemPacketType.NAK;
        else if(type ==YModemPacketType.CAN.getI())
            this.type = YModemPacketType.CAN;
        else if(type == YModemPacketType.EOT.getI())
            this.type = YModemPacketType.EOT;
        else if (type == YModemPacketType.CRC_REQUEST.getI()) {
            this.type = YModemPacketType.CRC_REQUEST;
        }
        else if (type == YModemPacketType.PASS.getI()) {
            this.type = YModemPacketType.PASS;
        }
    }
    public  YModemPacketType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("YModemPacket{type=%s}", type);
    }
}
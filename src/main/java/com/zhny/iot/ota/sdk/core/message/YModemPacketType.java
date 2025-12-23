package com.zhny.iot.ota.sdk.core.message;

/**
 * YModem数据包类型枚举
 */
public enum YModemPacketType {
    SOH(0x01),            // 128字节数据包
    STX(0x02),            // 1024字节数据包
    EOT(0x04),            // 传输结束
    ACK(0x06),            // 确认
    NAK(0x15),            // 否定确认
    CAN(0x18),            // 取消
    PASS(0x19),            // 通过
    CRC_REQUEST(0x43);   // CRC请求 (字符'C')

    private final int i;

     YModemPacketType(int i) {
        this.i = i;
    }

    public int getI() {
        return i;
    }
}
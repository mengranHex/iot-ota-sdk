package com.zhny.iot.ota.sdk.core;

/**
 * YModem协议常量定义
 */
public class YModemConstants {
    // YModem协议控制字符
    public static final byte SOH = 0x01;  // 128-byte packets
    public static final byte STX = 0x02;  // 1024-byte packets
    public static final byte EOT = 0x04;  // End of transmission
    public static final byte ACK = 0x06;  // Acknowledge
    public static final byte NAK = 0x15;  // Negative acknowledge
    public static final byte CAN = 0x18;  // Cancel
    public static final byte CTRL_Z = 0x1A; // Ctrl-Z (EOF on DOS systems)
    public static final byte C = 0x43;

    // 超时和重试参数
    public static final int DEFAULT_TIMEOUT = 30000; // 30秒超时
    public static final int MAX_RETRIES = 3;

    // 数据包大小
    public static final int PACKET_SIZE_128 = 128;
    public static final int PACKET_SIZE_1024 = 1024;
}
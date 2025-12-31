package com.zhny.iot.ota.sdk.handler;

import com.zhny.iot.ota.sdk.core.message.YModemFramePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * YModem协议编码器
 * 将YModem数据包编码为字节数组发送给设备
 */
public class YModemEncoder extends MessageToByteEncoder<YModemFramePacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, YModemFramePacket packet, ByteBuf out) {
        byte[] data = packet.build();
        if(data.length > 0){
            out.writeByte(packet.getFrameType());
            out.writeByte(packet.getSequenceNumber() & 0xFF);
            out.writeByte(~packet.getSequenceNumber() & 0xFF);
            out.writeBytes(data);
            int crc = ymodemCrc16(data, data.length);
            out.writeByte((crc >> 8) & 0xFF);
            out.writeByte(crc & 0xFF);
        }else {
            out.writeByte(packet.getFrameType());
        }
    }

    public static short ymodemCrc16(byte[] data, int length) {
        int crc = 0x0000;  // 初始值固定为0x0000
        int dataIndex = 0;

        while (length-- > 0) {
            // 先将当前字节与CRC高8位异或
            crc ^= (data[dataIndex++] & 0xFF) << 8;

            // 逐位计算（8位）
            for (int i = 0; i < 8; i++) {
                // 若最高位为1，左移后异或多项式；否则仅左移
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }

        // 将结果转换为short类型返回（保持16位值）
        return (short) (crc & 0xFFFF);
    }
    /**
     * 计算CRC16校验和
     */
    private int calculateCRC16(byte[] data, int offset, int length) {
        int crc = 0x0000;
        int polynomial = 0x1021; // CRC-16-CCITT polynomial

        for (int i = offset; i < offset + length; i++) {
            for (int j = 0; j < 8; j++) {
                boolean bit = ((data[i] >> (7 - j) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        crc &= 0xFFFF;
        return crc;
    }
}
package com.zhny.iot.ota.sdk.handler;

import com.zhny.iot.ota.sdk.core.message.YModemFramePacket;
import io.netty.buffer.ByteBuf;
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
            int crc = calculateCRC16( out, 0, out.readableBytes());
            out.writeByte((crc >> 8) & 0xFF);
            out.writeByte(crc & 0xFF);
        }else {
            out.writeByte(packet.getFrameType());
        }
    }
    /**
     * 计算CRC16校验和
     */
    private int calculateCRC16(ByteBuf data, int offset, int length) {
        int crc = 0x0000;
        int polynomial = 0x1021; // CRC-16-CCITT polynomial

        for (int i = offset; i < offset + length; i++) {
            for (int j = 0; j < 8; j++) {
                boolean bit = ((data.getByte( i) >> (7 - j) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }

        crc &= 0xFFFF;
        return crc;
    }
}
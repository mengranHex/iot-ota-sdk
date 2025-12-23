package com.zhny.iot.ota.sdk.handler;

import com.zhny.iot.ota.sdk.core.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * YModem协议解码器
 * 解析从设备接收到的YModem协议数据包
 */
public class YModemDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(YModemDecoder.class);

    private YModemDecoder.DecodeState state = YModemDecoder.DecodeState.OTA_REQUEST;

    // 解码状态枚举
    public enum DecodeState {
        OTA_REQUEST,
        WAITING_FOR_FILE_RECEIVER_START,
        WAITING_FOR_FILE_INFO_ACK,
        WAITING_FOR_FILE_DATA_RECEIVER_START,
        WAITING_FOR_DATA_EOT_FINAL_ACK
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Object decoded = this.decode(ctx, in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    private Object decode(ChannelHandlerContext ctx, ByteBuf input) throws Exception {
        switch (state) {
            case OTA_REQUEST: {
                int len = input.readableBytes();
                int index = input.readerIndex();
                byte[] registerBuf = new byte[len];
                if (len - index > 0) {
                    if (len - index > 100) {
                        input.discardReadBytes();
                        return null;
                    }
                    input.readBytes(registerBuf, 0, len - index);
                    String otaRequest = new String(registerBuf, StandardCharsets.UTF_8);
                    logger.info("IP:[{}];OTA request:[{}]", ctx.channel().remoteAddress(), otaRequest);
                    if (!otaRequest.endsWith("~||")) {
//                        throw new RuntimeException("OTA request not found end chart '~||'");
                        input.readerIndex(index);
                        return null;
                    }
                    try {
                        OTAPacket otaPacket = new OTAPacket(otaRequest);
//                        checkpoint(DecodeState.WAITING_FOR_FILE_RECEIVER_START);
                        state=DecodeState.WAITING_FOR_FILE_RECEIVER_START;
                        return otaPacket;
                    } catch (Exception e) {
                        logger.error("OTA request:[{}],error:[{}]", otaRequest, e.getMessage());
                        input.discardReadBytes();
                    }
                }
                break;
            }
            case WAITING_FOR_FILE_RECEIVER_START: {
                byte b = input.readByte();
                if (isControlCharacter(b)) {
                    if (b == YModemPacketType.CRC_REQUEST.getI()) {
                        state=DecodeState.WAITING_FOR_FILE_INFO_ACK;
                        return new YModemFileInfoStartPacket(b);
                    }
                }
                break;
            }
            case WAITING_FOR_FILE_INFO_ACK: {
                byte b = input.readByte();
                if (isControlCharacter(b)) {
                    if (b == YModemPacketType.ACK.getI() ||
                            b == YModemPacketType.NAK.getI() ||
                            b == YModemPacketType.CAN.getI()) {
                        state=DecodeState.WAITING_FOR_FILE_DATA_RECEIVER_START;
                        return new YModemFileInfoAckPacket(b);
                    }
                }
                break;
            }
            case WAITING_FOR_FILE_DATA_RECEIVER_START: {
                byte b = input.readByte();
                if (isControlCharacter(b)) {
                    if (b == YModemPacketType.CRC_REQUEST.getI()) {
                        state=DecodeState.WAITING_FOR_DATA_EOT_FINAL_ACK;
                        return new YModemFileDataStartPacket(b);
                    }
                }
                break;
            }
            case WAITING_FOR_DATA_EOT_FINAL_ACK: {
                byte b = input.readByte();
                if (isControlCharacter(b)) {
                    if (b == YModemPacketType.ACK.getI() ||
                            b == YModemPacketType.NAK.getI() ||
                            b == YModemPacketType.CAN.getI() ||
                            b == YModemPacketType.CRC_REQUEST.getI()
                    ) {
//                        state(DecodeState.WAITING_FOR_DATA_EOT_FINAL_ACK);
                        return new YModemFileDataEotFinalAckPacket(b);
                    }
                }
            }
        }
        return null;
    }

    private boolean isControlCharacter(byte b) {
        return b == YModemPacketType.EOT.getI() ||
                b == YModemPacketType.ACK.getI() ||
                b == YModemPacketType.NAK.getI() ||
                b == YModemPacketType.CAN.getI() ||
                b == YModemPacketType.CRC_REQUEST.getI(); // 'C' 也是控制字符，表示请求CRC模式
    }
}
package com.zhny.iot.ota.sdk.handler;

import com.zhny.iot.ota.sdk.OTAEngine;
import com.zhny.iot.ota.sdk.core.message.YModemPacket;
import com.zhny.iot.ota.sdk.core.message.YModemPacketType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * YModem协议处理器
 * 处理设备与服务器之间的YModem通信
 */
public class YModemFrameHandler extends SimpleChannelInboundHandler<YModemPacket> {

    private static final Logger logger = LoggerFactory.getLogger(YModemFrameHandler.class);
    private final OTAEngine engine;

    //    private String deviceId;
//    private boolean transferStarted = false;
//    private int expectedSequence = 0;
//    private String fileName;
//    private long fileSize;
//    private long bytesReceived = 0;
    public YModemFrameHandler(OTAEngine engine) {
        this.engine = engine;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 连接刚刚建立时，设备ID尚未确定
        logger.info("New connection established from: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.engine.onExceptionCaught(ctx.channel(), cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        engine.onDisconnect(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, YModemPacket packet) throws Exception {
//        switch (packet.getType()) {
//            case CRC_REQUEST:
//                handleCrcRequest(ctx);
//                break;
//            case ACK:
//                handleAck(ctx, packet);
//                break;
//            case NAK:
//                handleNak(ctx, packet);
//                break;
//            case CAN:
//                handleCancel(ctx, packet);
//                break;
//            case EOT:
//                handleEot(ctx, packet);
//                break;
//            default:
//                logger.warn("Unknown packet type received: {}", packet.getType());
//                break;
//        }

    }
//
//    /**
//     * 处理设备的CRC模式请求
//     */
//    private void handleCrcRequest(ChannelHandlerContext ctx) {
//        logger.info("Received CRC mode request from {}", ctx.channel().remoteAddress());
//
//        // 在实际应用中，设备ID应该在握手过程中确定
//        // 为了示例，这里创建一个临时的设备ID
//        if (deviceId == null) {
//            deviceId = "device_" + ctx.channel().remoteAddress().toString().replace("/", "").replace(":", "_");
//        }
//
//        // 发送文件头信息 - 这里我们使用默认的升级文件
//        sendFileHeader(ctx);
//    }
//
//    /**
//     * 发送文件头信息
//     */
//    private void sendFileHeader(ChannelHandlerContext ctx) {
//        try {
//            // 在实际应用中，这里应该获取待传输的文件信息
//            fileName = "firmware.bin";
//            fileSize = 102400L; // 假设100KB的固件文件
//
//            String fileInfo = fileName + " " + fileSize;
//            byte[] fileInfoBytes = fileInfo.getBytes();
//
//            // 创建文件头数据包 (序列号为0)
//            YModemPacket headerPacket = new YModemPacket(YModemPacketType.FILE_HEADER, fileInfoBytes, 0);
//            ctx.writeAndFlush(headerPacket);
//
//            logger.info("Sent file header to device {}: {} ({} bytes)", deviceId, fileName, fileSize);
//        } catch (Exception e) {
//            logger.error("Error sending file header: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 处理ACK确认
//     */
//    private void handleAck(ChannelHandlerContext ctx, YModemPacket packet) {
//        if (deviceId == null) {
//            logger.debug("Received ACK before device ID is established: seq={}", packet.getSequenceNumber());
//            return;
//        }
//
//        logger.debug("Received ACK from device {} for sequence {}", deviceId, packet.getSequenceNumber());
//
//        if (!transferStarted && packet.getSequenceNumber() == 0) {
//            // 文件头确认，开始发送数据
//            logger.info("File header acknowledged by device {}, starting data transfer", deviceId);
//            transferStarted = true;
//            expectedSequence = 1;
//
//            // 开始发送数据包
//            sendDataPackets(ctx);
//        } else if (transferStarted) {
//            // 数据包确认，继续发送下一个包
//            expectedSequence = (expectedSequence + 1) % 256;
//            logger.debug("Expected sequence now: {}", expectedSequence);
//        }
//    }
//
//    /**
//     * 处理NAK（否定确认）
//     */
//    private void handleNak(ChannelHandlerContext ctx, YModemPacket packet) {
//        if (deviceId == null) {
//            logger.warn("Received NAK from unknown device: seq={}", packet.getSequenceNumber());
//            return;
//        }
//
//        logger.warn("Received NAK from device {}, sequence: {}", deviceId, packet.getSequenceNumber());
//    }
//
//    /**
//     * 处理取消包
//     */
//    private void handleCancel(ChannelHandlerContext ctx, YModemPacket packet) {
//        if (deviceId == null) {
//            logger.warn("Received CAN from unknown device");
//            ctx.close();
//            return;
//        }
//
//        logger.warn("Received CAN from device {}, transfer cancelled", deviceId);
//        // 关闭连接
//        ctx.close();
//    }
//
//    /**
//     * 处理文件头包（在接收模式下）
//     */
//    private void handleFileHeader(ChannelHandlerContext ctx, YModemPacket packet) {
//        if (deviceId == null) {
//            logger.info("Received file header from unknown device: seq={}", packet.getSequenceNumber());
//            // 这通常是设备发起的上传请求，需要设备ID
//            return;
//        }
//
//        logger.info("Received file header from device {}: seq={}", deviceId, packet.getSequenceNumber());
//
//        // 发送ACK确认
//        YModemPacket ackPacket = new YModemPacket(YModemPacketType.ACK, new byte[0], 0);
//        ctx.writeAndFlush(ackPacket);
//
//        // 解析文件信息 (文件名 + 大小)
//        String fileInfo = new String(packet.getData()).trim();
//        String[] parts = fileInfo.split(" ");
//        if (parts.length >= 2) {
//            fileName = parts[0];
//            try {
//                fileSize = Long.parseLong(parts[1]);
//                logger.info("File info: name={}, size={}", fileName, fileSize);
//            } catch (NumberFormatException e) {
//                logger.error("Invalid file size in header: {}", parts[1]);
//            }
//        }
//    }
//
//    /**
//     * 处理数据包
//     */
//    private void handleData(ChannelHandlerContext ctx, YModemPacket packet) {
//        if (deviceId == null) {
//            logger.debug("Received data packet from unknown device: seq={}", packet.getSequenceNumber());
//            return;
//        }
//
//        logger.debug("Received data packet from device {}: seq={}, dataLength={}",
//                deviceId, packet.getSequenceNumber(), packet.getData().length);
//
//        // 发送ACK确认
//        YModemPacket ackPacket = new YModemPacket(YModemPacketType.ACK, new byte[0], 0);
//        ctx.writeAndFlush(ackPacket);
//
//        // 更新进度
//        bytesReceived += packet.getData().length;
//        int progress = (int) ((bytesReceived * 100) / fileSize);
//        logger.debug("Receiving data: {}% for device {}", progress, deviceId);
//    }
//
//    /**
//     * 处理EOT（传输结束）
//     */
//    private void handleEot(ChannelHandlerContext ctx, YModemPacket packet) {
//        if (deviceId == null) {
//            logger.info("Received EOT from unknown device, closing connection");
//            ctx.close();
//            return;
//        }
//
//        logger.info("Received EOT from device {}, transfer completed", deviceId);
//
//        // 发送ACK确认EOT
//        YModemPacket ackPacket = new YModemPacket(YModemPacketType.ACK, new byte[0], 0);
//        ctx.writeAndFlush(ackPacket);
//
//        // 完成传输
//        logger.info("Transfer completed successfully for device {}", deviceId);
//
//        // 关闭连接
//        ctx.close();
//    }
//
//    /**
//     * 发送数据包到设备
//     */
//    private void sendDataPackets(ChannelHandlerContext ctx) {
//        if (deviceId == null) {
//            logger.error("Cannot send data packets: device ID not established");
//            return;
//        }
//
//        try {
//            // 在实际应用中，这里应该从FTP服务获取文件流
//            // 这里我们使用一个示例数据流
//            byte[] sampleData = createSampleFirmwareData();
//            InputStream firmwareStream = new ByteArrayInputStream(sampleData);
//
//            byte[] buffer = new byte[1024];
//            int sequenceNumber = 1;
//            int bytesRead;
//
//            while ((bytesRead = firmwareStream.read(buffer)) != -1) {
//                // 根据数据大小决定使用哪种包类型
//                byte[] packetData = bytesRead == 1024 ? buffer : java.util.Arrays.copyOf(buffer, bytesRead);
//
//                YModemPacket dataPacket = new YModemPacket(YModemPacketType.DATA, packetData, sequenceNumber);
//                ctx.writeAndFlush(dataPacket);
//
//                logger.debug("Sent data packet to device {}: seq={}, size={}",
//                        deviceId, sequenceNumber, bytesRead);
//
//                sequenceNumber = (sequenceNumber + 1) % 256;
//
//                // 简单的流量控制，避免发送过快
//                Thread.sleep(10);
//            }
//
//            // 发送EOT表示传输结束
//            YModemPacket eotPacket = new YModemPacket(YModemPacketType.EOT, new byte[0], 0);
//            ctx.writeAndFlush(eotPacket);
//
//            logger.info("All data packets sent to device {}, sent EOT", deviceId);
//
//        } catch (Exception e) {
//            logger.error("Error sending data packets: {}", e.getMessage(), e);
//        }
//    }

    /**
     * 创建示例固件数据
     */
    private byte[] createSampleFirmwareData() {
        // 创建示例固件数据，实际应用中这将来自文件系统
        int size = 102400; // 100KB
        byte[] data = new byte[size];

        // 填充示例数据
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 256);
        }

        return data;
    }


}
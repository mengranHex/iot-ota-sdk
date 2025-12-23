package com.zhny.iot.ota.sdk.handler;

import com.zhny.iot.ota.sdk.OTAEngine;
import com.zhny.iot.ota.sdk.core.message.YModemFileInfoAckPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YModem协议处理器
 * 处理设备与服务器之间的YModem通信
 */
public class YModemFileInfoAckHandler extends SimpleChannelInboundHandler<YModemFileInfoAckPacket> {

    private static final Logger logger = LoggerFactory.getLogger(YModemFileInfoAckHandler.class);
    private final OTAEngine engine;

    //    private String deviceId;
//    private boolean transferStarted = false;
//    private int expectedSequence = 0;
//    private String fileName;
//    private long fileSize;
//    private long bytesReceived = 0;
    public YModemFileInfoAckHandler(OTAEngine engine) {
        this.engine = engine;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, YModemFileInfoAckPacket packet) throws Exception {
        engine.onFileInfoAck(ctx.channel(), packet);

    }
}
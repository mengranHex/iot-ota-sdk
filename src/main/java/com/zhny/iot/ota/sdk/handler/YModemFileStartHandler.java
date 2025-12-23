package com.zhny.iot.ota.sdk.handler;

import com.zhny.iot.ota.sdk.OTAEngine;
import com.zhny.iot.ota.sdk.core.message.YModemFileInfoStartPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YModem协议处理器
 * 处理设备与服务器之间的YModem通信
 */
public class YModemFileStartHandler extends SimpleChannelInboundHandler<YModemFileInfoStartPacket> {

    private static final Logger logger = LoggerFactory.getLogger(YModemFileStartHandler.class);
    private final OTAEngine engine;

    //    private String deviceId;
//    private boolean transferStarted = false;
//    private int expectedSequence = 0;
//    private String fileName;
//    private long fileSize;
//    private long bytesReceived = 0;
    public YModemFileStartHandler(OTAEngine engine) {
        this.engine = engine;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, YModemFileInfoStartPacket packet) throws Exception {
        engine.onFileInfoStart(ctx.channel(), packet);
    }
}
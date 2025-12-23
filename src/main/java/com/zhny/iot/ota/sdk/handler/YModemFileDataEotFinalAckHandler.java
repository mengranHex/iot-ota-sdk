package com.zhny.iot.ota.sdk.handler;

import com.zhny.iot.ota.sdk.OTAEngine;
import com.zhny.iot.ota.sdk.core.message.YModemFileDataEotFinalAckPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YModem协议处理器
 * 处理设备与服务器之间的YModem通信
 */
public class YModemFileDataEotFinalAckHandler extends SimpleChannelInboundHandler<YModemFileDataEotFinalAckPacket> {

    private static final Logger logger = LoggerFactory.getLogger(YModemFileDataEotFinalAckHandler.class);
    private final OTAEngine engine;
    public YModemFileDataEotFinalAckHandler(OTAEngine engine) {
        this.engine = engine;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
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
    public void channelRead0(ChannelHandlerContext ctx, YModemFileDataEotFinalAckPacket packet) throws Exception {
        engine.onFileDataAck(ctx.channel(), packet);

    }
}
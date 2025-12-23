package com.zhny.iot.ota.sdk.handler;

import com.zhny.iot.ota.sdk.OTAEngine;
import com.zhny.iot.ota.sdk.core.message.OTAPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OTARequestHandler extends SimpleChannelInboundHandler<OTAPacket> {
    private final OTAEngine otaEngine;

    public OTARequestHandler(OTAEngine otaEngine){
        this.otaEngine = otaEngine;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, OTAPacket otaPacket) throws Exception {
        otaEngine.onOTARequest(channelHandlerContext.channel(),otaPacket);
    }
}

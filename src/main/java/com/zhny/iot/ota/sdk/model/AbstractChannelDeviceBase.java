package com.zhny.iot.ota.sdk.model;

import com.zhny.iot.ota.sdk.core.message.YModemFramePacket;
import io.netty.channel.Channel;
import io.netty.util.internal.StringUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class AbstractChannelDeviceBase {
    private final Channel channel;
    private final String key;
    protected final BlockingQueue<YModemFramePacket> queueMessage = new LinkedBlockingDeque<>(1024);
    public AbstractChannelDeviceBase(Channel channel, String key){

        if(channel == null)
            throw new NullPointerException ("Channel is Null !");
        if(StringUtil.isNullOrEmpty (key))
            throw new NullPointerException ("key is Null !");
        this.channel = channel;
        this.key = key;
    }

    public String getKey() {
        return key;
    }


    public Channel getChannel() {
        return channel;
    }

    public void dispose(){
        if(channel.isActive ()){
            channel.close ();
        }
        queueMessage.clear();
    }


}

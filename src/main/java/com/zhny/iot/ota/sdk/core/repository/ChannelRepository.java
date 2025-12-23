package com.zhny.iot.ota.sdk.core.repository;

import com.zhny.iot.ota.sdk.model.ChannelDevice;
import io.netty.channel.ChannelId;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelRepository<T extends ChannelDevice> implements IChannelRepository<T> {
    private ConcurrentHashMap<ChannelId,T> channelMap = new ConcurrentHashMap<> ();

    @Override
    public synchronized void put(T channelDevice) {
        ChannelId key = channelDevice.getChannel ().id ();
        String codeKey = channelDevice.getKey();
        T chDevice = getCode(codeKey) ;
        if(chDevice != null) {
            remove(chDevice);
        }
        channelMap.put (key,channelDevice);
//        if(isExsit (key)){
//            remove (key);
//        }

    }

    @Override
    public boolean remove(T channelDevice) {
        return remove (channelDevice.getChannel ().id ());
    }

    @Override
    public synchronized boolean remove(ChannelId key) {
        if(isExsit (key)){
            ChannelDevice device = channelMap.remove (key);
            device.dispose ();
            return true;
        }
        return false;
    }

    @Override
    public boolean isExsit(ChannelId key) {
        return channelMap.containsKey (key);
    }

    @Override
    public T getCode(String code) {
        T cd = null;
        for (T channelDevice : channelMap.values ()) {
            if(StringUtils.equals ( channelDevice.getKey () , code)){
                cd = channelDevice;
                break;
            }
        }
        return cd;
    }

    @Override
    public T getKey(ChannelId key) {
        return channelMap.get (key);
    }


    @Override
    public int size() {
        return channelMap.size ();
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<> ( channelMap.values ());
    }
}

package com.zhny.iot.ota.sdk.core.repository;

import com.zhny.iot.ota.sdk.model.AbstractChannelDeviceBase;
import io.netty.channel.ChannelId;

import java.util.List;

public interface IChannelRepository<T extends AbstractChannelDeviceBase> {
    void put(T channelDevice);

    boolean remove(T channelDevice);

    boolean remove(ChannelId key);

    boolean isExsit(ChannelId key);

    T getCode(String code);

    T getKey(ChannelId key);

    int size();

    List<T> getAll();

}

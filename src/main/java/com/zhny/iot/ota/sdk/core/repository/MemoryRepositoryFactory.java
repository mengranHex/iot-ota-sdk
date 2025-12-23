package com.zhny.iot.ota.sdk.core.repository;


import com.zhny.iot.ota.sdk.model.ChannelDevice;

public final class MemoryRepositoryFactory {
    public static <T extends ChannelDevice> IChannelRepository<T> build(){
        return new ChannelRepository<>();
    }
}

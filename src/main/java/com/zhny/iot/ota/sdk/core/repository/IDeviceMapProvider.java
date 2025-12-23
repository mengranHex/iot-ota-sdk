package com.zhny.iot.ota.sdk.core.repository;

import com.zhny.iot.ota.sdk.model.Device;

import java.util.List;

public interface IDeviceMapProvider<T extends Device> {
    void put(T device);

    void remove(String key);

    T get(String key);

    List<T> getAllToCode(String code);

    void clear();
}

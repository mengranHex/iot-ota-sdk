package com.zhny.iot.ota.sdk.core.repository;


import com.zhny.iot.ota.sdk.model.Device;
import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryDeviceMapProvider<T extends Device> implements IDeviceMapProvider<T> {
    ConcurrentHashMap<String, T> deviceMap = new ConcurrentHashMap<> ();

    @Override
    public synchronized void put(T device) {
        if(device != null) {
            if (deviceMap.containsKey(device.getImei())){
                remove(device.getImei());
            }
            deviceMap.putIfAbsent(device.getImei(), device);
        }
    }

    @Override
    public synchronized void remove(String key) {
        if(deviceMap.containsKey (key))
            deviceMap.remove (key);
    }

    @Override
    public T get(String key) {
        return deviceMap.get (key);
    }

    @Override
    public List<T> getAllToCode(String code) {
        List<T> devices = new ArrayList<> ();
        if(!StringUtil.isNullOrEmpty (code)){
            for (String key : deviceMap.keySet ()) {
                String[] keys = key.split ("_");
                if(keys.length < 1)
                    continue;
                if(keys[0].equals (code)){
                    devices.add (deviceMap.get (key));
                }
            }
        }
        return devices;
    }

    @Override
    public void clear() {
        if(this.deviceMap.size () > 0)
            this.deviceMap.clear ();
    }
}

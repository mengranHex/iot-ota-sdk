package com.zhny.iot.ota.sdk.model;

import com.zhny.iot.ota.sdk.YModemServer;
import com.zhny.iot.ota.sdk.core.IEventNotifyHandler;

import java.io.File;

public class Device {
    private final DeviceEntry entry;
    private final IEventNotifyHandler handler;

    public Device(DeviceEntry entry, IEventNotifyHandler handler){
        this.entry = entry;
        this.handler = handler;
    }

    public  void initialize(){
        YModemServer.getInstance().getEngine().onRegisterDevice( this);
    }

    public File getUpgradeFile(){
        return null;
    }
    public boolean isAllowUpgrade(){
         return true;
    }

    public String getImei() {
        return entry.getImei();
    }

    public DeviceEntry getEntry() {
        return entry;
    }

    public IEventNotifyHandler getHandler() {
        return handler;
    }
}

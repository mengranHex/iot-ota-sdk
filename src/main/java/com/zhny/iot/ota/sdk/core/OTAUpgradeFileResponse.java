package com.zhny.iot.ota.sdk.core;

import java.io.File;

public class OTAUpgradeFileResponse {
    private final Long otaId;
    private final File file;

    public OTAUpgradeFileResponse(Long otaId, File file){
        this.otaId = otaId;
        this.file = file;
    }

    public Long getOtaId() {
        return otaId;
    }

    public File getFile() {
        return file;
    }

//    public void dispose(){
//    	if(file != null){
//    		file.();
//    	}
//    }
}

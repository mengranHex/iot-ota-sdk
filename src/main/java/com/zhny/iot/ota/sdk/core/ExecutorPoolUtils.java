package com.zhny.iot.ota.sdk.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorPoolUtils {
    // 创建专用的业务线程池
    public static final ExecutorService BUSINESS_EXECUTOR =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
}

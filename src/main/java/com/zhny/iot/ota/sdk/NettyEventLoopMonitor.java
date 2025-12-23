package com.zhny.iot.ota.sdk;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NettyEventLoopMonitor {
    private static final Pattern EVENT_LOOP_NAME_PATTERN = Pattern.compile("nioEventLoopGroup-\\d+-\\d+");
    private static final Logger log = LoggerFactory.getLogger(NettyEventLoopMonitor.class);
    // 监控线程池（单线程，避免干扰 IO 线程）
    private final ScheduledExecutorService monitorExecutor;

    // 记录每个 EventLoop 的最近耗时（解码/编码）
    private final Map<EventLoop, Long> eventLoopLastCost = new ConcurrentHashMap<>();

    // 线程 MXBean（获取线程状态）
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    // 监控阈值（可根据业务调整）
    private static final int PENDING_TASKS_THRESHOLD = 10; // 任务队列阈值
    private static final long HANDLER_COST_THRESHOLD = 50; // 解码/编码耗时阈值(ms)
    private static final long MONITOR_INTERVAL = 5; // 监控间隔(秒)

    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;

    public NettyEventLoopMonitor(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this.bossGroup = (NioEventLoopGroup) bossGroup;
        this.workerGroup = (NioEventLoopGroup) workerGroup;
        monitorExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "netty-eventloop-monitor");
            t.setDaemon(true); // 守护线程，不阻塞应用退出
            return t;
        });
    }

    /**
     * 启动监控
     */
    public void start() {
        // 1. 定时监控 EventLoop 任务队列和线程状态
        monitorExecutor.scheduleAtFixedRate(this::monitorEventLoopStatus, 0, MONITOR_INTERVAL, TimeUnit.SECONDS);
        log.info("Netty IO 线程监控已启动，监控间隔：{}秒", MONITOR_INTERVAL);
    }

    /**
     * 停止监控
     */
    public void stop() {
        monitorExecutor.shutdown();
        try {
            if (!monitorExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                monitorExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitorExecutor.shutdownNow();
        }
        log.info("Netty IO 线程监控已停止");
    }

    /**
     * 监控 EventLoop 核心状态（任务队列、线程状态）
     */
    private void monitorEventLoopStatus() {
        // 监控 boss 线程组
        log.info("===== Boss EventLoopGroup 监控 =====");
        monitorSingleGroup("boss", bossGroup);

        // 监控 worker 线程组（重点，处理解码/编码）
        log.info("===== Worker EventLoopGroup 监控 =====");
        monitorSingleGroup("worker", workerGroup);
    }

    /**
     * 监控单个 EventLoopGroup
     *
     * @param groupName 线程组名称（boss/worker）
     * @param group     待监控的线程组
     */
    private void monitorSingleGroup(String groupName, NioEventLoopGroup group) {
        // 1. 获取所有 JVM 线程信息（一次性获取，减少性能损耗）
        ThreadInfo[] allThreadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds());

        // 2. 遍历 EventLoopGroup 中的每个 EventExecutor
        for (EventExecutor executor : group) {
            // 获取 EventLoop 名称（Netty 内置格式：nioEventLoopGroup-3-1）
            String eventLoopName = extractEventLoopName(executor);
            if (!(executor instanceof NioEventLoop))
                continue;
            NioEventLoop eventLoop = (NioEventLoop) executor;
            int pendingTasks = eventLoop.pendingTasks(); // 待执行任务数（核心指标）
            long lastCost = eventLoopLastCost.getOrDefault(eventLoopName, 0L); // 最近处理耗时

            // 3. 匹配 JVM 线程状态
            String threadState = "UNKNOWN";
            if (allThreadInfos != null) {
                for (ThreadInfo threadInfo : allThreadInfos) {
                    if (threadInfo != null && eventLoopName.equals(threadInfo.getThreadName())) {
                        threadState = threadInfo.getThreadState().name();
                        break;
                    }
                }
            }

            // 4. 打印监控日志
            log.info(
                    "[{}] EventLoop: {}, 线程状态: {}, 待执行任务数: {}, 最近处理耗时: {}ms",
                    groupName,
                    eventLoopName,
                    threadState,
                    pendingTasks,
                    lastCost
            );

            // 5. 异常告警
            if (pendingTasks > PENDING_TASKS_THRESHOLD) {
                log.warn(
                        "[告警] {} EventLoop {} 任务队列堆积！待执行任务数: {}, 线程状态: {}",
                        groupName,
                        eventLoopName,
                        pendingTasks,
                        threadState
                );
            }
            if (lastCost > HANDLER_COST_THRESHOLD) {
                log.warn(
                        "[告警] {} EventLoop {} 处理耗时超限！耗时: {}ms (阈值: {}ms)",
                        groupName,
                        eventLoopName,
                        lastCost,
                        HANDLER_COST_THRESHOLD
                );
            }
        }
    }

    private String extractEventLoopName(EventExecutor executor) {
        String toString = executor.toString();
        Matcher matcher = EVENT_LOOP_NAME_PATTERN.matcher(toString);
        if (matcher.find()) {
            return matcher.group();
        }
        return toString.split("@")[0]; // 兜底：取 @ 前的部分
    }

    /**
     * 记录单个 EventLoop 的处理耗时（解码/编码时调用）
     *
     * @param eventLoop 当前处理的 EventLoop
     * @param cost      耗时(ms)
     */
    public void recordHandlerCost(EventLoop eventLoop, long cost) {
        eventLoopLastCost.put(eventLoop, cost);
    }
}

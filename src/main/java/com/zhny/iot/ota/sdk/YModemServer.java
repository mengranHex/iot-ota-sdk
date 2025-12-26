package com.zhny.iot.ota.sdk;

import com.zhny.iot.ota.sdk.handler.*;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * YModem协议服务器
 * 负责监听设备连接并处理YModem协议通信
 */
public class YModemServer extends NettyServer{

//    private static final Logger logger = LoggerFactory.getLogger(YModemServer.class);

    private static YModemServer instance;
    private final OTAEngine engine;

    public static YModemServer getInstance(){
        if(instance == null){
            synchronized (YModemServer.class){
                if(instance == null){
                    instance = new YModemServer(new OTAEngine());
                    instance.start (18080);
                }
            }
        }
        return instance;
    }
    public YModemServer(OTAEngine engine){
        this.engine = engine;
    }
    public  OTAEngine getEngine() {
        return engine;
    }
    @Override
    public ChannelHandler channelInitializer() {
//        EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(
//                Runtime.getRuntime().availableProcessors() * 2
//        );
        return new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new YModemDecoder());
                pipeline.addLast(new YModemEncoder());
                pipeline.addLast(new ReadTimeoutHandler(180));
                pipeline.addLast(new OTARequestHandler(engine));
                pipeline.addLast(new YModemFileStartHandler(engine));
                pipeline.addLast(new YModemFileInfoAckHandler(engine));
                pipeline.addLast(new YModemFileDataStartHandler(engine));
                pipeline.addLast(new YModemFileDataEotFinalAckHandler(engine));
//                pipeline.addLast(new YModemFrameHandler(engine));
            }
        };
    }

    @Override
    public void stop(){
        if(this.engine != null)
            this.engine.onStop();
        super.stop();
    }
}
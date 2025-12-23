package com.zhny.iot.ota.sdk;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public abstract class NettyServer implements IChannelServer {
    private EventLoopGroup boss = new NioEventLoopGroup (Runtime.getRuntime().availableProcessors());
    private EventLoopGroup worker = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
    NettyEventLoopMonitor eventLoopMonitor;
    public NettyServer() {
    }

    private ServerBootstrap createBootstrap() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(this.boss, this.worker);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_RCVBUF, 4096); // 增大服务器接收缓冲区
        bootstrap.childOption(ChannelOption.SO_SNDBUF, 4096); // 增大发送缓冲区

//        // 添加写缓冲区水位控制，防止写操作积压
//        bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
//                new WriteBufferWaterMark (32 * 1024, 64 * 1024));
//        eventLoopMonitor = new NettyEventLoopMonitor(boss, worker);
//        eventLoopMonitor.start();

        return bootstrap;
    }

    public abstract ChannelHandler channelInitializer();

    @Override
    public void start(int port) {
        ServerBootstrap bootstrap = this.createBootstrap();
        bootstrap.childHandler(this.channelInitializer());
        bootstrap.bind(port);
    }

    @Override
    public void stop() {
        if (this.boss != null) {
            this.boss.shutdownGracefully();
        }
        if (this.worker != null) {
            this.worker.shutdownGracefully();
        }
    }
}

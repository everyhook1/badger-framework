/**
 * @(#)NettyClient.java, 6月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.badger.core.bootstrap.entity.Peer;
import org.badger.core.bootstrap.handler.JSONDecoder;
import org.badger.core.bootstrap.handler.JSONEncoder;
import org.badger.core.bootstrap.handler.NettyClientHandler;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liubin01
 */
@Slf4j
public class NettyClient {

    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private Channel channel;

    private static final Map<Peer, List<Channel>> peerChanelMap = new ConcurrentHashMap<>();

    public NettyClient() {
        boolean isEpoll = Epoll.isAvailable();
        int cores = Runtime.getRuntime().availableProcessors();
        this.group = isEpoll ? new EpollEventLoopGroup(10 * cores) : new NioEventLoopGroup(10 * cores);
        this.bootstrap = new Bootstrap()
                .group(group)
                .channel(isEpoll ? EpollSocketChannel.class : NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, 30));
                        pipeline.addLast(new JSONEncoder());
                        pipeline.addLast(new JSONDecoder());
                        pipeline.addLast("handler", new NettyClientHandler());
                    }
                });
        if (isEpoll) {
            bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);
        }
    }

    @PreDestroy
    public void destroy() {
        group.shutdownGracefully();
        closeChannel();
    }

    public void send(Object request) {
        channel.writeAndFlush(request);
    }

    public void connect(String host, int port) {
        // Close the Channel if it's already connected
        if (!isConnected()) {
            closeChannel();
        }
        // Start the client and wait for the connection to be established.
        try {
            this.channel = this.bootstrap.connect(new InetSocketAddress(host, port)).sync().channel();
        } catch (InterruptedException e) {
            log.error("connect ", e);
        }
    }

    public void closeChannel() {
        if (isConnected()) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                log.error("closeChannel ", e);
            }
        }
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen() && this.channel.isActive();
    }
}

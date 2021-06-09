/**
 * @(#)NettyClient.java, 6月 04, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.badger.client.handler.NettyClientHandler;

import java.net.InetSocketAddress;

/**
 * @author liubin01
 */
public class NettyClient {

    private String host;
    private int port;
    private Bootstrap b;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void open() {
        EventLoopGroup group = new NioEventLoopGroup();
        b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)
                            throws Exception {
                        ch.pipeline().addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
                        ch.pipeline().addLast("frameDecoder", new FixedLengthFrameDecoder(80));
                        ch.pipeline().addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    public void connect() throws InterruptedException {
        Channel f = b.connect().sync().channel();
    }
}

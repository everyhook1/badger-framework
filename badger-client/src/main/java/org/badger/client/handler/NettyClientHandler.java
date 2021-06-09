/**
 * @(#)NettyClientHandler.java, 6月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.client.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liubin01
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("{}", msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }
}

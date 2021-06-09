/**
 * @(#)DispatchHandler.java, 6月 04, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.badger.core.bootstrap.NettyServer;
import org.badger.core.bootstrap.entity.RpcRequest;
import org.badger.core.bootstrap.entity.RpcResponse;

/**
 * @author liubin01
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {

    private final NettyServer nettyServer;

    public NettyServerHandler(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object s) throws Exception {
        RpcRequest rpcRequest = JSON.toJavaObject((JSON) s, RpcRequest.class);
        RpcResponse response = new RpcResponse();
        response.setSeqId(rpcRequest.getSeqId());
        response.setBody(nettyServer.dispatch(rpcRequest));
        ctx.writeAndFlush(response);
    }

    public void channelActive(ChannelHandlerContext ctx) {
        log.info("客户端连接成功!" + ctx.channel().remoteAddress());
    }

    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("客户端断开连接!{}", ctx.channel().remoteAddress());
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // server will close channel when server don't receive any request from client util timeout.
        if (evt instanceof IdleStateEvent) {
            Channel channel = ctx.channel();
            log.info("IdleStateEvent triggered, close channel " + channel);
            channel.close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}

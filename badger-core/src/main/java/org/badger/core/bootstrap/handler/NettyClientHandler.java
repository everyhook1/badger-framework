package org.badger.core.bootstrap.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.badger.core.bootstrap.entity.RpcResponse;

import java.net.InetSocketAddress;
import java.util.concurrent.SynchronousQueue;

import static org.badger.core.bootstrap.NettyClient.REQ_MAP;

/**
 * @author liubin01
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    public void channelActive(ChannelHandlerContext ctx) {
        log.info("已连接到RPC服务器.{}", ctx.channel().remoteAddress());
    }

    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        log.info("与RPC服务器断开连接." + address);
        ctx.channel().close();
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channelRead {}", msg);
        RpcResponse response = JSON.parseObject(msg.toString(), RpcResponse.class);
        long requestId = response.getSeqId();
        SynchronousQueue<Object> queue = REQ_MAP.get(requestId);
        queue.put(response);
        REQ_MAP.remove(requestId);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
}

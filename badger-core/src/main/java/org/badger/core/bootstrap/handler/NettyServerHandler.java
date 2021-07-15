package org.badger.core.bootstrap.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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
    public void channelRead0(ChannelHandlerContext ctx, Object s) {
        RpcRequest rpcRequest = JSON.toJavaObject((JSON) s, RpcRequest.class);
        RpcResponse response = new RpcResponse();
        response.setSeqId(rpcRequest.getSeqId());
        try {
            response.setCode(200);
            response.setBody(nettyServer.dispatch(rpcRequest));
        } catch (Exception e) {
            log.error("channelRead0 {} process error", rpcRequest, e);
            response.setCode(500);
            response.setErrMsg("process error");
        }
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
        super.userEventTriggered(ctx, evt);
    }
}

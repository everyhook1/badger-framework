package org.badger.core.bootstrap.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcRequest;
import org.badger.common.api.RpcResponse;
import org.badger.common.api.SpanContext;
import org.badger.core.bootstrap.NettyServer;

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
        RpcRequest rpcRequest = (RpcRequest) s;
        SpanContext.setCurRequest(rpcRequest);
        RpcResponse response = new RpcResponse();
        response.setSeqId(rpcRequest.getSeqId());
        try {
            response.setCode(200);
            response.setBody(nettyServer.dispatch(rpcRequest));
        } catch (Exception e) {
            log.error("channelRead0 {} process error", rpcRequest, e);
            response.setCode(500);
            response.setErrMsg("process error");
        } finally {
            SpanContext.removeRpcRequest();
        }
        ctx.writeAndFlush(response);
    }

    public void channelActive(ChannelHandlerContext ctx) {
        log.info("client connect success!" + ctx.channel().remoteAddress());
    }

    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("client close connect!{}", ctx.channel().remoteAddress());
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
}

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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.badger.core.bootstrap.codec.RpcDecoder;
import org.badger.core.bootstrap.codec.RpcEncoder;
import org.badger.core.bootstrap.codec.serializer.RpcSerializer;
import org.badger.core.bootstrap.codec.serializer.SerializerEnum;
import org.badger.core.bootstrap.entity.Peer;
import org.badger.core.bootstrap.entity.RpcRequest;
import org.badger.core.bootstrap.entity.RpcResponse;
import org.badger.core.bootstrap.handler.NettyClientHandler;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author liubin01
 */
@Slf4j
public class NettyClient {

    private static NettyClient INSTANCE;

    public static NettyClient getInstance() {
        if (INSTANCE == null) {
            synchronized (NettyClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NettyClient();
                }
            }
        }
        return INSTANCE;
    }

    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    public static final Map<Long, SynchronousQueue<Object>> REQ_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Peer> peerMap = new ConcurrentHashMap<>();
    private Set<String> serviceNameSet;
    private static final RpcSerializer rpcSerializer = SerializerEnum.DEFAULT();

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
                        pipeline.addLast("decoder", new RpcDecoder(rpcSerializer, RpcResponse.class));
                        pipeline.addLast("encoder", new RpcEncoder(rpcSerializer, RpcRequest.class));
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
        peerMap.forEach((k, v) -> v.destroy());
    }

    public Object send(RpcRequest request) throws InterruptedException {
        Peer peer;
        if (peerMap.containsKey(request.getServiceName())) {
            peer = peerMap.get(request.getServiceName());
        } else {
            throw new RuntimeException("peer not exist " + request);
        }
        List<Peer.End> ends = peer.getEnds();
        if (ends == null || ends.size() == 0) {
            log.error("{} channel is null", request);
            return null;
        }
        Channel channel = ends.get((int) (request.getSeqId() % ends.size())).getChannel();
        SynchronousQueue<Object> queue = new SynchronousQueue<>();
        REQ_MAP.put(request.getSeqId(), queue);
        channel.writeAndFlush(request);
        if (request.getTimeout() > 0) {
            return queue.poll(request.getTimeout(), TimeUnit.MILLISECONDS);
        }
        return queue.take();
    }

    public void setServiceNameSet(Set<String> serviceNameSet) {
        this.serviceNameSet = serviceNameSet;
    }

    public Set<String> getServiceNameSet() {
        return serviceNameSet;
    }

    public void connectChannel(String serviceName, String host, int port) {
        Peer peer;
        if (peerMap.containsKey(serviceName)) {
            peer = peerMap.get(serviceName);
        } else {
            peer = new Peer(serviceName, bootstrap);
            peerMap.put(serviceName, peer);
        }
        peer.addEnd(host, port);
    }

    public void removeChannel(String serviceName, String host, int port) {
        Peer peer;
        if (peerMap.containsKey(serviceName)) {
            peer = peerMap.get(serviceName);
            peer.removeEnd(host, port);
        }
    }
}

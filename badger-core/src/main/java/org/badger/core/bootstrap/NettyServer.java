package org.badger.core.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.badger.core.bootstrap.codec.RpcDecoder;
import org.badger.core.bootstrap.codec.RpcEncoder;
import org.badger.core.bootstrap.codec.serializer.RpcSerializer;
import org.badger.core.bootstrap.codec.serializer.SerializerEnum;
import org.badger.core.bootstrap.config.NettyServerConfig;
import org.badger.core.bootstrap.entity.RpcRequest;
import org.badger.core.bootstrap.entity.RpcResponse;
import org.badger.core.bootstrap.handler.NettyServerHandler;
import org.springframework.beans.factory.DisposableBean;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author liubin01
 */
@Slf4j
public class NettyServer implements DisposableBean {

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    private final NettyServerConfig config;

    private final AtomicBoolean started = new AtomicBoolean();

    private Channel channel;

    private final Map<String, Object> serviceMap;
    private final Map<Pair<String, String>, Object> servicePairMap;
    private final RpcSerializer rpcSerializer;

    public NettyServer(NettyServerConfig config, Map<String, Object> serviceMap,
                       Map<Pair<String, String>, Object> servicePairMap) {
        this(config, serviceMap, servicePairMap, SerializerEnum.DEFAULT());
    }

    public NettyServer(NettyServerConfig config, Map<String, Object> serviceMap,
                       Map<Pair<String, String>, Object> servicePairMap, RpcSerializer rpcSerializer) {
        this.config = config;
        this.serviceMap = serviceMap;
        this.servicePairMap = servicePairMap;
        this.rpcSerializer = rpcSerializer;
    }

    public Object dispatch(RpcRequest request) throws Exception {
        String className = request.getClzName();
        Object serviceBean;
        if (StringUtils.isEmpty(request.getQualifier())) {
            serviceBean = serviceMap.get(request.getClzName());
        } else {
            serviceBean = servicePairMap.get(ImmutablePair.of(request.getClzName(), request.getQualifier()));
        }

        if (serviceBean != null) {
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = request.getMethod();
            Class<?>[] parameterTypes = request.getArgTypes();
            Object[] parameters = request.getArgs();

            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(serviceBean, parameters);
        } else {
            throw new Exception("未找到服务接口,请检查配置!:" + className + "#" + request.getMethod());
        }
    }

    /**
     * start server, bind port
     */
    public void start() throws Throwable {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        boolean isEpoll = Epoll.isAvailable();

        // get runtime processors for thread-size
        int cores = Runtime.getRuntime().availableProcessors();

        // Check for eventloop-groups
        this.boss = isEpoll ? new EpollEventLoopGroup(2 * cores) : new NioEventLoopGroup(2 * cores);
        this.worker = isEpoll ? new EpollEventLoopGroup(10 * cores) : new NioEventLoopGroup(10 * cores);


        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(boss, worker)
                .channel(isEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.IP_TOS, 24)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, 60));
                        pipeline.addLast("decoder", new RpcDecoder(rpcSerializer, RpcRequest.class));
                        pipeline.addLast("encoder", new RpcEncoder(rpcSerializer, RpcResponse.class));
                        pipeline.addLast("dispatch", new NettyServerHandler(NettyServer.this));
                    }
                });
        if (isEpoll) {
            serverBootstrap
                    .childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
                    .option(EpollChannelOption.TCP_FASTOPEN, 3)
                    .option(EpollChannelOption.SO_REUSEPORT, true);
        }
        try {
            this.channel = serverBootstrap.bind(config.getPort()).sync().channel();
            log.info("netty-server bind localhost: {}", config.getPort());
        } catch (Throwable throwable) {
            log.error("netty-server can not bind localhost:" + config.getPort(), throwable);
            throw throwable;
        }
    }

    public void destroy() {
        log.info("netty-server stopped.");
        if (boss != null) {
            boss.shutdownGracefully();
        }
        if (worker != null) {
            worker.shutdownGracefully();
        }
        channel.close();
    }
}

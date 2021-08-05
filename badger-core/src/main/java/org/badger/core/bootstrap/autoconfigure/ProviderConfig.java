package org.badger.core.bootstrap.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.badger.common.api.RpcProvider;
import org.badger.core.bootstrap.NettyClient;
import org.badger.core.bootstrap.NettyServer;
import org.badger.core.bootstrap.config.NettyServerConfig;
import org.badger.core.bootstrap.config.ZkConfig;
import org.badger.core.bootstrap.util.IpUtil;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author liubin01
 */

@Slf4j
@Configuration
public class ProviderConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnProperty(value = "rpc.serviceName")
    @ConfigurationProperties(prefix = "rpc")
    public NettyServerConfig nettyServerConfig() {
        return new NettyServerConfig();
    }


    @Bean
    @ConditionalOnMissingBean(ZkConfig.class)
    @ConfigurationProperties(prefix = "zk")
    public ZkConfig zkConfig() {
        return new ZkConfig();
    }

    @Bean
    @ConditionalOnBean(ZkConfig.class)
    public CuratorFramework curatorFramework(ZkConfig zkConfig) {
        RetryPolicy policy = new ExponentialBackoffRetry(zkConfig.getSleepMsBetweenRetries(), zkConfig.getMaxRetries());
        CuratorFramework zkClient = CuratorFrameworkFactory
                .builder()
                .connectString(zkConfig.getAddress())
                .retryPolicy(policy)
                .build();
        zkClient.start();
        return zkClient;
    }

    @Bean
    @ConditionalOnBean(CuratorFramework.class)
    public NettyClient nettyClient(CuratorFramework client) {
        NettyClient nettyClient = NettyClient.getInstance();
        Set<String> serviceNameSet = nettyClient.getServiceNameSet();
        for (String serviceName : serviceNameSet) {
            CuratorCache cache = CuratorCache.builder(client, "/" + serviceName).build();
            CuratorCacheListener listener = CuratorCacheListener
                    .builder()
                    .forPathChildrenCache("/" + serviceName, client, (clt, event) -> {
                        log.info("childEvent {} {}", serviceName, event);
                        String[] splits;
                        String host = "";
                        int port = 0;
                        if (event.getData() != null && event.getData().getPath() != null) {
                            splits = event.getData().getPath().split("[/:]");
                            host = splits[2];
                            port = Integer.parseInt(splits[3]);
                        }
                        switch (event.getType()) {
                            case CHILD_ADDED:
                                nettyClient.connectChannel(serviceName, host, port);
                                break;
                            case CHILD_REMOVED:
                                nettyClient.removeChannel(serviceName, host, port);
                                break;
                            default:
                                break;
                        }
                    })
                    .build();
            cache.listenable().addListener(listener);
            cache.start();
        }
        return nettyClient;
    }

    @Bean
    @ConditionalOnBean(value = {NettyServerConfig.class, CuratorFramework.class})
    public NettyServer nettyServer(NettyServerConfig nettyServerConfig, CuratorFramework client) throws Throwable {
        Map<String, Object> objectMap = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        Map<String, Object> serviceMap = new HashMap<>();
        Map<Pair<String, String>, Object> servicePairMap = new HashMap<>();
        objectMap.forEach((k, v) -> {
            Class<?> clazz = v.getClass();
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> inter : interfaces) {
                String interfaceName = inter.getSimpleName();
                serviceMap.put(interfaceName, v);
                servicePairMap.put(ImmutablePair.of(interfaceName, k), v);
            }
        });
        NettyServer nettyServer = new NettyServer(nettyServerConfig, serviceMap, servicePairMap);
        nettyServer.start();
        register(client, nettyServerConfig);
        client.getConnectionStateListenable().addListener((cli, newState) -> {
            if (newState == ConnectionState.RECONNECTED) {
                register(client, nettyServerConfig);
            }
        });
        return nettyServer;
    }

    private void register(CuratorFramework client, NettyServerConfig nettyServerConfig) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                    .forPath(String.format("/%s/%s:%s", nettyServerConfig.getServiceName(), IpUtil.getIpAddress(), nettyServerConfig.getPort()));
        } catch (Exception e) {
            log.error("register error {} ,{}", client, nettyServerConfig, e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}

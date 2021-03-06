package org.badger.core.bootstrap.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.badger.common.api.RpcProvider;
import org.badger.common.api.SpanContext;
import org.badger.common.api.remote.CLIENT;
import org.badger.common.api.remote.SERVER;
import org.badger.core.bootstrap.NettyClient;
import org.badger.core.bootstrap.NettyServer;
import org.badger.core.bootstrap.config.ServerConfig;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    public ServerConfig nettyServerConfig() {
        return new ServerConfig();
    }


    @Bean
    @ConditionalOnMissingBean(ZkConfig.class)
    @ConfigurationProperties(prefix = "zk")
    public ZkConfig zkConfig() {
        return new ZkConfig();
    }

    @Bean
    @ConditionalOnBean(ZkConfig.class)
    public CuratorFramework curatorFramework(ZkConfig zkConfig) throws Exception {
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
    @ConditionalOnBean(value = {ServerConfig.class, CuratorFramework.class})
    public CLIENT nettyClient(ServerConfig serverConfig, CuratorFramework client) {
        SpanContext.setServiceName(serverConfig.getServiceName());
        NettyClient nettyClient = NettyClient.getInstance();
        nettyClient.initServiceListener(client);
        SpanContext.setClient(nettyClient);
        return nettyClient;
    }

    @Bean
    @ConditionalOnBean(value = {ServerConfig.class, CuratorFramework.class})
    public SERVER nettyServer(ServerConfig serverConfig, CuratorFramework client) throws Throwable {
        Map<String, Object> objectMap = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        Map<String, Object> serviceMap = new HashMap<>();
        objectMap.forEach((k, v) -> {
            serviceMap.put(k, v);
            Class<?> clazz = v.getClass();
            Class<?>[] interfaces = clazz.getInterfaces();
            RpcProvider rpcProvider = applicationContext.findAnnotationOnBean(k, RpcProvider.class);
            if (rpcProvider != null && StringUtils.isNotEmpty(rpcProvider.qualifier())) {
                serviceMap.put(rpcProvider.qualifier(), v);
            }
            Arrays.stream(interfaces).forEach(inter -> serviceMap.put(inter.getSimpleName(), v));
        });
        NettyServer nettyServer = new NettyServer(serverConfig, serviceMap);
        nettyServer.start();
        register(client, serverConfig);
        client.getConnectionStateListenable().addListener((cli, newState) -> {
            if (newState == ConnectionState.RECONNECTED) {
                register(client, serverConfig);
            }
        });
        return nettyServer;
    }

    private void register(CuratorFramework client, ServerConfig nettyServerConfig) {
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

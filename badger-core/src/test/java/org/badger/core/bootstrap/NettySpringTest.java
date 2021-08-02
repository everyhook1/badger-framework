package org.badger.core.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.badger.core.bootstrap.autoconfigure.EnhanceRpcProxyPostprocessor;
import org.badger.core.bootstrap.autoconfigure.ProviderConfig;
import org.badger.core.bootstrap.config.NettyServerConfig;
import org.badger.core.bootstrap.config.ZkConfig;
import org.badger.core.bootstrap.entity.RpcProvider;
import org.badger.core.bootstrap.entity.RpcProxy;
import org.badger.core.bootstrap.util.IpUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liubin01
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        EnhanceRpcProxyPostprocessor.class,
        ProviderConfig.class,
        NettySpringTest.class,
        EchoImpl.class, SpringUtils.class})
@Configuration
public class NettySpringTest {

    private static int zkPort = 9878;
    private static TestingServer server;
    private static File zkFile;
    private static final String serviceName = "badger-test";

    @BeforeClass
    public static void setUp() throws Exception {
        zkFile = new File("./zkData");
        server = new TestingServer(zkPort, zkFile);
        server.start();
    }

    @AfterClass
    public static void destroy() throws IOException {
        server.close();
        zkFile.delete();
    }

    @RpcProxy(serviceName = serviceName)
    private Echo echo;

    @Autowired
    private SpringUtils springUtils;

    @Bean
    public ZkConfig zkConfig() {
        ZkConfig zkConfig = new ZkConfig();
        zkConfig.setAddress("127.0.0.1:" + zkPort);
        return zkConfig;
    }

    @Bean
    public NettyServerConfig nettyServerConfig() {
        NettyServerConfig config = new NettyServerConfig();
        config.setServiceName(serviceName);
        config.setPort(12345);
        return config;
    }

    @Bean
    @ConditionalOnBean(value = {NettyServerConfig.class, CuratorFramework.class})
    public NettyServer nettyServer(NettyServerConfig nettyServerConfig, CuratorFramework client) throws Throwable {
        Map<String, Object> objectMap = springUtils.getApplicationContext().getBeansWithAnnotation(RpcProvider.class);
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

    @Test
    public void testSelfInvoke() throws Exception {
        Object obj = springUtils.getApplicationContext().getBean("org.badger.core.bootstrap.Echo");
        Method m = Echo.class.getDeclaredMethods()[0];
        Object res = m.invoke(obj, 234, 234);
        Assert.assertTrue(res instanceof Integer);
        Assert.assertEquals(res, 234 * 234);
    }
}

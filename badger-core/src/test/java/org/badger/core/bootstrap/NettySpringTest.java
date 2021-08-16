package org.badger.core.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.test.TestingServer;
import org.badger.common.api.RpcProxy;
import org.badger.core.bootstrap.autoconfigure.EnhanceRpcProxyPostprocessor;
import org.badger.core.bootstrap.autoconfigure.ProviderConfig;
import org.badger.core.bootstrap.config.ServerConfig;
import org.badger.core.bootstrap.config.ZkConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

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

    private static final int zkPort = 9878;
    private static TestingServer server;
    private static File zkFile;
    private static final String serviceName = "badger-test";

    @BeforeClass
    public static void setUp() throws Exception {
        zkFile = new File("./zkData");
        server = new TestingServer(zkPort, zkFile);
        server.start();
        System.setProperty("rpc.serviceName", serviceName);
        System.setProperty("rpc.port", "12345");
        System.setProperty("zk.address", "127.0.0.1:" + zkPort);
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
    public ServerConfig nettyServerConfig() {
        ServerConfig config = new ServerConfig();
        config.setServiceName(serviceName);
        config.setPort(12345);
        return config;
    }

    @Test
    public void testSelfInvoke() throws Exception {
        Object obj = springUtils.getApplicationContext().getBean("org.badger.core.bootstrap.Echo");
        Method m = Echo.class.getDeclaredMethods()[0];
        Thread.sleep(1000);
        Object res = m.invoke(obj, 234, 234);
        Assert.assertTrue(res instanceof Integer);
        Assert.assertEquals(res, 234 * 234);
    }
}

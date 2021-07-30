/**
 * @(#)ServerClientTest.java, 7月 31, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap;

import com.google.common.collect.ImmutableMap;
import org.badger.core.bootstrap.codec.serializer.RpcSerializer;
import org.badger.core.bootstrap.codec.serializer.SerializerEnum;
import org.badger.core.bootstrap.config.NettyServerConfig;
import org.badger.core.bootstrap.entity.RpcRequest;
import org.badger.core.bootstrap.entity.RpcResponse;
import org.badger.core.bootstrap.util.SnowflakeIdWorker;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author liubin01
 */
public class ServerClientTest {

    @Test
    public void testWithFstSerializer() throws Throwable {
        deltaWithSerializer(SerializerEnum.FST.getRpcSerializer());
    }

    @Test
    public void testWithHessianSerializer() throws Throwable {
        deltaWithSerializer(SerializerEnum.HESSIAN.getRpcSerializer());
    }

    @Test
    public void testWithHessian2Serializer() throws Throwable {
        deltaWithSerializer(SerializerEnum.HESSIAN2.getRpcSerializer());
    }

    @Test
    public void testWithJDKSerializer() throws Throwable {
        deltaWithSerializer(SerializerEnum.JDK.getRpcSerializer());
    }

    @Test
    public void testWithKryoSerializer() throws Throwable {
        deltaWithSerializer(SerializerEnum.KRYO.getRpcSerializer());
    }

    public void deltaWithSerializer(RpcSerializer rpcSerializer) throws Throwable {
        NettyClient nettyClient;
        NettyServer nettyServer;
        String serviceName = "server-test";
        int serverPort = new Random().nextInt(30000);
        nettyClient = NettyClient.getInstance(rpcSerializer);
        NettyServerConfig config = new NettyServerConfig(serverPort, serviceName);
        Map<String, Object> serviceMap = ImmutableMap.of("echo", new EchoImpl());
        nettyServer = new NettyServer(config, serviceMap, new HashMap<>(), rpcSerializer);
        nettyServer.start();
        nettyClient.connectChannel(serviceName, "localhost", serverPort);
        RpcRequest request = new RpcRequest();
        long seqId = SnowflakeIdWorker.getId();
        Assert.assertTrue(seqId > 0);
        request.setSeqId(seqId);
        request.setServiceName(serviceName);
        request.setClzName("echo");
        request.setQualifier("");
        request.setMethod("delta");
        Object[] args = {67, 89};
        request.setArgs(args);
        Method[] methods = Echo.class.getDeclaredMethods();
        request.setArgTypes(methods[0].getParameterTypes());
        Object obj = nettyClient.send(request);
        Assert.assertTrue(obj instanceof RpcResponse);
        RpcResponse response = (RpcResponse) obj;
        Assert.assertTrue(response.getBody() instanceof Integer);
        Assert.assertEquals((int) response.getBody(), 67 * 89);
        Assert.assertNotNull(nettyClient);
        Assert.assertNotNull(nettyServer);
        nettyClient.destroy();
        nettyServer.destroy();
    }
}

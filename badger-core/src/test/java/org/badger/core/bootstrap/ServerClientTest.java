package org.badger.core.bootstrap;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcRequest;
import org.badger.common.api.RpcResponse;
import org.badger.core.bootstrap.codec.serializer.RpcSerializer;
import org.badger.core.bootstrap.codec.serializer.SerializerEnum;
import org.badger.core.bootstrap.config.NettyServerConfig;
import org.badger.core.bootstrap.util.SnowflakeIdWorker;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author liubin01
 */
@Slf4j
public class ServerClientTest {

    @Test
    public void testSerializer() throws Throwable {
        StopWatch stopwatch = new StopWatch("testSerializer");
        for (SerializerEnum value : SerializerEnum.values()) {
            stopwatch.start(value.name());
            deltaWithSerializer(value.getRpcSerializer(), 100);
            stopwatch.stop();
        }
        log.info(stopwatch.prettyPrint());
    }

    public void deltaWithSerializer(RpcSerializer rpcSerializer, long times) throws Throwable {
        String serviceName = "server-test";
        int serverPort = new Random().nextInt(30000);
        NettyServerConfig config = new NettyServerConfig(serverPort, serviceName);
        Map<String, Object> serviceMap = ImmutableMap.of("echo", new EchoImpl());
        NettyServer nettyServer = new NettyServer(config, serviceMap, new HashMap<>(), rpcSerializer);
        nettyServer.start();
        NettyClient nettyClient = NettyClient.getInstance(rpcSerializer);
        nettyClient.connectChannel(serviceName, "localhost", serverPort);
        while (times-- > 0) sent(nettyClient, serviceName);
        nettyClient.destroy();
        nettyServer.destroy();
    }

    private void sent(NettyClient nettyClient, String serviceName) throws InterruptedException {
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
    }
}

package org.badger.core.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.badger.core.bootstrap.entity.RpcRequest;
import org.junit.Test;

@Slf4j
public class NettyServerTest {

    public static final int DEFAULT_PORT = 11311;

    @Test
    public void StartClient() throws Throwable {
        NettyClient nettyClient = new NettyClient();
        nettyClient.connect("127.0.0.1", DEFAULT_PORT);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setSeqId(1111L);
        rpcRequest.setClzName("userInfoImpl");
        Class<?>[] argTypes = {String.class};
        rpcRequest.setArgTypes(argTypes);
        Object[] args = {"xxxxx"};
        rpcRequest.setArgs(args);
        rpcRequest.setMethod("echo");
        nettyClient.send(rpcRequest);
        Thread.sleep(22222);
    }

    @Test
    public void f() {
        System.out.println(int[].class);
    }

}
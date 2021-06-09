package org.badger.client;

import junit.framework.TestCase;

public class NettyClientTest extends TestCase {

    public static final int DEFAULT_PORT = 11311;

    public void testAndOpen() throws InterruptedException {
        NettyClient nettyClient = new NettyClient("127.0.0.1", DEFAULT_PORT);
        nettyClient.open();
        nettyClient.connect();
        Thread.sleep(2000);
    }
}
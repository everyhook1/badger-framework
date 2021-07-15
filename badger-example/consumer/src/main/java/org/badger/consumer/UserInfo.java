package org.badger.consumer;

import org.badger.core.bootstrap.entity.RpcProxy;

@RpcProxy(serviceName = "badger-example")
public interface UserInfo {
    String echo(String str);
}

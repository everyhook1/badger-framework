package org.badger.example;

import org.badger.core.bootstrap.entity.RpcProxy;

@RpcProxy
public interface UserInfo {
    String echo(String str);
}

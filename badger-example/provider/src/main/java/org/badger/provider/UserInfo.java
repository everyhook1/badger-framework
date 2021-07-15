package org.badger.provider;
import org.badger.core.bootstrap.entity.RpcProvider;

@RpcProvider
public interface UserInfo {
    String echo(String str);
}

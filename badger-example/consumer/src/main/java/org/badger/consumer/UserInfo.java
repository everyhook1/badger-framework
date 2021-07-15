package org.badger.consumer;

import org.badger.core.bootstrap.entity.RpcProxy;

import java.util.List;
import java.util.Map;

@RpcProxy(serviceName = "badger-example")
public interface UserInfo {

    String echo(String str);

    int sum(int a, int b);

    List<String> getStrings(String str);

    Map<Integer, String> getMap(List<Integer> ids);
}

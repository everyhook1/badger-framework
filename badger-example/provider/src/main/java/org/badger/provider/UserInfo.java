package org.badger.provider;

import org.badger.core.bootstrap.entity.RpcProvider;

import java.util.List;
import java.util.Map;

@RpcProvider
public interface UserInfo {

    String echo(String str);

    int sum(int a, int b);

    List<String> getStrings(String str);

    Map<Integer, String> getMap(List<Integer> ids);
}

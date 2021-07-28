package org.badger.example.api;

import java.util.List;
import java.util.Map;

public interface UserInfo {

    String echo(String str);

    int sum(int a, int b);

    List<String> getStrings(String str);

    Map<Integer, String> getMap(List<Integer> ids);
}

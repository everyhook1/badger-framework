package org.badger.provider;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liubin01
 */
@Service
public class UserInfoImpl implements UserInfo {

    @Override
    public String echo(String str) {
        return String.format("echo from server %s ", str);
    }

    @Override
    public int sum(int a, int b) {
        return a + b;
    }

    @Override
    public List<String> getStrings(String str) {
        List<String> list = new ArrayList<>();
        for (char c : str.toCharArray()) {
            list.add(c + "");
        }
        return list;
    }

    @Override
    public Map<Integer, String> getMap(List<Integer> ids) {
        Map<Integer, String> map = new HashMap<>();
        for (Integer id : ids) {
            map.put(id, id + "");
        }
        return map;
    }
}

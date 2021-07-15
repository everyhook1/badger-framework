package org.badger.provider;

import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@Service
public class UserInfoImpl implements UserInfo {

    @Override
    public String echo(String str) {
        return String.format("echo from server %s ", str);
    }
}

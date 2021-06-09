/**
 * @(#)UserInfoImpl.java, 6月 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.example;

import org.badger.core.bootstrap.entity.RpcProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author liubin01
 */
@RpcProvider
@Service
public class UserInfoImpl implements UserInfo {

    @Override
    public String echo(String str) {
        return String.format("echo from server %s ", str);
    }
}

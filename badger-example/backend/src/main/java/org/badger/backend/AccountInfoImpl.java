package org.badger.backend;

import org.badger.common.api.RpcProvider;
import org.badger.example.api.AccountInfo;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@RpcProvider
@Service
public class AccountInfoImpl implements AccountInfo {

    @Override
    public int delta(int a, int b) {
        return a * b;
    }
}

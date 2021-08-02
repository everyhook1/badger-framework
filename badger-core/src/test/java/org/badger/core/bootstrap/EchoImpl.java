package org.badger.core.bootstrap;

import org.badger.core.bootstrap.entity.RpcProvider;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@Service
@RpcProvider
public class EchoImpl implements Echo {

    @Override
    public int delta(int a, int b) {
        return a * b;
    }
}

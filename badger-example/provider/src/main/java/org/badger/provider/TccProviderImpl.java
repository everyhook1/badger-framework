package org.badger.provider;

import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProvider;
import org.badger.common.api.transaction.Compensable;
import org.badger.example.api.TccProvider;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@RpcProvider(qualifier = "tccProvider")
@Slf4j
@Service
public class TccProviderImpl implements TccProvider {

    @Override
    @Compensable(identifier = "TccProvider", tryMethod = "tryProvider", confirmMethod = "confirmProvider", cancelMethod = "cancelProvider")
    public void tryProvider(int a, int b) {
        log.info("tryProvider {} {}", a, b);
    }

    @Override
    @Compensable(identifier = "TccProvider", tryMethod = "tryProvider", confirmMethod = "confirmProvider", cancelMethod = "cancelProvider")
    public void confirmProvider(int a, int b) {
        log.info("tryProvider {} {}", a, b);
    }

    @Override
    @Compensable(identifier = "TccProvider", tryMethod = "tryProvider", confirmMethod = "confirmProvider", cancelMethod = "cancelProvider")
    public void cancelProvider(int a, int b) {
        log.info("tryProvider {} {}", a, b);
    }
}

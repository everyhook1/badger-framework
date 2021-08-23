package org.badger.backend;

import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProvider;
import org.badger.common.api.transaction.Compensable;
import org.badger.example.api.TccBackend;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@RpcProvider(qualifier = "tccBackend")
@Slf4j
@Service
public class TccBackendImpl implements TccBackend {

    @Override
    @Compensable(identifier = "TccBackend", tryMethod = "tryBackend", confirmMethod = "confirmBackend", cancelMethod = "cancelBackend")
    public void tryBackend(int a, int b) {
        log.info("tryBackend {} {}", a, b);
    }

    @Override
    @Compensable(identifier = "TccBackend", tryMethod = "tryBackend", confirmMethod = "confirmBackend", cancelMethod = "cancelBackend")
    public void confirmBackend(int a, int b) {
        log.info("confirmBackend {} {}", a, b);
    }

    @Override
    @Compensable(identifier = "TccBackend", tryMethod = "tryBackend", confirmMethod = "confirmBackend", cancelMethod = "cancelBackend")
    public void cancelBackend(int a, int b) {
        log.info("cancelBackend {} {}", a, b);
    }
}

package org.badger.consumer.tcc;

import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProxy;
import org.badger.common.api.transaction.Compensable;
import org.badger.example.api.TccBackend;
import org.badger.example.api.TccProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@Slf4j
@Service
public class AccountService {

    @Autowired
    @RpcProxy(serviceName = "badger-backend", qualifier = "tccBackend")
    private TccBackend tccBackend;

    @Autowired
    @RpcProxy(serviceName = "badger-example", qualifier = "tccProvider")
    private TccProvider tccProvider;


    @Compensable(identifier = "account", tryMethod = "tryM", confirmMethod = "confirmM", cancelMethod = "cancelM")
    public void tryM(int a, int b) {
        tccProvider.tryProvider(a, b);
        tccBackend.tryBackend(a, b);
        log.info("tryM {} {}", a, b);
    }

    @Compensable(identifier = "account", tryMethod = "tryM", confirmMethod = "confirmM", cancelMethod = "cancelM")
    public void confirmM(int a, int b) {
        log.info("confirmM {} {}", a, b);
    }

    @Compensable(identifier = "account", tryMethod = "tryM", confirmMethod = "confirmM", cancelMethod = "cancelM")
    public void cancelM(int a, int b) {
        log.info("cancelM {} {}", a, b);
    }

}

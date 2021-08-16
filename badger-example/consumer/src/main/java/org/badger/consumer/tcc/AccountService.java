/**
 * @(#)AccountService.java, 8月 10, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.consumer.tcc;

import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.transaction.Compensable;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@Slf4j
@Service
public class AccountService {

    @Compensable(identifier = "account", tryMethod = "tryM", confirmMethod = "confirmM", cancelMethod = "cancelM")
    public void tryM(int a, int b) {
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

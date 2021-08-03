/**
 * @(#)TransactionInteceptorAutoconfig.java, 8月 02, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.autoconfigure;

import org.badger.tcc.config.TxConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liubin01
 */

@Configuration
public class TransactionInterceptorAutoconfig {

    @Bean
    @ConditionalOnProperty(value = "tx.tcc", havingValue = "true")
    public TxConfig txConfig() {
        return new TxConfig();
    }
}

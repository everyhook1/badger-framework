/**
 * @(#)TransactionConfiguration.java, 8月 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.config;

import org.badger.common.api.remote.CLIENT;
import org.badger.tcc.spring.CompensableManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liubin01
 */
@Configuration
public class TransactionConfiguration {

    @Bean
    @ConditionalOnBean(CLIENT.class)
    public CompensableManager compensableManager(CLIENT client) {
        return new CompensableManager(client);
    }
}

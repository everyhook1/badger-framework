/**
 * @(#)TransactionConfiguration.java, 8月 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.config;

import org.badger.common.api.RpcProxy;
import org.badger.common.api.remote.CLIENT;
import org.badger.tcc.TransactionManager;
import org.badger.tcc.aspect.CompensableAspect;
import org.badger.tcc.spring.CompensableManager;
import org.badger.tcc.spring.TransactionCoordinator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liubin01
 */
@Configuration
@ConditionalOnProperty(value = "tcc.enabled", havingValue = "true")
public class TransactionConfiguration {

    @Autowired
    @RpcProxy(serviceName = "badger-tcc-coordinator")
    private TransactionCoordinator transactionCoordinator;

    @Bean
    @ConditionalOnBean(CLIENT.class)
    public CompensableManager compensableManager(CLIENT client) {
        return new CompensableManager(client);
    }

    @Bean
    @ConditionalOnBean(value = {CompensableManager.class})
    public TransactionManager tccTransactionManager(CompensableManager compensableManager) {
        TransactionManager transactionManager = new TransactionManager();
        transactionManager.setTransactionCoordinator(transactionCoordinator);
        transactionManager.setCompensableManager(compensableManager);
        return transactionManager;
    }

    @Bean
    @ConditionalOnBean(value = {TransactionManager.class})
    public CompensableAspect compensableAspect(TransactionManager transactionManager) {
        CompensableAspect compensableAspect = new CompensableAspect();
        compensableAspect.setTransactionManager(transactionManager);
        return compensableAspect;
    }
}

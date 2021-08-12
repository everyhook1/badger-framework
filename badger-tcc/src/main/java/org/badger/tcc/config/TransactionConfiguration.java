/**
 * @(#)TransactionConfiguration.java, 8月 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.config;

import org.badger.common.api.RpcRequest;
import org.badger.common.api.RpcResponse;
import org.badger.common.api.SpanContext;
import org.badger.common.api.remote.CLIENT;
import org.badger.tcc.TransactionManager;
import org.badger.tcc.spring.CompensableManager;
import org.badger.tcc.spring.TransactionCoordinator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;
import java.util.Arrays;

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

    @Bean
    @ConditionalOnBean(CLIENT.class)
    public TransactionCoordinator transactionCoordinator(CLIENT client) {
        String coordinatorServiceName = "badger-tcc-coordinator";
        client.addListener(coordinatorServiceName);
        Class<?> clazz = TransactionCoordinator.class;
        return (TransactionCoordinator) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                (proxy, method, args) -> {
                    if (Arrays.stream(clazz.getDeclaredMethods()).noneMatch(m -> m.getName().equals(method.getName()))) {
                        return null;
                    }
                    RpcRequest request = new RpcRequest();
                    request.setClzName(clazz.getSimpleName());
                    request.setMethod(method.getName());
                    request.setServiceName(coordinatorServiceName);
                    request.setArgs(args);
                    request.setArgTypes(method.getParameterTypes());
//                    request.setSeqId(SnowflakeIdWorker.getId());
                    request.setParentRpc(SpanContext.getCurRequest());
                    RpcResponse response = (RpcResponse) client.send(request);
                    if (response.getCode() == 500) {
                        throw new Exception(response.getErrMsg());
                    }
                    return response.getBody();
                });
    }

    @Bean
    @ConditionalOnBean(value = {TransactionCoordinator.class, CompensableManager.class})
    public TransactionManager transactionManager(TransactionCoordinator transactionCoordinator, CompensableManager compensableManager) {
        TransactionManager transactionManager = new TransactionManager();
        transactionManager.setTransactionCoordinator(transactionCoordinator);
        transactionManager.setCompensableManager(compensableManager);
        return transactionManager;
    }
}

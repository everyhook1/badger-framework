/**
 * @(#)CompensableAspect.java, 8月 06, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.badger.tcc.Transaction;
import org.badger.tcc.TransactionManager;

/**
 * @author liubin01
 */
@Slf4j
@Aspect
public class CompensableAspect {

    private TransactionManager transactionManager;

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Pointcut("@annotation(org.badger.common.api.transaction.Compensable)")
    public void compensableService() {

    }

    @Around("compensableService()")
    public Object interceptCompensableMethod(ProceedingJoinPoint jp) throws Throwable {
        log.info("start aspect");
        Transaction transaction = transactionManager.begin(jp);
        Object returnValue;
        try {
            try {
                returnValue = jp.proceed(jp.getArgs());
            } catch (Throwable e) {
                transactionManager.rollback(transaction);
                throw e;
            }
            transactionManager.commit(transaction);
        } finally {
            transactionManager.cleanAfterCompletion(transaction);
        }
        log.info("end aspect");
        return returnValue;
    }
}

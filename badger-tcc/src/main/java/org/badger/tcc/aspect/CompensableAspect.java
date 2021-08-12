/**
 * @(#)CompensableAspect.java, 8月 06, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.badger.tcc.TransactionManager;
import org.badger.tcc.entity.Transaction;

/**
 * @author liubin01
 */
@Aspect
public abstract class CompensableAspect {

    private TransactionManager transactionManager;

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Pointcut("@annotation(org.badger.common.api.transaction.Compensable)")
    public void compensableService() {

    }

    @Around("compensableService()")
    public Object interceptCompensableMethod(ProceedingJoinPoint jp) throws Throwable {
        Transaction transaction = transactionManager.begin(jp);
        Object returnValue = null;
        try {
            returnValue = jp.proceed(jp.getArgs());
        } catch (Exception e) {
            transactionManager.rollback(transaction);
        } finally {
            transactionManager.cleanAfterCompletion(transaction);
        }
        transactionManager.commit(transaction);
        return returnValue;
    }
}

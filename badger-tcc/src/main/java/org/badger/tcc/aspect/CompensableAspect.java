
package org.badger.tcc.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
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
        String methodName = ((MethodSignature) jp.getSignature()).getMethod().getName();
        log.info("@@@ start aspect {} @@@", methodName);
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
        log.info("@@@ end aspect {} @@@", methodName);
        return returnValue;
    }
}

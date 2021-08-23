
package org.badger.tcc.spring.impl;

import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProvider;
import org.badger.common.api.SpanContext;
import org.badger.tcc.Participant;
import org.badger.tcc.Transaction;
import org.badger.tcc.TransactionManager;
import org.badger.tcc.entity.CompensableEnum;
import org.badger.tcc.entity.CompensableIdentifier;
import org.badger.tcc.entity.TransactionDTO;
import org.badger.tcc.spring.ResourceManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liubin01
 */
@Slf4j
@Component
@RpcProvider
public class ResourceManagerImpl implements ResourceManager, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final ThreadPoolExecutor THREAD_POOL =
            new ThreadPoolExecutor(32, 32, 0, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>(512));

    @Override
    public void commit(TransactionDTO transactionDTO) {
        THREAD_POOL.submit(() -> {
            try {
                process(transactionDTO, "commit");
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error("commit {} error", transactionDTO, e);
            }
        });
    }

    @Override
    public void rollback(TransactionDTO transactionDTO) {
        THREAD_POOL.submit(() -> {
            try {
                process(transactionDTO, "rollback");
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error("rollback {} error", transactionDTO, e);
            }
        });
    }

    private void process(TransactionDTO transactionDTO, String mName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Transaction transaction = transactionDTO.toTransaction();
        TransactionManager.setTransactionThreadLocal(transaction);
        Participant participant = transaction.getParticipants().get(0);
        transaction.setCurrentParticipant(participant);
        SpanContext.setTransactionContext(participant.getTransactionContext());
        CompensableIdentifier compensableIdentifier = participant.getCompensableIdentifier();
        Object serviceBean = applicationContext.getBean(compensableIdentifier.getBeanName());
        Class<?> serviceClass = serviceBean.getClass();
        String methodName;
        if ("commit".equals(mName)) {
            methodName = compensableIdentifier.getConfirmMethod();
            transaction.setCompensableEnum(CompensableEnum.CONFIRM);
        } else {
            methodName = compensableIdentifier.getCancelMethod();
            transaction.setCompensableEnum(CompensableEnum.CANCEL);
        }
        Class<?>[] parameterTypes = compensableIdentifier.getParameterTypes();
        Object[] parameters = compensableIdentifier.getArgs();

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        method.invoke(serviceBean, parameters);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

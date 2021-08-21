
package org.badger.tcc.spring.impl;

import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProvider;
import org.badger.common.api.SpanContext;
import org.badger.tcc.Participant;
import org.badger.tcc.entity.CompensableIdentifier;
import org.badger.tcc.entity.ParticipantDTO;
import org.badger.tcc.spring.ResourceManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author liubin01
 */
@Slf4j
@Component
@RpcProvider
public class ResourceManagerImpl implements ResourceManager, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Object commit(ParticipantDTO participantDTO) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Participant participant = participantDTO.toParticipant();
        SpanContext.setTransactionContext(participant.getTransactionContext());
        CompensableIdentifier compensableIdentifier = participant.getCompensableIdentifier();
        Object serviceBean = applicationContext.getBean(compensableIdentifier.getBeanName());
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = compensableIdentifier.getConfirmMethod();
        Class<?>[] parameterTypes = compensableIdentifier.getParameterTypes();
        Object[] parameters = compensableIdentifier.getArgs();

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    @Override
    public Object rollback(ParticipantDTO participantDTO) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Participant participant = participantDTO.toParticipant();
        SpanContext.setTransactionContext(participant.getTransactionContext());
        CompensableIdentifier compensableIdentifier = participant.getCompensableIdentifier();
        Object serviceBean = applicationContext.getBean(compensableIdentifier.getBeanName());
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = compensableIdentifier.getCancelMethod();
        Class<?>[] parameterTypes = compensableIdentifier.getParameterTypes();
        Object[] parameters = compensableIdentifier.getArgs();

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

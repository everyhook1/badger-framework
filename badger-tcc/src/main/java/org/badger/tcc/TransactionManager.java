/**
 * @(#)TransactionManager.java, 8月 05, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.badger.common.api.SpanContext;
import org.badger.common.api.transaction.Compensable;
import org.badger.common.api.transaction.TransactionContext;
import org.badger.common.api.transaction.TransactionRoles;
import org.badger.tcc.entity.CompensableEnum;
import org.badger.tcc.entity.CompensableIdentifier;
import org.badger.tcc.entity.ParticipantDTO;
import org.badger.tcc.entity.ParticipantStatus;
import org.badger.tcc.entity.TransactionDTO;
import org.badger.tcc.spring.CompensableManager;
import org.badger.tcc.spring.TransactionCoordinator;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author liubin01
 */
@Slf4j
@Data
public class TransactionManager {

    private TransactionCoordinator transactionCoordinator;

    private CompensableManager compensableManager;

    public Transaction begin(ProceedingJoinPoint jp) throws IOException {
        Transaction transaction;
        TransactionContext context = SpanContext.getTransactionContext();
        log.debug("context {}", context);
        if (context == null) {
            context = TransactionContext.init();
            transaction = new Transaction(context.getRootId());
            SpanContext.setTransactionContext(context);
        } else {
            transaction = transactionCoordinator.getTransaction(new String(context.getRootId().getGlobalTransactionId())).toTransaction();
        }

        Method method = ((MethodSignature) (jp.getSignature())).getMethod();
        Compensable compensable = method.getAnnotation(Compensable.class);
        String identifier = compensable.identifier();
        CompensableIdentifier compensableIdentifier = compensableManager.getIdentifier(identifier);

        CompensableEnum compensableEnum = compensableIdentifier.getCompensableEnum(method.getName());
        Participant participant = transaction.getParticipant(identifier);
        if (participant == null) {
            participant = new Participant();
            participant.setArgs(jp.getArgs());
            participant.setClient(compensableManager.getRemoteClient());
            participant.setCompensableIdentifier(compensableIdentifier);
            participant.setTransactionContext(SpanContext.getTransactionContext());
            participant.setParticipantStatus(ParticipantStatus.BEGIN);
            transaction.addParticipant(participant);
        }
        transaction.setCurrentParticipant(participant);
        transaction.setCompensableEnum(compensableEnum);
        transactionCoordinator.update(new TransactionDTO(transaction));
        return transaction;
    }

    public void commit(Transaction transaction) {
        if (transaction.getCompensableEnum().equals(CompensableEnum.TRY)
                && SpanContext.getTransactionContext().getRoles() == TransactionRoles.LEADER) {
            transaction.commit();
        }
        transactionCoordinator.update(new ParticipantDTO(transaction.getCurrentParticipant()));
    }

    public void rollback(Transaction transaction) {
        if (transaction.getCompensableEnum().equals(CompensableEnum.TRY)
                && SpanContext.getTransactionContext().getRoles() == TransactionRoles.LEADER) {
            transaction.rollback();
        }
        transactionCoordinator.update(new ParticipantDTO(transaction.getCurrentParticipant()));
    }

    public void cleanAfterCompletion(Transaction transaction) {
        if (transaction.getCompensableEnum().equals(CompensableEnum.TRY)
                && SpanContext.getTransactionContext().getRoles() == TransactionRoles.LEADER) {
            transaction.cleanAfterCompletion();
        }
        SpanContext.removeTransactionContext();
    }
}

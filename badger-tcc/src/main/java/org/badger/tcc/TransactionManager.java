/**
 * @(#)TransactionManager.java, 8月 05, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc;


import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.badger.common.api.SpanContext;
import org.badger.common.api.transaction.Compensable;
import org.badger.common.api.transaction.TransactionContext;
import org.badger.common.api.transaction.TransactionRoles;
import org.badger.tcc.entity.CompensableEnum;
import org.badger.tcc.entity.CompensableIdentifier;
import org.badger.tcc.entity.Participant;
import org.badger.tcc.entity.Transaction;
import org.badger.tcc.spring.CompensableManager;
import org.badger.tcc.spring.TransactionCoordinator;

import java.lang.reflect.Method;

/**
 * @author liubin01
 */
@Data
public class TransactionManager {

    private TransactionCoordinator transactionCoordinator;

    private CompensableManager compensableManager;

    public Transaction begin(ProceedingJoinPoint jp) {
        Transaction transaction;
        TransactionContext context = SpanContext.getTransactionContext();
        if (context == null) {
            context = TransactionContext.init();
            transaction = new Transaction(context.getRootId());
            SpanContext.setTransactionContext(context);
        } else {
            transaction = transactionCoordinator.getTransaction(context.getRootId());
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
            participant.setCompensableIdentifier(compensableIdentifier);
            participant.setTransactionContext(SpanContext.getTransactionContext());
            transaction.addParticipant(participant);
        }
        transaction.setCurrentParticipant(participant);
        transaction.setCompensableEnum(compensableEnum);
        transactionCoordinator.update(transaction);
        return transaction;
    }

    public void commit(Transaction transaction) {
        if (transaction.getCompensableEnum().equals(CompensableEnum.TRY)
                && SpanContext.getTransactionContext().getRoles() == TransactionRoles.LEADER) {
            transaction.commit();
        }
        transactionCoordinator.update(transaction.getCurrentParticipant());
    }

    public void rollback(Transaction transaction) {
        if (transaction.getCompensableEnum().equals(CompensableEnum.TRY)
                && SpanContext.getTransactionContext().getRoles() == TransactionRoles.LEADER) {
            transaction.rollback();
        }
        transactionCoordinator.update(transaction.getCurrentParticipant());
    }

    public void cleanAfterCompletion(Transaction transaction) {
        if (transaction.getCompensableEnum().equals(CompensableEnum.TRY)
                && SpanContext.getTransactionContext().getRoles() == TransactionRoles.LEADER) {
            transaction.cleanAfterCompletion();
        }
        SpanContext.removeTransactionContext();
    }
}

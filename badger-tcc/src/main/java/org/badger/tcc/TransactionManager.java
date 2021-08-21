
package org.badger.tcc;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.badger.common.api.RpcRequest;
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

import java.lang.reflect.Method;

/**
 * @author liubin01
 */
@Slf4j
@Data
public class TransactionManager {

    private TransactionCoordinator transactionCoordinator;

    private CompensableManager compensableManager;

    public Transaction begin(ProceedingJoinPoint jp) {
        Transaction transaction;
        TransactionContext context = SpanContext.getTransactionContext();
        RpcRequest request = SpanContext.getCurRequest();
        log.debug("context {}", context);
        if (context == null && request != null && request.getTransactionContext() != null) {
            TransactionContext last = request.getTransactionContext();
            context = TransactionContext.newBranch(last);
            transaction = transactionCoordinator.getTransaction(new String(context.getRootId().getGlobalTransactionId())).toTransaction();
        } else if (context == null) {
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
        compensableIdentifier.setArgs(jp.getArgs());
        CompensableEnum compensableEnum = compensableIdentifier.getCompensableEnum(method.getName());
        Participant participant = transaction.getParticipant(identifier);
        if (participant == null) {
            participant = new Participant();
            participant.setServiceName(SpanContext.getServiceName());
            participant.setCompensableIdentifier(compensableIdentifier);
            participant.setTransactionContext(SpanContext.getTransactionContext());
            participant.setParticipantStatus(ParticipantStatus.TRY);
            transaction.addParticipant(participant);
        }
        transaction.setCurrentParticipant(participant);
        transaction.setCompensableEnum(compensableEnum);
        return transaction;
    }

    public void commit(Transaction transaction) {
        Participant participant = transaction.getCurrentParticipant();
        switch (transaction.getCompensableEnum()) {
            case TRY:
                participant.setParticipantStatus(ParticipantStatus.TRY_SUCCESS);
                transactionCoordinator.update(new TransactionDTO(transaction));
                if (SpanContext.getTransactionContext().getRoles() == TransactionRoles.LEADER) {
                    transactionCoordinator.commit(new String(transaction.getRootId().getGlobalTransactionId()));
                }
                break;
            case CONFIRM:
                participant.setParticipantStatus(ParticipantStatus.CONFIRM_SUCCESS);
                transactionCoordinator.update(new ParticipantDTO(participant));
                break;
            case CANCEL:
                participant.setParticipantStatus(ParticipantStatus.CANCEL_SUCCESS);
                transactionCoordinator.update(new ParticipantDTO(participant));
                break;
        }
    }

    public void rollback(Transaction transaction) {
        Participant participant = transaction.getCurrentParticipant();
        switch (transaction.getCompensableEnum()) {
            case TRY:
                if (SpanContext.getTransactionContext().getRoles() == TransactionRoles.LEADER) {
                    transactionCoordinator.rollback(new String(transaction.getRootId().getGlobalTransactionId()));
                }
                break;
            case CONFIRM:
                participant.setParticipantStatus(ParticipantStatus.CONFIRM_FAILED);
                transactionCoordinator.update(new ParticipantDTO(participant));
                break;
            case CANCEL:
                participant.setParticipantStatus(ParticipantStatus.CANCEL_FAILED);
                transactionCoordinator.update(new ParticipantDTO(participant));
                break;
        }
    }

    public void cleanAfterCompletion(Transaction transaction) {
        if (transaction.getCompensableEnum().equals(CompensableEnum.TRY)
                && SpanContext.getTransactionContext().getRoles() == TransactionRoles.LEADER) {
            transaction.cleanAfterCompletion();
        }
        SpanContext.removeTransactionContext();
    }
}

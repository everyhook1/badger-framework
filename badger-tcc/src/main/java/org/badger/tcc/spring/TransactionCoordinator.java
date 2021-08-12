package org.badger.tcc.spring;

import org.badger.common.api.transaction.TransactionXid;
import org.badger.tcc.entity.Participant;
import org.badger.tcc.entity.Transaction;

/**
 * @author liubin01
 */
public interface TransactionCoordinator {

    Transaction getTransaction(TransactionXid rootId);

    void update(Transaction transaction);

    void update(Participant participant);
}

package org.badger.tcc.spring;

import org.badger.tcc.entity.ParticipantDTO;
import org.badger.tcc.entity.TransactionDTO;

/**
 * @author liubin01
 */
public interface TransactionCoordinator {

    TransactionDTO getTransaction(String gxid);

    void update(TransactionDTO transactionDTO);

    void update(ParticipantDTO participantDTO);

    void clean(String gxid);

    void commit(String gxid);

    void rollback(String gxid);
}

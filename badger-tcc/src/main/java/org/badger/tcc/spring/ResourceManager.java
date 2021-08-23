package org.badger.tcc.spring;

import org.badger.tcc.entity.TransactionDTO;

public interface ResourceManager {

    void commit(TransactionDTO transactionDTO);

    void rollback(TransactionDTO transactionDTO);
}

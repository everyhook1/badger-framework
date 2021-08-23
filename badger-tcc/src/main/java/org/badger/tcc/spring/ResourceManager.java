package org.badger.tcc.spring;

import org.badger.tcc.entity.TransactionDTO;

public interface ResourceManager {

    Object commit(TransactionDTO transactionDTO);

    Object rollback(TransactionDTO transactionDTO);
}

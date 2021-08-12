/**
 * @(#)TransactionCoordinatorImpl.java, 8月 11, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.coordinator.service;

import org.badger.common.api.RpcProvider;
import org.badger.common.api.transaction.TransactionXid;
import org.badger.tcc.entity.Participant;
import org.badger.tcc.entity.Transaction;
import org.badger.tcc.spring.TransactionCoordinator;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@RpcProvider
@Service
public class TransactionCoordinatorImpl implements TransactionCoordinator {

    @Override
    public Transaction getTransaction(TransactionXid rootId) {
        return null;
    }

    @Override
    public void update(Transaction transaction) {

    }

    @Override
    public void update(Participant participant) {

    }
}

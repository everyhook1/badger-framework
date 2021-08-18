/**
 * @(#)Participant.java, 8月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcRequest;
import org.badger.common.api.SpanContext;
import org.badger.common.api.remote.CLIENT;
import org.badger.common.api.transaction.TransactionContext;
import org.badger.tcc.entity.CompensableIdentifier;
import org.badger.tcc.entity.ParticipantStatus;

import java.lang.reflect.InvocationTargetException;

/**
 * @author liubin01
 */
@Data
@Slf4j
public class Participant {

    private CLIENT client;

    private CompensableIdentifier compensableIdentifier;

    private ParticipantStatus participantStatus;

    private Object[] args;

    private TransactionContext transactionContext;

    private int version;

    public void updateRegister() {

    }

    public void commit() {
        if (compensableIdentifier.getServiceName().equals(SpanContext.getServiceName())) {
            try {
                compensableIdentifier.getConfirmM().invoke(compensableIdentifier.getBean(), args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("commit error", e);
            }
        } else {

        }
    }

    public void rollback() {

    }

    public Object execute() throws InterruptedException {
        RpcRequest request = new RpcRequest();
        return client.send(request);
    }
}

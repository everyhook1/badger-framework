/**
 * @(#)Participant.java, 8月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.entity;

import lombok.Data;
import org.badger.common.api.RpcRequest;
import org.badger.common.api.remote.CLIENT;
import org.badger.common.api.transaction.TransactionContext;

/**
 * @author liubin01
 */
@Data
public class Participant {

    private CLIENT client;

    private CompensableIdentifier compensableIdentifier;

    private ParticipantStatus participantStatus;

    private Object[] args;

    private TransactionContext transactionContext;

    public void updateRegister() {

    }

    public Object execute() throws InterruptedException {
        RpcRequest request = new RpcRequest();
        return client.send(request);
    }
}

/**
 * @(#)Transaction.java, 8月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc;

import lombok.Data;
import org.badger.common.api.transaction.TransactionXid;
import org.badger.tcc.entity.CompensableEnum;
import org.badger.tcc.entity.TransactionStatus;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubin01
 */
@Data
public class Transaction {

    private TransactionXid rootId;

    private List<Participant> participants;

    private Participant currentParticipant;

    private CompensableEnum compensableEnum;

    private TransactionStatus transactionStatus;

    public void addParticipant(Participant participant) {
        if (participants == null) {
            participants = new ArrayList<>();
        }
        participants.add(participant);
    }

    public Transaction() {

    }

    public Transaction(TransactionXid rootId) {
        this.rootId = rootId;
    }

    public void updateRegister() {
        participants.forEach(Participant::updateRegister);
    }

    public Participant getParticipant(String identifier) {
        if (CollectionUtils.isEmpty(participants)) {
            return null;
        }
        return participants.stream().filter(o -> o.getCompensableIdentifier().getIdentifier().equals(identifier)).findFirst().orElseThrow(RuntimeException::new);
    }

    public void commit() {
        if (CollectionUtils.isEmpty(participants)) {
            return;
        }
        participants.forEach(Participant::commit);
    }

    public void rollback() {
        if (CollectionUtils.isEmpty(participants)) {
            return;
        }
        participants.forEach(Participant::rollback);
    }

    public void cleanAfterCompletion() {

    }
}

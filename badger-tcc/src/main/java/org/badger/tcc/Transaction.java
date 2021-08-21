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
        this.transactionStatus = TransactionStatus.TRY;
    }

    public Transaction(TransactionXid rootId) {
        this();
        this.rootId = rootId;
    }

    public Participant getParticipant(String identifier) {
        if (CollectionUtils.isEmpty(participants)) {
            return null;
        }
        return participants.stream().filter(o -> o.getCompensableIdentifier().getIdentifier().equals(identifier)).findFirst().orElseThrow(RuntimeException::new);
    }

    public void cleanAfterCompletion() {

    }
}

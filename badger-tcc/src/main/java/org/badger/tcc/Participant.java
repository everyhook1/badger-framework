package org.badger.tcc;

import lombok.Getter;
import lombok.Setter;
import org.badger.common.api.codec.serializer.SerializerEnum;
import org.badger.common.api.transaction.TransactionContext;
import org.badger.tcc.entity.CompensableIdentifier;
import org.badger.tcc.entity.ParticipantStatus;

/**
 * @author liubin01
 */
@Getter
@Setter
public class Participant {

    private CompensableIdentifier compensableIdentifier;

    private ParticipantStatus participantStatus;

    private TransactionContext transactionContext;

    private int version;

    private String serviceName;

    private byte[] payload;

    public CompensableIdentifier getCompensableIdentifier() {
        if (compensableIdentifier == null) {
            this.compensableIdentifier = (CompensableIdentifier) SerializerEnum.DEFAULT().deserialize(payload, CompensableIdentifier.class);
        }
        return compensableIdentifier;
    }
}

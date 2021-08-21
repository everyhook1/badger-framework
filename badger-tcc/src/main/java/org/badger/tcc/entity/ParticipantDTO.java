
package org.badger.tcc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.codec.serializer.RpcSerializer;
import org.badger.common.api.codec.serializer.SerializerEnum;
import org.badger.common.api.transaction.TransactionContext;
import org.badger.common.api.transaction.TransactionRoles;
import org.badger.common.api.transaction.TransactionXid;
import org.badger.tcc.Participant;

/**
 * @author liubin01
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO {

    private static final RpcSerializer serialize = SerializerEnum.DEFAULT();

    private String gxid;
    private String rxid;
    private String bxid;
    private int status;
    private int version;
    private String serviceName;
    private byte[] payload;

    public ParticipantDTO(Participant participant) {
        this.gxid = new String(participant.getTransactionContext().getRootId().getGlobalTransactionId());
        this.rxid = new String(participant.getTransactionContext().getRootId().getBranchQualifier());
        this.bxid = new String(participant.getTransactionContext().getBranchId().getBranchQualifier());
        this.status = participant.getParticipantStatus().toInt();
        this.version = participant.getVersion();
        this.serviceName = participant.getServiceName();
        this.payload = serialize.serialize(participant.getCompensableIdentifier());
    }

    public Participant toParticipant() {
        Participant participant = new Participant();
        TransactionContext context = new TransactionContext();
        TransactionXid rootId = new TransactionXid(gxid, rxid);
        TransactionXid branchId = new TransactionXid(gxid, bxid);
        context.setRootId(rootId);
        context.setBranchId(branchId);
        context.setRoles(rxid.equals(bxid) ? TransactionRoles.LEADER : TransactionRoles.FOLLOWER);
        participant.setTransactionContext(context);
        participant.setParticipantStatus(ParticipantStatus.fromInt(status));
        participant.setVersion(version);
        participant.setServiceName(serviceName);
        participant.setPayload(payload);
        return participant;
    }
}

package org.badger.tcc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.badger.common.api.SpanContext;
import org.badger.common.api.transaction.TransactionXid;
import org.badger.tcc.Participant;
import org.badger.tcc.Transaction;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liubin01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private String gxid;

    private String rxid;

    private List<ParticipantDTO> participantDTOS;

    private int status;

    public TransactionDTO(Transaction transaction) {
        this.gxid = new String(SpanContext.getTransactionContext().getRootId().getGlobalTransactionId());
        this.rxid = new String(SpanContext.getTransactionContext().getRootId().getBranchQualifier());
        List<ParticipantDTO> participantDTOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(transaction.getParticipants())) {
            participantDTOS = transaction.getParticipants().stream().map(ParticipantDTO::new).collect(Collectors.toList());
        }
        this.participantDTOS = participantDTOS;
    }

    public Transaction toTransaction() {
        Transaction transaction = new Transaction();
        transaction.setRootId(new TransactionXid(this.gxid, this.rxid));
        transaction.setTransactionStatus(TransactionStatus.fromInt(this.status));
        List<Participant> participants = new ArrayList<>();
        if (!CollectionUtils.isEmpty(this.participantDTOS)) {
            participants = this.participantDTOS.stream().map(ParticipantDTO::toParticipant).collect(Collectors.toList());
        }
        transaction.setParticipants(participants);
        return transaction;
    }
}

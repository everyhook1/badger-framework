/**
 * @(#)ParticipantParam.java, 8月 18, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.badger.tcc.Participant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author liubin01
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO {

    private String gxid;
    private String rxid;
    private String bxid;
    private byte[] arg;
    private String serviceName;
    private String clzName;
    private String beanName;
    private String identifier;
    private String tryMethod;
    private String cancelMethod;
    private String confirmMethod;
    private int status;
    private int version;

    public ParticipantDTO(Participant participant) {
        this.gxid = new String(participant.getTransactionContext().getRootId().getGlobalTransactionId());
        this.rxid = new String(participant.getTransactionContext().getRootId().getBranchQualifier());
        this.bxid = new String(participant.getTransactionContext().getBranchId().getBranchQualifier());
        this.serviceName = participant.getCompensableIdentifier().getServiceName();
        this.clzName = participant.getCompensableIdentifier().getClzName();
        this.beanName = participant.getCompensableIdentifier().getBeanName();
        this.identifier = participant.getCompensableIdentifier().getIdentifier();
        this.tryMethod = participant.getCompensableIdentifier().getTryMethod();
        this.cancelMethod = participant.getCompensableIdentifier().getCancelMethod();
        this.confirmMethod = participant.getCompensableIdentifier().getConfirmMethod();
        try {
            this.arg = serialize(new ARG(participant.getArgs(), participant.getCompensableIdentifier().getParameterTypes()));
        } catch (IOException e) {
            log.error("", e);
        }
        this.status = participant.getParticipantStatus().toInt();
        this.version = participant.getVersion();
    }


    public <T> byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteArr);
        out.writeObject(obj);
        out.flush();
        byte[] data = byteArr.toByteArray();
        out.close();
        return data;
    }

    public <T> Object deserialize(byte[] bytes, Class<T> clazz) throws IOException, ClassNotFoundException {
        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object readObject = input.readObject();
        input.close();
        return readObject;
    }

    public Participant toParticipant() {
        Participant participant = new Participant();
        return participant;
    }

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    static class ARG implements Serializable {
        private Object[] args;
        private Class<?>[] parameterTypes;
    }
}

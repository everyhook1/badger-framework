/**
 * @(#)ParticipantStatus.java, 8月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.entity;

/**
 * @author liubin01
 */
public enum ParticipantStatus {

    UNKNOWN(0),
    BEGIN(1),
    B(2),
    ;
    int value;

    ParticipantStatus(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public ParticipantStatus fromInt(int value) {
        for (ParticipantStatus participantStatus : ParticipantStatus.values()) {
            if (participantStatus.value == value) {
                return participantStatus;
            }
        }
        throw new IllegalArgumentException();
    }
}

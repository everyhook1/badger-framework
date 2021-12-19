
package org.badger.tcc.entity;

/**
 * @author liubin01
 */
public enum ParticipantStatus {
    UNKNOWN(0),
    TRY(1),
    TRY_SUCCESS(2),
    TRY_FAILED(3),
    CONFIRM(4),
    CONFIRM_SUCCESS(5),
    CONFIRM_FAILED(6),
    CANCEL(7),
    CANCEL_SUCCESS(8),
    CANCEL_FAILED(9),
    ;
    final int value;

    ParticipantStatus(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static ParticipantStatus fromInt(int value) {
        for (ParticipantStatus participantStatus : ParticipantStatus.values()) {
            if (participantStatus.value == value) {
                return participantStatus;
            }
        }
        throw new IllegalArgumentException();
    }
}

package org.badger.tcc.entity;

public enum TransactionStatus {
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
    int value;

    TransactionStatus(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static TransactionStatus fromInt(int value) {
        for (TransactionStatus transactionStatus : TransactionStatus.values()) {
            if (transactionStatus.value == value) {
                return transactionStatus;
            }
        }
        throw new IllegalArgumentException();
    }
}

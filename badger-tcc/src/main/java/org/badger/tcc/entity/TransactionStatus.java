package org.badger.tcc.entity;

public enum TransactionStatus {
    UNKNOWN(0),
    A(1),
    B(2),
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

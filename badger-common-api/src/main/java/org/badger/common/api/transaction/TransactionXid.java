/**
 * @(#)TransactionXid.java, 8月 05, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.common.api.transaction;

import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author liubin01
 */
public class TransactionXid implements Xid, Serializable {

    private int formatId = 1;
    private byte[] globalTransactionId;
    private byte[] branchQualifier;

    public TransactionXid(String gid, String bid) {
        globalTransactionId = gid.getBytes();
        branchQualifier = bid.getBytes();
    }

    public TransactionXid() {
        globalTransactionId = uuidToByteArray(UUID.randomUUID());
        branchQualifier = uuidToByteArray(UUID.randomUUID());
    }

    public TransactionXid(TransactionXid xid) {
        globalTransactionId = xid.getGlobalTransactionId();
        branchQualifier = uuidToByteArray(UUID.randomUUID());
    }

    @Override
    public int getFormatId() {
        return formatId;
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return globalTransactionId;
    }

    @Override
    public byte[] getBranchQualifier() {
        return branchQualifier;
    }

    private static byte[] uuidToByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}

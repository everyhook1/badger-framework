/**
 * @(#)TransactionContext.java, 8月 11, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.common.api.transaction;

import lombok.Data;

/**
 * @author liubin01
 */
@Data
public class TransactionContext {

    private TransactionXid rootId;
    private TransactionXid branchId;
    private TransactionRoles roles;

    public static TransactionContext newBranch(TransactionContext o) {
        TransactionContext t = new TransactionContext();
        TransactionXid x = new TransactionXid();
        t.setRootId(o.getRootId());
        t.setBranchId(x);
        t.setRoles(TransactionRoles.FOLLOWER);
        return t;
    }

    public static TransactionContext init() {
        TransactionContext t = new TransactionContext();
        TransactionXid x = new TransactionXid();
        t.setRootId(x);
        t.setBranchId(x);
        t.setRoles(TransactionRoles.LEADER);
        return t;
    }
}

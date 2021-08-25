package org.badger.common.api.transaction;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liubin01
 */
@Data
public class TransactionContext implements Serializable {

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

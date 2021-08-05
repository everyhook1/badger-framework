package org.badger.common.api;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liubin01
 */
@Data
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 7390539685431550199L;
    private long seqId;
    private String serviceName;
    private String clzName;
    private String qualifier;
    private String method;
    private Object[] args;
    private Class<?>[] argTypes;
    private RpcRequest parentRpc;
    private Object rpcContext;
    private long timeout;
}

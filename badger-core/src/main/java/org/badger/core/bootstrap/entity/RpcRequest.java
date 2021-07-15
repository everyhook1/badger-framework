package org.badger.core.bootstrap.entity;

import lombok.Data;

/**
 * @author liubin01
 */
@Data
public class RpcRequest {
    private long seqId;
    private String serviceName;
    private String clzName;
    private String qualifier;
    private String method;
    private Object[] args;
    private Class<?>[] argTypes;
}

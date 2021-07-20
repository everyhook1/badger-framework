package org.badger.core.bootstrap.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liubin01
 */
@Data
public class RpcRequest implements Serializable {
    private long seqId;
    private String serviceName;
    private String clzName;
    private String qualifier;
    private String method;
    private Object[] args;
    private Class<?>[] argTypes;
    private long timeout;
}

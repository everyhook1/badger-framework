package org.badger.core.bootstrap.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liubin01
 */
@Data
public class RpcResponse implements Serializable {
    private long seqId;
    private int code;
    private String errMsg;
    private Object body;
}

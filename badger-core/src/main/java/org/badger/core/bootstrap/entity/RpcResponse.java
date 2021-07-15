package org.badger.core.bootstrap.entity;

import lombok.Data;

/**
 * @author liubin01
 */
@Data
public class RpcResponse {
    private long seqId;
    private int code;
    private String errMsg;
    private Object body;
}

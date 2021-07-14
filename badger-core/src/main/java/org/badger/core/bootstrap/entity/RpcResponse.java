/**
 * @(#)RpcResponse.java, 6月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
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

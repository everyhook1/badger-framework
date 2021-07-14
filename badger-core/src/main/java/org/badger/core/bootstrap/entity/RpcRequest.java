/**
 * @(#)RpcRequest.java, 6月 08, 2021.
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
public class RpcRequest {
    private long seqId;
    private Peer peer;
    private String clzName;
    private String qualifier;
    private String method;
    private Object[] args;
    private Class<?>[] argTypes;
}

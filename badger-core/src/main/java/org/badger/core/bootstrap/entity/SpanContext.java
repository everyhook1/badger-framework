/**
 * @(#)RpcContext.java, 8月 02, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap.entity;

/**
 * @author liubin01
 */
public class SpanContext {

    private static final ThreadLocal<RpcRequest> curRequest = new InheritableThreadLocal<>();

    public static void setCurRequest(RpcRequest request) {
        curRequest.set(request);
    }

    public static RpcRequest getCurRequest() {
        return curRequest.get();
    }

    public static void removeRpcRequest() {
        curRequest.remove();
    }
}

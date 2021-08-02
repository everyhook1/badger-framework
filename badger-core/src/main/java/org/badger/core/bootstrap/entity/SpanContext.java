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

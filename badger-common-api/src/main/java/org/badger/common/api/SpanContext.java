package org.badger.common.api;

import org.badger.common.api.transaction.TransactionContext;

/**
 * @author liubin01
 */
public class SpanContext {

    private static String SERVICE_NAME;


    private static final ThreadLocal<RpcRequest> curRequest = new InheritableThreadLocal<>();

    private static final ThreadLocal<TransactionContext> tc = new InheritableThreadLocal<>();

    public static void setCurRequest(RpcRequest request) {
        curRequest.set(request);
    }

    public static RpcRequest getCurRequest() {
        return curRequest.get();
    }

    public static void removeRpcRequest() {
        curRequest.remove();
    }

    public static void setTransactionContext(TransactionContext transactionContext) {
        tc.set(transactionContext);
    }

    public static TransactionContext getTransactionContext() {
        return tc.get();
    }

    public static void removeTransactionContext() {
        tc.remove();
    }

    public static String getServiceName() {
        return SERVICE_NAME;
    }

    public static void setServiceName(String serviceName) {
        SERVICE_NAME = serviceName;
    }
}

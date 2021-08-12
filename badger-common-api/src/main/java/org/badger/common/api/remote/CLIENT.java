package org.badger.common.api.remote;

import org.badger.common.api.RpcRequest;

public interface CLIENT {

    Object send(RpcRequest request) throws InterruptedException;

    void addListener(String serviceName);
}

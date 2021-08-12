package org.badger.common.api.remote;

import lombok.Data;
import org.badger.common.api.RpcRequest;

@Data
public class Invoker {

    private CLIENT remoteClient;
    private RpcRequest request;

    public Invoker(CLIENT remoteClient, RpcRequest request) {
        this.remoteClient = remoteClient;
        this.request = request;
    }

    public Object execute() throws InterruptedException {
        return remoteClient.send(request);
    }
}

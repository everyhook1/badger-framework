package org.badger.core.bootstrap.codec.serializer.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.badger.common.api.RpcRequest;
import org.badger.common.api.RpcResponse;
import org.badger.core.bootstrap.codec.serializer.RpcSerializer;

import java.io.ByteArrayOutputStream;

public class KryoSerialization implements RpcSerializer {

    private static final KryoPool kryoPool = new KryoPool.Builder(
            () -> {
                Kryo kryo = new Kryo();
                kryo.register(RpcResponse.class);
                kryo.register(RpcRequest.class);
                return kryo;
            }).build();

    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Output output = new Output(out);
        Kryo kryo = kryoPool.borrow();
        kryo.writeObject(output, obj);
        kryoPool.release(kryo);
        output.flush();
        return out.toByteArray();
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        Input input = new Input(bytes);
        Kryo kryo = kryoPool.borrow();
        T res = kryo.readObject(input, clazz);
        kryoPool.release(kryo);
        return res;
    }
}

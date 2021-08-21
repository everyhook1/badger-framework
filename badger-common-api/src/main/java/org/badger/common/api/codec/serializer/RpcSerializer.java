package org.badger.common.api.codec.serializer;

public interface RpcSerializer {

    <T> byte[] serialize(T obj);

    <T> Object deserialize(byte[] bytes, Class<T> clazz);
}

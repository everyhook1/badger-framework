package org.badger.core.bootstrap.codec.serializer;

import java.io.IOException;

public interface RpcSerializer {

    <T> byte[] serialize(T obj) throws IOException;

    <T> Object deserialize(byte[] bytes, Class<T> clazz) throws IOException, ClassNotFoundException;
}

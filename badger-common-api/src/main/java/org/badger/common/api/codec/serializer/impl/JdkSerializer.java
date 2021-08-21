package org.badger.common.api.codec.serializer.impl;

import org.badger.common.api.codec.serializer.RpcSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author liubin01
 */
public class JdkSerializer implements RpcSerializer {

    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(byteArr);
            out.writeObject(obj);
            out.flush();
            byte[] data = byteArr.toByteArray();
            out.close();
            return data;
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        try {
            ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object readObject = input.readObject();
            input.close();
            return readObject;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}

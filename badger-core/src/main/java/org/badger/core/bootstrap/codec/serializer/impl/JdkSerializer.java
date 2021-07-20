/**
 * @(#)JdkSerializer.java, 7月 20, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap.codec.serializer.impl;

import org.badger.core.bootstrap.codec.serializer.RpcSerializer;

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
    public <T> byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteArr);
        out.writeObject(obj);
        out.flush();
        byte[] data = byteArr.toByteArray();
        out.close();
        return data;
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) throws IOException, ClassNotFoundException {
        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object readObject = input.readObject();
        input.close();
        return readObject;
    }


}

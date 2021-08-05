package org.badger.core.bootstrap.codec.serializer.impl;

import org.badger.common.api.RpcRequest;
import org.badger.common.api.RpcResponse;
import org.badger.core.bootstrap.codec.serializer.RpcSerializer;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FstSerializer implements RpcSerializer {
    private static final FSTConfiguration configuration = FSTConfiguration
            .createDefaultConfiguration();

    static {
        configuration.registerClass(RpcResponse.class);
        configuration.registerClass(RpcRequest.class);
        configuration.setShareReferences(false);
    }

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FSTObjectOutput objectOutput = configuration.getObjectOutput(out);
        objectOutput.writeObject(obj);
        objectOutput.flush();
        byte[] data = out.toByteArray();
        objectOutput.close();
        return data;
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) throws IOException, ClassNotFoundException {
        FSTObjectInput objectInput = configuration.getObjectInput(new ByteArrayInputStream(bytes));
        Object readObject = objectInput.readObject();
        objectInput.close();
        return readObject;
    }
}

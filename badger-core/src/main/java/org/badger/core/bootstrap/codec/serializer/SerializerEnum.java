package org.badger.core.bootstrap.codec.serializer;

import org.badger.core.bootstrap.codec.serializer.impl.FstSerializer;
import org.badger.core.bootstrap.codec.serializer.impl.Hessian2Serializer;
import org.badger.core.bootstrap.codec.serializer.impl.HessianSerializer;
import org.badger.core.bootstrap.codec.serializer.impl.JdkSerializer;
import org.badger.core.bootstrap.codec.serializer.impl.KryoSerialization;

public enum SerializerEnum {

    FST(new FstSerializer()),
    HESSIAN(new HessianSerializer()),
    HESSIAN2(new Hessian2Serializer()),
    JDK(new JdkSerializer()),
    KRYO(new KryoSerialization()),
    ;

    private final RpcSerializer rpcSerializer;

    SerializerEnum(RpcSerializer rpcSerializer) {
        this.rpcSerializer = rpcSerializer;
    }

    public static RpcSerializer DEFAULT() {
        return FST.rpcSerializer;
    }

    public RpcSerializer getRpcSerializer() {
        return this.rpcSerializer;
    }

    public static RpcSerializer findByName(String name) {
        for (SerializerEnum value : SerializerEnum.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value.rpcSerializer;
            }
        }
        return DEFAULT();
    }
}

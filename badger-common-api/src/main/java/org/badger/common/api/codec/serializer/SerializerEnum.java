package org.badger.common.api.codec.serializer;

import org.badger.common.api.codec.serializer.impl.FstSerializer;
import org.badger.common.api.codec.serializer.impl.Hessian2Serializer;
import org.badger.common.api.codec.serializer.impl.HessianSerializer;
import org.badger.common.api.codec.serializer.impl.JacksonSerializer;
import org.badger.common.api.codec.serializer.impl.JdkSerializer;
import org.badger.common.api.codec.serializer.impl.KryoSerialization;

public enum SerializerEnum {

    FST(new FstSerializer()),
    HESSIAN(new HessianSerializer()),
    HESSIAN2(new Hessian2Serializer()),
    JDK(new JdkSerializer()),
    KRYO(new KryoSerialization()),
    JACKSON(new JacksonSerializer()),
    ;

    private final RpcSerializer rpcSerializer;

    SerializerEnum(RpcSerializer rpcSerializer) {
        this.rpcSerializer = rpcSerializer;
    }

    public static RpcSerializer DEFAULT() {
        return KRYO.rpcSerializer;
    }

    public RpcSerializer getRpcSerializer() {
        return this.rpcSerializer;
    }

}

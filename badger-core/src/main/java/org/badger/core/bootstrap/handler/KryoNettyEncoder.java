package org.badger.core.bootstrap.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.badger.core.bootstrap.serial.KryoSerialization;

public class KryoNettyEncoder extends MessageToByteEncoder<Object> {

    protected void encode(ChannelHandlerContext ctx, Object object, ByteBuf out) {
        // Get object-encoding method from KryoSerialization
        byte[] objectBytes = KryoSerialization.getInstance().encodeObject(object);

        // Write the length to the output-buffer
        out.writeInt(objectBytes.length);
        // Write the content data to the output-buffer
        out.writeBytes(objectBytes);
    }
}

package org.badger.core.bootstrap.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.codec.serializer.RpcSerializer;

import java.util.List;

/**
 * @author liubin01
 */
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {

    private final RpcSerializer serializer;
    private final Class<?> clazz;

    public RpcDecoder(RpcSerializer serializer, Class<?> clazz) {
        super();
        this.serializer = serializer;
        this.clazz = clazz;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        try {
            if (buf.readableBytes() < 4) {
                return;
            }
            buf.markReaderIndex();
            int dataLength = buf.readInt();
            if (dataLength < 0) {
                ctx.close();
            }
            if (buf.readableBytes() < dataLength) {
                buf.resetReaderIndex();
                return;
            }
            byte[] data = new byte[dataLength];
            buf.readBytes(data);
            Object obj = serializer.deserialize(data, clazz);
            out.add(obj);
        } catch (Exception e) {
            log.error("decode {}", ctx, e);
            throw new RuntimeException();
        }
    }
}

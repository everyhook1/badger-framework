/**
 * @(#)RpcDecoder.java, 7月 20, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.badger.core.bootstrap.codec.serializer.RpcSerializer;

import java.io.IOException;
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

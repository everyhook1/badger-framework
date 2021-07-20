/**
 * @(#)RpcEncoder.java, 7月 20, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.badger.core.bootstrap.codec.serializer.RpcSerializer;

import java.io.IOException;

/**
 * @author liubin01
 */
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<Object> {

    private final RpcSerializer serializer;

    private final Class<?> clazz;

    public RpcEncoder(RpcSerializer serializer, Class<?> clazz) {
        super();
        this.serializer = serializer;
        this.clazz = clazz;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object encode, ByteBuf buf) throws IOException {
        try {
            if (clazz.isInstance(encode)) {
                byte[] data = serializer.serialize(encode);
                buf.writeInt(data.length);
                buf.writeBytes(data);
            }
        } catch (Exception e) {
            log.error("decode {}", ctx, e);
            throw new RuntimeException();
        }
    }
}

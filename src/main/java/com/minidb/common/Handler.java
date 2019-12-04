package com.minidb.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;

public interface Handler {

    public default void ctxWriteAndFlush(ChannelHandlerContext ctx, String msg) {
        ctx.writeAndFlush(ByteBufUtil.writeUtf8(ctx.alloc(), msg));
    }

    public default void ctxWriteAndFlush(ChannelHandlerContext ctx, int[] arr) {
        ByteBuf byteBuf = ctx.alloc().buffer(36);
        for (int i : arr) {
            byteBuf.writeInt(i);
        }
        ctx.writeAndFlush(byteBuf);
    }

}

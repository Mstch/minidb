package com.minidb.consensus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
@ChannelHandler.Sharable
public class ElectionEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        if (msg instanceof VoteReq) {
            VoteReq req = (VoteReq) msg;
            out.writeInt(req.term);
            out.writeInt(req.candidateId);
            out.writeInt(req.lastLogIndex);
            out.writeInt(req.lastLogTerm);
        }
    }
}

package com.minidb.consensus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ElectionDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int term = msg.readInt();
        int candidateId = msg.readInt();
        int lastLogIndex = msg.readInt();
        int lastLogTerm = msg.readInt();
        out.add(new VoteReq(term, candidateId, lastLogIndex, lastLogTerm));
    }
}

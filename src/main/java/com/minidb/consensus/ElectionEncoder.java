package com.minidb.consensus;

import com.minidb.consensus.raft.model.VoteReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class ElectionEncoder extends MessageToByteEncoder<VoteReq> {

    @Override
    protected void encode(ChannelHandlerContext ctx, VoteReq req, ByteBuf out) {
        out.writeInt(req.term);
        out.writeInt(req.candidateId);
        out.writeInt(req.lastLogIndex);
        out.writeInt(req.lastLogTerm);
        out.writeChar('\n');
    }
}

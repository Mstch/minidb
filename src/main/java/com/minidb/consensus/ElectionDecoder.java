package com.minidb.consensus;

import com.minidb.consensus.raft.model.VoteReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class ElectionDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        int term = msg.readInt();
        int candidateId = msg.readInt();
        int lastLogIndex = msg.readInt();
        int lastLogTerm = msg.readInt();
        VoteReq voteReq = new VoteReq(term, candidateId, lastLogIndex, lastLogTerm);
        log.info("成功造型一个VoteReq:{}", voteReq);
        out.add(voteReq);
    }
}

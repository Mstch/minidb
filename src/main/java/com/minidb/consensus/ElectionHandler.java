package com.minidb.consensus;

import com.minidb.common.model.Node;
import com.minidb.common.Handler;
import com.minidb.common.NodeRoleEnum;
import com.minidb.consensus.raft.model.VoteReq;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;

@ChannelHandler.Sharable
@Log4j2
public class ElectionHandler extends ChannelInboundHandlerAdapter implements Handler {
    Election election = Election.instance;
    Node node = Node.instance;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //flower收到投票请求,判断是否
        if (msg instanceof VoteReq) {
            if (node.getRole().equals(NodeRoleEnum.FLOWER)
                    && ((VoteReq) msg).term >= node.getTerm()
                    && (node.getVoteFor() == null || node.getVoteFor().equals(((VoteReq) msg).candidateId))) {
                node.setVoteFor(((VoteReq) msg).candidateId);
                node.setTerm(((VoteReq) msg).term);
                election.restart();
            }
        }
    }
}

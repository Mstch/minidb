package com.minidb.consensus;

import com.minidb.common.Node;
import com.minidb.common.Handler;
import com.minidb.common.NodeRoleEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ElectionHandler extends ChannelInboundHandlerAdapter implements Handler {
    Election election = Election.instance;
    Node node = Node.instance;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof VoteReq) {
            if (node.getRole().equals(NodeRoleEnum.FLOWER)) {
                ctxWriteAndFlush(ctx, new int[]{1, 2, 3, 4});
                election.restart();
            }
        }
    }
}

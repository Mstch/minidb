package com.minidb.consensus;

import com.minidb.common.IOTypeConstants;
import com.minidb.common.model.Node;
import com.minidb.consensus.model.VoteReq;
import com.minidb.consensus.model.VoteResp;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ConsensusServer {
    private final Node node = Node.instance;
    private final NioEventLoopGroup boos = new NioEventLoopGroup();
    private final NioEventLoopGroup worker = new NioEventLoopGroup();
    private final ServerBootstrap bootstrap = new ServerBootstrap();
    private final VoteHandler voteHandler = new VoteHandler();

    private void listenElection() {

    }

    private void listenHeartbeat() {

    }

    public void start() {
        try {
            bootstrap.group(boos, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline()
                                    //拆包
                                    .addLast(new LineBasedFrameDecoder(256))
                                    .addLast(new ConsensusDispatcher())
                                    .addLast(new VoteEncoder())
                                    .addLast(voteHandler);
                        }
                    })
                    .bind(node.getElectionPort()).get();
        } catch (InterruptedException | ExecutionException e) {
            //TODO
        }
    }

    /**
     * 将追加/投票分发到对应handler上
     */
    private class ConsensusDispatcher extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            if (in.readableBytes() == 18) {
                byte type = in.readByte();
                if (type == IOTypeConstants.VOTE_REQ) {
                    int term = in.readInt();
                    int candidateId = in.readInt();
                    int lastLogIndex = in.readInt();
                    int lastLogTerm = in.readInt();
                    VoteReq req = new VoteReq(term, candidateId, lastLogIndex, lastLogTerm);
                    out.add(req);
                } else if (type == IOTypeConstants.APPEND_REQ) {

                }
            }
        }
    }


    /**
     * 处理投票
     */
    @ChannelHandler.Sharable
    private class VoteHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof VoteReq) {
                VoteReq req = (VoteReq) msg;
                boolean voteGranted =
                        //发起投票的节点任期大于收到投票信息的任期
                        (req.term > node.getTerm()
                                //收到投票信息的节点未给其他节点投过票
                                && (node.getVoteFor() == null || node.getVoteFor().equals(req.candidateId)));
                if (voteGranted) {
                    node.setVoteFor(((VoteReq) msg).candidateId);
                    node.setTerm(req.term);
                }
                ctx.writeAndFlush(new VoteResp(node.getTerm(), voteGranted));
            }
        }
    }

    /**
     * 投票响应
     */
    private class VoteEncoder extends MessageToByteEncoder<VoteResp> {

        @Override
        protected void encode(ChannelHandlerContext ctx, VoteResp msg, ByteBuf out) throws Exception {
            out.writeByte(IOTypeConstants.VOTE_RESP);
            out.writeInt(msg.term);
            out.writeBoolean(msg.voteGranted);
            out.writeChar('\n');
        }
    }

}

package com.minidb.consensus.raft;

import com.minidb.common.IOTypeConstants;
import com.minidb.common.KryoSerializer;
import com.minidb.consensus.raft.model.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ConsensusServer {
    private final Node node = Node.instance;
    private final VoteHandler voteHandler = new VoteHandler();
    private final LogHandler logHandler = new LogHandler();

    private void listenElection() {
        NioEventLoopGroup boos = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap electionBootstrap = new ServerBootstrap();
        try {
            electionBootstrap.group(boos, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline()
                                    //拆包
                                    .addLast(new LineBasedFrameDecoder(256))
                                    .addLast(new VoteDecoder())
                                    .addLast(voteHandler)
                                    .addLast(new VoteEncoder());
                        }
                    })
                    .bind(node.getElectionPort()).get();
        } catch (InterruptedException | ExecutionException e) {
            //TODO
        }
    }

    private void listenLog() {
        NioEventLoopGroup logBoos = new NioEventLoopGroup();
        NioEventLoopGroup logWorker = new NioEventLoopGroup();
        ServerBootstrap logBootstrap = new ServerBootstrap();
        try {
            logBootstrap.group(logBoos, logWorker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline()
                                    //拆包
                                    .addLast(new LengthFieldBasedFrameDecoder(4096 * 1024, 1, 4))
                                    .addLast(new LogEncoder())
                                    .addLast(logHandler);
                        }
                    })
                    .bind(node.getPort()).sync();
        } catch (InterruptedException ignore) {
        }
    }

    public void start() {
        listenElection();
        listenLog();
    }


    /**
     * 投票请求解码
     */
    private class VoteDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            byte type = in.readByte();
            if (type == IOTypeConstants.VOTE_REQ) {
                int term = in.readInt();
                int candidateId = in.readInt();
                int lastLogIndex = in.readInt();
                int lastLogTerm = in.readInt();
                VoteReq req = new VoteReq(term, candidateId, lastLogIndex, lastLogTerm);
                out.add(req);
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
     * 投票响应编码
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

    /**
     * 日志请求解码
     */
    private class LogDecoder extends ByteToMessageDecoder {


        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            if (in.readByte() == IOTypeConstants.APPEND_REQ) {
                int len = in.readInt();
                int term = in.readInt();
                int leaderId = in.readInt();
                int prevLogIndex = in.readInt();
                int prevLogTerm = in.readInt();
                List<Entries.Entry> entries = new ArrayList<>();
                while (in.readInt() != Integer.MAX_VALUE) {
                    int logLen = in.readInt();
                    int entryIndex = in.readInt();
                    int entryTerm = in.readInt();
                    byte[] logBytes = in.readBytes(logLen).array();
                    Log log = KryoSerializer.instance.deserialize(logBytes);
                    entries.add(node.getEntries().new Entry(entryIndex, entryTerm, log));
                }
                int leaderCommit = in.readerIndex();
                LogReq logReq = new LogReq(term, leaderId, prevLogIndex, prevLogTerm, entries, leaderCommit);
                out.add(logReq);
            }
        }
    }

    /**
     * 日志处理
     */
    @ChannelHandler.Sharable
    private class LogHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof LogReq) {
                LogReq req = (LogReq) msg;
            }
        }
    }

    private class LogEncoder extends MessageToByteEncoder<LogResp> {

        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, LogResp logResp, ByteBuf byteBuf) throws Exception {

        }
    }
}

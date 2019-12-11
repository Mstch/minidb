package com.minidb.consensus.raft;

import com.minidb.common.*;
import com.minidb.common.model.Node;
import com.minidb.common.model.Req;
import com.minidb.consensus.raft.model.VoteReq;
import com.minidb.consensus.raft.model.VoteResp;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class ConsensusClient {

    private Node node = Node.instance;
    private int CONNECT_NODE_NUMBER = node.getNodes().size();
    private AtomicInteger voteReqCount = new AtomicInteger(0);
    private AtomicInteger voteRespCount = new AtomicInteger(0);
    private Set<Channel> channels = new HashSet<>(CONNECT_NODE_NUMBER);
    private NioEventLoopGroup clientEventLoop = new NioEventLoopGroup(CONNECT_NODE_NUMBER);
    private ReentrantLock electionLock = new ReentrantLock();
    private Condition loopWait = electionLock.newCondition();
    private BlockingQueue<Node> unConnectNodes = new ArrayBlockingQueue<>(CONNECT_NODE_NUMBER);
    private VoteResponseHandler voteResponseHandler = new VoteResponseHandler();
    private Bootstrap client = new Bootstrap()
            .group(clientEventLoop)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 50000)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline()
                            //TODO  io.netty.handler.codec.TooLongFrameException: frame length (157) exceeds the allowed maximum (20)
                            .addLast(new LineBasedFrameDecoder(256))
                            .addLast(new ReqEncoder())
                            .addLast(new RespDecoder())
                            .addLast(voteResponseHandler);
                }
            });
    private Thread autoConnectThread = new Thread(() -> {
        while (true) {
            try {
                Node unConnectNode = unConnectNodes.take();
                client.connect(unConnectNode.getHost(), unConnectNode.getElectionPort())
                        .addListener(future -> {
                            if (!future.isSuccess()) {
                                log.warn("连接至:{}失败", unConnectNode);
                                unConnectNodes.put(unConnectNode);
                            } else {
                                log.info("连接至:{}成功!",unConnectNode);
                                channels.add(((ChannelFuture) future).channel());
                            }
                        });
            } catch (InterruptedException e) {
                //TODO
            }
        }
    });

    private Thread electionLoopThread = new Thread(() -> {
        while (true) {
            electionLock.lock();
            int random = new Random().nextInt(150) + 150;
            try {
                if (!loopWait.await(random * 10, TimeUnit.MILLISECONDS ) && channels.size() > 0) {
                    if (node.getRole().equals(NodeRoleEnum.FLOWER)) {
                        node.setRole(NodeRoleEnum.CANDIDATE);
                        voteReq();
                    } else if (node.getRole().equals(NodeRoleEnum.CANDIDATE)) {
                        voteReq();
                    }
                }
            } catch (InterruptedException e) {
                //TODO
            }
        }
    });

    public void start() {
        autoConnectThread.start();
        node.getNodes().stream()
                .filter(item -> !node.getId().equals(item.getId()))
                .forEach(item ->
                        client.connect(item.getHost(), item.getElectionPort())
                                .addListener((future) -> {
                                    log.warn("连接至:{}失败", item);
                                    if (!future.isSuccess()) {
                                        unConnectNodes.put(item);
                                    } else {
                                        log.info("连接至:{}成功!",item);
                                        channels.add(((ChannelFuture) future).channel());
                                    }
                                })
                );
        try {
            Thread.sleep(1000);
            electionLoopThread.start();
        } catch (InterruptedException e) {
            //TODO
        }

    }

    public void voteReq() {
        node.setVoteFor(node.getId());
        node.setTerm(node.getTerm() + 1);
        log.info("{}发起投票，任期{}", node.getId(), node.getTerm());
        VoteReq req = new VoteReq(node.getTerm(), node.getId(), 1, 1);

        channels.forEach(ch -> {
            try {
                ch.writeAndFlush(req).addListener(future -> {
                    if (future.isSuccess()) {
                        voteReqCount.incrementAndGet();
                    }
                }).sync();
            } catch (InterruptedException e) {
                //TODO
            }
        });
    }

    public void heartbeatReq() {

    }

    /**
     * 投票/心跳请求编码器
     */
    private class ReqEncoder extends MessageToByteEncoder<Req> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Req msg, ByteBuf out) throws Exception {
            if (msg instanceof VoteReq) {
                VoteReq req = (VoteReq) msg;
                out.writeByte(IOTypeConstants.VOTE_REQ);
                out.writeInt(req.term);
                out.writeInt(req.candidateId);
                out.writeInt(req.lastLogIndex);
                out.writeInt(req.lastLogTerm);
                out.writeChar('\n');
            }
        }
    }


    /**
     * 投票/心跳响应解码器
     */
    private class RespDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            if (in.readByte() == IOTypeConstants.VOTE_RESP) {
                out.add(new VoteResp(in.readInt(), in.readBoolean()));
            }
        }
    }

    @ChannelHandler.Sharable
    private class VoteResponseHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof VoteResp) {
                VoteResp resp = (VoteResp) msg;
                log.info("收到来自{}的投票响应:{}", ctx.channel().remoteAddress(), resp);
                if (resp.term > node.getTerm()) {
                    node.setTerm(resp.term);
                    node.setRole(NodeRoleEnum.FLOWER);
                }
                if (resp.voteGranted) {
                    if (voteRespCount.incrementAndGet() > voteReqCount.get() / 2) {
                        node.setRole(NodeRoleEnum.LEADER);
                    }
                }
            }
        }
    }


}

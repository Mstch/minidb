package com.minidb.consensus;

import com.minidb.consensus.raft.model.Node;
import com.minidb.common.NodeRoleEnum;
import com.minidb.common.ExtensionBlockingQueue;
import com.minidb.consensus.raft.model.VoteReq;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Log4j2
public class Election {
    private Node node = Node.instance;
    public ElectionIO electionIO = new ElectionIO();
    public static Election instance = new Election();
    private boolean restart = false;
    private final Map<NodeRoleEnum, Function<Thread, Void>> electionTimeoutMap = new HashMap<>();
    private ReentrantLock lock = new ReentrantLock();
    private Condition trip = lock.newCondition();
    private Thread mainThread = new Thread(() -> {
        lock.lock();
        while (true) {
            int sleep = new Random().nextInt(150) + 150;
            try {
                trip.await(sleep, TimeUnit.MILLISECONDS);
                if (!restart) {
                    electionTimeoutMap.get(node.getRole()).apply(Thread.currentThread());
                }
            } catch (InterruptedException ignored) {
            }
        }
    });


    public void restart() {
        trip.signalAll();
        restart = true;
    }

    public void voted() {

    }

    public Election() {
        electionTimeoutMap.put(NodeRoleEnum.FLOWER, (thread) -> {
            node.setRole(NodeRoleEnum.CANDIDATE);
            node.setTerm(node.getTerm() + 1);
            electionIO.reqVote();
            return null;
        });
        electionTimeoutMap.put(NodeRoleEnum.CANDIDATE, (thread) -> {
            return null;
        });
    }

    public void start() {
        electionIO.start();
        mainThread.start();
    }

    public class ElectionIO {
        NioEventLoopGroup boos = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        NioEventLoopGroup clientWorker = new NioEventLoopGroup();
        List<Channel> channels = new ArrayList<>();
        final ServerBootstrap bootstrap = new ServerBootstrap();
        final Bootstrap clientBootstrap = new Bootstrap();

        ExtensionBlockingQueue<Node> unConnectNodes = new ExtensionBlockingQueue<>(node.getNodes().size());

        public void start() {
            ElectionHandler handler = new ElectionHandler();
            try {
                bootstrap.group(boos, worker)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                        .childHandler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) {
                                log.info("新连接接入: {}:{}", ch.remoteAddress().getHostString(), ch.remoteAddress().getPort());
                                ch.pipeline().addLast(new ByteToMessageDecoder() {
                                    @Override
                                    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                                        String str = in.toString(CharsetUtil.UTF_8);
                                        if (str.startsWith("hello i'm ")) {
                                            log.info(str);
                                        }
                                    }
                                }).addLast(new ElectionDecoder()).addLast(handler);
                            }
                        })
                        .bind(node.getPort())
                        .addListener((ChannelFutureListener) future -> log.info("选举eventLoop已开启"))
                        .sync();
                clientBootstrap
                        .group(clientWorker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) {
                                ch.pipeline().addLast(new ElectionEncoder());
                            }
                        });

                new Thread(() -> {
                    node.getNodes().stream().filter(item -> !item.getId().equals(node.getId())).forEach(unConnectNodes::offer);
                    while (true) {
                        Node unConnectNode = null;
                        try {
                            unConnectNode = unConnectNodes.take();
                        } catch (InterruptedException e) {
                            //TODO
                        }
                        ChannelFuture channelFuture = clientBootstrap
                                .connect(unConnectNode.getHost(), unConnectNode.getElectionPort());
                        try {
                            if (channelFuture.isSuccess() || channelFuture.await(1000L)) {
                                if (channelFuture.isSuccess()) {
                                    NioSocketChannel ch = (NioSocketChannel) channelFuture.channel();
                                    channels.add(ch);
                                    log.info("连接到服务端:{}:{}", ch.remoteAddress().getHostString(), ch.remoteAddress().getPort());
                                    ch.writeAndFlush(ByteBufUtil.writeUtf8(ch.alloc(), "hello i'm " + node.getId())).sync();
                                }
                            } else {
                                channelFuture.cancel(false);
                            }
                        } catch (InterruptedException e) {
                            //TODO
                        }
                    }
                }).start();
            } catch (InterruptedException ex) {
                //TODO
            }
        }


        public void reqVote() {
            channels.forEach(channel -> {
                channel.writeAndFlush(new VoteReq(node.getTerm(), node.getId(), 1, 1));
            });
        }


    }
}

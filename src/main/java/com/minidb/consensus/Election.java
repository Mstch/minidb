package com.minidb.consensus;

import com.minidb.common.Node;
import com.minidb.common.NodeRoleEnum;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
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

        BlockingQueue<Node> unConnectNodes = new LinkedBlockingDeque<>();

        public void start() {
            ElectionHandler handler = new ElectionHandler();
            LineBasedFrameDecoder lineDecoder = new LineBasedFrameDecoder(31);
            try {
                bootstrap.group(boos, worker)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                        .childHandler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                log.info("新连接接入: {}:{}", ch.remoteAddress().getHostString(), ch.remoteAddress().getPort());
                                ch.pipeline().addLast(new ElectionDecoder()).addLast(handler);
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
                            protected void initChannel(NioSocketChannel ch)  {
                                ch.pipeline().addLast(new ElectionEncoder());
                            }
                        });

                node.getNodes().stream().filter(item -> !item.getId().equals(node.getId())).forEach(unConnectNodes::offer);
                while (true) {
                    Node unConnectNode = unConnectNodes.take();
                    ChannelFuture channelFuture = clientBootstrap.connect(unConnectNode.getHost(), unConnectNode.getElectionPort());
                    if (channelFuture.await(1000L)) {
                        channels.add(channelFuture.channel());
                    } else {
                        unConnectNodes.offer(unConnectNode);
                    }

                }
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

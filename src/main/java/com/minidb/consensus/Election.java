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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Log4j2
public class Election {
    private Node node = Node.instance;
    public static Election instance = new Election();
    private boolean restart = false;
    private final Map<NodeRoleEnum, Function<Thread, Void>> roleMap = new HashMap<>();
    private ReentrantLock lock = new ReentrantLock();
    private Condition trip = lock.newCondition();
    private Thread mainThread = new Thread(() -> {
        lock.lock();
        while (true) {
            int sleep = new Random().nextInt(150) + 150;
            try {
                trip.await(sleep, TimeUnit.MILLISECONDS);
                if (!restart) {
                    roleMap.get(node.getRole()).apply(Thread.currentThread());
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
        roleMap.put(NodeRoleEnum.FLOWER, (thread) -> {
            return null;
        });
        roleMap.put(NodeRoleEnum.CANDIDATE, (thread) -> {
            return null;
        });
    }

    public void start() {

        ElectionIO server = new ElectionIO();
        server.start();
        mainThread.start();
    }

    class ElectionIO {
        NioEventLoopGroup boos = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        NioEventLoopGroup clientWorker = new NioEventLoopGroup();
        List<Channel> channels = new ArrayList<>();
        final ServerBootstrap bootstrap = new ServerBootstrap();
        final Bootstrap clientBootstrap = new Bootstrap();

        public void start() {
            ElectionHandler handler = new ElectionHandler();
            ElectionDecoder decoder = new ElectionDecoder();
            LineBasedFrameDecoder lineDecoder = new LineBasedFrameDecoder(31);
            try {
                bootstrap.group(boos, worker)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                        .handler(new ChannelHandler() {
                            @Override
                            public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

                            }

                            @Override
                            public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

                            }
                        })
                        .childHandler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                ch.pipeline().addLast(decoder).addLast(handler);
                            }
                        })
                        .bind(node.getPort())
                        .addListener((ChannelFutureListener) future -> log.info(""))
                        .sync();
                clientBootstrap
                        .group(clientWorker)
                        .channel(NioSocketChannel.class);
//                for (Node subNode : Election.this.node.getNodes()) {
//                    ChannelFuture channelFuture = clientBootstrap.connect(subNode.getHost(), subNode.getElectionPort());
//                    channels.add(channelFuture.channel());
//                }
            } catch (InterruptedException e) {
                //TODO
            }
        }

        public void reqVote() {
        }
    }
}

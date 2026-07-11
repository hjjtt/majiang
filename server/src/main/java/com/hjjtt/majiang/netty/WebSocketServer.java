package com.hjjtt.majiang.netty;

import com.hjjtt.majiang.room.RoomManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty WebSocket 服务。握手路径 /ws。
 * Pipeline：HttpServerCodec → HttpObjectAggregator → WebSocketServerProtocolHandler → GameFrameHandler
 */
public class WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    private static final String WS_PATH = "/ws";
    private static final int MAX_CONTENT_LENGTH = 64 * 1024;

    private final int port;
    private final RoomManager rooms;

    public WebSocketServer(int port, RoomManager rooms) {
        this.port = port;
        this.rooms = rooms;
    }

    public void start() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpServerCodec());
                        p.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
                        // 自动处理 WebSocket 握手 + close/ping/pong
                        p.addLast(new WebSocketServerProtocolHandler(WS_PATH));
                        p.addLast(new GameFrameHandler(rooms));
                    }
                });

            ChannelFuture f = b.bind(port).sync();
            log.info("麻将 WebSocket 服务已启动 → ws://localhost:{}{}", port, WS_PATH);
            f.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

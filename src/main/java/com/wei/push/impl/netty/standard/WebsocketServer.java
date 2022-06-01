package com.wei.push.impl.netty.standard;

import com.wei.push.impl.netty.standard.stomp.StompVersion;
import com.wei.push.impl.netty.standard.stomp.StompWebSocketProtocolDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author lw
 * @Date 2022/1/19  下午4:53
 * @Version 1.0
 */
@Component
@Slf4j
public class WebsocketServer implements SmartInitializingSingleton {


    @Resource
    private StompWebSocketProtocolDecoder stompWebSocketProtocolDecoder;

    @Value("${ws.port:8424}")
    private Integer port;

    private static WebsocketServerConfig config;

    public void init(WebsocketServerConfig websocketServerConfig) {
        config = websocketServerConfig;
    }

    public void start() {
        init(new WebsocketServerConfig(port));
        EventLoopGroup boss = new NioEventLoopGroup(config.getBossLoopGroupThreads());
        EventLoopGroup worker = new NioEventLoopGroup(config.getWorkerLoopGroupThreads());
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                //日志级别
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        //心跳监测
                        pipeline.addLast(new IdleStateHandler(180, 0, 0));
                        pipeline.addLast(new HeartBeatHandler());
                        //解析websocket协议
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                        pipeline.addLast(new WebSocketServerProtocolHandler("/websocket/hop-internet-hospital",
                                StompVersion.SUB_PROTOCOLS));
                        //解析stomp协议
                        pipeline.addLast(stompWebSocketProtocolDecoder);
                    }
                });
        bootstrap.bind(config.getPort()).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
            log.info("websocket启动成功：" + config.getHost() + ":" + config.getPort());
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boss.shutdownGracefully().syncUninterruptibly();
            worker.shutdownGracefully().syncUninterruptibly();
        }));
    }

    @Override
    public void afterSingletonsInstantiated() {
        start();
    }
}

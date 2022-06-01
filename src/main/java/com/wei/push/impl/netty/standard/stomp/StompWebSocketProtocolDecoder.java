package com.wei.push.impl.netty.standard.stomp;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete;
import io.netty.handler.codec.stomp.StompSubframeAggregator;
import io.netty.handler.codec.stomp.StompSubframeDecoder;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author blingweiwei
 */
@Sharable
@Component
public class StompWebSocketProtocolDecoder extends MessageToMessageDecoder<WebSocketFrame> {

    @Resource
    private StompChatHandler stompChatHandler;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //成功握手后的操作
        if (evt instanceof HandshakeComplete) {
            StompVersion stompVersion = StompVersion.findBySubProtocol(((HandshakeComplete) evt).selectedSubprotocol());
            ctx.channel().attr(StompVersion.CHANNEL_ATTRIBUTE_KEY).set(stompVersion);
            ctx.pipeline()
               .addLast(new WebSocketFrameAggregator(65536))
               .addLast(new StompSubframeDecoder()).addLast(new StompWebSocketFrameEncoder())
               .addLast(new StompSubframeAggregator(65536))
               .addLast(stompChatHandler);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame, List<Object> out) {
        if (webSocketFrame instanceof TextWebSocketFrame || webSocketFrame instanceof BinaryWebSocketFrame) {
            out.add(webSocketFrame.content().retain());
        } else {
            ctx.close();
        }
    }
}

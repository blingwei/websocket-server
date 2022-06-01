package com.wei.push.impl.netty.standard.stomp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.stomp.LastStompContentSubframe;
import io.netty.handler.codec.stomp.StompContentSubframe;
import io.netty.handler.codec.stomp.StompFrame;
import io.netty.handler.codec.stomp.StompHeaders;
import io.netty.handler.codec.stomp.StompHeadersSubframe;
import io.netty.handler.codec.stomp.StompSubframe;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author blingweiwei
 */
public class StompWebSocketFrameEncoder extends MessageToMessageEncoder<StompSubframe> {

    @Override
    protected void encode(ChannelHandlerContext ctx, StompSubframe msg, List<Object> out) throws Exception {
        if (msg instanceof StompFrame) {
            StompFrame stompFrame = (StompFrame) msg;
            ByteBuf buf = encodeFullFrame(stompFrame, ctx);

            out.add(convertFullFrame(stompFrame, buf));
        } else if (msg instanceof StompHeadersSubframe) {
            StompHeadersSubframe stompHeadersSubframe = (StompHeadersSubframe) msg;
            ByteBuf buf = ctx.alloc().buffer(headersSubFrameSize(stompHeadersSubframe));
            encodeHeaders(stompHeadersSubframe, buf);

            out.add(convertHeadersSubFrame(stompHeadersSubframe, buf));
        } else if (msg instanceof StompContentSubframe) {
            StompContentSubframe stompContentSubframe = (StompContentSubframe) msg;
            ByteBuf buf = encodeContent(stompContentSubframe, ctx);

            out.add(convertContentSubFrame(stompContentSubframe, buf));
        }
    }

    protected WebSocketFrame convertFullFrame(StompFrame original, ByteBuf encoded) {
        if (isTextFrame(original)) {
            return new TextWebSocketFrame(encoded);
        }

        return new BinaryWebSocketFrame(encoded);
    }


    protected WebSocketFrame convertHeadersSubFrame(StompHeadersSubframe original, ByteBuf encoded) {
        if (isTextFrame(original)) {
            return new TextWebSocketFrame(false, 0, encoded);
        }

        return new BinaryWebSocketFrame(false, 0, encoded);
    }

    private static boolean isTextFrame(StompHeadersSubframe headersSubframe) {
        String contentType = headersSubframe.headers().getAsString(StompHeaders.CONTENT_TYPE);
        return contentType != null && (contentType.startsWith("text") || contentType.startsWith("application/json"));
    }


    protected WebSocketFrame convertContentSubFrame(StompContentSubframe original, ByteBuf encoded) {
        if (original instanceof LastStompContentSubframe) {
            return new ContinuationWebSocketFrame(true, 0, encoded);
        }

        return new ContinuationWebSocketFrame(false, 0, encoded);
    }

    /**
     * Returns a heuristic size for headers (32 bytes per header line) + (2 bytes for colon and eol) + (additional
     * command buffer).
     */
    protected int headersSubFrameSize(StompHeadersSubframe headersSubframe) {
        int estimatedSize = headersSubframe.headers().size() * 34 + 48;
        if (estimatedSize < 128) {
            return 128;
        } else if (estimatedSize < 256) {
            return 256;
        }

        return estimatedSize;
    }

    private ByteBuf encodeFullFrame(StompFrame frame, ChannelHandlerContext ctx) {
        int contentReadableBytes = frame.content().readableBytes();
        ByteBuf buf = ctx.alloc().buffer(headersSubFrameSize(frame) + contentReadableBytes);
        encodeHeaders(frame, buf);

        if (contentReadableBytes > 0) {
            buf.writeBytes(frame.content());
        }

        return buf.writeByte(StompConstants.NUL);
    }

    private static void encodeHeaders(StompHeadersSubframe frame, ByteBuf buf) {
        ByteBufUtil.writeUtf8(buf, frame.command().toString());
        buf.writeByte(StompConstants.LF);

        for (Entry<CharSequence, CharSequence> entry : frame.headers()) {
            ByteBufUtil.writeUtf8(buf, entry.getKey());
            buf.writeByte(StompConstants.COLON);
            ByteBufUtil.writeUtf8(buf, entry.getValue());
            buf.writeByte(StompConstants.LF);
        }

        buf.writeByte(StompConstants.LF);
    }

    private static ByteBuf encodeContent(StompContentSubframe content, ChannelHandlerContext ctx) {
        if (content instanceof LastStompContentSubframe) {
            ByteBuf buf = ctx.alloc().buffer(content.content().readableBytes() + 1);
            buf.writeBytes(content.content());
            buf.writeByte(StompConstants.NUL);
            return buf;
        }

        return content.content().retain();
    }
}

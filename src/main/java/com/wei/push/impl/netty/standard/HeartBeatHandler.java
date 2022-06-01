package com.wei.push.impl.netty.standard;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Author lw
 * @Date 2022/1/20  下午2:22
 * @Version 1.0
 */
public class HeartBeatHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                ChannelManager.removeChannel(ctx.channel().attr(ChannelManager.USER_ID).get());
                ctx.close();
            }
        }
    }

}

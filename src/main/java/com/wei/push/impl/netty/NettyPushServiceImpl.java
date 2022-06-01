package com.wei.push.impl.netty;

import static io.netty.handler.codec.stomp.StompHeaders.CONTENT_LENGTH;
import static io.netty.handler.codec.stomp.StompHeaders.CONTENT_TYPE;
import static io.netty.handler.codec.stomp.StompHeaders.DESTINATION;
import static io.netty.handler.codec.stomp.StompHeaders.MESSAGE_ID;
import static io.netty.handler.codec.stomp.StompHeaders.SUBSCRIPTION;

import cn.hutool.core.collection.CollectionUtil;
import com.wei.push.bo.Message;
import com.wei.push.impl.DefaultPushService;
import com.wei.push.impl.netty.standard.ChannelManager;
import com.wei.push.impl.netty.standard.stomp.StompChatHandler;
import com.wei.push.impl.netty.standard.stomp.StompSubscription;
import io.netty.channel.Channel;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompFrame;
import io.netty.util.CharsetUtil;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Author lw
 * @Date 2022/1/24  下午2:40
 * @Version 1.0
 */
@Service("nettyPushServiceImpl")
@Slf4j
public class NettyPushServiceImpl extends DefaultPushService {


    @Override
    public boolean checkUserConnectedToThisServer(String userId) {
        return ChannelManager.getChannel(userId).isPresent();
    }

    @Override
    public boolean sendMsgToUserFromThisServer(String userId, Message message) {
        Optional<Channel> optionalChannel = ChannelManager.getChannel(userId);
        if (!optionalChannel.isPresent() || message == null || StringUtils.isBlank(message.getSeq())) {
            return false;
        }
        try {
            Channel channel = optionalChannel.get();
            Set<StompSubscription> destList = channel.attr(StompChatHandler.DESTINATIONS).get();
            if(CollectionUtil.isNotEmpty(destList)){
                for(StompSubscription stompSubscription: destList){
                    if(stompSubscription.destination().equals(message.getDest())){
                        channel.writeAndFlush(transformToMessage(message.getSeq(), message.getContent(),stompSubscription.destination(),  stompSubscription.id()));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("消息发送失败" + message.toString());
            log.error(e.getMessage());
        }
        return false;
    }

    private static StompFrame transformToMessage(String messageId, String content, String dest, String destId) {
        StompFrame messageFrame = new DefaultStompFrame(StompCommand.MESSAGE);
        messageFrame.content().writeCharSequence(content, CharsetUtil.UTF_8);
        messageFrame.headers()
                .set(MESSAGE_ID, messageId)
                .set(SUBSCRIPTION, destId)
                .set(CONTENT_LENGTH, Integer.toString(messageFrame.content().readableBytes()))
                .set(DESTINATION, dest)
                .set(CONTENT_TYPE, "text/plain");
        return messageFrame;
    }

}


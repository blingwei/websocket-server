package com.wei.push.common;

import com.wei.push.impl.DefaultPushService;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @Descripition:
 * @Author: wb-cgh760152
 * @Date: 2020/10/15
 * @Version: 1.0
 */
@Configuration
public class RedisSubConfig {

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter,new ChannelTopic(Constants.SEND_TO_USER_CHANNEL_DEFAULT));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(){
        return new MessageListenerAdapter(pushService,"onMessage");
    }

    @Resource
    private DefaultPushService pushService;
}

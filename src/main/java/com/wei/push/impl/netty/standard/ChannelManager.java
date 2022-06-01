package com.wei.push.impl.netty.standard;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

/**
 * 连接管理器
 * @Author lw
 * @Date 2022/1/21  下午4:45
 * @Version 1.0
 */
public class ChannelManager {

    private static final ConcurrentHashMap<String, Channel> CHANNEL_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();
    public static final AttributeKey<String> USER_ID = AttributeKey.valueOf("userId");

    /**
     *
     */
    public static void addChannel(String userId, Channel channel){
        if(StringUtils.isEmpty(userId)){
            userId = UUID.randomUUID().toString();
        }
        channel.attr(ChannelManager.USER_ID).set(userId);
        CHANNEL_CONCURRENT_HASH_MAP.put(userId, channel);
    }

    public static void removeChannel(String userId){
        CHANNEL_CONCURRENT_HASH_MAP.remove(userId);
    }

    public static Optional<Channel>  getChannel(String userId){
        return Optional.ofNullable(CHANNEL_CONCURRENT_HASH_MAP.get(userId));
    }

    public static Integer getUserNum(){
        return CHANNEL_CONCURRENT_HASH_MAP.size();
    }




}

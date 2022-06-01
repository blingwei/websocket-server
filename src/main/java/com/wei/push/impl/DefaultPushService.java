package com.wei.push.impl;

import cn.hutool.json.JSONUtil;
import com.wei.push.bo.ClusterMessage;
import com.wei.push.bo.Message;
import com.wei.push.common.Constants;
import java.util.ArrayList;
import java.util.List;
import com.wei.push.PushService;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

/**
 * @Author lw
 * @Date 2022/2/21  下午6:50
 * @Version 1.0
 */
@Slf4j
public abstract class DefaultPushService implements PushService, MessageListener {

    @Resource
    private RedisTemplate<String, Object> stringRedisTemplate;

    @Override
    public Map<Object, Object> getSupplementMsg(String userId, String dest) {
        String redisKey = getRedisKeyOfSupplement(userId, dest);
        return stringRedisTemplate.opsForHash().entries(redisKey);
    }

    @Override
    public boolean pushSupplementMsgMerge(String userId, String dest) {
        String redisKey = getRedisKeyOfSupplement(userId, dest);
        List<Object> list = stringRedisTemplate.opsForHash().values(redisKey);
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<Message> messages = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Message) {
                messages.add((Message) o);
            }
        }
        //只发一条
        if (sendToUser(userId, dest, messages.get(0))) {
            //删除全部
            delMsgListForSupplement(userId, dest, messages);
        }
        return true;
    }

    @Override
    public boolean pushSupplementMsg(String userId, String dest) {
        String redisKey = getRedisKeyOfSupplement(userId, dest);
        List<Object> list = stringRedisTemplate.opsForHash().values(redisKey);
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }
        for (Object o : list) {
            Message msg = JSONUtil.toBean((String) o, Message.class);
            if (sendToUser(userId, dest, msg)) {
                delMsgForSupplement(userId, dest, msg);
            } else {
                return false;
            }

        }
        return true;
    }

    @Override
    public boolean delMsgForSupplement(String userId, String dest, Message message) {
        String redisKey = getRedisKeyOfSupplement(userId, dest);
        stringRedisTemplate.opsForHash().delete(redisKey, message.getSeq());
        return true;
    }

    @Override
    public boolean delMsgListForSupplement(String userId, String dest, List<Message> messages) {
        String redisKey = getRedisKeyOfSupplement(userId, dest);
        if (CollectionUtils.isEmpty(messages)) {
            return false;
        }
        stringRedisTemplate.opsForHash().delete(redisKey, messages.stream().map(Message::getSeq).collect(
                Collectors.toList()));
        return true;
    }

    private String getRedisKeyOfSupplement(String userId, String dest) {
        return Constants.WS_MSG_SUPPLEMENT + dest + ":" + userId;
    }

    @Override
    public boolean saveMsgForSupplement(String userId, String dest, Message message, Long expireMinutes) {
        //消息已经被保存为离线消息
        if (Constants.WS_MSG_STATUS_SUPPLEMENT.equals(message.getSendStatus())) {
            return true;
        }
        String redisKey = getRedisKeyOfSupplement(userId, dest);
        message.setSendStatus(Constants.WS_MSG_STATUS_SUPPLEMENT);
        stringRedisTemplate.opsForHash().put(redisKey, message.getSeq(), JSONUtil.toJsonStr(message));
        //默认半个小时
        stringRedisTemplate.expire(redisKey, Optional.ofNullable(expireMinutes).orElse(30L), TimeUnit.MINUTES);
        return true;
    }

    @Override
    public boolean sendToUser(String userId, String dest, Message message) {
        return sendToUserMsg(userId, dest, message, false, null);
    }


    @Override
    public boolean sendToUser(String userId, String dest, List<Message> messages) {
        for (Message message : messages) {
            sendToUser(userId, dest, message);
        }
        return true;
    }

    @Override
    public boolean sendToUserWithSupplement(String userId, String dest, Message message, Long expireMinutes) {
        return sendToUserMsg(userId, dest, message, true, expireMinutes);
    }

    @Override
    public boolean sendToUserWithSupplement(String userId, String dest, List<Message> messages, Long expireMinutes) {
        for (Message message : messages) {
            sendToUserWithSupplement(userId, dest, message, expireMinutes);
        }
        return true;
    }

    private Boolean sendToUserMsg(String userId, String dest, Message message, Boolean supplement, Long expireMinutes) {
        try {
            if (checkUserConnectedToCluster(userId)) {
                if (supplement) {
                    //保存为离线消息
                    saveMsgForSupplement(userId, dest, message, expireMinutes);
                }
                //与本机相连
                if (checkUserConnectedToThisServer(userId)) {
                    sendMsgToUserFromThisServer(userId, message);
                } else {
                    ClusterMessage clusterMessage = new ClusterMessage();
                    clusterMessage.setMessage(message);
                    clusterMessage.setSupplement(supplement);
                    clusterMessage.setExpireMinutes(expireMinutes);
                    clusterMessage.setUserId(userId);
                    broadcastMsgToCluster(dest, clusterMessage);
                }
                return true;
            } else {
                log.info("用户{}未连接，WS消息发送失败", userId);
                return false;
            }
        } catch (Exception e) {
            log.error("WebSocketPushService.sendToUser fail", e);
        }
        return false;
    }

    @Override
    public boolean sendToUserFromThisServer(String userId, Message message) {
        try {
            if (checkUserConnectedToThisServer(userId)) {
                return sendMsgToUserFromThisServer(userId, message);
            }
            return false;
        } catch (Exception e) {
            log.error("WebSocketPushService.sendToUserFromThisServer fail", e);
            return false;
        }
    }

    @Override
    public boolean sendToUserFromThisServer(String userId, List<Message> messages) {
        for (Message message : messages) {
            sendToUserFromThisServer(userId, message);
        }
        return true;
    }

    private boolean checkUserConnectedToCluster(String userId) {
        return stringRedisTemplate.opsForValue().get(Constants.WS_CLUSTER_USER_ID + userId) != null;
    }

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] bytes) {
        String channel = stringRedisTemplate.getStringSerializer().deserialize(message.getChannel());
        String body = stringRedisTemplate.getStringSerializer().deserialize(message.getBody());
        ClusterMessage msg = JSONUtil.toBean(body, ClusterMessage.class);
        log.info("redis onMessage: channel={} body={}", channel, body);
        if (Objects.equals(channel, Constants.SEND_TO_USER_CHANNEL_DEFAULT) && msg != null) {
            //本机发送消息失败并且需要补发消息
            if (!sendMsgToUserFromThisServer(msg.getUserId(), msg.getMessage()) && Boolean.TRUE
                    .equals(msg.getSupplement())) {
                //补发消息
                saveMsgForSupplement(msg.getUserId(), msg.getDest(), msg.getMessage(), msg.getExpireMinutes());
            }
        }
    }

    /**
     * 广播至集群
     */
    public void broadcastMsgToCluster(String channel, ClusterMessage message) {
        stringRedisTemplate.convertAndSend(channel, JSONUtil.toJsonStr(message));
        log.info("ws消息广播至集群{}", message);
    }

    /**
     * 是否与本机相连
     *
     * @param userId
     * @return
     */
    protected abstract boolean checkUserConnectedToThisServer(String userId);

    /**
     * 本机发送消息
     *
     * @param userId
     * @param message
     * @return
     */
    protected abstract boolean sendMsgToUserFromThisServer(String userId, Message message);

}

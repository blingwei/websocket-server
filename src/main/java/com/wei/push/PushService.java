package com.wei.push;

import com.wei.push.bo.Message;
import java.util.List;
import java.util.Map;

/**
 * 推送服务
 * @Author lw
 * @Date 2022/1/19  下午4:50
 * @Version 1.0
 */
public interface PushService {
    /**
     * 获取待补发消息
     * @param userId
     * @param dest
     * @return
     */
    Map<Object, Object> getSupplementMsg(String userId, String dest);


    /**
     * 补发离线消息（所有离线消息合并）
     * @param userId
     * @param dest
     * @return
     */
    boolean pushSupplementMsgMerge(String userId, String dest);

    /**
     * 补发离线消息
     * @param userId
     * @param dest
     * @return
     */
    boolean pushSupplementMsg(String userId, String dest);

    /**
     * 删除补发的消息
     * @param userId
     * @param dest
     * @param message
     * @return
     */
    boolean delMsgForSupplement(String userId, String dest, Message message);

    /**
     * 删除补发的消息
     * @param userId
     * @param dest
     * @param messages
     * @return
     */
    boolean delMsgListForSupplement(String userId, String dest, List<Message> messages);

    /**
     * 保存消息用于补发
     * @param userId
     * @param dest
     * @param message
     * @param expireMinutes
     * @return
     */
    boolean saveMsgForSupplement(String userId, String dest, Message message, Long expireMinutes);


    /**
     * 发送消息给user  不需要补发
     * @param userId
     * @param dest
     * @param message
     * @return
     */
    boolean sendToUser(String userId, String dest, Message message);

    /**
     * 发送消息给user  不需要补发
     * @param userId
     * @param dest
     * @param messages
     * @return
     */
    boolean sendToUser(String userId, String dest, List<Message> messages);

    /**
     * 发送消息，如果没发送成功，需要保存补发
     * @param userId
     * @param dest
     * @param message
     * @param expireMinutes
     * @return
     */
    boolean sendToUserWithSupplement(String userId, String dest, Message message, Long expireMinutes);

    /**
     * 发送消息，如果没发送成功，需要保存补发
     * @param userId
     * @param dest
     * @param messages
     * @param expireMinutes
     * @return
     */
    boolean sendToUserWithSupplement(String userId, String dest, List<Message> messages, Long expireMinutes);

    /**
     * 从本机发送ws信息
     * @param userId
     * @param message
     * @return
     */
    boolean sendToUserFromThisServer(String userId, Message message);

    /**
     * 从本机发送ws信息
     * @param userId
     * @param messages
     * @return
     */
    boolean sendToUserFromThisServer(String userId, List<Message> messages);
}

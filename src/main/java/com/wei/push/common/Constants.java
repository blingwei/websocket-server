package com.wei.push.common;

/**
 * @Descripition:
 * @Author: wb-cgh760152
 * @Date: 2020/7/22
 * @Version: 1.0
 */
public class Constants {

    /**
     * redis保存用户id key prefix
     * 支付宝pid
     */
    public static final String WS_CLUSTER_USER_ID = "mini:ws:cluster:user:id:";

    /**
     * redis 保存ws用户id过期时间
     */
    public static final Long WS_SESSION_EXPIRE_MINUTES = 60L;

    /**
     * 点对点发送，redis广播channel
     */
    public static final String SEND_TO_USER_CHANNEL_DEFAULT = "mini:ws:channel:default";

    /**
     * 消息补发存储
     */
    public static final String WS_MSG_SUPPLEMENT = "ws:msg:supplement:";

    /**
     * 消息已保存为离线消息
     */
    public static final String WS_MSG_STATUS_SUPPLEMENT = "supplement";

}

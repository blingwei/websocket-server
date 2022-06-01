package com.wei.push.bo;

import lombok.Data;

/**
 * @Author lw
 * @Date 2022/2/21  下午7:16
 * @Version 1.0
 */
@Data
public class ClusterMessage {

    private Boolean supplement;

    /**
     * 过期时间
     */
    private Long expireMinutes;

    private Message message;

    private String userId;

    private String dest;

}

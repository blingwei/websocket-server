package com.wei.push.bo;

import lombok.Data;

/**
 * @Author lw
 * @Date 2022/1/19  下午4:51
 * @Version 1.0
 */
@Data
public class Message {

    private String from;

    private String to;

    private String seq;

    private String type;

    private String sendStatus;

    private String content;

    private String contentType;

    private String dest;

    public static Message genErrMessage(String content) {
        Message message = new Message();
        message.setContent(content);
        message.setType("error");
        return message;
    }

}

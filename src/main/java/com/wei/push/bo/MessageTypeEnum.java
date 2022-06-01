package com.wei.push.bo;

import java.util.Arrays;
import lombok.Getter;

/**
 * @Author lw
 * @Date 2022/1/21  上午11:24
 * @Version 1.0
 */
@Getter
public enum MessageTypeEnum {

    DEFAULT("default", "默认普通消息"),
    ACK("ack", "确认消息")
    ;


    MessageTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    private String type;
    private String desc;

    public static MessageTypeEnum getEnumType(String type) {
        return Arrays.asList(MessageTypeEnum.values()).stream()
                .filter(e -> e.getType().equals(type))
                .findFirst().orElse(MessageTypeEnum.DEFAULT);
    }
}

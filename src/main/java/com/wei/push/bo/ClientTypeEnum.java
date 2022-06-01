package com.wei.push.bo;

import java.util.Arrays;
import lombok.Getter;

/**
 * @Author lw
 * @Date 2022/1/21  上午11:24
 * @Version 1.0
 */
@Getter
public enum ClientTypeEnum {

    DEFAULT("default", "默认"),
    PATIENT("patient", "患者端"),
    DOCTOR("doctor", "医生端")
    ;


    ClientTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    private String type;
    private String desc;

    public static ClientTypeEnum getEnumType(String type) {
        return Arrays.asList(ClientTypeEnum.values()).stream()
                .filter(e -> e.getType().equals(type))
                .findFirst().orElse(ClientTypeEnum.DEFAULT);
    }
}

package com.wei.push.impl.netty.standard;

import lombok.Data;

/**
 * @Author lw
 * @Date 2022/1/19  下午4:55
 * @Version 1.0
 */
@Data
public class WebsocketServerConfig {

    private  String host;

    private  int port;

    private int bossLoopGroupThreads;

    private int workerLoopGroupThreads;

    public WebsocketServerConfig(){
        this.host = "0.0.0.0";
        this.port = 8345;
        this.bossLoopGroupThreads = 16;
        this.workerLoopGroupThreads = 16;
    }

    public WebsocketServerConfig(int port){
        this.host = "0.0.0.0";
        this.port = port;
        this.bossLoopGroupThreads = 16;
        this.workerLoopGroupThreads = 16;
    }

}

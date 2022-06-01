package com.wei.controller;

import static io.netty.handler.codec.stomp.StompHeaders.MESSAGE;

import com.wei.push.PushService;
import com.wei.push.bo.Message;
import com.wei.push.common.Constants;
import com.wei.push.impl.netty.standard.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompFrame;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author lw
 * @Date 2022/1/24  下午2:36
 * @Version 1.0
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/num")
    public Integer getNum(){
        return ChannelManager.getUserNum();
    }

    @Resource
    private PushService pushService;

    @Resource
    private RedisTemplate<String, Object> stringRedisTemplate;

    @GetMapping("connect")
    public void connect(@RequestParam String loginUserId){
        stringRedisTemplate.opsForValue().set(Constants.WS_CLUSTER_USER_ID + loginUserId, "1", Constants.WS_SESSION_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    @GetMapping("disconnect")
    public void disconnect(@RequestParam String loginUserId){
        Optional<Channel> optionalChannel = ChannelManager.getChannel(loginUserId);
        if(optionalChannel.isPresent()){
            stringRedisTemplate.delete(Constants.WS_CLUSTER_USER_ID + loginUserId);
            Channel channel = optionalChannel.get();
            StompFrame errorFrame = new DefaultStompFrame(StompCommand.ERROR);
            errorFrame.headers().set(MESSAGE, "close");
            channel.writeAndFlush(errorFrame).addListener(ChannelFutureListener.CLOSE);
        }

    }

    @GetMapping("sub")
    public void sub(@RequestParam String loginUserId, @RequestParam String dest){
        pushService.pushSupplementMsg(loginUserId, dest);
    }

    @GetMapping
    public void push(@RequestParam String loginUserId, @RequestParam String message, @RequestParam String dest){
        Message message1 = new Message();
        message1.setContent(message);
        message1.setSeq(UUID.randomUUID().toString());
        message1.setDest(dest);
        pushService.sendToUserWithSupplement(loginUserId, dest, message1, 10L);
    }
}

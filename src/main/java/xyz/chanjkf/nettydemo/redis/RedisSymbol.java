package xyz.chanjkf.nettydemo.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisSymbol {
    
    @Autowired
    RedisTemplate redisTemplate;
    public void receiveMsg() {
        Object o = redisTemplate.opsForList().rightPop("a");
        if (o == null) {
            System.out.println("无消息");
            return;
        }
        dealMsg(o.toString());
        

    }

    private void dealMsg(String s) {
    }
}

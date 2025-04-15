package com.ddiv.zcfun;

import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.ddiv.zcfun.domain.ApiResult;
import com.ddiv.zcfun.domain.po.im.message.MessagePO;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@SpringBootTest
public class UtilTest {


    private final RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public UtilTest(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Test
    public void testJson() {
        String token = "11111111111";
        System.out.println(JSONUtil.toJsonStr(ApiResult.success(token)));
        JWT jwt = JWTUtil.parseToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoxOTA1OTEyNDI1OTgzNjQzNjAwLCJ1c2VybmFtZSI6IjEifQ.aK_WgQTXD--tyx1ayM0GEcy0_TzP63hB_1aQXtrE_a8");
        String id = jwt.getPayload().getClaim("user_id").toString();
        System.out.println(id);
    }

    @Test
    public void testRabbitmq() {
        assertDoesNotThrow(() -> rabbitTemplate.execute(channel -> {
            channel.exchangeDeclarePassive("amq.direct");
            return true;
        }));
        rabbitTemplate.convertAndSend("im.main.exchange", "private.ddiv", "useMytel");
    }

    @Test
    public void testRedisTemplate() {
        redisTemplate.opsForHash().put("test", "payload", new MessagePO());
        System.out.println(redisTemplate.opsForHash().get("test", "payload"));
        redisTemplate.opsForHash().delete("test", "status", "payload");
    }

}

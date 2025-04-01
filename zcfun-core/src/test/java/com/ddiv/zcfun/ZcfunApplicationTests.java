package com.ddiv.zcfun;

import com.ddiv.zcfun.domain.dto.UserLoginDTO;
import com.ddiv.zcfun.domain.dto.UserRegisterDTO;
import com.ddiv.zcfun.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;

@SpringBootTest
class ZcfunApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
    }

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Test
    void redisTest(){
        redisTemplate.opsForValue().set("aaa","bbb");
        UserLoginDTO user = new UserLoginDTO(1,"张三","123456", LocalDateTime.now(),LocalDateTime.now(),null);
        redisTemplate.opsForValue().set("user",user);
        System.out.println(redisTemplate.opsForValue().get("aaa"));
        System.out.println(redisTemplate.opsForValue().get("user"));
        System.out.println(user);
        redisTemplate.delete("user");
        redisTemplate.delete("aaa");
    }

    @Test
    void mysqlTest(){
        userService.register(new UserRegisterDTO("test","123456"));
    }

}

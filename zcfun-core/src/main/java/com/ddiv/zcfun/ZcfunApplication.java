package com.ddiv.zcfun;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.ddiv.zcfun.mapper")
@EnableScheduling
public class ZcfunApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZcfunApplication.class, args);
    }
}

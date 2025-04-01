package com.ddiv.zcfun;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ddiv.zcfun.mapper")
public class ZcfunApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZcfunApplication.class, args);
    }
}

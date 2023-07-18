package com.luo.yupao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.luo.yupao.mapper")
@EnableScheduling
public class YuPaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuPaoApplication.class, args);
    }

}

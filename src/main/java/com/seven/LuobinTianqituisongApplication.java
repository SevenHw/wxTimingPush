package com.seven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 开启定时任务
@ComponentScan({"com.seven"})
public class LuobinTianqituisongApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuobinTianqituisongApplication.class, args);
        System.out.println("启动成功");
    }



}

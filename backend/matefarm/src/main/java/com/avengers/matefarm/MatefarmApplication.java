package com.avengers.matefarm;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;


@Slf4j
@EnableAsync    // 비동기 처리를 위해 추가
@SpringBootApplication
public class MatefarmApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatefarmApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // JVM 전역 시간대 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        log.info("현재 시스템 시간대: {}", TimeZone.getDefault().getID());
    }

}

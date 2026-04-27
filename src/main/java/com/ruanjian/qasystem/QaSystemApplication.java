package com.ruanjian.qasystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.ruanjian.qasystem.repository")
@SpringBootApplication
public class QaSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(QaSystemApplication.class, args);
    }

}

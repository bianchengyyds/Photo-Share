package com.photoshare.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.photoshare.backend.mapper")
public class PhotoShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotoShareApplication.class, args);
    }
}

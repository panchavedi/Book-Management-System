package com.bms.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.bms.library.feign")
public class LibraryServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(LibraryServiceApplication.class, args);
    }
}

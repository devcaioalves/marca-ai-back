package com.marcaaiback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.marcaaiback.client")
public class MarcaAiBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarcaAiBackApplication.class, args);
    }
}

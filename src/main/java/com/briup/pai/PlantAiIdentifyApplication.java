package com.briup.pai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class PlantAiIdentifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlantAiIdentifyApplication.class, args);
    }
}
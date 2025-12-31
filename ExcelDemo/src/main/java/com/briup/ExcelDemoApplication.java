package com.briup;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.briup.mapper")
@SpringBootApplication
public class ExcelDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExcelDemoApplication.class, args);
	}

}

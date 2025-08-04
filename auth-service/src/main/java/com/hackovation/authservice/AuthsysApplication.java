package com.hackovation.authservice;

import com.hackovation.authservice.feign.UserInterface;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackageClasses = {UserInterface.class})
public class AuthsysApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthsysApplication.class, args);
	}

}

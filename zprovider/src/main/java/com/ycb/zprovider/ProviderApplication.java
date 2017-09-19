package com.ycb.zprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by zhuhui on 17-6-16.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ProviderApplication.class);
        application.run(args);
    }
}

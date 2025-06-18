package com.xight.ai.platform;

import com.xight.ai.platform.config.FeishuConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableConfigurationProperties(FeishuConfig.class)
public class AIPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIPlatformApplication.class, args);
    }

}

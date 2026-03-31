package com.wasteai;

import com.wasteai.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class WasteAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WasteAiApplication.class, args);
    }
}

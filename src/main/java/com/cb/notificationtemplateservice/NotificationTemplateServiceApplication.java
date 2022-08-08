package com.cb.notificationtemplateservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class NotificationTemplateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationTemplateServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        // Notification Preference service
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }

}

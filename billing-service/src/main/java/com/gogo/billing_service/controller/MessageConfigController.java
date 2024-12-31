package com.gogo.billing_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class MessageConfigController {

    @Value("${spring.boot.message}")
    private String message;

    @Value("${spring.boot.bus.message}")
    private String message2;

    @GetMapping("message")
    public String message() {
        return message;
    }

    @GetMapping("/users/message")
    public String message2() {
        return message;
    }
}
//http://localhost:8083/message     //get
//http://localhost:8083/actuator/refresh   //post
// ou http://localhost:8084/actuator/busrefresh //post
//http://localhost:8083/swagger-ui/index.html
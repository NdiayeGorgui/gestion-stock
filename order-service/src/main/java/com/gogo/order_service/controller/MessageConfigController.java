package com.gogo.order_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class MessageConfigController {

    @Value("${spring.boot.message}")
    private String message;

    @GetMapping("message")
    public String message() {
        return message;
    }
}
//http://localhost:8083/message     //get
//http://localhost:8083/actuator/refresh   //post

package com.gogo.mcp_java_client.controller;
import com.gogo.mcp_java_client.agents.MyAIAgent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RequestMapping("/api/v1")
@RestController
public class ChatController {
    private final MyAIAgent agent;

    public ChatController(MyAIAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/")
    public String chatPage() {
        return "chat";
    }

    @PostMapping("/chat/send")
    @ResponseBody
    public Flux<String> sendMessage(@RequestParam String message) {
        return agent.prompt(message);
    }
}

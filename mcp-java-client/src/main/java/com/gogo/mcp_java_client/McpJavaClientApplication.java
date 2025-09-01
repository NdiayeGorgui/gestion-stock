package com.gogo.mcp_java_client;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class McpJavaClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpJavaClientApplication.class, args);
	}
	@Bean
	CommandLineRunner run(List<McpSyncClient> clients) {
		return args -> {
			clients.forEach(client -> {
				client.listTools().tools().forEach(tool -> {
					System.out.println("***************");
					System.out.println(tool.name());
					System.out.println(tool.inputSchema());
					System.out.println(tool.description());
					System.out.println("************");
				});
			});

		};
	}
}

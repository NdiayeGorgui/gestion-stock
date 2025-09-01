package com.gogo.mcp_java_server;

import com.gogo.mcp_java_server.tools.StockTool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpJavaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpJavaServerApplication.class, args);
	}

	@Bean
	public MethodToolCallbackProvider getMethodToolCallbackProvider(StockTool stockTool) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(stockTool)
				.build();
	}


}

package com.gogo.order_service;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
		info = @Info(
				title = "Order Service",
				description = "Order microservice",
				version = "v1",
				contact = @Contact(name = "gorgui",
						email = "gogo@gmail.com",
						url="gorgui.com"
				),
				license = @License(name = "apache 2.0",
						url="gorgui.com"
				)
		),
		externalDocs = @ExternalDocumentation(description = "Order service config",
				url="https://github.com/NdiayeGorgui/gestion-stock-config-server/blob/main/order-service.properties"
		)
)
@SpringBootApplication
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}

package com.gogo.inventory_service;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@OpenAPIDefinition(
		info = @Info(
				title = "Inventory Service",
				description = "Inventory microservice",
				version = "v1",
				contact = @Contact(name = "gorgui",
						email = "gorgui@gmail.com",
						url="gorgui.com"

				),
				license = @License(name = "apache 2.0",
						url="gorgui.com"
				)
		),
		externalDocs = @ExternalDocumentation(description = "Inventory service config",
				url="https://github.com/NdiayeGorgui/gestion-stock-config-server/blob/main/inventory-service.properties"
		)
)

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}



}

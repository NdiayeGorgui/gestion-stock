package com.gogo.shipping_service;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@OpenAPIDefinition(
		info = @Info(
				title = "Shipping Service",
				description = "Shipping microservice",
				version = "v1",
				contact = @Contact(name = "gorgui",
						email = "gorgui@gmail.com",
						url="gorgui.com"
				),
				license = @License(name = "apache 2.0",
						url="gorgui.com"
				)
		),
		externalDocs = @ExternalDocumentation(description = "Shipping service config",
				url="https://github.com/NdiayeGorgui/gestion-stock-config-server/blob/main/shipping-service.properties"
		)
)
@SpringBootApplication
public class ShippingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShippingServiceApplication.class, args);
	}

}

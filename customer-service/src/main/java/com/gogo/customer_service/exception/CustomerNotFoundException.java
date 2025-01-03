package com.gogo.customer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	public CustomerNotFoundException(String string) {
		super(string);
		
	}
}
package com.gogo.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderNotFoundException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	public OrderNotFoundException(String string) {
		super(string);
		
	}
}
package com.gogo.delivered_query_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DeliveredCommandNotFoundException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	public DeliveredCommandNotFoundException(String string) {
		super(string);
		
	}
}
package com.gogo.billing_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BillNotFoundException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	public BillNotFoundException(String string) {
		super(string);
		
	}
}
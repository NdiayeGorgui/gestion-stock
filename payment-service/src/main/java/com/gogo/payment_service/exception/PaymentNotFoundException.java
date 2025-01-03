package com.gogo.payment_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentNotFoundException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	public PaymentNotFoundException(String string) {
		super(string);
		
	}
}
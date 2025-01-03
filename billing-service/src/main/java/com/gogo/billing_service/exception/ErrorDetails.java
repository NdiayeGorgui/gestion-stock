package com.gogo.billing_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Data
@NoArgsConstructor @AllArgsConstructor
public class ErrorDetails {
	private Date timestamp;
	private String messase;
	private HttpStatus status;

}

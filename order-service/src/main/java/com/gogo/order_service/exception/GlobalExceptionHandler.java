package com.gogo.order_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //manipulation exceptions spécifiques
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(OrderNotFoundException exception){
        ErrorDetails errorDetails=new ErrorDetails(new Date(),exception.getMessage(),HttpStatus.NOT_FOUND);
        log.error("Exception: {}",exception.getMessage());

        return  ResponseEntity.internalServerError().body(errorDetails);
    }

    //manipulation  exceptions globales

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception exception){
        ErrorDetails errorDetails=new ErrorDetails(new Date(),exception.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        log.error("Exception: {}",exception.getMessage());
        return  ResponseEntity.internalServerError().body(errorDetails);
    }
}

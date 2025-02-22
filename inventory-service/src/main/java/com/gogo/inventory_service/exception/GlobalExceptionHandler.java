package com.gogo.inventory_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //manipulation exceptions spécifiques
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ProductNotFoundException exception){
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String,
            String> handleMethodArgumentNotValidException(MethodArgumentNotValidException
                                                                  execption) {
        Map<String, String> errMap = new HashMap<>();
        execption.getBindingResult().getFieldErrors().forEach(error -> {
            errMap.put(error.getField(), error.getDefaultMessage());
        });
        return errMap;
    }
}

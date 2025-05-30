package com.gogo.customer_service.dto;

public class CustomerExistsResponse {
    private boolean exists;
    private String message;

    public CustomerExistsResponse(boolean exists, String message) {
        this.exists = exists;
        this.message = message;
    }

    // Getters et setters
    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package com.tourfolio.app.exception;

public class TouristSpotNotFoundException extends RuntimeException {
    
    public TouristSpotNotFoundException(String message) {
        super(message);
    }
    
    public TouristSpotNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.courseflow.exception;

public class SeatFullException extends RuntimeException {
    public SeatFullException(String message) {
        super(message);
    }
}

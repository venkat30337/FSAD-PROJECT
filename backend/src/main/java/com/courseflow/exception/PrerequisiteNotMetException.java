package com.courseflow.exception;

public class PrerequisiteNotMetException extends RuntimeException {
    public PrerequisiteNotMetException(String message) {
        super(message);
    }
}

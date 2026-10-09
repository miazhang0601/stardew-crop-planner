package com.miazhang.cropplanner.common;

/** The request is valid but clashes with data that already exists. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

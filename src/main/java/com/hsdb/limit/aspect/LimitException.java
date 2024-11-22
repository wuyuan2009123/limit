package com.hsdb.limit.aspect;

public class LimitException extends RuntimeException{
    public LimitException(String message) {
        super(message);
    }
}

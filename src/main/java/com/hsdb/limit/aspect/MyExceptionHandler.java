package com.hsdb.limit.aspect;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@ControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(value =LimitException.class)
    @ResponseBody
    public Map<String,String> exceptionHandler(LimitException e){
        return Map.of("errorLimit",e.getMessage());
    }

    @ExceptionHandler(value =Exception.class)
    @ResponseBody
    public Map<String,String> exceptionHandler(Exception e){
        return Map.of("error",e.getMessage());
    }

}

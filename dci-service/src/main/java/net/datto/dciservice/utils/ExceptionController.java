package net.datto.dciservice.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity notFoundExceptionHandler(Exception exception) {
        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put("code", HttpStatus.NOT_FOUND.value());
        returnMap.put("status", HttpStatus.NOT_FOUND);
        returnMap.put("message", exception.getMessage());
        return new ResponseEntity<>(returnMap, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity internalServerErrorExceptionHandler(Exception exception) {
        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        returnMap.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
        returnMap.put("message", exception.getMessage());
        return new ResponseEntity<>(returnMap, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity unauthorizedExceptionHandler(Exception exception) {
        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put("code", HttpStatus.UNAUTHORIZED.value());
        returnMap.put("status", HttpStatus.UNAUTHORIZED);
        returnMap.put("message", exception.getMessage());
        return new ResponseEntity<>(returnMap, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity badRequestExceptionHandler(Exception exception) {
        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put("code", HttpStatus.BAD_REQUEST.value());
        returnMap.put("status", HttpStatus.BAD_REQUEST);
        returnMap.put("message", exception.getMessage());
        return new ResponseEntity<>(returnMap, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity serviceUnavailableExceptionHandler(Exception exception) {
        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put("code", HttpStatus.SERVICE_UNAVAILABLE.value());
        returnMap.put("status", HttpStatus.SERVICE_UNAVAILABLE);
        returnMap.put("message", exception.getMessage());
        return new ResponseEntity<>(returnMap, HttpStatus.SERVICE_UNAVAILABLE);
    }
}

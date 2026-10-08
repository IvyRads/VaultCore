package com.vaultcore.vbms.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiExceptions.NotFound.class) ResponseEntity<ErrorBody> notFound(ApiExceptions.NotFound e,HttpServletRequest r){return error(HttpStatus.NOT_FOUND,e.getMessage(),r);}
    @ExceptionHandler(ApiExceptions.Forbidden.class) ResponseEntity<ErrorBody> forbidden(ApiExceptions.Forbidden e,HttpServletRequest r){return error(HttpStatus.FORBIDDEN,e.getMessage(),r);}
    @ExceptionHandler({ApiExceptions.BadRequest.class,ApiExceptions.InsufficientBalance.class,ConstraintViolationException.class}) ResponseEntity<ErrorBody> bad(RuntimeException e,HttpServletRequest r){return error(HttpStatus.BAD_REQUEST,e.getMessage(),r);}
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorBody> invalid(MethodArgumentNotValidException e,HttpServletRequest r){String m=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).collect(Collectors.joining(", "));return error(HttpStatus.BAD_REQUEST,m,r);}
    private ResponseEntity<ErrorBody> error(HttpStatus s,String m,HttpServletRequest r){return ResponseEntity.status(s.value()).body(new ErrorBody(Instant.now(),s.value(),s.getReasonPhrase(),m,r.getRequestURI()));}
    public record ErrorBody(Instant timestamp,int status,String error,String message,String path){}
}

package com.dev.transcribeflow.core.exception.handler;

import com.dev.transcribeflow.core.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Map<Class<? extends Exception>, HttpStatus> exceptionStatusMap = new HashMap<>();

    public GlobalExceptionHandler() {
        exceptionStatusMap.put(EmailAlreadyExistsException.class, HttpStatus.CONFLICT);
        exceptionStatusMap.put(EmailNotFoundException.class, HttpStatus.NOT_FOUND);
        exceptionStatusMap.put(ExpireTokenException.class, HttpStatus.UNAUTHORIZED);
        exceptionStatusMap.put(BadCredentialsException.class, HttpStatus.UNAUTHORIZED);
        exceptionStatusMap.put(TokenNotFoundException.class, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ProblemDetail> handleCustomException(CustomException ex, HttpServletRequest request) {

        HttpStatus status = exceptionStatusMap.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);

        if (status.is5xxServerError()) {
            log.error(
                    "Internal server error [{}] - {} | Path: {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    request.getRequestURI(),
                    ex
            );
        } else {
            log.warn(
                    "Client error [{}] - {} | Path: {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    request.getRequestURI()
            );
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());

        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setType(URI.create("errors/" + ex.getClass().getSimpleName()));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation failed for fields: {} | Path: {}", errors.keySet(), request.getRequestURI());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for one or more fields");
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(URI.create("errors/MethodArgumentNotValidException"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        problemDetail.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
}

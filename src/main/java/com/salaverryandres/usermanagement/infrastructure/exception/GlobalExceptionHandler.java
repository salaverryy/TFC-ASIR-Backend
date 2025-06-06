package com.salaverryandres.usermanagement.infrastructure.exception;

import com.salaverryandres.usermanagement.application.exception.BadRequestException;
import com.salaverryandres.usermanagement.application.exception.ChallengeRequiredException;
import com.salaverryandres.usermanagement.application.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Bad Request",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.NOT_FOUND.value(),
                        "error", "Not Found",
                        "message", ex.getMessage()
                )
        );
    }

    // 403 - Acceso denegado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.FORBIDDEN.value(),
                        "error", "Forbidden",
                        "message", "No tienes permisos para acceder a este recurso"
                )
        );
    }

    @ExceptionHandler(ChallengeRequiredException.class)
    public ResponseEntity<?> handleChallenge(ChallengeRequiredException ex) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(
                Map.of(
                        "challenge", ex.getChallenge(),
                        "session", ex.getSession(),
                        "message", ex.getMessage(),
                        "timestamp", LocalDateTime.now()
                )
        );
    }

}

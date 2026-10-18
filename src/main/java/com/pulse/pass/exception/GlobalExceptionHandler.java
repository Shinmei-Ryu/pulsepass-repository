package com.pulse.pass.exception;

import com.pulse.pass.dto.response.ErrorResponse;
import com.pulse.pass.security.InvalidJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(
            ResourceNotFoundException.class
    )
    public ResponseEntity<ErrorResponse>
    handleResourceNotFound(
            ResourceNotFoundException ex) {
        HttpStatus status =
                HttpStatus.NOT_FOUND;
        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        ex.getMessage(),
                        Map.of()
                );
        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(
            BusinessRuleException.class
    )
    public ResponseEntity<ErrorResponse>
    handleBusinessRule(
            BusinessRuleException ex) {
        HttpStatus status =
                HttpStatus.CONFLICT;
        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        ex.getMessage(),
                        Map.of()
                );
        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(
            DuplicateResourceException.class
    )
    public ResponseEntity<ErrorResponse>
    handleDuplicateResource(
            DuplicateResourceException ex) {

        HttpStatus status =
                HttpStatus.CONFLICT;

        ErrorResponse error =
                new ErrorResponse(
                    LocalDateTime.now(),
                    status.value(),
                    status.getReasonPhrase(),
                    ex.getMessage(),
                    Map.of()
            );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ErrorResponse>
    handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> details =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        FieldError::getField,
                                        FieldError::getDefaultMessage,
                                        (first, second) -> first
                                )
                        );
        HttpStatus status =
                HttpStatus.BAD_REQUEST;
        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        "Request validation failed",
                        details
                );
        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ErrorResponse>
    handleMessageNotReadable(
            HttpMessageNotReadableException ex) {
        HttpStatus status =
                HttpStatus.BAD_REQUEST;
        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        "Malformed or invalid JSON request",
                        Map.of(
                                "body",
                                "Check JSON syntax and enum values"
                        )
                );
        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ErrorResponse>
    handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        HttpStatus status =
                HttpStatus.BAD_REQUEST;
        Map<String, String> details =
                Map.of(
                        ex.getName(),
                        "Invalid value: "
                                + String.valueOf(
                                ex.getValue()
                        )
                );
        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        "Invalid request parameter",
                        details
                );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Missing request parameter",
                Map.of(ex.getParameterName(), "Parameter is required")
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(
            Exception.class
    )
    public ResponseEntity<ErrorResponse>
    handleUnexpectedException(
            Exception ex) {
        HttpStatus status =
                HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse error =
                new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        "An unexpected error occurred",
                        Map.of()
                );
        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Invalid credentials",
                Map.of()
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", currentRequest());
        HttpStatus status = HttpStatus.FORBIDDEN;

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Access denied",
                Map.of()
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJwt(InvalidJwtException ex) {
        log.warn("Invalid token rejected: {}", currentRequest());
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Invalid or expired token",
                Map.of()
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationRequired(AuthenticationRequiredException ex) {
        log.warn("Unauthenticated request: {}", currentRequest());
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Authentication is required",
                Map.of()
        );

        return ResponseEntity.status(status).body(error);
    }
    // Obtiene "METODO /ruta" del request en curso. Devuelve un texto neutro si no hay request
    // (por ejemplo en un test unitario del handler), así que nunca falla.
    private String currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            return request.getMethod() + " " + request.getRequestURI();
        }
        return "unknown request";
    }

}
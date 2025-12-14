package com.a1.demo.handlers;

import com.a1.demo.handlers.exceptions.model.CustomException;
import com.a1.demo.handlers.exceptions.model.ExceptionHandlerResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        Set<ConstraintViolation<?> > violations = ex.getConstraintViolations();
        List<String> details = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        var body = new ExceptionHandlerResponseDTO(
                "Constraint violation",
                status.getReasonPhrase(),
                status.value(),
                ex.getClass().getSimpleName(),
                details,
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(body);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<String> details = new ArrayList<>();
        for (ObjectError err : ex.getBindingResult().getAllErrors()) {
            if (err instanceof FieldError fe) {
                details.add(fe.getField() + ": " + fe.getDefaultMessage());
            } else {
                details.add(err.getObjectName() + ": " + err.getDefaultMessage());
            }
        }

        var status = HttpStatus.BAD_REQUEST;
        var body = new ExceptionHandlerResponseDTO(
                "Validation failed",
                status.getReasonPhrase(),
                status.value(),
                MethodArgumentNotValidException.class.getSimpleName(),
                details,
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(body);
    }


    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(BindException ex, WebRequest request) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        var status = HttpStatus.BAD_REQUEST;
        var body = new ExceptionHandlerResponseDTO(
                "Binding failed",
                status.getReasonPhrase(),
                status.value(),
                BindException.class.getSimpleName(),
                details,
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(body);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        var status = HttpStatus.BAD_REQUEST;
        var body = new ExceptionHandlerResponseDTO(
                "Malformed JSON request",
                status.getReasonPhrase(),
                status.value(),
                HttpMessageNotReadableException.class.getSimpleName(),
                List.of(ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(body);
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {

        var status = HttpStatus.BAD_REQUEST;
        var body = new ExceptionHandlerResponseDTO(
                "Missing request parameter",
                status.getReasonPhrase(),
                status.value(),
                MissingServletRequestParameterException.class.getSimpleName(),
                List.of(ex.getParameterName() + " is required"),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(body);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        var status = HttpStatus.BAD_REQUEST;
        String param = ex.getName();
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        var body = new ExceptionHandlerResponseDTO(
                "Type mismatch",
                status.getReasonPhrase(),
                status.value(),
                MethodArgumentTypeMismatchException.class.getSimpleName(),
                List.of(param + " must be of type " + required),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(body);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        var status = HttpStatus.CONFLICT;
        var body = new ExceptionHandlerResponseDTO(
                "Data integrity violation",
                status.getReasonPhrase(),
                status.value(),
                DataIntegrityViolationException.class.getSimpleName(),
                List.of(ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(body);
    }


    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomExceptions(CustomException ex, WebRequest request) {
        var body = new ExceptionHandlerResponseDTO(
                ex.getResource(),
                ex.getStatus().getReasonPhrase(),
                ex.getStatus().value(),
                ex.getMessage(),
                ex.getValidationErrors(),
                request.getDescription(false)
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {

        var status = HttpStatus.NOT_FOUND;
        var body = new ExceptionHandlerResponseDTO(
                "No handler found",
                status.getReasonPhrase(),
                status.value(),
                NoHandlerFoundException.class.getSimpleName(),
                List.of(ex.getHttpMethod() + " " + ex.getRequestURL()),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(body);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        var body = new ExceptionHandlerResponseDTO(
                "Unexpected error",
                status.getReasonPhrase(),
                status.value(),
                ex.getClass().getSimpleName(),
                List.of("An unexpected error occurred"),
                request.getDescription(false)
        );
        return ResponseEntity.status(status).body(body);
    }
}
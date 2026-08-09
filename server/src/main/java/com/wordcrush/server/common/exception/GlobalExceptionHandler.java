package com.wordcrush.server.common.exception;

import com.wordcrush.api.ApiCode;
import com.wordcrush.server.common.api.ApiResponse;
import com.wordcrush.server.common.api.ApiResponseWriter;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return response(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return response(ApiCode.BAD_REQUEST, message.isBlank() ? "invalid request" : message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException exception) {
        String message = exception.getMessage();
        return response(ApiCode.BAD_REQUEST, message == null || message.isBlank()
                ? "invalid request"
                : message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableMessage() {
        return response(ApiCode.BAD_REQUEST, "invalid request body");
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(Exception exception) {
        String message = "invalid request";
        if (exception instanceof MissingServletRequestParameterException missing) {
            message = "missing request parameter: " + missing.getParameterName();
        } else if (exception instanceof MissingServletRequestPartException missing) {
            message = "missing request part: " + missing.getRequestPartName();
        } else if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            message = "invalid request parameter: " + mismatch.getName();
        }
        return response(ApiCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported() {
        return response(ApiCode.METHOD_NOT_ALLOWED, "method not allowed");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported() {
        return response(ApiCode.UNSUPPORTED_MEDIA_TYPE, "unsupported media type");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound() {
        return response(ApiCode.NOT_FOUND, "resource not found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return response(ApiCode.INTERNAL_SERVER_ERROR, "internal server error");
    }

    private ResponseEntity<ApiResponse<Void>> response(ApiCode code, String message) {
        return ApiResponseWriter.entity(code, message);
    }
}

package ir.maktab127.exception;

import ir.maktab127.dto.ApiResponseDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ApiResponseDto("Validation failed: " + errors, false));
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Invalid input format. Expected format for dates: yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX (e.g., 2025-08-05T14:11:10.000000+03:30)";
        if (ex.getMessage().contains("Enum")) {
            message = "Invalid enum value. Check valid values for enums like status or role.";
        }
        return ResponseEntity.badRequest()
                .body(new ApiResponseDto(message, false));
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Database constraint violation occurred.";
        if (ex.getMessage().contains("status")) {
            message = "Status cannot be null. Provide a valid status (e.g., PENDING, ACCEPTED, REJECTED).";
        } else if (ex.getMessage().contains("email")) {
            message = "Email already exists. Please use a different email.";
        } else if (ex.getMessage().contains("unique_user_id")) {
            message = "A wallet already exists for this user.";
        }
        return ResponseEntity.badRequest()
                .body(new ApiResponseDto(message, false));
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponseDto(ex.getMessage(), false));
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponseDto("Access denied: You do not have permission to perform this action.", false));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto("An unexpected error occurred: " + ex.getMessage(), false));
    }
}
package com.accesa.exception;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class EmploymentCertificateAdvisor {

	public EmploymentCertificateAdvisor() {
		// TODO Auto-generated constructor stub
	}
	
	@ExceptionHandler(IOException.class)
	public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}
	
	@ExceptionHandler(DateTimeException.class)
	public ResponseEntity<Map<String, Object>> handleDateTimeException(DateTimeException ex) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
			MethodArgumentTypeMismatchException ex) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "wrong parameter type " + ex.getMessage());
	}
	
	@ExceptionHandler(GenerationException.class)
	public ResponseEntity<Map<String, Object>> handleGenerationException(
			GenerationException ex) {
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errores);
    }

	@ExceptionHandler(JsonParseException.class)
	public ResponseEntity<Map<String, Object>> handleJsonParseException(JsonParseException ex) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "error processing json message " + ex.getMessage());
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
	}
	
	@ExceptionHandler(DatabaseException.class)
	public ResponseEntity<Map<String, Object>> handleDatabaseException(DatabaseException ex) {
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}
	
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
	}
	
	private static ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", LocalDateTime.now());
		response.put("status", status.value());
		response.put("error", status.getReasonPhrase());
		response.put("message", message);
		return new ResponseEntity<>(response, status);
	}

}

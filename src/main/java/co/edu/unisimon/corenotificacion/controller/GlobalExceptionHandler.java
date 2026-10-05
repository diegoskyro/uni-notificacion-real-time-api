package co.edu.unisimon.corenotificacion.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import co.edu.unisimon.corenotificacion.exception.BusinessException;
import co.edu.unisimon.corenotificacion.exception.ResourceNotFoundException;
import co.edu.unisimon.corenotificacion.exception.TokenException;
import co.edu.unisimon.corenotificacion.exception.UnauthorizedException;
import co.edu.unisimon.corenotificacion.response.ResponseApi;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ResponseApi<?>> handleNotFound(ResourceNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ResponseApi<>(e.getMessage(), HttpStatus.NOT_FOUND.value(), null));
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ResponseApi<?>> handleBusiness(BusinessException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ResponseApi<>(e.getMessage(), HttpStatus.BAD_REQUEST.value(), null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseApi<?>> handleValidation(MethodArgumentNotValidException e) {
		String mensaje = e.getBindingResult().getFieldError().getDefaultMessage();
		return ResponseEntity.badRequest().body(new ResponseApi<>(mensaje, HttpStatus.BAD_REQUEST.value(), null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseApi<?>> handleGeneral(Exception e) {
		e.printStackTrace(); 
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ResponseApi<>("Error interno del servidor: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResponseApi<?>> handleJsonParse(HttpMessageNotReadableException ex) {

		String mensaje = "Error en el formato del JSON";

		Throwable cause = ex.getCause();
		if (cause instanceof tools.jackson.databind.DatabindException mappingException) {
			if (!mappingException.getPath().isEmpty()) {
				String campo = mappingException.getPath().get(mappingException.getPath().size() - 1).getPropertyName();

				mensaje = "Error en el campo: " + campo;
			}
		}

		return ResponseEntity.badRequest().body(new ResponseApi<>(mensaje, HttpStatus.BAD_REQUEST.value(), null));
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ResponseApi<?>> handleUnauthorized(UnauthorizedException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ResponseApi<>(e.getMessage(), HttpStatus.UNAUTHORIZED.value(), null));
	}

	@ExceptionHandler(TokenException.class)
	public ResponseEntity<ResponseApi<?>> handleTokenException(TokenException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ResponseApi<>(ex.getMessage(), HttpStatus.UNAUTHORIZED.value(), null));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ResponseApi<?>> handleDataIntegrity(DataIntegrityViolationException ex) {
		String mensaje = "Error de integridad de datos";

		String causa = ex.getMostSpecificCause().getMessage().toLowerCase();

		// DUPLICADO (UNIQUE)
		if (causa.contains("unique") || causa.contains("duplicate")) {
			mensaje = "El registro ya existe";
		}
		// RELACIÓN (FOREIGN KEY)
		else if (causa.contains("foreign key") || causa.contains("constraint")) {
			mensaje = "No se puede eliminar el registro porque está relacionado con otros elementos";
		}

		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ResponseApi<>(mensaje, HttpStatus.CONFLICT.value(), null));
	}

	@ExceptionHandler({ AccessDeniedException.class,
			org.springframework.security.authorization.AuthorizationDeniedException.class })
	public ResponseEntity<ResponseApi<Void>> handleAccessDenied(Exception ex) {
		String mensaje = "usted no tiene permisos para este proceso.";
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ResponseApi<>(mensaje, HttpStatus.FORBIDDEN.value(), null));
	}

}

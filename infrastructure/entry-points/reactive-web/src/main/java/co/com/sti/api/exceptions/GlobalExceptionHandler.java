package co.com.sti.api.exceptions;

import co.com.sti.usecase.exceptios.InvalidUserDataException;
import co.com.sti.usecase.exceptios.UserAlreadyExistsException;
import co.com.sti.usecase.exceptios.UserNotExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@Order(-2)
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Conflicto");
        errorResponse.put("message", ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
    }

    @ExceptionHandler(UserNotExistsException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleUserNotExists(UserNotExistsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "No encontrado");
        errorResponse.put("message", ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleInvalidUserData(InvalidUserDataException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Datos de usuario inválidos");
        errorResponse.put("message", ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, String>>> handleAllException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Error interno del servidor");
        errorResponse.put("message", "Ocurrió un error inesperado: " + ex.getMessage());
        log.error(errorResponse.toString());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

}

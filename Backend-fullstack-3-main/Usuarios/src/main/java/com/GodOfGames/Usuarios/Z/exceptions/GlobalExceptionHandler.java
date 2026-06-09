package com.GodOfGames.Usuarios.Z.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j // Lombok inyecta automáticamente el sistema de Logs aquí
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        // Registramos el error en la consola del servidor (Log de nivel ERROR)
        log.error("Alerta del sistema, señor. Se ha producido una excepción: {}", ex.getMessage());

        // Armamos la respuesta limpia para el cliente 
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
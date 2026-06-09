package com.GodOfGames.Inventario.exceptions;

import com.GodOfGames.Inventario.dtos.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Este método "atrapa" cualquier RuntimeException que ocurra en todo tu proyecto
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDTO> handleRuntimeException(RuntimeException ex) {
        
        // Armamos nuestro DTO de error estructurado
        ErrorDTO error = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Petición Inválida o Regla de Negocio Incumplida")
                .message(ex.getMessage()) // Aquí irá nuestro mensaje de "¡Stock insuficiente!"
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
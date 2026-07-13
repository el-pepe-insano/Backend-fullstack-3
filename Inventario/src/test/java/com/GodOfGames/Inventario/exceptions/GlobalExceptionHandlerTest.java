package com.GodOfGames.Inventario.exceptions;

import com.GodOfGames.Inventario.dtos.ErrorDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_retorna400ConErrorDTO() {
        ResponseEntity<ErrorDTO> response = handler.handleRuntimeException(new RuntimeException("Stock insuficiente"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Stock insuficiente", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }
}
package com.GodOfGames.Pedidos.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundException_retorna404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Pedido no encontrado: 5");

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Pedido no encontrado: 5", response.getBody().get("mensaje"));
        assertEquals(404, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleValidationExceptions_retorna400ConErrores() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("pedidoRequestDTO", "detalles", "El pedido debe contener al menos un detalle");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Errores de validación en la petición", response.getBody().get("mensaje"));
        assertEquals(400, response.getBody().get("status"));

        @SuppressWarnings("unchecked")
        Map<String, String> errores = (Map<String, String>) response.getBody().get("errores");
        assertEquals("El pedido debe contener al menos un detalle", errores.get("detalles"));
    }
}
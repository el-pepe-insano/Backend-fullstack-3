package com.GodOfGames.Pedidos.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_asignaMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("mensaje de prueba");
        assertEquals("mensaje de prueba", ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }
}
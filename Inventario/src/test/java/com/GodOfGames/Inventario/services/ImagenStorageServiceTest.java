package com.GodOfGames.Inventario.services;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ImagenStorageServiceTest {

    private final ImagenStorageService service = new ImagenStorageService();

    @Test
    void guardarImagen_archivoValido_retornaNombreConExtension() {
        MockMultipartFile archivo = new MockMultipartFile(
                "imagen", "portada.png", "image/png", "contenido-falso".getBytes());

        String nombreGuardado = service.guardarImagen(archivo);

        assertNotNull(nombreGuardado);
        assertTrue(nombreGuardado.endsWith(".png"));

        service.eliminarImagen(nombreGuardado);
    }

    @Test
    void guardarImagen_sinExtension_retornaNombreSinExtension() {
        MockMultipartFile archivo = new MockMultipartFile(
                "imagen", "portada", "image/png", "contenido-falso".getBytes());

        String nombreGuardado = service.guardarImagen(archivo);

        assertNotNull(nombreGuardado);
        assertFalse(nombreGuardado.contains("."));

        service.eliminarImagen(nombreGuardado);
    }

    @Test
    void guardarImagen_archivoVacio_lanzaExcepcion() {
        MockMultipartFile archivo = new MockMultipartFile(
                "imagen", "vacio.png", "image/png", new byte[0]);

        assertThrows(RuntimeException.class, () -> service.guardarImagen(archivo));
    }

    @Test
    void eliminarImagen_nombreNulo_noLanzaExcepcion() {
        assertDoesNotThrow(() -> service.eliminarImagen(null));
    }

    @Test
    void eliminarImagen_nombreBlanco_noLanzaExcepcion() {
        assertDoesNotThrow(() -> service.eliminarImagen("   "));
    }

    @Test
    void eliminarImagen_archivoNoExiste_noLanzaExcepcion() {
        assertDoesNotThrow(() -> service.eliminarImagen("no-existe-12345.png"));
    }
}
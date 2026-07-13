package com.GodOfGames.Inventario;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import com.GodOfGames.Inventario.repositories.ProductoRepository;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    @Test
    void cargarDatos_baseVacia_precargaCatalogo() throws Exception {
        ProductoRepository repo = mock(ProductoRepository.class);
        when(repo.count()).thenReturn(0L);

        DataInitializer initializer = new DataInitializer();
        CommandLineRunner runner = initializer.cargarDatos(repo);
        runner.run();

        verify(repo, times(1)).saveAll(anyList());
    }

    @Test
    void cargarDatos_baseConDatos_noRecarga() throws Exception {
        ProductoRepository repo = mock(ProductoRepository.class);
        when(repo.count()).thenReturn(16L);

        DataInitializer initializer = new DataInitializer();
        CommandLineRunner runner = initializer.cargarDatos(repo);
        runner.run();

        verify(repo, never()).saveAll(anyList());
    }
}
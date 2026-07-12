package com.GodOfGames.Inventario;

import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.model.Producto;
import com.GodOfGames.Inventario.repositories.ProductoRepository;
import com.GodOfGames.Inventario.services.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void obtenerTodos_retornaLista() {
        Producto p = Producto.builder().id(1L).nombre("FIFA").precio(59.99).stock(10).build();
        when(productoRepository.findAll()).thenReturn(List.of(p));
        List<ProductoDTO> resultado = productoService.obtenerTodos();
        assertEquals(1, resultado.size());
        assertEquals("FIFA", resultado.get(0).getNombre());
    }

    @Test
    void obtenerPorId_existente() {
        Producto p = Producto.builder().id(1L).nombre("FIFA").precio(59.99).stock(10).build();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        ProductoDTO resultado = productoService.obtenerPorId(1L);
        assertNotNull(resultado);
        assertEquals("FIFA", resultado.getNombre());
    }

    @Test
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productoService.obtenerPorId(99L));
    }

    @Test
    void reservarStock_stockSuficiente() {
        Producto p = Producto.builder().id(1L).nombre("FIFA").precio(59.99).stock(10).build();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ProductoDTO resultado = productoService.reservarStock(1L, 3);
        assertEquals(7, resultado.getStock());
        assertNotNull(resultado.getClaveJuego());
    }

    @Test
    void reservarStock_stockInsuficiente_lanzaExcepcion() {
        Producto p = Producto.builder().id(1L).nombre("FIFA").precio(59.99).stock(2).build();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThrows(RuntimeException.class, () -> productoService.reservarStock(1L, 5));
    }

    @Test
    void actualizarStockDirecto_exitoso() {
        Producto p = Producto.builder().id(1L).nombre("FIFA").precio(59.99).stock(5).build();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ProductoDTO resultado = productoService.actualizarStockDirecto(1L, 100);
        assertEquals(100, resultado.getStock());
    }

    @Test
    void eliminarProducto_noExiste_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productoService.eliminarProducto(99L));
    }
}
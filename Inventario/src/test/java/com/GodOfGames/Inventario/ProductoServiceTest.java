package com.GodOfGames.Inventario;

import com.GodOfGames.Inventario.dtos.ActualizarProductoDTO;
import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.model.Producto;
import com.GodOfGames.Inventario.repositories.ProductoRepository;
import com.GodOfGames.Inventario.services.ImagenStorageService;
import com.GodOfGames.Inventario.services.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ImagenStorageService imagenStorageService;

    @InjectMocks
    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Producto productoBase() {
        return Producto.builder().id(1L).nombre("FIFA").descripcion("Juego").precio(59.99).stock(10).build();
    }

    @Test
    void obtenerTodos_retornaLista() {
        when(productoRepository.findAll()).thenReturn(List.of(productoBase()));
        List<ProductoDTO> resultado = productoService.obtenerTodos();
        assertEquals(1, resultado.size());
        assertEquals("FIFA", resultado.get(0).getNombre());
    }

    @Test
    void obtenerPorId_existente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase()));
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
    void guardarProducto_sinImagen() {
        Producto p = productoBase();
        when(productoRepository.save(any())).thenReturn(p);
        ProductoDTO resultado = productoService.guardarProducto(p, null);
        assertNotNull(resultado);
        verify(imagenStorageService, never()).guardarImagen(any());
    }

    @Test
    void guardarProducto_conImagen() {
        Producto p = productoBase();
        MultipartFile imagen = mock(MultipartFile.class);
        when(imagen.isEmpty()).thenReturn(false);
        when(imagenStorageService.guardarImagen(any())).thenReturn("imagen.jpg");
        when(productoRepository.save(any())).thenReturn(p);
        ProductoDTO resultado = productoService.guardarProducto(p, imagen);
        assertNotNull(resultado);
        verify(imagenStorageService).guardarImagen(any());
    }

    @Test
    void reservarStock_stockSuficiente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase()));
        when(productoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ProductoDTO resultado = productoService.reservarStock(1L, 3);
        assertEquals(7, resultado.getStock());
        assertNotNull(resultado.getClaveJuego());
    }

    @Test
    void reservarStock_stockInsuficiente_lanzaExcepcion() {
        Producto p = productoBase();
        p.setStock(2);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThrows(RuntimeException.class, () -> productoService.reservarStock(1L, 5));
    }

    @Test
    void reservarStock_noExiste_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productoService.reservarStock(99L, 1));
    }

    @Test
    void actualizarStockDirecto_exitoso() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase()));
        when(productoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ProductoDTO resultado = productoService.actualizarStockDirecto(1L, 100);
        assertEquals(100, resultado.getStock());
    }

    @Test
    void actualizarStockDirecto_noExiste_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productoService.actualizarStockDirecto(99L, 10));
    }

    @Test
    void actualizarProducto_cambiaNombre() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase()));
        when(productoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ActualizarProductoDTO dto = new ActualizarProductoDTO();
        dto.setNombre("Call of Duty");
        ProductoDTO resultado = productoService.actualizarProducto(1L, dto, null);
        assertEquals("Call of Duty", resultado.getNombre());
    }

    @Test
    void actualizarProducto_conImagen() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase()));
        when(productoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(imagenStorageService.guardarImagen(any())).thenReturn("nueva.jpg");
        MultipartFile imagen = mock(MultipartFile.class);
        when(imagen.isEmpty()).thenReturn(false);
        ActualizarProductoDTO dto = new ActualizarProductoDTO();
        dto.setPrecio(49.99);
        ProductoDTO resultado = productoService.actualizarProducto(1L, dto, imagen);
        assertNotNull(resultado);
        verify(imagenStorageService).guardarImagen(any());
    }

    @Test
    void actualizarProducto_noExiste_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productoService.actualizarProducto(99L, new ActualizarProductoDTO(), null));
    }

    @Test
    void eliminarProducto_noExiste_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productoService.eliminarProducto(99L));
    }

    @Test
    void eliminarProducto_exitoso() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase()));
        doNothing().when(imagenStorageService).eliminarImagen(any());
        doNothing().when(productoRepository).deleteById(1L);
        assertDoesNotThrow(() -> productoService.eliminarProducto(1L));
        verify(productoRepository).deleteById(1L);
    }

    @Test
    void listarProductoBusqueda_retornaLista() {
        when(productoRepository.buscarPorNombre("FIFA")).thenReturn(List.of(productoBase()));
        List<Producto> resultado = productoService.ListarProductoBusqueda("FIFA");
        assertEquals(1, resultado.size());
    }
}
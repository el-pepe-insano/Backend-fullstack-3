package com.GodOfGames.Inventario;

import com.GodOfGames.Inventario.controllers.ProductoController;
import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.model.Producto;
import com.GodOfGames.Inventario.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    private ProductoDTO productoDTO() {
        return ProductoDTO.builder().id(1L).nombre("FIFA").precio(59.99).stock(10).build();
    }

    @Test
    @WithMockUser
    void listarTodos_retornaLista() throws Exception {
        when(productoService.obtenerTodos()).thenReturn(List.of(productoDTO()));
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("FIFA"));
    }

    @Test
    @WithMockUser
    void obtenerPorId_existente() throws Exception {
        when(productoService.obtenerPorId(1L)).thenReturn(productoDTO());
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("FIFA"));
    }

    @Test
    @WithMockUser
    void obtenerPorId_noExiste_retorna4xx() throws Exception {
        when(productoService.obtenerPorId(99L)).thenThrow(new RuntimeException("No encontrado"));
        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser
    void reservarStock_exitoso() throws Exception {
        ProductoDTO dto = productoDTO();
        dto.setClaveJuego("GOG-1234-5678-9ABC");
        when(productoService.reservarStock(1L, 2)).thenReturn(dto);
        mockMvc.perform(post("/api/productos/1/reservar").param("cantidad", "2").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claveJuego").value("GOG-1234-5678-9ABC"));
    }

    @Test
    @WithMockUser
    void actualizarStockAdmin_exitoso() throws Exception {
        when(productoService.actualizarStockDirecto(1L, 50)).thenReturn(productoDTO());
        mockMvc.perform(put("/api/productos/1/stock").param("nuevoStock", "50").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void eliminarProducto_exitoso() throws Exception {
        doNothing().when(productoService).eliminarProducto(1L);
        mockMvc.perform(delete("/api/productos/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void listarPorNombre_retornaLista() throws Exception {
        Producto p = Producto.builder().id(1L).nombre("FIFA").precio(59.99).stock(10).build();
        when(productoService.ListarProductoBusqueda("FIFA")).thenReturn(List.of(p));
        mockMvc.perform(get("/api/productos/buscar/FIFA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("FIFA"));
    }
}
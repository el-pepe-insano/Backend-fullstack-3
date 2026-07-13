package com.GodOfGames.Inventario;

import com.GodOfGames.Inventario.controllers.ProductoController;
import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.services.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ProductoController.class)
class ProductoControllerExtraTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void crearProducto_sinImagen_exitoso() throws Exception {
        String productoJson = "{\"nombre\":\"FIFA\",\"precio\":59.99,\"stock\":10}";
        MockMultipartFile producto = new MockMultipartFile(
                "producto", "producto", "application/json", productoJson.getBytes());

        ProductoDTO dto = ProductoDTO.builder().id(1L).nombre("FIFA").precio(59.99).stock(10).build();
        when(productoService.guardarProducto(any(), any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/productos").file(producto).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("FIFA"));
    }

    @Test
    @WithMockUser
    void crearProducto_conImagen_exitoso() throws Exception {
        String productoJson = "{\"nombre\":\"FIFA\",\"precio\":59.99,\"stock\":10}";
        MockMultipartFile producto = new MockMultipartFile(
                "producto", "producto", "application/json", productoJson.getBytes());
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "portada.png", "image/png", "fake".getBytes());

        ProductoDTO dto = ProductoDTO.builder().id(1L).nombre("FIFA").precio(59.99).stock(10).imagen("uuid.png").build();
        when(productoService.guardarProducto(any(), any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/productos").file(producto).file(imagen).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagen").value("uuid.png"));
    }

    @Test
    @WithMockUser
    void actualizarProducto_sinImagen_exitoso() throws Exception {
        String dtoJson = "{\"nombre\":\"Call of Duty\"}";
        MockMultipartFile producto = new MockMultipartFile(
                "producto", "producto", "application/json", dtoJson.getBytes());

        ProductoDTO dto = ProductoDTO.builder().id(1L).nombre("Call of Duty").build();
        when(productoService.actualizarProducto(any(), any(), any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/productos/1")
                        .file(producto)
                        .with(csrf())
                        .with(req -> { req.setMethod("PATCH"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Call of Duty"));
    }
}
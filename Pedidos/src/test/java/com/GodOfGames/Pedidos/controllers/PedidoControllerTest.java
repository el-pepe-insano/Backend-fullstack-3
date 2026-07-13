package com.GodOfGames.Pedidos.controllers;

import com.GodOfGames.Pedidos.dtos.PedidoRequestDTO;
import com.GodOfGames.Pedidos.dtos.PedidoResponseDTO;
import com.GodOfGames.Pedidos.exceptions.GlobalExceptionHandler;
import com.GodOfGames.Pedidos.exceptions.ResourceNotFoundException;
import com.GodOfGames.Pedidos.models.EstadoPedido;
import com.GodOfGames.Pedidos.services.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        PedidoController controller = new PedidoController(pedidoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private PedidoResponseDTO respuestaBase() {
        return PedidoResponseDTO.builder()
                .id(1L)
                .usuarioId("user1")
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE)
                .total(BigDecimal.valueOf(20))
                .detalles(new ArrayList<>())
                .build();
    }

    private PedidoRequestDTO requestValido() {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        PedidoRequestDTO.DetalleRequestDTO detalle = new PedidoRequestDTO.DetalleRequestDTO();
        detalle.setProductoId(1L);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(BigDecimal.TEN);
        List<PedidoRequestDTO.DetalleRequestDTO> detalles = new ArrayList<>();
        detalles.add(detalle);
        dto.setDetalles(detalles);
        return dto;
    }

    @Test
    void crearPedido_exito() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");
        when(pedidoService.crearPedido(any(), anyString(), anyString())).thenReturn(respuestaBase());

        mockMvc.perform(post("/api/v1/pedidos")
                        .principal(auth)
                        .header("Authorization", "Bearer testtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value("user1"));
    }

    @Test
    void crearPedido_sinDetalles_retorna400() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setDetalles(new ArrayList<>());

        mockMvc.perform(post("/api/v1/pedidos")
                        .principal(auth)
                        .header("Authorization", "Bearer testtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPedido_existente() throws Exception {
        when(pedidoService.obtenerPedidoPorId(1L)).thenReturn(respuestaBase());

        mockMvc.perform(get("/api/v1/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerPedido_noExiste_retorna404() throws Exception {
        when(pedidoService.obtenerPedidoPorId(99L)).thenThrow(new ResourceNotFoundException("Pedido no encontrado: 99"));

        mockMvc.perform(get("/api/v1/pedidos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerTodos_retornaLista() throws Exception {
        when(pedidoService.obtenerTodosLosPedidos()).thenReturn(List.of(respuestaBase()));

        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void obtenerPorUsuario_retornaLista() throws Exception {
        when(pedidoService.obtenerPedidosPorUsuario("user1")).thenReturn(List.of(respuestaBase()));

        mockMvc.perform(get("/api/v1/pedidos/usuario/user1"))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarEstado_exito() throws Exception {
        PedidoResponseDTO respuesta = respuestaBase();
        respuesta.setEstado(EstadoPedido.CANCELADO);
        when(pedidoService.actualizarEstado(eq(1L), eq(EstadoPedido.CANCELADO), anyString())).thenReturn(respuesta);

        mockMvc.perform(patch("/api/v1/pedidos/1/estado")
                        .param("nuevoEstado", "CANCELADO")
                        .header("Authorization", "Bearer testtoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    void historialVentas_sinFiltros() throws Exception {
        when(pedidoService.obtenerHistorial(any(), any(), any())).thenReturn(List.of(respuestaBase()));

        mockMvc.perform(get("/api/v1/pedidos/historial"))
                .andExpect(status().isOk());
    }

    @Test
    void historialVentas_conFiltros() throws Exception {
        when(pedidoService.obtenerHistorial(any(), any(), eq(EstadoPedido.COMPLETADO))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/pedidos/historial")
                        .param("desde", "2026-01-01T00:00:00")
                        .param("hasta", "2026-12-31T00:00:00")
                        .param("estado", "COMPLETADO"))
                .andExpect(status().isOk());
    }

    @Test
    void estadisticas_calculaCorrectamente() throws Exception {
        PedidoResponseDTO completado = respuestaBase();
        completado.setEstado(EstadoPedido.COMPLETADO);
        completado.setTotal(BigDecimal.valueOf(100));
        PedidoResponseDTO pendiente = respuestaBase();
        pendiente.setEstado(EstadoPedido.PENDIENTE);
        PedidoResponseDTO cancelado = respuestaBase();
        cancelado.setEstado(EstadoPedido.CANCELADO);

        when(pedidoService.obtenerTodosLosPedidos()).thenReturn(List.of(completado, pendiente, cancelado));

        mockMvc.perform(get("/api/v1/pedidos/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPedidos").value(3))
                .andExpect(jsonPath("$.completados").value(1))
                .andExpect(jsonPath("$.pendientes").value(1))
                .andExpect(jsonPath("$.cancelados").value(1));
    }
}
package com.GodOfGames.Pedidos;

import com.GodOfGames.Pedidos.dtos.PedidoRequestDTO;
import com.GodOfGames.Pedidos.dtos.PedidoResponseDTO;
import com.GodOfGames.Pedidos.models.EstadoPedido;
import com.GodOfGames.Pedidos.models.Pedido;
import com.GodOfGames.Pedidos.repositories.PedidoRepository;
import com.GodOfGames.Pedidos.services.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Pedido pedidoBase() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUsuarioId("user1");
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setTotal(BigDecimal.valueOf(59.99));
        pedido.setDetalles(new ArrayList<>());
        return pedido;
    }

    @Test
    void obtenerTodosLosPedidos_retornaLista() {
        when(pedidoRepository.findAll()).thenReturn(List.of(pedidoBase()));
        List<PedidoResponseDTO> resultado = pedidoService.obtenerTodosLosPedidos();
        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerTodosLosPedidos_listaVacia() {
        when(pedidoRepository.findAll()).thenReturn(List.of());
        List<PedidoResponseDTO> resultado = pedidoService.obtenerTodosLosPedidos();
        assertTrue(resultado.isEmpty());
    }

    @Test
    void obtenerPedidoPorId_existente() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoBase()));
        PedidoResponseDTO resultado = pedidoService.obtenerPedidoPorId(1L);
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerPedidoPorId_noExiste_lanzaExcepcion() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> pedidoService.obtenerPedidoPorId(99L));
    }

    @Test
    void obtenerPedidosPorUsuario_retornaLista() {
        when(pedidoRepository.findByUsuarioId("user1")).thenReturn(List.of(pedidoBase()));
        List<PedidoResponseDTO> resultado = pedidoService.obtenerPedidosPorUsuario("user1");
        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerPedidosPorUsuario_listaVacia() {
        when(pedidoRepository.findByUsuarioId("userX")).thenReturn(List.of());
        List<PedidoResponseDTO> resultado = pedidoService.obtenerPedidosPorUsuario("userX");
        assertTrue(resultado.isEmpty());
    }

    @Test
    void crearPedido_sinDetalles() {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setDetalles(new ArrayList<>());
        Pedido pedido = pedidoBase();
        when(pedidoRepository.save(any())).thenReturn(pedido);

        PedidoResponseDTO resultado = pedidoService.crearPedido(dto, "user1", "token");
        assertNotNull(resultado);
    }

    @Test
    void obtenerHistorial_retornaLista() {
        when(pedidoRepository.findHistorial(any(), any(), any())).thenReturn(List.of(pedidoBase()));
        List<PedidoResponseDTO> resultado = pedidoService.obtenerHistorial(null, null, null);
        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerHistorial_conFiltroEstado() {
        when(pedidoRepository.findHistorial(null, null, EstadoPedido.COMPLETADO)).thenReturn(List.of());
        List<PedidoResponseDTO> resultado = pedidoService.obtenerHistorial(null, null, EstadoPedido.COMPLETADO);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void actualizarEstado_noExiste_lanzaExcepcion() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> pedidoService.actualizarEstado(99L, EstadoPedido.CANCELADO, "token"));
    }

    @Test
    void actualizarEstado_aCancelado() {
        Pedido pedido = pedidoBase();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PedidoResponseDTO resultado = pedidoService.actualizarEstado(1L, EstadoPedido.CANCELADO, "token");
        assertEquals(EstadoPedido.CANCELADO, resultado.getEstado());
    }
}
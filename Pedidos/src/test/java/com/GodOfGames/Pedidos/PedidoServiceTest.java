package com.GodOfGames.Pedidos;

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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void obtenerTodosLosPedidos_retornaLista() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDetalles(List.of());
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));

        List<PedidoResponseDTO> resultado = pedidoService.obtenerTodosLosPedidos();
        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerPedidoPorId_existente() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDetalles(List.of());
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

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
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUsuarioId("user1");
        pedido.setDetalles(List.of());
        when(pedidoRepository.findByUsuarioId("user1")).thenReturn(List.of(pedido));

        List<PedidoResponseDTO> resultado = pedidoService.obtenerPedidosPorUsuario("user1");
        assertEquals(1, resultado.size());
    }
}

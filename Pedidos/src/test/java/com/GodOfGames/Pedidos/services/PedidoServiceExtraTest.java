package com.GodOfGames.Pedidos.services;

import com.GodOfGames.Pedidos.dtos.PedidoRequestDTO;
import com.GodOfGames.Pedidos.dtos.PedidoResponseDTO;
import com.GodOfGames.Pedidos.dtos.ProductoClientDTO;
import com.GodOfGames.Pedidos.models.DetallePedido;
import com.GodOfGames.Pedidos.models.EstadoPedido;
import com.GodOfGames.Pedidos.models.Pedido;
import com.GodOfGames.Pedidos.repositories.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PedidoServiceExtraTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Pedido pedidoConDetalle(EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUsuarioId("user1");
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setEstado(estado);
        pedido.setTotal(BigDecimal.valueOf(20));
        List<DetallePedido> detalles = new ArrayList<>();
        DetallePedido detalle = DetallePedido.builder().id(1L).productoId(1L).cantidad(2).precioUnitario(BigDecimal.TEN).build();
        detalles.add(detalle);
        pedido.setDetalles(detalles);
        return pedido;
    }

    @Test
    void crearPedido_conDetalles_calculaTotal() {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        PedidoRequestDTO.DetalleRequestDTO detalle = new PedidoRequestDTO.DetalleRequestDTO();
        detalle.setProductoId(1L);
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(BigDecimal.valueOf(10));
        List<PedidoRequestDTO.DetalleRequestDTO> detalles = new ArrayList<>();
        detalles.add(detalle);
        dto.setDetalles(detalles);

        when(pedidoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PedidoResponseDTO resultado = pedidoService.crearPedido(dto, "user1", "token");

        assertEquals(BigDecimal.valueOf(30), resultado.getTotal());
        assertEquals(1, resultado.getDetalles().size());
    }

    @Test
    void actualizarEstado_aCompletado_reservaStockExitosamente() {
        Pedido pedido = pedidoConDetalle(EstadoPedido.PENDIENTE);
        ProductoClientDTO producto = new ProductoClientDTO();
        producto.setClaveJuego("CLAVE-XYZ");

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductoClientDTO.class)).thenReturn(Mono.just(producto));

        PedidoResponseDTO resultado = pedidoService.actualizarEstado(1L, EstadoPedido.COMPLETADO, "token");

        assertEquals(EstadoPedido.COMPLETADO, resultado.getEstado());
        assertEquals("CLAVE-XYZ", resultado.getDetalles().get(0).getClaveJuego());
    }

    @Test
    void actualizarEstado_aCompletado_errorDeStock_lanzaExcepcion() {
        Pedido pedido = pedidoConDetalle(EstadoPedido.PENDIENTE);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductoClientDTO.class)).thenThrow(new RuntimeException("sin stock"));

        assertThrows(RuntimeException.class, () -> pedidoService.actualizarEstado(1L, EstadoPedido.COMPLETADO, "token"));
    }

    @Test
    void actualizarEstado_yaCompletado_noLlamaWebClient() {
        Pedido pedido = pedidoConDetalle(EstadoPedido.COMPLETADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PedidoResponseDTO resultado = pedidoService.actualizarEstado(1L, EstadoPedido.COMPLETADO, "token");

        assertEquals(EstadoPedido.COMPLETADO, resultado.getEstado());
        verifyNoInteractions(webClient);
    }

    @Test
    void actualizarEstado_aCancelado_noLlamaWebClient() {
        Pedido pedido = pedidoConDetalle(EstadoPedido.PENDIENTE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PedidoResponseDTO resultado = pedidoService.actualizarEstado(1L, EstadoPedido.CANCELADO, "token");

        assertEquals(EstadoPedido.CANCELADO, resultado.getEstado());
        verifyNoInteractions(webClient);
    }
}
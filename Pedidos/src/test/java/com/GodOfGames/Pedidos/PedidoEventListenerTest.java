package com.GodOfGames.Pedidos;

import com.GodOfGames.Pedidos.messaging.PedidoEventListener;
import com.GodOfGames.Pedidos.messaging.StockReservadoEvent;
import com.GodOfGames.Pedidos.models.EstadoPedido;
import com.GodOfGames.Pedidos.models.Pedido;
import com.GodOfGames.Pedidos.repositories.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoEventListenerTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoEventListener pedidoEventListener;

    private Pedido pedidoBase() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDetalles(new ArrayList<>());
        return pedido;
    }

    @Test
    void handleStockReservado_marcaComoCompletado() {
        Pedido pedido = pedidoBase();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenReturn(pedido);

        StockReservadoEvent event = new StockReservadoEvent(1L, true, "OK");
        pedidoEventListener.handleStockReservado(event);

        verify(pedidoRepository).save(argThat(p -> p.getEstado() == EstadoPedido.COMPLETADO));
    }

    @Test
    void handleStockReservado_pedidoNoExiste_noHaceNada() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());
        StockReservadoEvent event = new StockReservadoEvent(99L, true, "OK");
        pedidoEventListener.handleStockReservado(event);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void handleStockFallido_marcaComoCancelado() {
        Pedido pedido = pedidoBase();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenReturn(pedido);

        StockReservadoEvent event = new StockReservadoEvent(1L, false, "Stock insuficiente");
        pedidoEventListener.handleStockFallido(event);

        verify(pedidoRepository).save(argThat(p -> p.getEstado() == EstadoPedido.CANCELADO));
    }

    @Test
    void handleStockFallido_pedidoNoExiste_noHaceNada() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());
        StockReservadoEvent event = new StockReservadoEvent(99L, false, "Error");
        pedidoEventListener.handleStockFallido(event);
        verify(pedidoRepository, never()).save(any());
    }
}
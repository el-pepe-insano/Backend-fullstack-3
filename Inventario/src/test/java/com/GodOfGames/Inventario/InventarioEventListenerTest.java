package com.GodOfGames.Inventario;

import com.GodOfGames.Inventario.dtos.ProductoDTO;
import com.GodOfGames.Inventario.messaging.InventarioEventListener;
import com.GodOfGames.Inventario.messaging.PedidoCreadoEvent;
import com.GodOfGames.Inventario.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.awspring.cloud.sqs.operations.SqsOperations;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioEventListenerTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private SqsOperations sqsOperations;

    @InjectMocks
    private InventarioEventListener inventarioEventListener;

    @Test
    void handlePedidoCreado_stockExitoso() {
        PedidoCreadoEvent.DetalleEvent detalle = new PedidoCreadoEvent.DetalleEvent(1L, 2);
        PedidoCreadoEvent event = new PedidoCreadoEvent(1L, "user1", List.of(detalle));

        ProductoDTO dto = ProductoDTO.builder().id(1L).nombre("FIFA").stock(8).build();
        when(productoService.reservarStock(1L, 2)).thenReturn(dto);

        inventarioEventListener.handlePedidoCreado(event);

        verify(productoService).reservarStock(1L, 2);
        verify(sqsOperations).send(anyString(), any(Object.class));
    }

    @Test
    void handlePedidoCreado_stockFallido_publicaEventoFallo() {
        PedidoCreadoEvent.DetalleEvent detalle = new PedidoCreadoEvent.DetalleEvent(1L, 100);
        PedidoCreadoEvent event = new PedidoCreadoEvent(1L, "user1", List.of(detalle));

        when(productoService.reservarStock(1L, 100)).thenThrow(new RuntimeException("Stock insuficiente"));

        inventarioEventListener.handlePedidoCreado(event);

        verify(sqsOperations).send(anyString(), any(Object.class));
    }
}
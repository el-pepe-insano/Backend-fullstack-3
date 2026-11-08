package com.GodOfGames.Inventario.messaging;

import com.GodOfGames.Inventario.config.SqsConfig;
import com.GodOfGames.Inventario.services.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsOperations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sqs.listeners.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class InventarioEventListener {

    private final ProductoService productoService;
    private final SqsOperations sqsOperations;

    @SqsListener(SqsConfig.QUEUE_PEDIDO_CREADO)
    public void handlePedidoCreado(PedidoCreadoEvent event) {
        log.info("Evento recibido: pedido.creado para pedidoId: {}", event.getPedidoId());

        try {
            // Reservar stock para cada producto del pedido
            for (PedidoCreadoEvent.DetalleEvent detalle : event.getDetalles()) {
                productoService.reservarStock(detalle.getProductoId(), detalle.getCantidad());
                log.info("Stock reservado - productoId: {}, cantidad: {}",
                        detalle.getProductoId(), detalle.getCantidad());
            }

            // Publicar evento de éxito
            StockReservadoEvent response = new StockReservadoEvent(
                    event.getPedidoId(), true, "Stock reservado exitosamente");
            sqsOperations.send(SqsConfig.QUEUE_STOCK_RESERVADO, response);
            log.info("Evento stock.reservado publicado para pedidoId: {}", event.getPedidoId());

        } catch (Exception e) {
            log.error("Error reservando stock para pedidoId {}: {}", event.getPedidoId(), e.getMessage());

            // Publicar evento de fallo
            StockReservadoEvent response = new StockReservadoEvent(
                    event.getPedidoId(), false, e.getMessage());
            sqsOperations.send(SqsConfig.QUEUE_STOCK_FALLIDO, response);
        }
    }
}
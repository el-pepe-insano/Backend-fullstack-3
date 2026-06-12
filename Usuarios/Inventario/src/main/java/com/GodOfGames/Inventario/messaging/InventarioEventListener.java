package com.GodOfGames.Inventario.messaging;

import com.GodOfGames.Inventario.config.RabbitMQConfig;
import com.GodOfGames.Inventario.services.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventarioEventListener {

    private final ProductoService productoService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDO_CREADO)
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
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.QUEUE_STOCK_RESERVADO,
                    response);
            log.info("Evento stock.reservado publicado para pedidoId: {}", event.getPedidoId());

        } catch (Exception e) {
            log.error("Error reservando stock para pedidoId {}: {}", event.getPedidoId(), e.getMessage());

            // Publicar evento de fallo
            StockReservadoEvent response = new StockReservadoEvent(
                    event.getPedidoId(), false, e.getMessage());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.QUEUE_STOCK_FALLIDO,
                    response);
        }
    }
}
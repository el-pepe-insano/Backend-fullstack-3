package com.GodOfGames.Pedidos.messaging;

import com.GodOfGames.Pedidos.config.RabbitMQConfig;
import com.GodOfGames.Pedidos.models.EstadoPedido;
import com.GodOfGames.Pedidos.repositories.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PedidoEventListener {

    private final PedidoRepository pedidoRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STOCK_RESERVADO)
    public void handleStockReservado(StockReservadoEvent event) {
        log.info("Stock reservado exitosamente para pedidoId: {}", event.getPedidoId());
        pedidoRepository.findById(event.getPedidoId()).ifPresent(pedido -> {
            pedido.setEstado(EstadoPedido.COMPLETADO);
            pedidoRepository.save(pedido);
            log.info("Pedido {} marcado como COMPLETADO", event.getPedidoId());
        });
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STOCK_FALLIDO)
    public void handleStockFallido(StockReservadoEvent event) {
        log.error("Stock insuficiente para pedidoId: {} - {}", event.getPedidoId(), event.getMensaje());
        pedidoRepository.findById(event.getPedidoId()).ifPresent(pedido -> {
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedidoRepository.save(pedido);
            log.info("Pedido {} marcado como CANCELADO", event.getPedidoId());
        });
    }
}
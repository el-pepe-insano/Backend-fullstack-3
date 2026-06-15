package com.GodOfGames.Notificaciones.messaging;

import com.GodOfGames.Notificaciones.config.RabbitMQConfig;
import com.GodOfGames.Notificaciones.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionEventListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RECUPERACION)
    public void handleRecuperacion(RecuperacionEvent event) {
        log.info("Evento recuperacion recibido para: {}", event.getCorreo());
        notificacionService.enviarCodigoRecuperacion(event.getCorreo(), event.getNombre());
    }
}
package com.GodOfGames.Notificaciones.messaging;

import com.GodOfGames.Notificaciones.config.SqsConfig;
import com.GodOfGames.Notificaciones.services.NotificacionService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sqs.listeners.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class NotificacionEventListener {

    private final NotificacionService notificacionService;

    @SqsListener(SqsConfig.QUEUE_RECUPERACION)
    public void handleRecuperacion(RecuperacionEvent event) {
        log.info("Evento recuperacion recibido para: {}", event.getCorreo());
        notificacionService.enviarCodigoRecuperacion(event.getCorreo(), event.getNombre());
    }
}

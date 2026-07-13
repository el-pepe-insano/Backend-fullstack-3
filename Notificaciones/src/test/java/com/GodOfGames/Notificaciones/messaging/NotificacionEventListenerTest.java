package com.GodOfGames.Notificaciones.messaging;

import com.GodOfGames.Notificaciones.services.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacionEventListenerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionEventListener listener;

    @Test
    void handleRecuperacion_llamaAlServicioConDatosDelEvento() {
        RecuperacionEvent event = new RecuperacionEvent("juan@test.com", "Juan");

        listener.handleRecuperacion(event);

        verify(notificacionService).enviarCodigoRecuperacion("juan@test.com", "Juan");
    }
}